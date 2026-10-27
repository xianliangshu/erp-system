package com.erp.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.basedata.entity.BaseMaterial;
import com.erp.basedata.entity.BaseWarehouse;
import com.erp.basedata.mapper.BaseMaterialMapper;
import com.erp.basedata.mapper.BaseWarehouseMapper;
import com.erp.business.entity.ProductStock;
import com.erp.business.entity.StockWarning;
import com.erp.business.mapper.ProductStockMapper;
import com.erp.business.mapper.StockWarningMapper;
import com.erp.business.service.IStockWarningService;
import com.erp.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 库存预警 Service 实现
 *
 * @author ERP System
 */
@Service
@RequiredArgsConstructor
public class StockWarningServiceImpl extends ServiceImpl<StockWarningMapper, StockWarning>
        implements IStockWarningService {

    private final BaseWarehouseMapper warehouseMapper;
    private final BaseMaterialMapper materialMapper;
    private final ProductStockMapper productStockMapper;

    @Override
    public IPage<StockWarning> pageList(Long current, Long size, Long scId, String productName, Integer status) {
        Page<StockWarning> page = new Page<>(current != null ? current : 1, size != null ? size : 10);
        LambdaQueryWrapper<StockWarning> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(scId != null, StockWarning::getScId, scId)
                .eq(status != null, StockWarning::getStatus, status);

        // 商品名筛选：先命中商品 ID 集合，避免在分页结果里做内存过滤破坏分页语义
        if (StringUtils.hasText(productName)) {
            List<Long> productIds = materialMapper.selectList(
                            new LambdaQueryWrapper<BaseMaterial>().like(BaseMaterial::getName, productName))
                    .stream().map(BaseMaterial::getId).collect(Collectors.toList());
            if (productIds.isEmpty()) {
                return page;
            }
            wrapper.in(StockWarning::getProductId, productIds);
        }

        wrapper.orderByDesc(StockWarning::getId);
        this.page(page, wrapper);
        fillRelations(page.getRecords());
        return page;
    }

    @Override
    public List<StockWarning> hitList(Long scId, String productName) {
        return baseMapper.selectHitList(scId, StringUtils.hasText(productName) ? productName : null);
    }

    @Override
    public boolean addWarning(StockWarning warning) {
        validate(warning);
        assertNotDuplicated(warning.getScId(), warning.getProductId(), null);
        if (warning.getStatus() == null) {
            warning.setStatus(1);
        }
        warning.setId(null);
        return save(warning);
    }

    @Override
    public boolean modifyWarning(StockWarning warning) {
        if (warning.getId() == null) {
            throw new BusinessException("缺少预警配置ID");
        }
        StockWarning exists = getById(warning.getId());
        if (exists == null) {
            throw new BusinessException("预警配置不存在");
        }
        validate(warning);
        assertNotDuplicated(warning.getScId(), warning.getProductId(), warning.getId());
        return updateById(warning);
    }

    private void validate(StockWarning warning) {
        if (warning.getScId() == null) {
            throw new BusinessException("请选择仓库");
        }
        if (warning.getProductId() == null) {
            throw new BusinessException("请选择商品");
        }
        if (warning.getMinLimit() == null && warning.getMaxLimit() == null) {
            throw new BusinessException("预警上限与下限至少填写一个");
        }
        if (warning.getMinLimit() != null && warning.getMaxLimit() != null
                && warning.getMinLimit().compareTo(warning.getMaxLimit()) > 0) {
            throw new BusinessException("预警下限不能大于预警上限");
        }
    }

    /**
     * 同仓库 + 同商品 只允许一条启用配置（数据库层刻意不加唯一键，判重放这里）
     */
    private void assertNotDuplicated(Long scId, Long productId, Long excludeId) {
        LambdaQueryWrapper<StockWarning> wrapper = new LambdaQueryWrapper<StockWarning>()
                .eq(StockWarning::getScId, scId)
                .eq(StockWarning::getProductId, productId)
                .ne(excludeId != null, StockWarning::getId, excludeId);
        if (count(wrapper) > 0) {
            throw new BusinessException("该仓库下此商品已存在预警配置，请直接编辑原记录");
        }
    }

    /**
     * 批量填充仓库名 / 商品信息 / 当前库存，避免逐行查库
     */
    private void fillRelations(List<StockWarning> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        Set<Long> scIds = list.stream().map(StockWarning::getScId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> productIds = list.stream().map(StockWarning::getProductId).filter(Objects::nonNull).collect(Collectors.toSet());

        Map<Long, String> scMap = scIds.isEmpty() ? Map.of()
                : warehouseMapper.selectBatchIds(scIds).stream()
                        .collect(Collectors.toMap(BaseWarehouse::getId, BaseWarehouse::getName));

        Map<Long, BaseMaterial> materialMap = productIds.isEmpty() ? Map.of()
                : materialMapper.selectBatchIds(productIds).stream()
                        .collect(Collectors.toMap(BaseMaterial::getId, Function.identity()));

        Map<String, BigDecimal> stockMap = new HashMap<>();
        if (!scIds.isEmpty() && !productIds.isEmpty()) {
            List<ProductStock> stocks = productStockMapper.selectList(new LambdaQueryWrapper<ProductStock>()
                    .in(ProductStock::getScId, scIds)
                    .in(ProductStock::getProductId, productIds));
            for (ProductStock stock : stocks) {
                stockMap.put(stock.getScId() + "_" + stock.getProductId(), stock.getStockNum());
            }
        }

        for (StockWarning warning : list) {
            warning.setScName(scMap.get(warning.getScId()));
            BaseMaterial material = materialMap.get(warning.getProductId());
            if (material != null) {
                warning.setProductCode(material.getCode());
                warning.setProductName(material.getName());
                warning.setSpecification(material.getSpecification());
                warning.setSalePropertyText(material.getSalePropertyText());
            }
            BigDecimal stockNum = stockMap.get(warning.getScId() + "_" + warning.getProductId());
            warning.setStockNum(stockNum != null ? stockNum : BigDecimal.ZERO);
        }
    }
}

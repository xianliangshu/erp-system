package com.erp.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.basedata.entity.BaseMaterial;
import com.erp.basedata.entity.BaseWarehouse;
import com.erp.business.entity.ProductStock;
import com.erp.business.entity.ProductStockLog;
import com.erp.business.enums.ProductStockBizType;
import com.erp.business.mapper.ProductStockLogMapper;
import com.erp.business.mapper.ProductStockMapper;
import com.erp.business.service.IProductStockService;
import com.erp.basedata.mapper.BaseWarehouseMapper;
import com.erp.basedata.mapper.BaseMaterialMapper;
import com.erp.basedata.mapper.BaseUnitMapper;
import com.erp.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 商品库存服务实现
 */
@Service
@RequiredArgsConstructor
public class ProductStockServiceImpl extends ServiceImpl<ProductStockMapper, ProductStock>
        implements IProductStockService {

    private final ProductStockLogMapper productStockLogMapper;
    private final BaseWarehouseMapper warehouseMapper;
    private final BaseMaterialMapper materialMapper;
    private final BaseUnitMapper unitMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addStock(Long scId, Long productId, BigDecimal stockNum, BigDecimal taxPrice, Long bizId,
            String bizCode, ProductStockBizType bizType) {
        updateStock(scId, productId, stockNum, taxPrice, bizId, bizCode, bizType, true, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addStockKeepCost(Long scId, Long productId, BigDecimal stockNum, BigDecimal taxPrice, Long bizId,
            String bizCode, ProductStockBizType bizType) {
        updateStock(scId, productId, stockNum, taxPrice, bizId, bizCode, bizType, true, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void subStock(Long scId, Long productId, BigDecimal stockNum, BigDecimal taxPrice, Long bizId,
            String bizCode, ProductStockBizType bizType) {
        updateStock(scId, productId, stockNum.negate(), taxPrice, bizId, bizCode, bizType, false, false);
    }

    @Override
    public void checkStockEnough(Long scId, Long productId, BigDecimal requireNum) {
        // 只对正数的扣减量做校验；退货、盘盈这类入库调用方不会走到这里
        if (requireNum == null || requireNum.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        LambdaQueryWrapper<ProductStock> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductStock::getScId, scId).eq(ProductStock::getProductId, productId);
        ProductStock stock = this.getOne(wrapper);

        BigDecimal available = (stock == null || stock.getStockNum() == null)
                ? BigDecimal.ZERO
                : stock.getStockNum();

        if (available.compareTo(requireNum) < 0) {
            BaseWarehouse warehouse = scId == null ? null : warehouseMapper.selectById(scId);
            BaseMaterial material = productId == null ? null : materialMapper.selectById(productId);

            String warehouseName = warehouse != null ? warehouse.getName() : ("仓库#" + scId);
            String materialName = material != null
                    ? (material.getCode() + " " + material.getName())
                    : ("商品#" + productId);

            throw new BusinessException(String.format("库存不足：%s 的「%s」当前库存 %s，本次需要 %s",
                    warehouseName,
                    materialName,
                    plain(available),
                    plain(requireNum)));
        }
    }

    /** 把 BigDecimal 打印成人看的数字（去掉多余的 0 和小数点） */
    private String plain(BigDecimal value) {
        return value == null ? "0" : value.stripTrailingZeros().toPlainString();
    }

    /**
     * 更新库存核心逻辑
     *
     * @param isAdd      是否为入库方向
     * @param updateCost 是否用 taxPrice 覆盖成本价。销售/零售退货按售价入库时必须传 false，
     *                   否则售价会被写进成本价，污染库存金额
     */
    private void updateStock(Long scId, Long productId, BigDecimal changeNum, BigDecimal taxPrice, Long bizId,
            String bizCode, ProductStockBizType bizType, boolean isAdd, boolean updateCost) {
        // 1. 获取当前库存
        LambdaQueryWrapper<ProductStock> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductStock::getScId, scId).eq(ProductStock::getProductId, productId);
        ProductStock stock = this.getOne(wrapper);

        BigDecimal oriStockNum = BigDecimal.ZERO;
        BigDecimal oriTaxPrice = BigDecimal.ZERO;

        if (stock == null) {
            // 如果库存不存在，则创建
            stock = new ProductStock();
            stock.setScId(scId);
            stock.setProductId(productId);
            stock.setStockNum(BigDecimal.ZERO);
            stock.setTaxPrice(taxPrice);
            stock.setTaxAmount(BigDecimal.ZERO);
        } else {
            oriStockNum = stock.getStockNum();
            oriTaxPrice = stock.getTaxPrice();
        }

        // 2. 计算新库存
        BigDecimal curStockNum = oriStockNum.add(changeNum);

        // 更新库存信息
        stock.setStockNum(curStockNum);
        // 这里简化处理，如果是入库则更新成本价（加权平均或直接覆盖，视业务而定，这里参考xingyun逻辑）
        // 简单起见，如果是增加库存且提供了价格，则更新价格
        if (updateCost && isAdd && taxPrice != null) {
            stock.setTaxPrice(taxPrice);
        }
        // 成本价为 null 时用本次价格兜底，避免下面 multiply 抛空指针（此前 taxPrice 传 null 会 NPE）
        if (stock.getTaxPrice() == null) {
            stock.setTaxPrice(taxPrice != null ? taxPrice : BigDecimal.ZERO);
        }
        stock.setTaxAmount(stock.getTaxPrice().multiply(curStockNum));

        this.saveOrUpdate(stock);

        // 3. 记录日志
        ProductStockLog log = new ProductStockLog();
        log.setScId(scId);
        log.setProductId(productId);
        log.setOriStockNum(oriStockNum);
        log.setCurStockNum(curStockNum);
        log.setStockNum(changeNum.abs());
        log.setBizId(bizId);
        log.setBizCode(bizCode);
        log.setBizType(bizType);

        productStockLogMapper.insert(log);
    }

    @Override
    public com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.erp.business.vo.ProductStockVO> getStockPage(
            Long current, Long size, Long scId, String productName) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<ProductStock> page = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(
                current != null ? current : 1, size != null ? size : 10);

        LambdaQueryWrapper<ProductStock> wrapper = new LambdaQueryWrapper<>();
        if (scId != null)
            wrapper.eq(ProductStock::getScId, scId);
        wrapper.orderByDesc(ProductStock::getId);

        this.page(page, wrapper);

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.erp.business.vo.ProductStockVO> result = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(
                page.getCurrent(), page.getSize(), page.getTotal());

        java.util.List<com.erp.business.vo.ProductStockVO> voList = new java.util.ArrayList<>();
        for (ProductStock stock : page.getRecords()) {
            com.erp.business.vo.ProductStockVO vo = new com.erp.business.vo.ProductStockVO();
            vo.setId(stock.getId());
            vo.setScId(stock.getScId());
            vo.setProductId(stock.getProductId());
            vo.setStockNum(stock.getStockNum());
            vo.setTaxPrice(stock.getTaxPrice());
            vo.setTaxAmount(stock.getTaxAmount());

            // 关联查询仓库名称 - 通过BaseWarehouseMapper
            com.erp.basedata.entity.BaseWarehouse warehouse = warehouseMapper.selectById(stock.getScId());
            if (warehouse != null)
                vo.setScName(warehouse.getName());

            // 关联查询商品信息 - 通过BaseMaterialMapper
            com.erp.basedata.entity.BaseMaterial material = materialMapper.selectById(stock.getProductId());
            if (material != null) {
                vo.setProductCode(material.getCode());
                vo.setProductName(material.getName());
                vo.setProductSpec(material.getSpecification());
                // 获取单位名称
                if (material.getUnitId() != null) {
                    com.erp.basedata.entity.BaseUnit unit = unitMapper.selectById(material.getUnitId());
                    if (unit != null)
                        vo.setUnitName(unit.getName());
                }
            }
            voList.add(vo);
        }
        result.setRecords(voList);
        return result;
    }
}

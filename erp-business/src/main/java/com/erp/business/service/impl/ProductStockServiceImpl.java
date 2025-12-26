package com.erp.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.business.entity.ProductStock;
import com.erp.business.entity.ProductStockLog;
import com.erp.business.enums.ProductStockBizType;
import com.erp.business.mapper.ProductStockLogMapper;
import com.erp.business.mapper.ProductStockMapper;
import com.erp.business.service.IProductStockService;
import com.erp.basedata.mapper.BaseWarehouseMapper;
import com.erp.basedata.mapper.BaseMaterialMapper;
import com.erp.basedata.mapper.BaseUnitMapper;
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
        updateStock(scId, productId, stockNum, taxPrice, bizId, bizCode, bizType, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void subStock(Long scId, Long productId, BigDecimal stockNum, BigDecimal taxPrice, Long bizId,
            String bizCode, ProductStockBizType bizType) {
        updateStock(scId, productId, stockNum.negate(), taxPrice, bizId, bizCode, bizType, false);
    }

    /**
     * 更新库存核心逻辑
     */
    private void updateStock(Long scId, Long productId, BigDecimal changeNum, BigDecimal taxPrice, Long bizId,
            String bizCode, ProductStockBizType bizType, boolean isAdd) {
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
        if (isAdd && taxPrice != null) {
            stock.setTaxPrice(taxPrice);
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

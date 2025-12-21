package com.erp.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.business.entity.ProductStock;
import com.erp.business.entity.ProductStockLog;
import com.erp.business.enums.ProductStockBizType;
import com.erp.business.mapper.ProductStockLogMapper;
import com.erp.business.mapper.ProductStockMapper;
import com.erp.business.service.IProductStockService;
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
}

package com.erp.business.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 商品库存
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("product_stock")
public class ProductStock extends BaseBusinessEntity {

    /**
     * 仓库ID
     */
    private Long scId;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 库存数量
     */
    private BigDecimal stockNum;

    /**
     * 含税价格
     */
    private BigDecimal taxPrice;

    /**
     * 含税金额
     */
    private BigDecimal taxAmount;
}

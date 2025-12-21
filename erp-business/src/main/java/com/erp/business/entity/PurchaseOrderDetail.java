package com.erp.business.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 采购订单明细
 */
@Data
@TableName("purchase_order_detail")
public class PurchaseOrderDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 采购数量
     */
    private BigDecimal orderNum;

    /**
     * 含税单价
     */
    private BigDecimal taxPrice;

    /**
     * 含税金额
     */
    private BigDecimal taxAmount;

    /**
     * 已收货数量
     */
    private BigDecimal receivedNum;

    /**
     * 备注
     */
    private String description;

    /**
     * 排序
     */
    private Integer sort;
}

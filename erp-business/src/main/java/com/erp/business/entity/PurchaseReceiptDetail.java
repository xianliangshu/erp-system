package com.erp.business.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 采购收货明细表
 */
@Data
@TableName("purchase_receipt_detail")
public class PurchaseReceiptDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 收货单ID
     */
    private Long receiptId;

    /**
     * 订单明细ID
     */
    private Long orderDetailId;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 订单数量
     */
    private BigDecimal orderNum;

    /**
     * 本次收货数量
     */
    private BigDecimal receiveNum;

    /**
     * 含税单价
     */
    private BigDecimal taxPrice;

    /**
     * 含税金额
     */
    private BigDecimal taxAmount;

    /**
     * 备注
     */
    private String description;
}

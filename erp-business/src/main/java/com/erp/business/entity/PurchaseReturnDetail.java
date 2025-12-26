package com.erp.business.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 采购退货明细表
 */
@Data
@TableName("purchase_return_detail")
public class PurchaseReturnDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 退货单ID
     */
    private Long returnId;

    /**
     * 收货明细ID
     */
    private Long receiptDetailId;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 原收货数量
     */
    private BigDecimal receiveNum;

    /**
     * 退货数量
     */
    private BigDecimal returnNum;

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

package com.erp.business.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 采购收货表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("purchase_receipt")
public class PurchaseReceipt extends BaseBusinessEntity {

    /**
     * 收货单编号
     */
    private String code;

    /**
     * 采购订单ID
     */
    private Long orderId;

    /**
     * 采购订单编号
     */
    private String orderCode;

    /**
     * 仓库ID
     */
    private Long scId;

    /**
     * 供应商ID
     */
    private Long supplierId;

    /**
     * 收货数量
     */
    private BigDecimal totalNum;

    /**
     * 收货金额
     */
    private BigDecimal totalAmount;

    /**
     * 状态: 0-待确认 1-已确认
     */
    private Integer status;

    /**
     * 备注
     */
    private String description;
}

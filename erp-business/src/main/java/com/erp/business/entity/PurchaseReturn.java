package com.erp.business.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 采购退货表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("purchase_return")
public class PurchaseReturn extends BaseBusinessEntity {

    /**
     * 退货单编号
     */
    private String code;

    /**
     * 收货单ID
     */
    private Long receiptId;

    /**
     * 收货单编号
     */
    private String receiptCode;

    /**
     * 仓库ID
     */
    private Long scId;

    /**
     * 供应商ID
     */
    private Long supplierId;

    /**
     * 退货数量
     */
    private BigDecimal totalNum;

    /**
     * 退货金额
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

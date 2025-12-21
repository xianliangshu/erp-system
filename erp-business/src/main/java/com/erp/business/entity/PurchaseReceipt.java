package com.erp.business.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 采购收货表
 */
@Data
@TableName("purchase_receipt")
public class PurchaseReceipt implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

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

    /**
     * 创建者
     */
    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新者
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除
     */
    @TableLogic
    private Integer deleted;
}

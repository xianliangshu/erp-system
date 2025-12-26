package com.erp.business.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 销售出库
 */
@Data
@TableName("sale_delivery")
public class SaleDelivery implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 出库单编号 */
    private String code;

    /** 销售订单ID */
    private Long orderId;

    /** 销售订单编号 */
    private String orderCode;

    /** 仓库ID */
    private Long scId;

    /** 客户ID */
    private Long customerId;

    /** 出库数量 */
    private BigDecimal totalNum;

    /** 出库金额 */
    private BigDecimal totalAmount;

    /** 状态: 0-待确认 1-已确认 */
    private Integer status;

    /** 备注 */
    private String description;

    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}

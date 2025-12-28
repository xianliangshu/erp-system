package com.erp.business.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 销售出库
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sale_delivery")
public class SaleDelivery extends BaseBusinessEntity {

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
}

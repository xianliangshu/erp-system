package com.erp.business.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.erp.business.enums.SaleOrderStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 销售订单
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sale_order")
public class SaleOrder extends BaseBusinessEntity {

    /**
     * 订单编号
     */
    private String code;

    /**
     * 仓库ID
     */
    private Long scId;

    /**
     * 客户ID
     */
    private Long customerId;

    /**
     * 销售员ID
     */
    private Long salerId;

    /**
     * 预计发货日期
     */
    private LocalDate expectDeliveryDate;

    /**
     * 销售数量
     */
    private BigDecimal totalNum;

    /**
     * 销售金额
     */
    private BigDecimal totalAmount;

    /**
     * 状态
     */
    private SaleOrderStatus status;

    /**
     * 备注
     */
    private String description;

    /**
     * 审核人
     */
    private String approveBy;

    /**
     * 审核时间
     */
    private LocalDateTime approveTime;

    /**
     * 拒绝原因
     */
    private String refuseReason;
}

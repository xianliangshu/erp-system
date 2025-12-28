package com.erp.business.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 供应商预付款单
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("settle_pre_sheet")
public class SettlePreSheet extends BaseBusinessEntity {

    /**
     * 单据编号
     */
    private String code;

    /**
     * 供应商ID
     */
    private Long supplierId;

    /**
     * 总金额
     */
    private BigDecimal totalAmount;

    /**
     * 状态: 0-待审核, 1-已审核, 2-已拒绝
     */
    private Integer status;

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

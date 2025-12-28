package com.erp.business.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 供应商结算单
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("settle_sheet")
public class SettleSheet extends BaseBusinessEntity {
    private String code;
    private Long supplierId;
    private BigDecimal totalAmount;
    private BigDecimal totalDiscountAmount;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer status;
    private String description;
    private String approveBy;
    private LocalDateTime approveTime;
    private String refuseReason;
}

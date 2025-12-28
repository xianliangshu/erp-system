package com.erp.business.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("customer_check_sheet")
public class CustomerCheckSheet extends BaseBusinessEntity {
    private String code;
    private Long customerId;
    private BigDecimal totalAmount;
    private BigDecimal totalPayedAmount;
    private BigDecimal totalDiscountAmount;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer status;
    private String description;
    private String approveBy;
    private LocalDateTime approveTime;
    private String refuseReason;
}

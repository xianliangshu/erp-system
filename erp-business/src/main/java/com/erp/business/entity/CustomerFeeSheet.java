package com.erp.business.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("customer_fee_sheet")
public class CustomerFeeSheet extends BaseBusinessEntity {
    private String code;
    private Long customerId;
    private Integer sheetType; // 1-收款, 2-扣款
    private BigDecimal totalAmount;
    private Integer status;
    private String description;
    private String approveBy;
    private LocalDateTime approveTime;
    private String refuseReason;
}

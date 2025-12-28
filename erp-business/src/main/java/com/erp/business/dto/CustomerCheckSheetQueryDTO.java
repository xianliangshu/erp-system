package com.erp.business.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class CustomerCheckSheetQueryDTO {
    private String code;
    private Long customerId;
    private Integer status;
    private LocalDate startDate;
    private LocalDate endDate;
}

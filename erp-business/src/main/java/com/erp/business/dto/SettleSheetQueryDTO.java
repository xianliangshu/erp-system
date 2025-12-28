package com.erp.business.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class SettleSheetQueryDTO {
    private String code;
    private Long supplierId;
    private Integer status;
    private LocalDate startDate;
    private LocalDate endDate;
}

package com.erp.business.dto;

import lombok.Data;

@Data
public class CustomerFeeSheetQueryDTO {
    private String code;
    private Long customerId;
    private Integer sheetType;
    private Integer status;
}

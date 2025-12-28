package com.erp.business.dto;

import lombok.Data;

@Data
public class CustomerSettleSheetQueryDTO {
    private String code;
    private Long customerId;
    private Integer status;
}

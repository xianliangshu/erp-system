package com.erp.business.dto;

import lombok.Data;

/**
 * 供应商预付款单查询DTO
 */
@Data
public class SettlePreSheetQueryDTO {
    private String code;
    private Long supplierId;
    private Integer status;
}

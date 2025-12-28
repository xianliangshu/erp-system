package com.erp.business.dto;

import lombok.Data;

/**
 * 供应商费用单查询DTO
 */
@Data
public class SettleFeeSheetQueryDTO {

    /**
     * 单据编号
     */
    private String code;

    /**
     * 供应商ID
     */
    private Long supplierId;

    /**
     * 单据类型
     */
    private Integer sheetType;

    /**
     * 状态
     */
    private Integer status;
}

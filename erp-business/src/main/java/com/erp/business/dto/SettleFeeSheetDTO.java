package com.erp.business.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 供应商费用单DTO
 */
@Data
public class SettleFeeSheetDTO {

    /**
     * ID (更新时需要)
     */
    private Long id;

    /**
     * 供应商ID
     */
    private Long supplierId;

    /**
     * 单据类型: 1-付款, 2-扣款
     */
    private Integer sheetType;

    /**
     * 备注
     */
    private String description;

    /**
     * 明细列表
     */
    private List<DetailDTO> details;

    @Data
    public static class DetailDTO {
        private Long itemId;
        private BigDecimal amount;
    }
}

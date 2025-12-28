package com.erp.business.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 供应商预付款单DTO
 */
@Data
public class SettlePreSheetDTO {

    private Long id;
    private Long supplierId;
    private String description;
    private List<DetailDTO> details;

    @Data
    public static class DetailDTO {
        private Long itemId;
        private BigDecimal amount;
    }
}

package com.erp.business.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class SettleSheetDTO {
    private Long id;
    private Long supplierId;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalDiscountAmount;
    private String description;
    private List<DetailDTO> details;

    @Data
    public static class DetailDTO {
        private Long bizId;
        private String bizCode;
        private BigDecimal payAmount;
        private BigDecimal discountAmount;
        private String description;
    }
}

package com.erp.business.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class CustomerFeeSheetDTO {
    private Long id;
    private Long customerId;
    private Integer sheetType;
    private String description;
    private List<DetailDTO> details;

    @Data
    public static class DetailDTO {
        private Long itemId;
        private BigDecimal amount;
    }
}

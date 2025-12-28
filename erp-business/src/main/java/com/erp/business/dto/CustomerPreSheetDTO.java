package com.erp.business.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class CustomerPreSheetDTO {
    private Long id;
    private Long customerId;
    private String description;
    private List<DetailDTO> details;

    @Data
    public static class DetailDTO {
        private Long itemId;
        private BigDecimal amount;
    }
}

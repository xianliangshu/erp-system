package com.erp.business.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class StockCheckDTO {
    private Long id;
    private Long scId;
    private String checkDate;
    private String description;
    private List<StockCheckDetailDTO> details;

    @Data
    public static class StockCheckDetailDTO {
        private Long productId;
        private BigDecimal stockNum;
        private BigDecimal actualNum;
        private BigDecimal costPrice;
        private String description;
    }
}

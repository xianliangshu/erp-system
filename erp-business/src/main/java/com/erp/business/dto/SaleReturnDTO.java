package com.erp.business.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class SaleReturnDTO {
    private Long id;
    private Long deliveryId;
    private Long scId;
    private Long customerId;
    private String description;
    private List<SaleReturnDetailDTO> details;

    @Data
    public static class SaleReturnDetailDTO {
        private Long deliveryDetailId;
        private Long productId;
        private BigDecimal deliveryNum;
        private BigDecimal returnNum;
        private BigDecimal taxPrice;
        private String description;
    }
}

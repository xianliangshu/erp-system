package com.erp.business.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 销售出库DTO
 */
@Data
public class SaleDeliveryDTO {

    private Long id;
    private Long orderId;
    private Long scId;
    private Long customerId;
    private String description;
    private List<SaleDeliveryDetailDTO> details;

    @Data
    public static class SaleDeliveryDetailDTO {
        private Long orderDetailId;
        private Long productId;
        private BigDecimal orderNum;
        private BigDecimal deliveryNum;
        private BigDecimal taxPrice;
        private String description;
    }
}

package com.erp.business.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 销售订单DTO
 */
@Data
public class SaleOrderDTO {

    private Long id;
    private Long scId;
    private Long customerId;
    private Long salerId;
    private String expectDeliveryDate;
    private String description;
    private List<SaleOrderDetailDTO> details;

    @Data
    public static class SaleOrderDetailDTO {
        private Long productId;
        private BigDecimal orderNum;
        private BigDecimal taxPrice;
        private String description;
    }
}

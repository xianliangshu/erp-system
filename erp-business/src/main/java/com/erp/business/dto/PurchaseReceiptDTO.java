package com.erp.business.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 采购收货DTO
 */
@Data
public class PurchaseReceiptDTO {

    /**
     * ID (更新时需要)
     */
    private Long id;

    /**
     * 采购订单ID
     */
    private Long orderId;

    /**
     * 仓库ID
     */
    private Long scId;

    /**
     * 供应商ID
     */
    private Long supplierId;

    /**
     * 备注
     */
    private String description;

    /**
     * 收货明细
     */
    private List<PurchaseReceiptDetailDTO> details;

    @Data
    public static class PurchaseReceiptDetailDTO {
        /**
         * 订单明细ID
         */
        private Long orderDetailId;

        /**
         * 商品ID
         */
        private Long productId;

        /**
         * 订单数量
         */
        private BigDecimal orderNum;

        /**
         * 本次收货数量
         */
        private BigDecimal receiveNum;

        /**
         * 含税单价
         */
        private BigDecimal taxPrice;

        /**
         * 备注
         */
        private String description;
    }
}

package com.erp.business.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 采购退货DTO
 */
@Data
public class PurchaseReturnDTO {

    /**
     * 退货单ID（更新时使用）
     */
    private Long id;

    /**
     * 收货单ID
     */
    private Long receiptId;

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
     * 退货明细
     */
    private List<PurchaseReturnDetailDTO> details;

    @Data
    public static class PurchaseReturnDetailDTO {
        /**
         * 收货明细ID
         */
        private Long receiptDetailId;

        /**
         * 商品ID
         */
        private Long productId;

        /**
         * 原收货数量
         */
        private BigDecimal receiveNum;

        /**
         * 退货数量
         */
        private BigDecimal returnNum;

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

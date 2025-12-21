package com.erp.business.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 采购订单DTO
 */
@Data
public class PurchaseOrderDTO {

    /**
     * ID (更新时需要)
     */
    private Long id;

    /**
     * 仓库ID
     */
    private Long scId;

    /**
     * 供应商ID
     */
    private Long supplierId;

    /**
     * 采购员ID
     */
    private Long purchaserId;

    /**
     * 预计到货日期
     */
    private LocalDate expectArriveDate;

    /**
     * 备注
     */
    private String description;

    /**
     * 订单明细
     */
    private List<PurchaseOrderDetailDTO> details;

    @Data
    public static class PurchaseOrderDetailDTO {
        private Long productId;
        private BigDecimal orderNum;
        private BigDecimal taxPrice;
        private String description;
    }
}

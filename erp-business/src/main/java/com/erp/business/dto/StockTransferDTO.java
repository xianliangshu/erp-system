package com.erp.business.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 库存调拨DTO
 */
@Data
public class StockTransferDTO {

    private Long id;

    /** 调出仓库ID */
    private Long outScId;

    /** 调入仓库ID */
    private Long inScId;

    /** 调拨日期 */
    private String transferDate;

    /** 备注 */
    private String description;

    /** 明细列表 */
    private List<StockTransferDetailDTO> details;

    @Data
    public static class StockTransferDetailDTO {
        private Long productId;
        private BigDecimal transferNum;
        private BigDecimal costPrice;
        private String description;
    }
}

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
    /** 关联盘点任务ID（可空，仅「由任务生成差异」时传入） */
    private Long planId;
    /** 关联预先盘点单ID（可空） */
    private Long preSheetId;
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

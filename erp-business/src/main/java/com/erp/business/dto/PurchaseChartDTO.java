package com.erp.business.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class PurchaseChartDTO {
    private BigDecimal totalAmount;
    private Integer totalCount;
    private List<DailyData> dailyData;
    private List<SupplierRank> supplierRanking;

    @Data
    public static class DailyData {
        private String date;
        private BigDecimal amount;
        private Integer count;
    }

    @Data
    public static class SupplierRank {
        private Long supplierId;
        private String supplierName;
        private BigDecimal amount;
        private Integer count;
    }
}

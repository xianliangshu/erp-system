package com.erp.business.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class SummaryReportDTO {
    private BigDecimal purchaseAmount;
    private BigDecimal salesAmount;
    private BigDecimal profit;
    private BigDecimal stockValue;
    private List<MonthlyData> monthlyData;

    @Data
    public static class MonthlyData {
        private String month;
        private BigDecimal purchaseAmount;
        private BigDecimal salesAmount;
        private BigDecimal profit;
    }
}

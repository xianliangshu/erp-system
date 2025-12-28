package com.erp.business.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class SalesChartDTO {
    private BigDecimal totalAmount;
    private Integer totalCount;
    private BigDecimal profit;
    private List<DailyData> dailyData;
    private List<CustomerRank> customerRanking;

    @Data
    public static class DailyData {
        private String date;
        private BigDecimal amount;
        private Integer count;
    }

    @Data
    public static class CustomerRank {
        private Long customerId;
        private String customerName;
        private BigDecimal amount;
        private Integer count;
    }
}

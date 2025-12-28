package com.erp.business.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class DashboardDTO {
    // 今日数据
    private TodayStats today;
    // 本月数据
    private MonthStats month;
    // 低库存预警
    private Integer lowStockCount;
    // 待处理订单
    private Integer pendingOrders;

    @Data
    public static class TodayStats {
        private BigDecimal purchaseAmount;
        private Integer purchaseCount;
        private BigDecimal salesAmount;
        private Integer salesCount;
    }

    @Data
    public static class MonthStats {
        private BigDecimal purchaseAmount;
        private Integer purchaseCount;
        private BigDecimal salesAmount;
        private Integer salesCount;
        private BigDecimal profit;
    }
}

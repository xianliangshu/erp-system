package com.erp.business.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class StockReportDTO {
    private BigDecimal totalValue;
    private Integer totalItems;
    private Integer lowStockCount;
    private List<WarehouseStock> warehouseStats;
    private List<CategoryStock> categoryStats;
    private List<LowStockItem> lowStockItems;

    @Data
    public static class WarehouseStock {
        private Long warehouseId;
        private String warehouseName;
        private BigDecimal value;
        private Integer itemCount;
    }

    @Data
    public static class CategoryStock {
        private Long categoryId;
        private String categoryName;
        private BigDecimal value;
        private Integer itemCount;
    }

    @Data
    public static class LowStockItem {
        private Long materialId;
        private String materialCode;
        private String materialName;
        private BigDecimal currentStock;
        private BigDecimal minStock;
    }
}

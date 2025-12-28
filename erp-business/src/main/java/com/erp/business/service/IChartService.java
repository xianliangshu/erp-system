package com.erp.business.service;

import com.erp.business.dto.*;
import java.time.LocalDate;

public interface IChartService {
    /**
     * 获取仪表盘数据
     */
    DashboardDTO getDashboard();

    /**
     * 获取采购统计
     */
    PurchaseChartDTO getPurchaseStats(LocalDate startDate, LocalDate endDate);

    /**
     * 获取销售统计
     */
    SalesChartDTO getSalesStats(LocalDate startDate, LocalDate endDate);

    /**
     * 获取库存报表
     */
    StockReportDTO getStockReport(Long warehouseId);

    /**
     * 获取进销存汇总
     */
    SummaryReportDTO getSummaryReport(LocalDate startDate, LocalDate endDate);
}

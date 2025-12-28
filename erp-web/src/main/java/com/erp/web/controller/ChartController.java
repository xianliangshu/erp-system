package com.erp.web.controller;

import com.erp.business.dto.*;
import com.erp.business.service.IChartService;
import com.erp.common.core.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Tag(name = "报表统计")
@RestController
@RequestMapping("/chart")
@RequiredArgsConstructor
public class ChartController {

    private final IChartService chartService;

    @Operation(summary = "获取仪表盘数据")
    @GetMapping("/dashboard")
    public Result<DashboardDTO> getDashboard() {
        return Result.success(chartService.getDashboard());
    }

    @Operation(summary = "获取采购统计")
    @GetMapping("/purchase")
    public Result<PurchaseChartDTO> getPurchaseStats(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return Result.success(chartService.getPurchaseStats(startDate, endDate));
    }

    @Operation(summary = "获取销售统计")
    @GetMapping("/sales")
    public Result<SalesChartDTO> getSalesStats(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return Result.success(chartService.getSalesStats(startDate, endDate));
    }

    @Operation(summary = "获取库存报表")
    @GetMapping("/stock")
    public Result<StockReportDTO> getStockReport(
            @RequestParam(required = false) Long warehouseId) {
        return Result.success(chartService.getStockReport(warehouseId));
    }

    @Operation(summary = "获取进销存汇总")
    @GetMapping("/summary")
    public Result<SummaryReportDTO> getSummaryReport(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return Result.success(chartService.getSummaryReport(startDate, endDate));
    }
}

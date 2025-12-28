package com.erp.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.erp.basedata.entity.BaseCustomer;
import com.erp.basedata.entity.BaseMaterial;
import com.erp.basedata.entity.BaseSupplier;
import com.erp.basedata.entity.BaseWarehouse;
import com.erp.basedata.mapper.BaseCustomerMapper;
import com.erp.basedata.mapper.BaseMaterialMapper;
import com.erp.basedata.mapper.BaseSupplierMapper;
import com.erp.basedata.mapper.BaseWarehouseMapper;
import com.erp.business.dto.*;
import com.erp.business.entity.*;
import com.erp.business.mapper.*;
import com.erp.business.service.IChartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChartServiceImpl implements IChartService {

    private final PurchaseOrderMapper purchaseOrderMapper;
    private final SaleOrderMapper saleOrderMapper;
    private final ProductStockMapper productStockMapper;
    private final BaseSupplierMapper supplierMapper;
    private final BaseCustomerMapper customerMapper;
    private final BaseMaterialMapper materialMapper;
    private final BaseWarehouseMapper warehouseMapper;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public DashboardDTO getDashboard() {
        DashboardDTO result = new DashboardDTO();

        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = LocalDateTime.of(today, LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(today, LocalTime.MAX);

        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDateTime monthStartTime = LocalDateTime.of(monthStart, LocalTime.MIN);

        // 今日统计
        DashboardDTO.TodayStats todayStats = new DashboardDTO.TodayStats();
        todayStats.setPurchaseAmount(getTodayPurchaseAmount(todayStart, todayEnd));
        todayStats.setPurchaseCount(getTodayPurchaseCount(todayStart, todayEnd));
        todayStats.setSalesAmount(getTodaySalesAmount(todayStart, todayEnd));
        todayStats.setSalesCount(getTodaySalesCount(todayStart, todayEnd));
        result.setToday(todayStats);

        // 本月统计
        DashboardDTO.MonthStats monthStats = new DashboardDTO.MonthStats();
        monthStats.setPurchaseAmount(getMonthPurchaseAmount(monthStartTime, todayEnd));
        monthStats.setPurchaseCount(getMonthPurchaseCount(monthStartTime, todayEnd));
        monthStats.setSalesAmount(getMonthSalesAmount(monthStartTime, todayEnd));
        monthStats.setSalesCount(getMonthSalesCount(monthStartTime, todayEnd));
        monthStats.setProfit(monthStats.getSalesAmount().subtract(monthStats.getPurchaseAmount()));
        result.setMonth(monthStats);

        // 低库存预警数量
        result.setLowStockCount(getLowStockCount());

        // 待处理订单
        result.setPendingOrders(getPendingOrderCount());

        return result;
    }

    @Override
    public PurchaseChartDTO getPurchaseStats(LocalDate startDate, LocalDate endDate) {
        PurchaseChartDTO result = new PurchaseChartDTO();

        if (startDate == null)
            startDate = LocalDate.now().minusDays(30);
        if (endDate == null)
            endDate = LocalDate.now();

        LocalDateTime startTime = LocalDateTime.of(startDate, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(endDate, LocalTime.MAX);

        // 查询采购订单
        LambdaQueryWrapper<PurchaseOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.between(PurchaseOrder::getCreateTime, startTime, endTime)
                .eq(PurchaseOrder::getStatus, 1);
        List<PurchaseOrder> orders = purchaseOrderMapper.selectList(wrapper);

        // 汇总
        BigDecimal totalAmount = orders.stream()
                .map(PurchaseOrder::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        result.setTotalAmount(totalAmount);
        result.setTotalCount(orders.size());

        // 按日期分组
        final LocalDate finalStartDate = startDate;
        final LocalDate finalEndDate = endDate;
        Map<String, List<PurchaseOrder>> dailyMap = orders.stream()
                .filter(o -> o.getCreateTime() != null)
                .collect(Collectors.groupingBy(o -> o.getCreateTime().toLocalDate().format(DATE_FMT)));
        List<PurchaseChartDTO.DailyData> dailyData = new ArrayList<>();
        LocalDate current = finalStartDate;
        while (!current.isAfter(finalEndDate)) {
            String dateStr = current.format(DATE_FMT);
            PurchaseChartDTO.DailyData data = new PurchaseChartDTO.DailyData();
            data.setDate(dateStr);
            List<PurchaseOrder> dayOrders = dailyMap.getOrDefault(dateStr, Collections.emptyList());
            data.setAmount(dayOrders.stream().map(PurchaseOrder::getTotalAmount).filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
            data.setCount(dayOrders.size());
            dailyData.add(data);
            current = current.plusDays(1);
        }
        result.setDailyData(dailyData);

        // 获取供应商名称映射
        Set<Long> supplierIds = orders.stream()
                .map(PurchaseOrder::getSupplierId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, String> supplierNameMap = getSupplierNameMap(supplierIds);

        // 供应商排名（Top 10）
        Map<Long, List<PurchaseOrder>> supplierMap = orders.stream()
                .filter(o -> o.getSupplierId() != null)
                .collect(Collectors.groupingBy(PurchaseOrder::getSupplierId));
        List<PurchaseChartDTO.SupplierRank> supplierRanking = supplierMap.entrySet().stream()
                .map(e -> {
                    PurchaseChartDTO.SupplierRank rank = new PurchaseChartDTO.SupplierRank();
                    rank.setSupplierId(e.getKey());
                    rank.setSupplierName(supplierNameMap.getOrDefault(e.getKey(), "未知供应商"));
                    rank.setAmount(e.getValue().stream().map(PurchaseOrder::getTotalAmount).filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add));
                    rank.setCount(e.getValue().size());
                    return rank;
                })
                .sorted((a, b) -> b.getAmount().compareTo(a.getAmount()))
                .limit(10)
                .collect(Collectors.toList());
        result.setSupplierRanking(supplierRanking);

        return result;
    }

    @Override
    public SalesChartDTO getSalesStats(LocalDate startDate, LocalDate endDate) {
        SalesChartDTO result = new SalesChartDTO();

        if (startDate == null)
            startDate = LocalDate.now().minusDays(30);
        if (endDate == null)
            endDate = LocalDate.now();

        LocalDateTime startTime = LocalDateTime.of(startDate, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(endDate, LocalTime.MAX);

        // 查询销售订单
        LambdaQueryWrapper<SaleOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.between(SaleOrder::getCreateTime, startTime, endTime)
                .eq(SaleOrder::getStatus, 1);
        List<SaleOrder> orders = saleOrderMapper.selectList(wrapper);

        // 汇总
        BigDecimal totalAmount = orders.stream()
                .map(SaleOrder::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        result.setTotalAmount(totalAmount);
        result.setTotalCount(orders.size());
        result.setProfit(totalAmount.multiply(new BigDecimal("0.2"))); // TODO: 计算真实利润

        // 按日期分组
        final LocalDate finalStartDate = startDate;
        final LocalDate finalEndDate = endDate;
        Map<String, List<SaleOrder>> dailyMap = orders.stream()
                .filter(o -> o.getCreateTime() != null)
                .collect(Collectors.groupingBy(o -> o.getCreateTime().toLocalDate().format(DATE_FMT)));
        List<SalesChartDTO.DailyData> dailyData = new ArrayList<>();
        LocalDate current = finalStartDate;
        while (!current.isAfter(finalEndDate)) {
            String dateStr = current.format(DATE_FMT);
            SalesChartDTO.DailyData data = new SalesChartDTO.DailyData();
            data.setDate(dateStr);
            List<SaleOrder> dayOrders = dailyMap.getOrDefault(dateStr, Collections.emptyList());
            data.setAmount(dayOrders.stream().map(SaleOrder::getTotalAmount).filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
            data.setCount(dayOrders.size());
            dailyData.add(data);
            current = current.plusDays(1);
        }
        result.setDailyData(dailyData);

        // 获取客户名称映射
        Set<Long> customerIds = orders.stream()
                .map(SaleOrder::getCustomerId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, String> customerNameMap = getCustomerNameMap(customerIds);

        // 客户排名（Top 10）
        Map<Long, List<SaleOrder>> customerMap = orders.stream()
                .filter(o -> o.getCustomerId() != null)
                .collect(Collectors.groupingBy(SaleOrder::getCustomerId));
        List<SalesChartDTO.CustomerRank> customerRanking = customerMap.entrySet().stream()
                .map(e -> {
                    SalesChartDTO.CustomerRank rank = new SalesChartDTO.CustomerRank();
                    rank.setCustomerId(e.getKey());
                    rank.setCustomerName(customerNameMap.getOrDefault(e.getKey(), "未知客户"));
                    rank.setAmount(e.getValue().stream().map(SaleOrder::getTotalAmount).filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add));
                    rank.setCount(e.getValue().size());
                    return rank;
                })
                .sorted((a, b) -> b.getAmount().compareTo(a.getAmount()))
                .limit(10)
                .collect(Collectors.toList());
        result.setCustomerRanking(customerRanking);

        return result;
    }

    @Override
    public StockReportDTO getStockReport(Long warehouseId) {
        StockReportDTO result = new StockReportDTO();

        // 查询库存
        LambdaQueryWrapper<ProductStock> wrapper = new LambdaQueryWrapper<>();
        if (warehouseId != null) {
            wrapper.eq(ProductStock::getScId, warehouseId);
        }
        List<ProductStock> stocks = productStockMapper.selectList(wrapper);

        // 总库存价值
        BigDecimal totalValue = stocks.stream()
                .map(s -> s.getStockNum().multiply(s.getTaxPrice() != null ? s.getTaxPrice() : BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        result.setTotalValue(totalValue);
        result.setTotalItems(stocks.size());

        // 获取物料名称映射
        Set<Long> productIds = stocks.stream()
                .map(ProductStock::getProductId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, BaseMaterial> materialMap = getMaterialMap(productIds);

        // 低库存商品
        List<StockReportDTO.LowStockItem> lowStockItems = stocks.stream()
                .filter(s -> s.getStockNum().compareTo(new BigDecimal("10")) < 0)
                .map(s -> {
                    StockReportDTO.LowStockItem item = new StockReportDTO.LowStockItem();
                    item.setMaterialId(s.getProductId());
                    BaseMaterial material = materialMap.get(s.getProductId());
                    if (material != null) {
                        item.setMaterialCode(material.getCode());
                        item.setMaterialName(material.getName());
                    } else {
                        item.setMaterialCode("M" + s.getProductId());
                        item.setMaterialName("未知物料");
                    }
                    item.setCurrentStock(s.getStockNum());
                    item.setMinStock(new BigDecimal("10"));
                    return item;
                })
                .limit(20)
                .collect(Collectors.toList());
        result.setLowStockItems(lowStockItems);
        result.setLowStockCount(lowStockItems.size());

        // 获取仓库名称映射
        Set<Long> warehouseIds = stocks.stream()
                .map(ProductStock::getScId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, String> warehouseNameMap = getWarehouseNameMap(warehouseIds);

        // 按仓库分组
        Map<Long, List<ProductStock>> warehouseMap = stocks.stream()
                .filter(s -> s.getScId() != null)
                .collect(Collectors.groupingBy(ProductStock::getScId));
        List<StockReportDTO.WarehouseStock> warehouseStats = warehouseMap.entrySet().stream()
                .map(e -> {
                    StockReportDTO.WarehouseStock ws = new StockReportDTO.WarehouseStock();
                    ws.setWarehouseId(e.getKey());
                    ws.setWarehouseName(warehouseNameMap.getOrDefault(e.getKey(), "未知仓库"));
                    ws.setValue(e.getValue().stream().map(
                            s -> s.getStockNum().multiply(s.getTaxPrice() != null ? s.getTaxPrice() : BigDecimal.ZERO))
                            .reduce(BigDecimal.ZERO, BigDecimal::add));
                    ws.setItemCount(e.getValue().size());
                    return ws;
                })
                .collect(Collectors.toList());
        result.setWarehouseStats(warehouseStats);

        return result;
    }

    @Override
    public SummaryReportDTO getSummaryReport(LocalDate startDate, LocalDate endDate) {
        SummaryReportDTO result = new SummaryReportDTO();

        if (startDate == null)
            startDate = LocalDate.now().minusMonths(12).withDayOfMonth(1);
        if (endDate == null)
            endDate = LocalDate.now();

        PurchaseChartDTO purchaseStats = getPurchaseStats(startDate, endDate);
        SalesChartDTO salesStats = getSalesStats(startDate, endDate);
        StockReportDTO stockReport = getStockReport(null);

        result.setPurchaseAmount(purchaseStats.getTotalAmount());
        result.setSalesAmount(salesStats.getTotalAmount());
        result.setProfit(salesStats.getProfit());
        result.setStockValue(stockReport.getTotalValue());
        result.setMonthlyData(new ArrayList<>());

        return result;
    }

    // ========== 名称查询辅助方法 ==========

    private Map<Long, String> getSupplierNameMap(Set<Long> ids) {
        if (ids == null || ids.isEmpty())
            return Collections.emptyMap();
        List<BaseSupplier> suppliers = supplierMapper.selectBatchIds(ids);
        return suppliers.stream().collect(Collectors.toMap(BaseSupplier::getId, BaseSupplier::getName, (a, b) -> a));
    }

    private Map<Long, String> getCustomerNameMap(Set<Long> ids) {
        if (ids == null || ids.isEmpty())
            return Collections.emptyMap();
        List<BaseCustomer> customers = customerMapper.selectBatchIds(ids);
        return customers.stream().collect(Collectors.toMap(BaseCustomer::getId, BaseCustomer::getName, (a, b) -> a));
    }

    private Map<Long, BaseMaterial> getMaterialMap(Set<Long> ids) {
        if (ids == null || ids.isEmpty())
            return Collections.emptyMap();
        List<BaseMaterial> materials = materialMapper.selectBatchIds(ids);
        return materials.stream().collect(Collectors.toMap(BaseMaterial::getId, m -> m, (a, b) -> a));
    }

    private Map<Long, String> getWarehouseNameMap(Set<Long> ids) {
        if (ids == null || ids.isEmpty())
            return Collections.emptyMap();
        List<BaseWarehouse> warehouses = warehouseMapper.selectBatchIds(ids);
        return warehouses.stream().collect(Collectors.toMap(BaseWarehouse::getId, BaseWarehouse::getName, (a, b) -> a));
    }

    // ========== 统计辅助方法 ==========

    private BigDecimal getTodayPurchaseAmount(LocalDateTime start, LocalDateTime end) {
        LambdaQueryWrapper<PurchaseOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.between(PurchaseOrder::getCreateTime, start, end).eq(PurchaseOrder::getStatus, 1);
        List<PurchaseOrder> orders = purchaseOrderMapper.selectList(wrapper);
        return orders.stream().map(PurchaseOrder::getTotalAmount).filter(Objects::nonNull).reduce(BigDecimal.ZERO,
                BigDecimal::add);
    }

    private Integer getTodayPurchaseCount(LocalDateTime start, LocalDateTime end) {
        LambdaQueryWrapper<PurchaseOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.between(PurchaseOrder::getCreateTime, start, end).eq(PurchaseOrder::getStatus, 1);
        return Math.toIntExact(purchaseOrderMapper.selectCount(wrapper));
    }

    private BigDecimal getTodaySalesAmount(LocalDateTime start, LocalDateTime end) {
        LambdaQueryWrapper<SaleOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.between(SaleOrder::getCreateTime, start, end).eq(SaleOrder::getStatus, 1);
        List<SaleOrder> orders = saleOrderMapper.selectList(wrapper);
        return orders.stream().map(SaleOrder::getTotalAmount).filter(Objects::nonNull).reduce(BigDecimal.ZERO,
                BigDecimal::add);
    }

    private Integer getTodaySalesCount(LocalDateTime start, LocalDateTime end) {
        LambdaQueryWrapper<SaleOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.between(SaleOrder::getCreateTime, start, end).eq(SaleOrder::getStatus, 1);
        return Math.toIntExact(saleOrderMapper.selectCount(wrapper));
    }

    private BigDecimal getMonthPurchaseAmount(LocalDateTime start, LocalDateTime end) {
        return getTodayPurchaseAmount(start, end);
    }

    private Integer getMonthPurchaseCount(LocalDateTime start, LocalDateTime end) {
        return getTodayPurchaseCount(start, end);
    }

    private BigDecimal getMonthSalesAmount(LocalDateTime start, LocalDateTime end) {
        return getTodaySalesAmount(start, end);
    }

    private Integer getMonthSalesCount(LocalDateTime start, LocalDateTime end) {
        return getTodaySalesCount(start, end);
    }

    private Integer getLowStockCount() {
        LambdaQueryWrapper<ProductStock> wrapper = new LambdaQueryWrapper<>();
        wrapper.lt(ProductStock::getStockNum, new BigDecimal("10"));
        return Math.toIntExact(productStockMapper.selectCount(wrapper));
    }

    private Integer getPendingOrderCount() {
        LambdaQueryWrapper<PurchaseOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PurchaseOrder::getStatus, 0);
        int purchasePending = Math.toIntExact(purchaseOrderMapper.selectCount(wrapper));

        LambdaQueryWrapper<SaleOrder> saleWrapper = new LambdaQueryWrapper<>();
        saleWrapper.eq(SaleOrder::getStatus, 0);
        int salePending = Math.toIntExact(saleOrderMapper.selectCount(saleWrapper));

        return purchasePending + salePending;
    }
}

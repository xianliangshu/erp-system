package com.erp.web.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.business.dto.SaleDeliveryDTO;
import com.erp.business.entity.SaleDelivery;
import com.erp.business.entity.SaleDeliveryDetail;
import com.erp.business.mapper.SaleDeliveryDetailMapper;
import com.erp.business.service.ISaleDeliveryService;
import com.erp.common.core.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 销售出库Controller
 */
@RestController
@RequestMapping("/business/sale/delivery")
@RequiredArgsConstructor
public class SaleDeliveryController {

    private final ISaleDeliveryService saleDeliveryService;
    private final SaleDeliveryDetailMapper saleDeliveryDetailMapper;

    @GetMapping("/page")
    public Result<Page<SaleDelivery>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Long orderId,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Integer status) {
        return Result.success(saleDeliveryService.getDeliveryPage(current, size, orderId, customerId, status));
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> getById(@PathVariable Long id) {
        SaleDelivery delivery = saleDeliveryService.getById(id);
        if (delivery == null)
            return Result.success(null);

        LambdaQueryWrapper<SaleDeliveryDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SaleDeliveryDetail::getDeliveryId, id);
        List<SaleDeliveryDetail> details = saleDeliveryDetailMapper.selectList(wrapper);

        Map<String, Object> result = new HashMap<>();
        result.put("delivery", delivery);
        result.put("details", details);
        return Result.success(result);
    }

    @PostMapping
    public Result<Long> create(@RequestBody SaleDeliveryDTO dto) {
        return Result.success(saleDeliveryService.createDelivery(dto));
    }

    @PutMapping
    public Result<Void> update(@RequestBody SaleDeliveryDTO dto) {
        saleDeliveryService.updateDelivery(dto);
        return Result.success();
    }

    @PostMapping("/{id}/confirm")
    public Result<Void> confirm(@PathVariable Long id) {
        saleDeliveryService.confirmDelivery(id);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        saleDeliveryService.removeById(id);
        return Result.success();
    }

    @GetMapping("/pending-orders")
    public Result<?> getPendingDeliveryOrders(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Long customerId) {
        return Result.success(saleDeliveryService.getPendingDeliveryOrders(current, size, customerId));
    }
}

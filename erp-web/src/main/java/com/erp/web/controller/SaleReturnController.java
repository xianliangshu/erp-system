package com.erp.web.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.business.dto.SaleReturnDTO;
import com.erp.business.entity.SaleReturn;
import com.erp.business.entity.SaleReturnDetail;
import com.erp.business.mapper.SaleReturnDetailMapper;
import com.erp.business.service.ISaleReturnService;
import com.erp.common.core.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/business/sale/return")
@RequiredArgsConstructor
public class SaleReturnController {

    private final ISaleReturnService saleReturnService;
    private final SaleReturnDetailMapper saleReturnDetailMapper;

    @GetMapping("/page")
    public Result<Page<SaleReturn>> page(
            @RequestParam(defaultValue = "1") Long current, @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Long deliveryId, @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Integer status) {
        return Result.success(saleReturnService.getReturnPage(current, size, deliveryId, customerId, status));
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> getById(@PathVariable Long id) {
        SaleReturn saleReturn = saleReturnService.getById(id);
        if (saleReturn == null)
            return Result.success(null);
        LambdaQueryWrapper<SaleReturnDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SaleReturnDetail::getReturnId, id);
        List<SaleReturnDetail> details = saleReturnDetailMapper.selectList(wrapper);
        Map<String, Object> result = new HashMap<>();
        result.put("saleReturn", saleReturn);
        result.put("details", details);
        return Result.success(result);
    }

    @PostMapping
    public Result<Long> create(@RequestBody SaleReturnDTO dto) {
        return Result.success(saleReturnService.createReturn(dto));
    }

    @PutMapping
    public Result<Void> update(@RequestBody SaleReturnDTO dto) {
        saleReturnService.updateReturn(dto);
        return Result.success();
    }

    @PostMapping("/{id}/confirm")
    public Result<Void> confirm(@PathVariable Long id) {
        saleReturnService.confirmReturn(id);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        saleReturnService.removeById(id);
        return Result.success();
    }

    @GetMapping("/pending-deliveries")
    public Result<?> getPendingReturnDeliveries(
            @RequestParam(defaultValue = "1") Long current, @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Long customerId) {
        return Result.success(saleReturnService.getPendingReturnDeliveries(current, size, customerId));
    }
}

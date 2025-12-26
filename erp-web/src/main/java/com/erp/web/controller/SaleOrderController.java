package com.erp.web.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.business.dto.SaleOrderDTO;
import com.erp.business.entity.SaleOrder;
import com.erp.business.entity.SaleOrderDetail;
import com.erp.business.mapper.SaleOrderDetailMapper;
import com.erp.business.service.ISaleOrderService;
import com.erp.common.core.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 销售订单Controller
 */
@RestController
@RequestMapping("/business/sale/order")
@RequiredArgsConstructor
public class SaleOrderController {

    private final ISaleOrderService saleOrderService;
    private final SaleOrderDetailMapper saleOrderDetailMapper;

    /**
     * 分页查询销售订单
     */
    @GetMapping("/page")
    public Result<Page<SaleOrder>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Integer status) {
        return Result.success(saleOrderService.getOrderPage(current, size, customerId, status));
    }

    /**
     * 获取订单详情（包含明细）
     */
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getById(@PathVariable Long id) {
        SaleOrder order = saleOrderService.getById(id);
        if (order == null) {
            return Result.success(null);
        }

        LambdaQueryWrapper<SaleOrderDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SaleOrderDetail::getOrderId, id);
        List<SaleOrderDetail> details = saleOrderDetailMapper.selectList(wrapper);

        Map<String, Object> result = new HashMap<>();
        result.put("order", order);
        result.put("details", details);

        return Result.success(result);
    }

    /**
     * 创建销售订单
     */
    @PostMapping
    public Result<Long> create(@RequestBody SaleOrderDTO dto) {
        Long orderId = saleOrderService.createOrder(dto);
        return Result.success(orderId);
    }

    /**
     * 更新销售订单
     */
    @PutMapping
    public Result<Void> update(@RequestBody SaleOrderDTO dto) {
        saleOrderService.updateOrder(dto);
        return Result.success();
    }

    /**
     * 审核通过
     */
    @PostMapping("/{id}/approve")
    public Result<Void> approve(@PathVariable Long id) {
        saleOrderService.approveOrder(id);
        return Result.success();
    }

    /**
     * 审核拒绝
     */
    @PostMapping("/{id}/reject")
    public Result<Void> reject(@PathVariable Long id, @RequestParam String reason) {
        saleOrderService.rejectOrder(id, reason);
        return Result.success();
    }

    /**
     * 取消订单
     */
    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id) {
        saleOrderService.cancelOrder(id);
        return Result.success();
    }

    /**
     * 删除订单
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        saleOrderService.removeById(id);
        return Result.success();
    }
}

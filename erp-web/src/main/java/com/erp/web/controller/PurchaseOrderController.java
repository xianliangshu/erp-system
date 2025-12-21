package com.erp.web.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.business.dto.PurchaseOrderDTO;
import com.erp.business.entity.PurchaseOrder;
import com.erp.business.entity.PurchaseOrderDetail;
import com.erp.business.mapper.PurchaseOrderDetailMapper;
import com.erp.business.service.IPurchaseOrderService;
import com.erp.common.core.result.Result;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 采购订单Controller
 */
@RestController
@RequestMapping("/business/purchase/order")
@RequiredArgsConstructor
public class PurchaseOrderController {

    private final IPurchaseOrderService purchaseOrderService;
    private final PurchaseOrderDetailMapper purchaseOrderDetailMapper;

    /**
     * 分页查询采购订单
     */
    @GetMapping("/page")
    public Result<Page<PurchaseOrder>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) Integer status) {
        return Result.success(purchaseOrderService.getOrderPage(current, size, supplierId, status));
    }

    /**
     * 获取订单详情（包含明细）
     */
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getById(@PathVariable Long id) {
        PurchaseOrder order = purchaseOrderService.getById(id);
        if (order == null) {
            return Result.success(null);
        }

        // 查询明细
        LambdaQueryWrapper<PurchaseOrderDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PurchaseOrderDetail::getOrderId, id).orderByAsc(PurchaseOrderDetail::getSort);
        List<PurchaseOrderDetail> details = purchaseOrderDetailMapper.selectList(wrapper);

        Map<String, Object> result = new HashMap<>();
        result.put("order", order);
        result.put("details", details);

        return Result.success(result);
    }

    /**
     * 创建采购订单
     */
    @PostMapping
    public Result<Long> create(@RequestBody PurchaseOrderDTO dto) {
        Long orderId = purchaseOrderService.createOrder(dto);
        return Result.success(orderId);
    }

    /**
     * 更新采购订单
     */
    @PutMapping
    public Result<Void> update(@RequestBody PurchaseOrderDTO dto) {
        purchaseOrderService.updateOrder(dto);
        return Result.success();
    }

    /**
     * 审核通过
     */
    @PostMapping("/{id}/approve")
    public Result<Void> approve(@PathVariable Long id) {
        purchaseOrderService.approve(id);
        return Result.success();
    }

    /**
     * 审核拒绝
     */
    @PostMapping("/{id}/reject")
    public Result<Void> reject(@PathVariable Long id, @RequestParam String reason) {
        purchaseOrderService.reject(id, reason);
        return Result.success();
    }

    /**
     * 取消订单
     */
    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id) {
        purchaseOrderService.cancel(id);
        return Result.success();
    }

    /**
     * 删除订单
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        purchaseOrderService.removeById(id);
        return Result.success();
    }
}

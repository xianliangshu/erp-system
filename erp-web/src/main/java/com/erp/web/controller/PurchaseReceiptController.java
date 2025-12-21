package com.erp.web.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.business.dto.PurchaseReceiptDTO;
import com.erp.business.entity.PurchaseReceipt;
import com.erp.business.entity.PurchaseReceiptDetail;
import com.erp.business.mapper.PurchaseReceiptDetailMapper;
import com.erp.business.service.IPurchaseReceiptService;
import com.erp.common.core.result.Result;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 采购收货Controller
 */
@RestController
@RequestMapping("/business/purchase/receipt")
@RequiredArgsConstructor
public class PurchaseReceiptController {

    private final IPurchaseReceiptService purchaseReceiptService;
    private final PurchaseReceiptDetailMapper purchaseReceiptDetailMapper;

    /**
     * 分页查询采购收货
     */
    @GetMapping("/page")
    public Result<Page<PurchaseReceipt>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Long orderId,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) Integer status) {
        return Result.success(purchaseReceiptService.getReceiptPage(current, size, orderId, supplierId, status));
    }

    /**
     * 获取收货单详情（包含明细）
     */
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getById(@PathVariable Long id) {
        PurchaseReceipt receipt = purchaseReceiptService.getById(id);
        if (receipt == null) {
            return Result.success(null);
        }

        // 查询明细
        LambdaQueryWrapper<PurchaseReceiptDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PurchaseReceiptDetail::getReceiptId, id);
        List<PurchaseReceiptDetail> details = purchaseReceiptDetailMapper.selectList(wrapper);

        Map<String, Object> result = new HashMap<>();
        result.put("receipt", receipt);
        result.put("details", details);

        return Result.success(result);
    }

    /**
     * 创建采购收货
     */
    @PostMapping
    public Result<Long> create(@RequestBody PurchaseReceiptDTO dto) {
        Long receiptId = purchaseReceiptService.createReceipt(dto);
        return Result.success(receiptId);
    }

    /**
     * 更新采购收货
     */
    @PutMapping
    public Result<Void> update(@RequestBody PurchaseReceiptDTO dto) {
        purchaseReceiptService.updateReceipt(dto);
        return Result.success();
    }

    /**
     * 确认收货
     */
    @PostMapping("/{id}/confirm")
    public Result<Void> confirm(@PathVariable Long id) {
        purchaseReceiptService.confirmReceipt(id);
        return Result.success();
    }

    /**
     * 删除收货单
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        purchaseReceiptService.removeById(id);
        return Result.success();
    }

    /**
     * 获取待收货的采购订单
     */
    @GetMapping("/pending-orders")
    public Result<?> getPendingReceiveOrders(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Long supplierId) {
        return Result.success(purchaseReceiptService.getPendingReceiveOrders(current, size, supplierId));
    }
}

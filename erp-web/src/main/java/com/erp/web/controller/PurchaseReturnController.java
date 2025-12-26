package com.erp.web.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.business.dto.PurchaseReturnDTO;
import com.erp.business.entity.PurchaseReturn;
import com.erp.business.entity.PurchaseReturnDetail;
import com.erp.business.mapper.PurchaseReturnDetailMapper;
import com.erp.business.service.IPurchaseReturnService;
import com.erp.common.core.result.Result;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 采购退货Controller
 */
@RestController
@RequestMapping("/business/purchase/return")
@RequiredArgsConstructor
public class PurchaseReturnController {

    private final IPurchaseReturnService purchaseReturnService;
    private final PurchaseReturnDetailMapper purchaseReturnDetailMapper;

    /**
     * 分页查询采购退货
     */
    @GetMapping("/page")
    public Result<Page<PurchaseReturn>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Long receiptId,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) Integer status) {
        return Result.success(purchaseReturnService.getReturnPage(current, size, receiptId, supplierId, status));
    }

    /**
     * 获取退货单详情（包含明细）
     */
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getById(@PathVariable Long id) {
        PurchaseReturn purchaseReturn = purchaseReturnService.getById(id);
        if (purchaseReturn == null) {
            return Result.success(null);
        }

        // 查询明细
        LambdaQueryWrapper<PurchaseReturnDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PurchaseReturnDetail::getReturnId, id);
        List<PurchaseReturnDetail> details = purchaseReturnDetailMapper.selectList(wrapper);

        Map<String, Object> result = new HashMap<>();
        result.put("purchaseReturn", purchaseReturn);
        result.put("details", details);

        return Result.success(result);
    }

    /**
     * 创建采购退货
     */
    @PostMapping
    public Result<Long> create(@RequestBody PurchaseReturnDTO dto) {
        Long returnId = purchaseReturnService.createReturn(dto);
        return Result.success(returnId);
    }

    /**
     * 更新采购退货
     */
    @PutMapping
    public Result<Void> update(@RequestBody PurchaseReturnDTO dto) {
        purchaseReturnService.updateReturn(dto);
        return Result.success();
    }

    /**
     * 确认退货
     */
    @PostMapping("/{id}/confirm")
    public Result<Void> confirm(@PathVariable Long id) {
        purchaseReturnService.confirmReturn(id);
        return Result.success();
    }

    /**
     * 删除退货单
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        purchaseReturnService.removeById(id);
        return Result.success();
    }

    /**
     * 获取可退货的收货单列表
     */
    @GetMapping("/pending-receipts")
    public Result<?> getPendingReturnReceipts(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Long supplierId) {
        return Result.success(purchaseReturnService.getPendingReturnReceipts(current, size, supplierId));
    }
}

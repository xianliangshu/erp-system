package com.erp.web.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.business.dto.StockTransferDTO;
import com.erp.business.entity.StockTransfer;
import com.erp.business.entity.StockTransferDetail;
import com.erp.business.mapper.StockTransferDetailMapper;
import com.erp.business.service.IStockTransferService;
import com.erp.common.core.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 库存调拨Controller
 */
@RestController
@RequestMapping("/business/stock/transfer")
@RequiredArgsConstructor
public class StockTransferController {

    private final IStockTransferService stockTransferService;
    private final StockTransferDetailMapper transferDetailMapper;

    /**
     * 分页查询调拨单
     */
    @GetMapping("/page")
    public Result<Page<StockTransfer>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Long outScId,
            @RequestParam(required = false) Long inScId,
            @RequestParam(required = false) Integer status) {
        return Result.success(stockTransferService.getTransferPage(current, size, outScId, inScId, status));
    }

    /**
     * 获取调拨单详情（含明细）
     */
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getById(@PathVariable Long id) {
        StockTransfer transfer = stockTransferService.getById(id);
        if (transfer == null) {
            return Result.error("调拨单不存在");
        }

        LambdaQueryWrapper<StockTransferDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StockTransferDetail::getTransferId, id);
        List<StockTransferDetail> details = transferDetailMapper.selectList(wrapper);

        Map<String, Object> result = new HashMap<>();
        result.put("transfer", transfer);
        result.put("details", details);
        return Result.success(result);
    }

    /**
     * 创建调拨单
     */
    @PostMapping
    public Result<Long> create(@RequestBody StockTransferDTO dto) {
        Long id = stockTransferService.createTransfer(dto);
        return Result.success(id);
    }

    /**
     * 更新调拨单
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody StockTransferDTO dto) {
        dto.setId(id);
        stockTransferService.updateTransfer(dto);
        return Result.success();
    }

    /**
     * 确认调拨
     */
    @PostMapping("/{id}/confirm")
    public Result<Void> confirm(@PathVariable Long id) {
        stockTransferService.confirmTransfer(id);
        return Result.success();
    }

    /**
     * 删除调拨单
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        stockTransferService.deleteTransfer(id);
        return Result.success();
    }
}

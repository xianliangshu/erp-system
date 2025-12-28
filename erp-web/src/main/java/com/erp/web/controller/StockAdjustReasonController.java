package com.erp.web.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.business.entity.StockAdjustReason;
import com.erp.business.service.IStockAdjustReasonService;
import com.erp.common.core.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 库存调整原因 Controller
 */
@RestController
@RequestMapping("/stock/adjust/reason")
public class StockAdjustReasonController {

    @Autowired
    private IStockAdjustReasonService stockAdjustReasonService;

    /**
     * 分页查询
     */
    @GetMapping("/page")
    public Result<IPage<StockAdjustReason>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer status) {
        Page<StockAdjustReason> page = new Page<>(current, size);
        IPage<StockAdjustReason> result = stockAdjustReasonService.page(page, code, name, status);
        return Result.success(result);
    }

    /**
     * 查询所有启用的调整原因（用于下拉选择）
     */
    @GetMapping("/list")
    public Result<?> list() {
        return Result.success(stockAdjustReasonService.list(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<StockAdjustReason>()
                        .eq(StockAdjustReason::getStatus, 1)
                        .orderByAsc(StockAdjustReason::getCode)));
    }

    /**
     * 根据ID查询
     */
    @GetMapping("/{id}")
    public Result<StockAdjustReason> getById(@PathVariable Long id) {
        return Result.success(stockAdjustReasonService.getById(id));
    }

    /**
     * 新增
     */
    @PostMapping
    public Result<?> add(@RequestBody StockAdjustReason reason) {
        boolean success = stockAdjustReasonService.add(reason);
        return success ? Result.success() : Result.error("新增失败");
    }

    /**
     * 修改
     */
    @PutMapping
    public Result<?> modify(@RequestBody StockAdjustReason reason) {
        boolean success = stockAdjustReasonService.modify(reason);
        return success ? Result.success() : Result.error("修改失败");
    }

    /**
     * 删除
     */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        boolean success = stockAdjustReasonService.removeById(id);
        return success ? Result.success() : Result.error("删除失败");
    }
}

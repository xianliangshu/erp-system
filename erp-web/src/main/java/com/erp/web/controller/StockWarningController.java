package com.erp.web.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.erp.business.entity.StockWarning;
import com.erp.business.service.IStockWarningService;
import com.erp.common.core.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 库存预警 Controller
 *
 * @author ERP System
 */
@RestController
@RequestMapping("/stock/warning")
@RequiredArgsConstructor
public class StockWarningController {

    private final IStockWarningService stockWarningService;

    /**
     * 分页查询预警配置
     */
    @SaCheckPermission("stock:warning:query")
    @GetMapping("/page")
    public Result<IPage<StockWarning>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Long scId,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) Integer status) {
        return Result.success(stockWarningService.pageList(current, size, scId, productName, status));
    }

    /**
     * 实时命中查询：列出当前已低于下限 / 高于上限的商品
     */
    @SaCheckPermission("stock:warning:query")
    @GetMapping("/hit-list")
    public Result<List<StockWarning>> hitList(
            @RequestParam(required = false) Long scId,
            @RequestParam(required = false) String productName) {
        return Result.success(stockWarningService.hitList(scId, productName));
    }

    /**
     * 根据ID查询
     */
    @SaCheckPermission("stock:warning:query")
    @GetMapping("/{id}")
    public Result<StockWarning> getById(@PathVariable Long id) {
        return Result.success(stockWarningService.getById(id));
    }

    /**
     * 新增
     */
    @SaCheckPermission("stock:warning:add")
    @PostMapping
    public Result<?> add(@RequestBody StockWarning warning) {
        return stockWarningService.addWarning(warning) ? Result.success() : Result.error("新增失败");
    }

    /**
     * 修改
     */
    @SaCheckPermission("stock:warning:modify")
    @PutMapping
    public Result<?> modify(@RequestBody StockWarning warning) {
        return stockWarningService.modifyWarning(warning) ? Result.success() : Result.error("修改失败");
    }

    /**
     * 删除
     */
    @SaCheckPermission("stock:warning:delete")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        return stockWarningService.removeById(id) ? Result.success() : Result.error("删除失败");
    }
}

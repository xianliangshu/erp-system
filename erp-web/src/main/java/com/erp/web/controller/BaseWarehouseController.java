package com.erp.web.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.basedata.dto.WarehousePageParam;
import com.erp.basedata.entity.BaseWarehouse;
import com.erp.basedata.service.IBaseWarehouseService;
import com.erp.common.core.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 仓库管理Controller
 * 
 * @author ERP System
 * @since 2025-12-14
 */
@Tag(name = "仓库管理", description = "仓库信息的增删改查接口")
@RestController
@RequestMapping("/basedata/warehouse")
@RequiredArgsConstructor
public class BaseWarehouseController {

    private final IBaseWarehouseService warehouseService;

    /**
     * 分页查询仓库
     */
    @Operation(summary = "分页查询仓库", description = "根据条件分页查询仓库列表")
    @GetMapping("/page")
    public Result<Page<BaseWarehouse>> page(WarehousePageParam param) {
        Page<BaseWarehouse> page = warehouseService.getWarehousePage(param);
        return Result.success(page);
    }

    /**
     * 根据ID查询仓库
     */
    @Operation(summary = "查询仓库详情", description = "根据仓库ID查询仓库详细信息")
    @GetMapping("/{id}")
    public Result<BaseWarehouse> getById(
            @Parameter(description = "仓库ID", required = true) @PathVariable Long id) {
        BaseWarehouse warehouse = warehouseService.getById(id);
        return Result.success(warehouse);
    }

    /**
     * 新增仓库
     */
    @Operation(summary = "新增仓库", description = "创建新仓库")
    @PostMapping
    public Result<Void> save(@RequestBody BaseWarehouse warehouse) {
        warehouseService.saveWarehouse(warehouse);
        return Result.success();
    }

    /**
     * 更新仓库
     */
    @Operation(summary = "更新仓库", description = "更新仓库信息")
    @PutMapping
    public Result<Void> update(@RequestBody BaseWarehouse warehouse) {
        warehouseService.updateWarehouse(warehouse);
        return Result.success();
    }

    /**
     * 删除仓库
     */
    @Operation(summary = "删除仓库", description = "根据仓库ID删除仓库(逻辑删除)")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @Parameter(description = "仓库ID", required = true) @PathVariable Long id) {
        warehouseService.deleteWarehouse(id);
        return Result.success();
    }

    /**
     * 设置默认仓库
     */
    @Operation(summary = "设置默认仓库", description = "将指定仓库设置为默认仓库")
    @PostMapping("/{id}/set-default")
    public Result<Void> setDefault(
            @Parameter(description = "仓库ID", required = true) @PathVariable Long id) {
        warehouseService.setDefaultWarehouse(id);
        return Result.success();
    }

    /**
     * 获取所有启用的仓库
     */
    @Operation(summary = "获取所有启用仓库", description = "查询所有状态为启用的仓库列表")
    @GetMapping("/all")
    public Result<List<BaseWarehouse>> getAllEnabled() {
        List<BaseWarehouse> list = warehouseService.getAllEnabledWarehouses();
        return Result.success(list);
    }

    /**
     * 获取所有启用的仓库列表 (别名)
     */
    @Operation(summary = "获取所有启用仓库列表", description = "查询所有状态为启用的仓库列表")
    @GetMapping("/list")
    public Result<List<BaseWarehouse>> getList() {
        return getAllEnabled();
    }
}

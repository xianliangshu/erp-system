package com.erp.web.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.basedata.dto.SupplierPageParam;
import com.erp.basedata.entity.BaseSupplier;
import com.erp.basedata.service.IBaseSupplierService;
import com.erp.common.core.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 供应商信息Controller
 *
 * @author ERP System
 * @since 2025-12-16
 */
@RestController
@RequestMapping("/basedata/supplier")
@RequiredArgsConstructor
public class BaseSupplierController {

    private final IBaseSupplierService supplierService;

    /**
     * 分页查询供应商
     */
    @GetMapping("/page")
    public Result<Page<BaseSupplier>> page(SupplierPageParam param) {
        Page<BaseSupplier> page = supplierService.getSupplierPage(param);
        return Result.success(page);
    }

    /**
     * 获取所有启用的供应商
     */
    @GetMapping("/list")
    public Result<List<BaseSupplier>> list() {
        List<BaseSupplier> list = supplierService.getAllEnabledSuppliers();
        return Result.success(list);
    }

    /**
     * 根据ID获取供应商
     */
    @GetMapping("/{id}")
    public Result<BaseSupplier> getById(@PathVariable Long id) {
        BaseSupplier supplier = supplierService.getById(id);
        return Result.success(supplier);
    }

    /**
     * 新增供应商
     */
    @PostMapping
    public Result<Void> save(@RequestBody BaseSupplier supplier) {
        supplierService.saveSupplier(supplier);
        return Result.success();
    }

    /**
     * 更新供应商
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody BaseSupplier supplier) {
        supplier.setId(id);
        supplierService.updateSupplier(supplier);
        return Result.success();
    }

    /**
     * 删除供应商
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        supplierService.deleteSupplier(id);
        return Result.success();
    }
}

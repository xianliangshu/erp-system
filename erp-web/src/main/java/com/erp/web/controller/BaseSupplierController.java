package com.erp.web.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.basedata.dto.SupplierPageParam;
import com.erp.basedata.entity.BaseSupplier;
import com.erp.basedata.service.IBaseSupplierService;
import com.erp.basedata.vo.SupplierExcelVO;
import com.erp.common.core.result.Result;
import com.erp.common.core.vo.ExcelImportResult;
import com.erp.common.util.ExcelUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
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
    @SaCheckPermission("basedata:supplier:list")
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
    @SaCheckPermission("basedata:supplier:list")
    @GetMapping("/{id}")
    public Result<BaseSupplier> getById(@PathVariable Long id) {
        BaseSupplier supplier = supplierService.getById(id);
        return Result.success(supplier);
    }

    /**
     * 新增供应商
     */
    @SaCheckPermission("basedata:supplier:list")
    @PostMapping
    public Result<Void> save(@RequestBody BaseSupplier supplier) {
        supplierService.saveSupplier(supplier);
        return Result.success();
    }

    /**
     * 更新供应商
     */
    @SaCheckPermission("basedata:supplier:list")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody BaseSupplier supplier) {
        supplier.setId(id);
        supplierService.updateSupplier(supplier);
        return Result.success();
    }

    /**
     * 删除供应商
     */
    @SaCheckPermission("basedata:supplier:list")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        supplierService.deleteSupplier(id);
        return Result.success();
    }

    /**
     * 导出供应商 Excel（按当前查询条件，不分页）
     */
    @SaCheckPermission("basedata:supplier:list")
    @GetMapping("/export")
    public void export(SupplierPageParam param, HttpServletResponse response) {
        List<SupplierExcelVO> list = supplierService.listForExport(param);
        ExcelUtils.export(response, "供应商列表", "供应商", SupplierExcelVO.class, list);
    }

    /**
     * 下载供应商导入模板（只有表头，无数据行）
     */
    @SaCheckPermission("basedata:supplier:list")
    @GetMapping("/template")
    public void template(HttpServletResponse response) {
        ExcelUtils.export(response, "供应商导入模板", "供应商", SupplierExcelVO.class, List.of());
    }

    /**
     * 批量导入供应商
     */
    @SaCheckPermission("basedata:supplier:list")
    @PostMapping("/import")
    public Result<ExcelImportResult> importExcel(@RequestParam("file") MultipartFile file) throws IOException {
        try (InputStream in = file.getInputStream()) {
            return Result.success(supplierService.importExcel(in));
        }
    }
}

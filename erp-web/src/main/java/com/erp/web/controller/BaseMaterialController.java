package com.erp.web.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.basedata.dto.MaterialPageParam;
import com.erp.basedata.entity.BaseMaterial;
import com.erp.basedata.service.IBaseMaterialService;
import com.erp.basedata.vo.MaterialExcelVO;
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
 * 物料信息Controller
 *
 * @author ERP System
 * @since 2025-12-14
 */
@RestController
@RequestMapping("/basedata/material")
@RequiredArgsConstructor
public class BaseMaterialController {

    private final IBaseMaterialService materialService;

    /**
     * 分页查询物料
     */
    @SaCheckPermission("basedata:material:info")
    @GetMapping("/page")
    public Result<Page<BaseMaterial>> getPage(MaterialPageParam param) {
        Page<BaseMaterial> page = materialService.getMaterialPage(param);
        return Result.success(page);
    }

    /**
     * 根据ID获取物料
     */
    @SaCheckPermission("basedata:material:info")
    @GetMapping("/{id}")
    public Result<BaseMaterial> getById(@PathVariable Long id) {
        BaseMaterial material = materialService.getById(id);
        return Result.success(material);
    }

    /**
     * 根据分类ID获取物料列表
     */
    @GetMapping("/by-category/{categoryId}")
    public Result<List<BaseMaterial>> getByCategory(@PathVariable Long categoryId) {
        List<BaseMaterial> list = materialService.getByCategory(categoryId);
        return Result.success(list);
    }

    /**
     * 获取所有物料列表（下拉选择用）
     */
    @GetMapping("/list")
    public Result<List<BaseMaterial>> getList() {
        List<BaseMaterial> list = materialService.getAll();
        return Result.success(list);
    }

    /**
     * 新增物料
     */
    @SaCheckPermission("basedata:material:info")
    @PostMapping
    public Result<Void> save(@RequestBody BaseMaterial material) {
        materialService.saveMaterial(material);
        return Result.success();
    }

    /**
     * 更新物料
     */
    @SaCheckPermission("basedata:material:info")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody BaseMaterial material) {
        material.setId(id);
        materialService.updateMaterial(material);
        return Result.success();
    }

    /**
     * 删除物料
     */
    @SaCheckPermission("basedata:material:info")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        materialService.deleteMaterial(id);
        return Result.success();
    }

    /**
     * 导出物料 Excel（按当前查询条件，不分页）
     *
     * <p>直接写响应流，返回的不是 JSON —— 前端用 blob 方式下载。</p>
     */
    @SaCheckPermission("basedata:material:info")
    @GetMapping("/export")
    public void export(MaterialPageParam param, HttpServletResponse response) {
        List<MaterialExcelVO> list = materialService.listForExport(param);
        ExcelUtils.export(response, "物料列表", "物料", MaterialExcelVO.class, list);
    }

    /**
     * 下载物料导入模板（只有表头，无数据行）
     */
    @SaCheckPermission("basedata:material:info")
    @GetMapping("/template")
    public void template(HttpServletResponse response) {
        ExcelUtils.export(response, "物料导入模板", "物料", MaterialExcelVO.class, List.of());
    }

    /**
     * 批量导入物料
     *
     * <p>逐行校验、一行不合法则整体不落库，错误明细带 Excel 行号返回。</p>
     */
    @SaCheckPermission("basedata:material:info")
    @PostMapping("/import")
    public Result<ExcelImportResult> importExcel(@RequestParam("file") MultipartFile file) throws IOException {
        try (InputStream in = file.getInputStream()) {
            return Result.success(materialService.importExcel(in));
        }
    }
}

package com.erp.web.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.basedata.dto.MaterialPageParam;
import com.erp.basedata.entity.BaseMaterial;
import com.erp.basedata.service.IBaseMaterialService;
import com.erp.common.core.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
    @GetMapping("/page")
    public Result<Page<BaseMaterial>> getPage(MaterialPageParam param) {
        Page<BaseMaterial> page = materialService.getMaterialPage(param);
        return Result.success(page);
    }

    /**
     * 根据ID获取物料
     */
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
     * 新增物料
     */
    @PostMapping
    public Result<Void> save(@RequestBody BaseMaterial material) {
        materialService.saveMaterial(material);
        return Result.success();
    }

    /**
     * 更新物料
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody BaseMaterial material) {
        material.setId(id);
        materialService.updateMaterial(material);
        return Result.success();
    }

    /**
     * 删除物料
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        materialService.deleteMaterial(id);
        return Result.success();
    }
}

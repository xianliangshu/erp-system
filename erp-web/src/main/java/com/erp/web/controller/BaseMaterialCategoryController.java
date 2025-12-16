package com.erp.web.controller;

import com.erp.basedata.entity.BaseMaterialCategory;
import com.erp.basedata.service.IBaseMaterialCategoryService;
import com.erp.common.core.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 物料分类Controller
 *
 * @author ERP System
 * @since 2025-12-14
 */
@RestController
@RequestMapping("/basedata/material-category")
@RequiredArgsConstructor
public class BaseMaterialCategoryController {

    private final IBaseMaterialCategoryService categoryService;

    /**
     * 获取分类树
     */
    @GetMapping("/tree")
    public Result<List<BaseMaterialCategory>> getTree() {
        List<BaseMaterialCategory> tree = categoryService.getCategoryTree();
        return Result.success(tree);
    }

    /**
     * 根据ID获取分类
     */
    @GetMapping("/{id}")
    public Result<BaseMaterialCategory> getById(@PathVariable Long id) {
        BaseMaterialCategory category = categoryService.getById(id);
        return Result.success(category);
    }

    /**
     * 新增分类
     */
    @PostMapping
    public Result<Void> save(@RequestBody BaseMaterialCategory category) {
        categoryService.saveCategory(category);
        return Result.success();
    }

    /**
     * 更新分类
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody BaseMaterialCategory category) {
        category.setId(id);
        categoryService.updateCategory(category);
        return Result.success();
    }

    /**
     * 删除分类
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return Result.success();
    }
}

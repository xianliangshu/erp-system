package com.erp.web.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.basedata.dto.BrandPageParam;
import com.erp.basedata.entity.BaseBrand;
import com.erp.basedata.service.IBaseBrandService;
import com.erp.common.core.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 品牌信息Controller
 *
 * @author ERP System
 * @since 2025-12-21
 */
@RestController
@RequestMapping("/basedata/brand")
@RequiredArgsConstructor
public class BaseBrandController {

    private final IBaseBrandService brandService;

    /**
     * 分页查询品牌
     */
    @GetMapping("/page")
    public Result<Page<BaseBrand>> page(BrandPageParam param) {
        Page<BaseBrand> page = brandService.getBrandPage(param);
        return Result.success(page);
    }

    /**
     * 获取所有启用的品牌
     */
    @GetMapping("/list")
    public Result<List<BaseBrand>> list() {
        List<BaseBrand> list = brandService.getAllEnabledBrands();
        return Result.success(list);
    }

    /**
     * 根据ID获取品牌
     */
    @GetMapping("/{id}")
    public Result<BaseBrand> getById(@PathVariable Long id) {
        BaseBrand brand = brandService.getById(id);
        return Result.success(brand);
    }

    /**
     * 新增品牌
     */
    @PostMapping
    public Result<Void> save(@RequestBody BaseBrand brand) {
        brandService.saveBrand(brand);
        return Result.success();
    }

    /**
     * 更新品牌
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody BaseBrand brand) {
        brand.setId(id);
        brandService.updateBrand(brand);
        return Result.success();
    }

    /**
     * 删除品牌
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        brandService.deleteBrand(id);
        return Result.success();
    }
}

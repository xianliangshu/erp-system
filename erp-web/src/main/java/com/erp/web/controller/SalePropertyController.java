package com.erp.web.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.basedata.dto.SalePropertyPageParam;
import com.erp.basedata.entity.BaseSaleProperty;
import com.erp.basedata.entity.BaseSalePropertyItem;
import com.erp.basedata.service.ISalePropertyService;
import com.erp.common.core.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品销售属性 Controller
 *
 * @author ERP System
 * @since 2026-10-03
 */
@RestController
@RequestMapping("/basedata/sale-property")
@RequiredArgsConstructor
public class SalePropertyController {

    private final ISalePropertyService salePropertyService;

    /**
     * 分页查询属性组（含属性值）
     */
    @SaCheckPermission("basedata:sale-property:list")
    @GetMapping("/page")
    public Result<Page<BaseSaleProperty>> page(SalePropertyPageParam param) {
        return Result.success(salePropertyService.getPropertyPage(param));
    }

    /**
     * 获取所有启用的属性组（含属性值，跨模块下拉/勾选面板用，只要求登录）
     */
    @GetMapping("/list")
    public Result<List<BaseSaleProperty>> list() {
        return Result.success(salePropertyService.listWithItems());
    }

    /**
     * 查询某属性组下的属性值（SPU 勾选面板用，只要求登录）
     */
    @GetMapping("/{id}/items")
    public Result<List<BaseSalePropertyItem>> listItems(@PathVariable Long id) {
        return Result.success(salePropertyService.listItems(id));
    }

    /**
     * 新增属性组（items 非空时一并写入）
     */
    @SaCheckPermission("basedata:sale-property:list")
    @PostMapping
    public Result<Void> save(@RequestBody BaseSaleProperty property) {
        salePropertyService.saveProperty(property);
        return Result.success();
    }

    /**
     * 更新属性组（属性值差异更新，ID 保持稳定）
     */
    @SaCheckPermission("basedata:sale-property:list")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody BaseSaleProperty property) {
        property.setId(id);
        salePropertyService.updateProperty(property);
        return Result.success();
    }

    /**
     * 删除属性组（已被 SKU 使用时拒绝）
     */
    @SaCheckPermission("basedata:sale-property:list")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        salePropertyService.deleteProperty(id);
        return Result.success();
    }
}

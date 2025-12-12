package com.erp.web.controller;

import com.erp.common.core.result.Result;
import com.erp.system.entity.SysMenu;
import com.erp.system.param.SysMenuQueryParam;
import com.erp.system.service.ISysMenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单管理Controller
 * 
 * @author ERP System
 * @since 2025-12-12
 */
@Tag(name = "菜单管理", description = "菜单的增删改查接口")
@RestController
@RequestMapping("/system/menu")
@RequiredArgsConstructor
public class SysMenuController {

    private final ISysMenuService menuService;

    /**
     * 查询菜单列表(树形结构)
     */
    @Operation(summary = "查询菜单列表", description = "根据条件查询菜单列表")
    @GetMapping("/list")
    public Result<List<SysMenu>> list(SysMenuQueryParam param) {
        // 调用Service业务方法
        List<SysMenu> list = menuService.listQuery(param);
        return Result.success(list);
    }

    /**
     * 根据ID查询菜单
     */
    @Operation(summary = "根据ID查询菜单", description = "根据ID查询菜单")
    @GetMapping("/{id}")
    public Result<SysMenu> getById(@PathVariable Long id) {
        SysMenu menu = menuService.getById(id);
        return Result.success(menu);
    }

    /**
     * 新增菜单
     */
    @Operation(summary = "新增菜单", description = "新增菜单")
    @PostMapping
    public Result<Void> save(@RequestBody SysMenu menu) {
        menuService.save(menu);
        return Result.success();
    }

    /**
     * 修改菜单
     */
    @Operation(summary = "修改菜单", description = "修改菜单")
    @PutMapping
    public Result<Void> update(@RequestBody SysMenu menu) {
        menuService.updateById(menu);
        return Result.success();
    }

    /**
     * 删除菜单
     */
    @Operation(summary = "删除菜单", description = "删除菜单")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        menuService.removeById(id);
        return Result.success();
    }
}

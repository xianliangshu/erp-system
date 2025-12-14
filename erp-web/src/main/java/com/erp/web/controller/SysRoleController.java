package com.erp.web.controller;

import com.erp.common.core.page.PageResult;
import com.erp.common.core.result.Result;
import com.erp.system.entity.SysRole;
import com.erp.system.param.AssignMenusParam;
import com.erp.system.param.SysRolePageParam;
import com.erp.system.service.ISysRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理Controller
 * 
 * @author ERP System
 * @since 2025-12-12
 */
@Tag(name = "角色管理", description = "角色的增删改查接口")
@RestController
@RequestMapping("/system/role")
@RequiredArgsConstructor
public class SysRoleController {

    private final ISysRoleService roleService;

    /**
     * 分页查询角色列表
     */
    @Operation(summary = "分页查询角色", description = "根据条件分页查询角色列表")
    @GetMapping("/page")
    public Result<PageResult<SysRole>> page(SysRolePageParam param) {
        // 调用Service业务方法
        PageResult<SysRole> pageResult = roleService.pageQuery(param);
        return Result.success(pageResult);
    }

    /**
     * 根据ID查询角色
     */
    @Operation(summary = "根据ID查询角色", description = "根据ID查询角色")
    @GetMapping("/{id}")
    public Result<SysRole> getById(@PathVariable Long id) {
        SysRole role = roleService.getById(id);
        return Result.success(role);
    }

    /**
     * 新增角色
     */
    @Operation(summary = "新增角色", description = "新增角色")
    @PostMapping
    public Result<Void> save(@RequestBody SysRole role) {
        roleService.save(role);
        return Result.success();
    }

    /**
     * 修改角色
     */
    @Operation(summary = "修改角色", description = "修改角色")
    @PutMapping
    public Result<Void> update(@RequestBody SysRole role) {
        roleService.updateById(role);
        return Result.success();
    }

    /**
     * 删除角色
     */
    @Operation(summary = "删除角色", description = "删除角色")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        roleService.removeById(id);
        return Result.success();
    }

    /**
     * 分配菜单权限
     */
    @Operation(summary = "分配菜单权限", description = "为角色分配菜单权限")
    @PostMapping("/{id}/menus")
    public Result<Void> assignMenus(
            @Parameter(description = "角色ID", required = true) @PathVariable Long id,
            @RequestBody AssignMenusParam param) {
        roleService.assignMenus(id, param.getMenuIds());
        return Result.success();
    }

    /**
     * 获取角色菜单
     */
    @Operation(summary = "获取角色菜单", description = "查询角色拥有的菜单ID列表")
    @GetMapping("/{id}/menus")
    public Result<List<Long>> getRoleMenus(
            @Parameter(description = "角色ID", required = true) @PathVariable Long id) {
        List<Long> menuIds = roleService.getRoleMenuIds(id);
        return Result.success(menuIds);
    }

    /**
     * 获取所有角色
     */
    @Operation(summary = "获取所有角色", description = "获取所有启用的角色(用于下拉选择)")
    @GetMapping("/all")
    public Result<List<SysRole>> getAllRoles() {
        List<SysRole> roles = roleService.getAllRoles();
        return Result.success(roles);
    }

    /**
     * 统计角色用户数量
     */
    @Operation(summary = "统计角色用户数量", description = "统计指定角色下的用户数量")
    @GetMapping("/{id}/user-count")
    public Result<Long> countRoleUsers(
            @Parameter(description = "角色ID", required = true) @PathVariable Long id) {
        long count = roleService.countRoleUsers(id);
        return Result.success(count);
    }
}

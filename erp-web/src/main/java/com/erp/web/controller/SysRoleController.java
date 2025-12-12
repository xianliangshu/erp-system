package com.erp.web.controller;

import com.erp.common.core.page.PageResult;
import com.erp.common.core.result.Result;
import com.erp.system.entity.SysRole;
import com.erp.system.param.SysRolePageParam;
import com.erp.system.service.ISysRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
}

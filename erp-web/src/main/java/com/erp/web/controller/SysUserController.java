package com.erp.web.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.erp.common.core.page.PageResult;
import com.erp.common.core.result.Result;
import com.erp.system.annotation.Log;
import com.erp.system.entity.SysUser;
import com.erp.system.enums.BusinessType;
import com.erp.system.param.*;
import com.erp.system.service.ISysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户管理Controller
 * 
 * @author ERP System
 * @since 2025-12-12
 */
@Tag(name = "用户管理", description = "用户信息的增删改查接口")
@RestController
@RequestMapping("/system/user")
@RequiredArgsConstructor
public class SysUserController {

    private final ISysUserService userService;

    /**
     * 分页查询用户列表
     */
    @Operation(summary = "分页查询用户", description = "根据条件分页查询用户列表")
    @SaCheckPermission("system:user:list")
    @GetMapping("/page")
    public Result<PageResult<SysUser>> page(SysUserPageParam param) {
        // 调用Service业务方法,不包含业务逻辑
        PageResult<SysUser> pageResult = userService.pageQuery(param);
        return Result.success(pageResult);
    }

    /**
     * 根据ID查询用户
     */
    @Operation(summary = "查询用户详情", description = "根据用户ID查询用户详细信息")
    @SaCheckPermission("system:user:list")
    @GetMapping("/{id}")
    public Result<SysUser> getById(
            @Parameter(description = "用户ID", required = true) @PathVariable Long id) {
        SysUser user = userService.getById(id);
        return Result.success(user);
    }

    /**
     * 新增用户
     */
    @Operation(summary = "新增用户", description = "创建新用户")
    @SaCheckPermission("system:user:add")
    @PostMapping
    public Result<Void> save(@RequestBody SysUser user) {
        userService.save(user);
        return Result.success();
    }

    /**
     * 修改用户
     */
    @Operation(summary = "修改用户", description = "更新用户信息")
    @SaCheckPermission("system:user:edit")
    @PutMapping
    public Result<Void> update(@RequestBody SysUser user) {
        userService.updateById(user);
        return Result.success();
    }

    /**
     * 删除用户
     */
    @Operation(summary = "删除用户", description = "根据用户ID删除用户(逻辑删除)")
    @SaCheckPermission("system:user:delete")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @Parameter(description = "用户ID", required = true) @PathVariable Long id) {
        userService.removeById(id);
        return Result.success();
    }

    /**
     * 重置密码
     *
     * <p>参数里带明文新密码，必须关掉参数记录。</p>
     */
    @Operation(summary = "重置密码", description = "管理员重置用户密码")
    @SaCheckPermission("system:user:resetPwd")
    @Log(title = "重置用户密码", businessType = BusinessType.UPDATE, saveParam = false)
    @PostMapping("/{id}/reset-password")
    public Result<Void> resetPassword(
            @Parameter(description = "用户ID", required = true) @PathVariable Long id,
            @RequestBody ResetPasswordParam param) {
        userService.resetPassword(id, param.getNewPassword());
        return Result.success();
    }

    /**
     * 修改密码
     *
     * <p>参数里带明文新旧密码，必须关掉参数记录。</p>
     */
    @Operation(summary = "修改密码", description = "用户修改自己的密码")
    @Log(title = "修改密码", businessType = BusinessType.UPDATE, saveParam = false)
    @PostMapping("/change-password")
    public Result<Void> changePassword(@RequestBody ChangePasswordParam param) {
        userService.changePassword(
                param.getUserId(),
                param.getOldPassword(),
                param.getNewPassword());
        return Result.success();
    }

    /**
     * 分配角色
     *
     * <p>自动推导出的标题是「用户管理」，这里换成更明确的「分配用户角色」，
     * 审计时一眼能看出这次改动动的是授权关系。</p>
     */
    @Operation(summary = "分配角色", description = "为用户分配角色")
    @SaCheckPermission("system:user:edit")
    @Log(title = "分配用户角色", businessType = BusinessType.UPDATE)
    @PostMapping("/{id}/roles")
    public Result<Void> assignRoles(
            @Parameter(description = "用户ID", required = true) @PathVariable Long id,
            @RequestBody AssignRolesParam param) {
        userService.assignRoles(id, param.getRoleIds());
        return Result.success();
    }

    /**
     * 获取用户角色
     */
    @Operation(summary = "获取用户角色", description = "查询用户拥有的角色ID列表")
    @SaCheckPermission("system:user:list")
    @GetMapping("/{id}/roles")
    public Result<List<Long>> getUserRoles(
            @Parameter(description = "用户ID", required = true) @PathVariable Long id) {
        List<Long> roleIds = userService.getUserRoleIds(id);
        return Result.success(roleIds);
    }

    /**
     * 分配部门
     */
    @Operation(summary = "分配部门", description = "为用户分配部门")
    @SaCheckPermission("system:user:edit")
    @Log(title = "分配用户部门", businessType = BusinessType.UPDATE)
    @PostMapping("/{id}/depts")
    public Result<Void> assignDepts(
            @Parameter(description = "用户ID", required = true) @PathVariable Long id,
            @RequestBody AssignDeptsParam param) {
        userService.assignDepts(id, param.getDeptIds(), param.getMainDeptId());
        return Result.success();
    }

    /**
     * 获取用户部门
     */
    @Operation(summary = "获取用户部门", description = "查询用户所属的部门ID列表")
    @SaCheckPermission("system:user:list")
    @GetMapping("/{id}/depts")
    public Result<List<Long>> getUserDepts(
            @Parameter(description = "用户ID", required = true) @PathVariable Long id) {
        List<Long> deptIds = userService.getUserDeptIds(id);
        return Result.success(deptIds);
    }

    /**
     * 批量删除用户
     */
    @Operation(summary = "批量删除用户", description = "根据用户ID列表批量删除用户")
    @SaCheckPermission("system:user:delete")
    @DeleteMapping("/batch")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        userService.removeByIds(ids);
        return Result.success();
    }
}

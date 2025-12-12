package com.erp.web.controller;

import com.erp.common.core.page.PageResult;
import com.erp.common.core.result.Result;
import com.erp.system.entity.SysUser;
import com.erp.system.param.SysUserPageParam;
import com.erp.system.service.ISysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
    @PostMapping
    public Result<Void> save(@RequestBody SysUser user) {
        userService.save(user);
        return Result.success();
    }

    /**
     * 修改用户
     */
    @Operation(summary = "修改用户", description = "更新用户信息")
    @PutMapping
    public Result<Void> update(@RequestBody SysUser user) {
        userService.updateById(user);
        return Result.success();
    }

    /**
     * 删除用户
     */
    @Operation(summary = "删除用户", description = "根据用户ID删除用户(逻辑删除)")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @Parameter(description = "用户ID", required = true) @PathVariable Long id) {
        userService.removeById(id);
        return Result.success();
    }
}

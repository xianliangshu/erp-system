package com.erp.web.controller;

import com.erp.common.core.result.Result;
import com.erp.system.entity.SysDept;
import com.erp.system.param.SysDeptQueryParam;
import com.erp.system.service.ISysDeptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 部门管理Controller
 * 
 * @author ERP System
 * @since 2025-12-12
 */
@Tag(name = "部门管理", description = "部门的增删改查接口")
@RestController
@RequestMapping("/system/dept")
@RequiredArgsConstructor
public class SysDeptController {

    private final ISysDeptService deptService;

    /**
     * 查询部门列表(树形结构)
     */
    @Operation(summary = "查询部门列表", description = "根据条件查询部门列表")
    @GetMapping("/list")
    public Result<List<SysDept>> list(SysDeptQueryParam param) {
        // 调用Service业务方法
        List<SysDept> list = deptService.listQuery(param);
        return Result.success(list);
    }

    /**
     * 获取部门树
     */
    @Operation(summary = "获取部门树", description = "获取部门树形结构(顶级部门列表)")
    @GetMapping("/tree")
    public Result<List<SysDept>> getTree() {
        List<SysDept> tree = deptService.buildTree();
        return Result.success(tree);
    }

    /**
     * 根据ID查询部门
     */
    @Operation(summary = "根据ID查询部门", description = "根据ID查询部门")
    @GetMapping("/{id}")
    public Result<SysDept> getById(@PathVariable Long id) {
        SysDept dept = deptService.getById(id);
        return Result.success(dept);
    }

    /**
     * 新增部门
     */
    @Operation(summary = "新增部门", description = "新增部门")
    @PostMapping
    public Result<Void> save(@RequestBody SysDept dept) {
        deptService.save(dept);
        return Result.success();
    }

    /**
     * 修改部门
     */
    @Operation(summary = "修改部门", description = "修改部门")
    @PutMapping
    public Result<Void> update(@RequestBody SysDept dept) {
        deptService.updateById(dept);
        return Result.success();
    }

    /**
     * 删除部门
     */
    @Operation(summary = "删除部门", description = "删除部门")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        deptService.removeById(id);
        return Result.success();
    }

    /**
     * 统计部门用户数量
     */
    @Operation(summary = "统计部门用户数量", description = "统计指定部门下的用户数量")
    @GetMapping("/{id}/user-count")
    public Result<Long> countDeptUsers(
            @Parameter(description = "部门ID", required = true) @PathVariable Long id) {
        long count = deptService.countDeptUsers(id);
        return Result.success(count);
    }
}

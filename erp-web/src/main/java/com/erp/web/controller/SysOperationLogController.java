package com.erp.web.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.common.core.result.Result;
import com.erp.system.entity.SysOperationLog;
import com.erp.system.param.LogQueryParam;
import com.erp.system.service.ISysOperationLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 操作日志Controller
 *
 * <p>日志由 {@code LogAspect} 自动写入，本控制器只提供查询与清空。</p>
 *
 * @author ERP System
 * @since 2026-10-03
 */
@Tag(name = "操作日志", description = "后台写操作审计记录查询")
@RestController
@RequestMapping("/system/operation-log")
@RequiredArgsConstructor
public class SysOperationLogController {

    private final ISysOperationLogService operationLogService;

    /**
     * 分页查询操作日志
     */
    @Operation(summary = "分页查询操作日志")
    @SaCheckPermission("system:operationlog:list")
    @GetMapping("/page")
    public Result<Page<SysOperationLog>> page(LogQueryParam param) {
        return Result.success(operationLogService.getPage(param));
    }

    /**
     * 清空操作日志
     */
    @Operation(summary = "清空操作日志")
    @SaCheckPermission("system:operationlog:list")
    @DeleteMapping("/clean")
    public Result<Void> clean() {
        operationLogService.clean();
        return Result.success();
    }
}

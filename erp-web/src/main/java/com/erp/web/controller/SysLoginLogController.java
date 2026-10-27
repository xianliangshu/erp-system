package com.erp.web.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.common.core.result.Result;
import com.erp.system.entity.SysLoginLog;
import com.erp.system.param.LogQueryParam;
import com.erp.system.service.ISysLoginLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 登录日志Controller
 *
 * <p>日志是只读视图，只有「查询」与「清空」两个动作，没有增删改。</p>
 *
 * @author ERP System
 * @since 2026-10-03
 */
@Tag(name = "登录日志", description = "登录成功/失败记录查询")
@RestController
@RequestMapping("/system/login-log")
@RequiredArgsConstructor
public class SysLoginLogController {

    private final ISysLoginLogService loginLogService;

    /**
     * 分页查询登录日志
     */
    @Operation(summary = "分页查询登录日志")
    @SaCheckPermission("system:loginlog:list")
    @GetMapping("/page")
    public Result<Page<SysLoginLog>> page(LogQueryParam param) {
        return Result.success(loginLogService.getPage(param));
    }

    /**
     * 清空登录日志
     */
    @Operation(summary = "清空登录日志")
    @SaCheckPermission("system:loginlog:list")
    @DeleteMapping("/clean")
    public Result<Void> clean() {
        loginLogService.clean();
        return Result.success();
    }
}

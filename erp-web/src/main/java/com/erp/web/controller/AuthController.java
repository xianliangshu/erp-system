package com.erp.web.controller;

import com.erp.common.core.result.Result;
import com.erp.common.core.vo.LoginResult;
import com.erp.system.param.LoginParam;
import com.erp.system.service.IAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证Controller
 * 
 * @author ERP System
 * @since 2025-12-12
 */
@Tag(name = "认证管理", description = "登录、退出等认证接口")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IAuthService authService;

    /**
     * 用户登录
     */
    @Operation(summary = "用户登录", description = "用户名密码登录,返回token和用户信息")
    @PostMapping("/login")
    public Result<LoginResult> login(@RequestBody LoginParam param) {
        LoginResult result = authService.login(param);
        return Result.success(result);
    }

    /**
     * 退出登录
     */
    @Operation(summary = "退出登录", description = "清除用户登录状态")
    @PostMapping("/logout")
    public Result<Void> logout() {
        authService.logout();
        return Result.success();
    }
}

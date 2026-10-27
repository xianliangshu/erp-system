package com.erp.common.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import com.erp.common.core.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * 
 * @author ERP System
 * @since 2025-12-12
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 未登录 / token 无效 —— 必须返回<b>真实的 HTTP 401</b>。
     * <p>
     * 下面几个业务异常都返回 HTTP 200 + 响应体 code, 但鉴权异常不行:
     * 前端 erp-adminPro/src/utils/request.ts 是靠 {@code error.response.status}
     * 判断 401 并清 token 跳登录页的, 如果这里回 200, 前端会当成正常响应去解析 data,
     * 结果就是"接口 401 了但页面不跳登录"。
     */
    @ExceptionHandler(NotLoginException.class)
    public ResponseEntity<Result<?>> handleNotLoginException(NotLoginException e) {
        // 注意: 这里用 if-else 而不是 switch-case, 因为 switch 的 case 标签要求
        // 编译期常量, Sa-Token 的这几个常量不保证是字面量初始化
        String type = e.getType();
        String message;
        if (NotLoginException.NOT_TOKEN.equals(type)) {
            message = "未提供登录凭证,请先登录";
        } else if (NotLoginException.INVALID_TOKEN.equals(type)) {
            message = "登录凭证无效,请重新登录";
        } else if (NotLoginException.TOKEN_TIMEOUT.equals(type)) {
            message = "登录已过期,请重新登录";
        } else if (NotLoginException.BE_REPLACED.equals(type)) {
            message = "账号已在其它端登录";
        } else if (NotLoginException.KICK_OUT.equals(type)) {
            message = "账号已被强制下线";
        } else if (NotLoginException.TOKEN_FREEZE.equals(type)) {
            message = "账号已被冻结";
        } else if (NotLoginException.NO_PREFIX.equals(type)) {
            message = "登录凭证格式不正确(缺少 token 前缀)";
        } else {
            message = "请先登录";
        }
        log.warn("鉴权失败(未登录): type={}, msg={}", type, message);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Result.build(401, message, null));
    }

    /**
     * 已登录但缺少权限 —— HTTP 403
     */
    @ExceptionHandler(NotPermissionException.class)
    public ResponseEntity<Result<?>> handleNotPermissionException(NotPermissionException e) {
        String message = "没有该功能的操作权限: " + e.getPermission();
        log.warn("鉴权失败(无权限): permission={}", e.getPermission());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Result.build(403, message, null));
    }

    /**
     * 已登录但缺少角色 —— HTTP 403
     */
    @ExceptionHandler(NotRoleException.class)
    public ResponseEntity<Result<?>> handleNotRoleException(NotRoleException e) {
        log.warn("鉴权失败(无角色): {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Result.build(403, "没有该功能所需的角色", null));
    }

    /**
     * 处理业务异常
     * 返回HTTP 200,通过响应体的code字段标识错误
     */
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        log.error("业务异常: {}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理运行时异常
     * 返回HTTP 200,通过响应体的code字段标识错误
     */
    @ExceptionHandler(RuntimeException.class)
    public Result<?> handleRuntimeException(RuntimeException e) {
        log.error("运行时异常: {}", e.getMessage(), e);
        return Result.error(e.getMessage());
    }

    /**
     * 处理所有异常
     * 返回HTTP 200,通过响应体的code字段标识错误
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("系统异常: {}", e.getMessage(), e);
        return Result.error("系统异常,请联系管理员");
    }
}

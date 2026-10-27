package com.erp.web.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 拦截器配置。
 * <p>
 * 这里做的第一层是"<b>是否登录</b>"的全局校验 —— 未登录直接 401, 白名单放行。
 * 第二层"<b>有没有这个权限</b>"由各 Controller 上的 {@code @SaCheckPermission} 承担,
 * 由 SaInterceptor 自身解析方法注解(其 isAnnotation 默认为 true, 不需要额外的 AOP 依赖)。
 * <p>
 * 分两层走是有意为之: 一次性给 37 个 Controller 的近 200 个端点标权限串风险太大,
 * 标错就是线上 403。先把"未登录不可访问"这条底线立起来, 权限注解再按模块分批补。
 *
 * @author ERP System
 * @since 2026-10-01
 */
@Configuration
public class SaTokenConfigure implements WebMvcConfigurer {

    /**
     * 免登录白名单。
     * <p>
     * ⚠️ 路径<b>不要</b>带 context-path 前缀。本项目 {@code server.servlet.context-path=/api},
     * 但拦截器做路径匹配时 context-path 已经被剥掉了, 写 "/api/auth/login" 会匹配不上,
     * 结果是登录接口自己被拦下来, 永远登不进去。
     */
    private static final String[] WHITE_LIST = {
            // 登录接口本身
            "/auth/login",
            // Knife4j / OpenAPI 文档
            "/doc.html",
            "/webjars/**",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-resources/**",
            "/favicon.ico",
            // 错误转发页, 不放行会把原始错误覆盖成 401
            "/error"
    };

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> StpUtil.checkLogin()))
                .addPathPatterns("/**")
                .excludePathPatterns(WHITE_LIST);
    }
}

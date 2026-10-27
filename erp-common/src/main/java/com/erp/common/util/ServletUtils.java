package com.erp.common.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Servlet 请求信息工具
 *
 * <p>给登录日志 / 操作日志提供「谁、从哪、用什么设备」三个维度的原始信息。
 * 放在 erp-common 是因为 erp-system（登录日志）与 erp-web（操作日志切面）都要用，
 * 而 erp-common 是两者唯一的共同依赖。</p>
 *
 * @author ERP System
 * @since 2026-10-03
 */
public final class ServletUtils {

    private static final String UNKNOWN = "未知";

    private ServletUtils() {
    }

    /**
     * 取当前线程绑定的请求对象（非 Web 线程或未绑定请求时返回 null）
     *
     * @return 当前 HttpServletRequest
     */
    public static HttpServletRequest getRequest() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servletAttributes) {
            return servletAttributes.getRequest();
        }
        return null;
    }

    /**
     * 取客户端真实 IP
     *
     * <p>依次尝试常见反向代理头，取第一个非 unknown 的值；多级代理时
     * {@code X-Forwarded-For} 是逗号分隔的链，第一个才是客户端。</p>
     *
     * @param request 请求对象，可为 null
     * @return 客户端 IP，取不到时返回「未知」
     */
    public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return UNKNOWN;
        }
        String[] headers = {"X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP", "WL-Proxy-Client-IP"};
        for (String header : headers) {
            String value = request.getHeader(header);
            if (value != null && !value.isBlank() && !"unknown".equalsIgnoreCase(value)) {
                int comma = value.indexOf(',');
                return (comma > 0 ? value.substring(0, comma) : value).trim();
            }
        }
        String ip = request.getRemoteAddr();
        // IPv6 回环地址统一显示成 IPv4 写法，避免日志里一堆 0:0:0:0:0:0:0:1
        return "0:0:0:0:0:0:0:1".equals(ip) ? "127.0.0.1" : ip;
    }

    /**
     * 解析浏览器类型（基于 User-Agent 的粗略识别，够审计用）
     *
     * @param userAgent User-Agent 原文
     * @return 浏览器名称，识别不出时返回「未知」
     */
    public static String getBrowser(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return UNKNOWN;
        }
        // 顺序有讲究：Edge/Chrome 的 UA 里都含 Safari，必须先判断更具体的
        if (userAgent.contains("Edg")) {
            return "Edge";
        }
        if (userAgent.contains("Chrome")) {
            return "Chrome";
        }
        if (userAgent.contains("Firefox")) {
            return "Firefox";
        }
        if (userAgent.contains("Safari")) {
            return "Safari";
        }
        if (userAgent.contains("MSIE") || userAgent.contains("Trident")) {
            return "IE";
        }
        if (userAgent.contains("curl")) {
            return "curl";
        }
        return "其它";
    }

    /**
     * 解析操作系统
     *
     * @param userAgent User-Agent 原文
     * @return 操作系统名称，识别不出时返回「未知」
     */
    public static String getOs(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return UNKNOWN;
        }
        // Android 的 UA 里也含 Linux，iPhone/iPad 的 UA 里含 Mac OS X，都要先判断
        if (userAgent.contains("Android")) {
            return "Android";
        }
        if (userAgent.contains("iPhone") || userAgent.contains("iPad")) {
            return "iOS";
        }
        if (userAgent.contains("Windows NT")) {
            return "Windows";
        }
        if (userAgent.contains("Mac OS X")) {
            return "macOS";
        }
        if (userAgent.contains("Linux")) {
            return "Linux";
        }
        return "其它";
    }
}

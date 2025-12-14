package com.erp.common.util;

/**
 * 密码工具类
 * 注意: 当前使用明文存储,生产环境应使用加密存储
 * 
 * @author ERP System
 * @since 2025-12-12
 */
public class PasswordUtil {

    /**
     * 默认密码
     */
    private static final String DEFAULT_PASSWORD = "123456";

    /**
     * 加密密码 (当前为明文,直接返回)
     * 
     * @param rawPassword 原始密码
     * @return 加密后的密码 (当前返回原始密码)
     */
    public static String encode(String rawPassword) {
        // TODO: 生产环境应使用 BCrypt 或其他加密算法
        return rawPassword;
    }

    /**
     * 验证密码 (当前为明文比较)
     * 
     * @param rawPassword     原始密码
     * @param encodedPassword 加密后的密码
     * @return 是否匹配
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        // TODO: 生产环境应使用加密算法验证
        return rawPassword != null && rawPassword.equals(encodedPassword);
    }

    /**
     * 获取默认密码
     * 
     * @return 默认密码
     */
    public static String getDefaultPassword() {
        return DEFAULT_PASSWORD;
    }
}

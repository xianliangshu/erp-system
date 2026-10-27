package com.erp.common.constant;

/**
 * Sa-Token 会话（Session）中约定的 key
 *
 * <p>登录时往会话里塞，后续鉴权/审计直接读，避免每个请求都回查数据库。</p>
 *
 * @author ERP System
 * @since 2026-10-03
 */
public final class SessionKeys {

    private SessionKeys() {
    }

    /**
     * 当前登录用户的登录名。
     *
     * <p>Sa-Token 的 loginId 存的是 userId，但日志、审计这些面向人的地方
     * 需要的是 {@code admin} 这种登录名，所以在登录时一并放进会话。</p>
     */
    public static final String USERNAME = "username";

    /**
     * 当前登录用户的昵称
     */
    public static final String NICKNAME = "nickname";
}

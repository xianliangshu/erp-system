package com.erp.system.service;

import com.erp.common.core.vo.LoginResult;
import com.erp.system.param.LoginParam;

/**
 * 认证Service接口
 * 
 * @author ERP System
 * @since 2025-12-12
 */
public interface IAuthService {

    /**
     * 用户登录
     * 
     * @param param 登录参数
     * @return 登录结果(token和用户信息)
     */
    LoginResult login(LoginParam param);

    /**
     * 退出登录
     */
    void logout();
}

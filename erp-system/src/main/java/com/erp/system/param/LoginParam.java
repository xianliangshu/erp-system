package com.erp.system.param;

import lombok.Data;

/**
 * 登录请求参数
 * 
 * @author ERP System
 * @since 2025-12-12
 */
@Data
public class LoginParam {

    /** 用户名 */
    private String username;

    /** 密码 */
    private String password;
}

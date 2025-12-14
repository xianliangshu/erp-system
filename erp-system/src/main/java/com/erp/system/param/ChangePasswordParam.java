package com.erp.system.param;

import lombok.Data;

/**
 * 修改密码参数
 * 
 * @author ERP System
 * @since 2025-12-13
 */
@Data
public class ChangePasswordParam {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 旧密码
     */
    private String oldPassword;

    /**
     * 新密码
     */
    private String newPassword;
}

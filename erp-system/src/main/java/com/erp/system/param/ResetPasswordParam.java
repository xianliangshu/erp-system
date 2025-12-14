package com.erp.system.param;

import lombok.Data;

/**
 * 重置密码参数
 * 
 * @author ERP System
 * @since 2025-12-13
 */
@Data
public class ResetPasswordParam {

    /**
     * 新密码
     */
    private String newPassword;
}

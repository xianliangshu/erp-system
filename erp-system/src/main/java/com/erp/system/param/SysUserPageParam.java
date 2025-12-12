package com.erp.system.param;

import com.erp.common.core.page.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户分页查询参数
 * 
 * @author ERP System
 * @since 2025-12-12
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysUserPageParam extends PageRequest {

    /** 用户名(模糊查询) */
    private String username;

    /** 手机号(模糊查询) */
    private String phone;

    /** 状态: 0-禁用 1-启用 */
    private Integer status;
}

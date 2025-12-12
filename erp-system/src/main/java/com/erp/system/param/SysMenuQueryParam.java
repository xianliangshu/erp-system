package com.erp.system.param;

import lombok.Data;

/**
 * 菜单查询参数
 * 
 * @author ERP System
 * @since 2025-12-12
 */
@Data
public class SysMenuQueryParam {

    /** 菜单名称(模糊查询) */
    private String name;

    /** 状态: 0-禁用 1-启用 */
    private Integer status;
}

package com.erp.system.param;

import lombok.Data;

import java.util.List;

/**
 * 分配角色参数
 * 
 * @author ERP System
 * @since 2025-12-13
 */
@Data
public class AssignRolesParam {

    /**
     * 角色ID列表
     */
    private List<Long> roleIds;
}

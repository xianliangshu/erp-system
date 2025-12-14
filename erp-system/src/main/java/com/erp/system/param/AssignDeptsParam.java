package com.erp.system.param;

import lombok.Data;

import java.util.List;

/**
 * 分配部门参数
 * 
 * @author ERP System
 * @since 2025-12-13
 */
@Data
public class AssignDeptsParam {

    /**
     * 部门ID列表
     */
    private List<Long> deptIds;

    /**
     * 主部门ID
     */
    private Long mainDeptId;
}

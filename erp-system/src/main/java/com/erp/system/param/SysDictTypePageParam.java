package com.erp.system.param;

import com.erp.common.core.page.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字典类型分页查询参数
 * 
 * @author ERP System
 * @since 2025-12-12
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysDictTypePageParam extends PageRequest {

    /** 字典名称(模糊查询) */
    private String dictName;

    /** 状态: 0-禁用 1-启用 */
    private Integer status;
}

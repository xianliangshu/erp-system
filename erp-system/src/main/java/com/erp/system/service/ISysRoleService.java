package com.erp.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.common.core.page.PageResult;
import com.erp.system.entity.SysRole;
import com.erp.system.param.SysRolePageParam;

/**
 * 角色Service接口
 * 
 * @author ERP System
 * @since 2025-12-12
 */
public interface ISysRoleService extends IService<SysRole> {

    /**
     * 分页查询角色
     * 
     * @param param 查询参数
     * @return 分页结果
     */
    PageResult<SysRole> pageQuery(SysRolePageParam param);
}

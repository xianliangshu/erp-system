package com.erp.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.common.core.page.PageResult;
import com.erp.system.entity.SysUser;
import com.erp.system.param.SysUserPageParam;

/**
 * 用户Service接口
 * 
 * @author ERP System
 * @since 2025-12-12
 */
public interface ISysUserService extends IService<SysUser> {

    /**
     * 分页查询用户
     * 
     * @param param 查询参数
     * @return 分页结果
     */
    PageResult<SysUser> pageQuery(SysUserPageParam param);
}

package com.erp.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.common.core.page.PageResult;
import com.erp.system.entity.SysRole;
import com.erp.system.param.SysRolePageParam;

import java.util.List;

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

    /**
     * 分配菜单权限
     * 
     * @param roleId  角色ID
     * @param menuIds 菜单ID列表
     * @return 是否成功
     */
    boolean assignMenus(Long roleId, List<Long> menuIds);

    /**
     * 获取角色的菜单ID列表
     * 
     * @param roleId 角色ID
     * @return 菜单ID列表
     */
    List<Long> getRoleMenuIds(Long roleId);

    /**
     * 获取所有角色(用于下拉选择)
     * 
     * @return 角色列表
     */
    List<SysRole> getAllRoles();

    /**
     * 统计角色下的用户数量
     * 
     * @param roleId 角色ID
     * @return 用户数量
     */
    long countRoleUsers(Long roleId);
}

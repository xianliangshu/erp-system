package com.erp.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.common.core.page.PageResult;
import com.erp.system.entity.SysUser;
import com.erp.system.param.SysUserPageParam;

import java.util.List;

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

    /**
     * 重置密码
     * 
     * @param id          用户ID
     * @param newPassword 新密码
     * @return 是否成功
     */
    boolean resetPassword(Long id, String newPassword);

    /**
     * 修改密码
     * 
     * @param id          用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 是否成功
     */
    boolean changePassword(Long id, String oldPassword, String newPassword);

    /**
     * 分配角色
     * 
     * @param userId  用户ID
     * @param roleIds 角色ID列表
     * @return 是否成功
     */
    boolean assignRoles(Long userId, List<Long> roleIds);

    /**
     * 分配部门
     * 
     * @param userId     用户ID
     * @param deptIds    部门ID列表
     * @param mainDeptId 主部门ID
     * @return 是否成功
     */
    boolean assignDepts(Long userId, List<Long> deptIds, Long mainDeptId);

    /**
     * 获取用户的角色ID列表
     * 
     * @param userId 用户ID
     * @return 角色ID列表
     */
    List<Long> getUserRoleIds(Long userId);

    /**
     * 获取用户的部门ID列表
     * 
     * @param userId 用户ID
     * @return 部门ID列表
     */
    List<Long> getUserDeptIds(Long userId);
}

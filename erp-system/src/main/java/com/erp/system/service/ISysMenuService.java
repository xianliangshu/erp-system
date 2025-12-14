package com.erp.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.system.entity.SysMenu;
import com.erp.system.param.SysMenuQueryParam;

import java.util.List;

/**
 * 菜单Service接口
 * 
 * @author ERP System
 * @since 2025-12-12
 */
public interface ISysMenuService extends IService<SysMenu> {

    /**
     * 列表查询菜单
     * 
     * @param param 查询参数
     * @return 菜单列表
     */
    List<SysMenu> listQuery(SysMenuQueryParam param);

    /**
     * 构建菜单树
     * 
     * @return 菜单树(顶级菜单列表)
     */
    List<SysMenu> buildTree();

    /**
     * 获取用户菜单
     * 
     * @param userId 用户ID
     * @return 用户菜单列表
     */
    List<SysMenu> getUserMenus(Long userId);
}

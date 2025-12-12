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
     * 查询菜单列表
     * 
     * @param param 查询参数
     * @return 菜单列表
     */
    List<SysMenu> listQuery(SysMenuQueryParam param);
}

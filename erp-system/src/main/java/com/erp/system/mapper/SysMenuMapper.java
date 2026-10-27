package com.erp.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.system.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 菜单Mapper接口
 * 
 * @author ERP System
 * @since 2025-12-12
 */
@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    /**
     * 根据用户ID查询菜单列表(基于角色权限)
     * 
     * @param userId 用户ID
     * @return 菜单列表
     */
    List<SysMenu> selectMenusByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID查询权限标识集合(用于 Sa-Token 鉴权)
     * <p>
     * 注意: 这里<b>不</b>过滤 visible, 因为隐藏菜单(visible=0)只是不在侧边栏显示,
     * 它背后的接口该有的权限仍然要生效; 只过滤 status=1(菜单被停用则权限一并收回)。
     *
     * @param userId 用户ID
     * @return 权限标识集合, 例如 system:user:add
     */
    List<String> selectPermissionsByUserId(@Param("userId") Long userId);

}

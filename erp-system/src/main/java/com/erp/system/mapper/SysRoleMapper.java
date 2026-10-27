package com.erp.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.system.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色Mapper接口
 * 
 * @author ERP System
 * @since 2025-12-12
 */
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {

    /**
     * 根据用户ID查询角色编号集合(用于 Sa-Token 鉴权)
     *
     * @param userId 用户ID
     * @return 角色编号集合, 例如 R000001
     */
    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);

}

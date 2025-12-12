package com.erp.system.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.common.core.page.PageResult;
import com.erp.system.entity.SysRole;
import com.erp.system.mapper.SysRoleMapper;
import com.erp.system.param.SysRolePageParam;
import com.erp.system.service.ISysRoleService;
import org.springframework.stereotype.Service;

/**
 * 角色Service实现类
 * 
 * @author ERP System
 * @since 2025-12-12
 */
@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements ISysRoleService {

    @Override
    public PageResult<SysRole> pageQuery(SysRolePageParam param) {
        // 创建分页对象
        Page<SysRole> page = new Page<>(param.getCurrent(), param.getSize());

        // 构建查询条件
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(param.getName()),
                SysRole::getName, param.getName())
                .eq(param.getStatus() != null,
                        SysRole::getStatus, param.getStatus())
                .orderByDesc(SysRole::getCreateTime);

        // 执行分页查询
        Page<SysRole> result = this.page(page, wrapper);

        // 封装返回结果
        return PageResult.build(
                result.getTotal(),
                result.getCurrent(),
                result.getSize(),
                result.getRecords());
    }
}

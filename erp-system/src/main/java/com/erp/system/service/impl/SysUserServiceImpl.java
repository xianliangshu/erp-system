package com.erp.system.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.common.core.page.PageResult;
import com.erp.system.entity.SysUser;
import com.erp.system.mapper.SysUserMapper;
import com.erp.system.param.SysUserPageParam;
import com.erp.system.service.ISysUserService;
import org.springframework.stereotype.Service;

/**
 * 用户Service实现类
 * 
 * @author ERP System
 * @since 2025-12-12
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

    @Override
    public PageResult<SysUser> pageQuery(SysUserPageParam param) {
        // 创建分页对象
        Page<SysUser> page = new Page<>(param.getCurrent(), param.getSize());

        // 构建查询条件
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(param.getUsername()),
                SysUser::getUsername, param.getUsername())
                .like(StrUtil.isNotBlank(param.getPhone()),
                        SysUser::getPhone, param.getPhone())
                .eq(param.getStatus() != null,
                        SysUser::getStatus, param.getStatus())
                .orderByDesc(SysUser::getCreateTime);

        // 执行分页查询
        Page<SysUser> result = this.page(page, wrapper);

        // 封装返回结果
        return PageResult.build(
                result.getTotal(),
                result.getCurrent(),
                result.getSize(),
                result.getRecords());
    }
}

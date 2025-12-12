package com.erp.system.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.system.entity.SysDept;
import com.erp.system.mapper.SysDeptMapper;
import com.erp.system.param.SysDeptQueryParam;
import com.erp.system.service.ISysDeptService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 部门Service实现类
 * 
 * @author ERP System
 * @since 2025-12-12
 */
@Service
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept> implements ISysDeptService {

    @Override
    public List<SysDept> listQuery(SysDeptQueryParam param) {
        // 构建查询条件
        LambdaQueryWrapper<SysDept> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(param.getName()),
                SysDept::getName, param.getName())
                .eq(param.getStatus() != null,
                        SysDept::getStatus, param.getStatus())
                .orderByAsc(SysDept::getSort);

        return this.list(wrapper);
    }
}

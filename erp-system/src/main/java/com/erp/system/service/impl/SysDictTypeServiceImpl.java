package com.erp.system.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.common.core.page.PageResult;
import com.erp.system.entity.SysDictType;
import com.erp.system.mapper.SysDictTypeMapper;
import com.erp.system.param.SysDictTypePageParam;
import com.erp.system.service.ISysDictTypeService;
import org.springframework.stereotype.Service;

/**
 * 字典类型Service实现类
 * 
 * @author ERP System
 * @since 2025-12-12
 */
@Service
public class SysDictTypeServiceImpl extends ServiceImpl<SysDictTypeMapper, SysDictType> implements ISysDictTypeService {

    @Override
    public PageResult<SysDictType> pageQuery(SysDictTypePageParam param) {
        // 创建分页对象
        Page<SysDictType> page = new Page<>(param.getCurrent(), param.getSize());

        // 构建查询条件
        LambdaQueryWrapper<SysDictType> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(param.getDictName()),
                SysDictType::getDictName, param.getDictName())
                .eq(param.getStatus() != null,
                        SysDictType::getStatus, param.getStatus())
                .orderByDesc(SysDictType::getCreateTime);

        // 执行分页查询
        Page<SysDictType> result = this.page(page, wrapper);

        // 封装返回结果
        return PageResult.build(
                result.getTotal(),
                result.getCurrent(),
                result.getSize(),
                result.getRecords());
    }
}

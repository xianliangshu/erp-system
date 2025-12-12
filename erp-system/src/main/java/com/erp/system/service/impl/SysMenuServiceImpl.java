package com.erp.system.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.system.entity.SysMenu;
import com.erp.system.mapper.SysMenuMapper;
import com.erp.system.param.SysMenuQueryParam;
import com.erp.system.service.ISysMenuService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 菜单Service实现类
 * 
 * @author ERP System
 * @since 2025-12-12
 */
@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements ISysMenuService {

    @Override
    public List<SysMenu> listQuery(SysMenuQueryParam param) {
        // 构建查询条件
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(param.getName()),
                SysMenu::getName, param.getName())
                .eq(param.getStatus() != null,
                        SysMenu::getStatus, param.getStatus())
                .orderByAsc(SysMenu::getSort);

        return this.list(wrapper);
    }
}

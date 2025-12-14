package com.erp.system.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.common.exception.BusinessException;
import com.erp.system.entity.SysMenu;
import com.erp.system.entity.SysRoleMenu;
import com.erp.system.mapper.SysMenuMapper;
import com.erp.system.mapper.SysRoleMenuMapper;
import com.erp.system.param.SysMenuQueryParam;
import com.erp.system.service.ISysMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 菜单Service实现类
 * 
 * @author ERP System
 * @since 2025-12-12
 */
@Service
@RequiredArgsConstructor
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements ISysMenuService {

    private final SysRoleMenuMapper roleMenuMapper;

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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean save(SysMenu menu) {
        // 1. 生成菜单编号
        if (StrUtil.isBlank(menu.getCode())) {
            menu.setCode(generateMenuCode());
        }

        // 2. 设置默认值
        if (menu.getStatus() == null) {
            menu.setStatus(1); // 默认启用
        }
        if (menu.getVisible() == null) {
            menu.setVisible(1); // 默认显示
        }
        if (menu.getSort() == null) {
            menu.setSort(0);
        }

        // 3. 保存菜单
        return super.save(menu);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateById(SysMenu menu) {
        // 1. 检查菜单是否存在
        SysMenu existMenu = this.getById(menu.getId());
        if (existMenu == null) {
            throw new BusinessException("菜单不存在");
        }

        // 2. 如果修改了父菜单,检查不能将菜单设置为自己的子菜单
        if (menu.getParentId() != null && !menu.getParentId().equals(existMenu.getParentId())) {
            if (isChildMenu(menu.getId(), menu.getParentId())) {
                throw new BusinessException("不能将菜单设置为自己的子菜单");
            }
        }

        // 3. 更新菜单
        return super.updateById(menu);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean removeById(Long id) {
        // 1. 检查菜单是否存在
        SysMenu menu = this.getById(id);
        if (menu == null) {
            throw new BusinessException("菜单不存在");
        }

        // 2. 检查是否有子菜单
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysMenu::getParentId, id);
        long childCount = this.count(wrapper);
        if (childCount > 0) {
            throw new BusinessException("该菜单下还有子菜单,不允许删除");
        }

        // 3. 删除菜单角色关联
        LambdaQueryWrapper<SysRoleMenu> roleWrapper = new LambdaQueryWrapper<>();
        roleWrapper.eq(SysRoleMenu::getMenuId, id);
        roleMenuMapper.delete(roleWrapper);

        // 4. 逻辑删除菜单
        return super.removeById(id);
    }

    @Override
    public List<SysMenu> buildTree() {
        // 1. 查询所有菜单
        List<SysMenu> allMenus = this.list();

        // 2. 构建树形结构(这里返回顶级菜单列表,前端自行构建树)
        return allMenus.stream()
                .filter(menu -> menu.getParentId() == null)
                .collect(Collectors.toList());
    }

    @Override
    public List<SysMenu> getUserMenus(Long userId) {
        // 根据用户角色查询菜单
        List<SysMenu> allMenus = baseMapper.selectMenusByUserId(userId);

        // 构建树形结构
        return buildTreeFromList(allMenus);
    }

    /**
     * 从菜单列表构建树形结构
     * 
     * @param allMenus 所有菜单列表
     * @return 树形结构的菜单列表(顶级菜单)
     */
    private List<SysMenu> buildTreeFromList(List<SysMenu> allMenus) {
        // 1. 找出所有顶级菜单(parentId为null或0)
        List<SysMenu> rootMenus = allMenus.stream()
                .filter(menu -> menu.getParentId() == null || menu.getParentId() == 0)
                .collect(Collectors.toList());

        // 2. 为每个顶级菜单递归设置子菜单
        rootMenus.forEach(menu -> setChildren(menu, allMenus));

        return rootMenus;
    }

    /**
     * 递归设置菜单的子菜单
     * 
     * @param parentMenu 父菜单
     * @param allMenus   所有菜单列表
     */
    private void setChildren(SysMenu parentMenu, List<SysMenu> allMenus) {
        List<SysMenu> children = allMenus.stream()
                .filter(menu -> parentMenu.getId().equals(menu.getParentId()))
                .collect(Collectors.toList());

        if (!children.isEmpty()) {
            parentMenu.setChildren(children);
            // 递归设置子菜单的子菜单
            children.forEach(child -> setChildren(child, allMenus));
        }
    }

    /**
     * 生成菜单编号
     * 格式: M + 6位数字 (例如: M000001)
     * 
     * @return 菜单编号
     */
    private String generateMenuCode() {
        // 查询最大的菜单编号
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(SysMenu::getCode).last("LIMIT 1");
        SysMenu lastMenu = this.getOne(wrapper);

        if (lastMenu == null || StrUtil.isBlank(lastMenu.getCode())) {
            // 如果没有菜单,从M000001开始
            return "M000001";
        }

        // 提取数字部分并加1
        String lastCode = lastMenu.getCode();
        String numberPart = lastCode.substring(1); // 去掉前缀M
        int nextNumber = Integer.parseInt(numberPart) + 1;

        // 格式化为6位数字
        return String.format("M%06d", nextNumber);
    }

    /**
     * 检查是否为子菜单
     * 
     * @param menuId         菜单ID
     * @param targetParentId 目标父菜单ID
     * @return 是否为子菜单
     */
    private boolean isChildMenu(Long menuId, Long targetParentId) {
        if (targetParentId == null) {
            return false;
        }

        // 如果目标父菜单就是当前菜单,返回true
        if (menuId.equals(targetParentId)) {
            return true;
        }

        // 递归检查目标父菜单的父菜单
        SysMenu targetParent = this.getById(targetParentId);
        if (targetParent == null || targetParent.getParentId() == null) {
            return false;
        }

        return isChildMenu(menuId, targetParent.getParentId());
    }
}

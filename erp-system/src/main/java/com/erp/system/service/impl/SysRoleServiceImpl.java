package com.erp.system.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.common.core.page.PageResult;
import com.erp.common.exception.BusinessException;
import com.erp.system.entity.SysRole;
import com.erp.system.entity.SysRoleMenu;
import com.erp.system.entity.SysUser;
import com.erp.system.entity.SysUserRole;
import com.erp.system.mapper.SysRoleMapper;
import com.erp.system.mapper.SysRoleMenuMapper;
import com.erp.system.mapper.SysUserRoleMapper;
import com.erp.system.param.SysRolePageParam;
import com.erp.system.service.ISysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色Service实现类
 * 
 * @author ERP System
 * @since 2025-12-12
 */
@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements ISysRoleService {

        private final SysRoleMenuMapper roleMenuMapper;
        private final SysUserRoleMapper userRoleMapper;

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

        @Override
        @Transactional(rollbackFor = Exception.class)
        public boolean save(SysRole role) {
                // 1. 校验角色名称唯一性
                checkNameUnique(role.getName(), null);

                // 2. 校验角色编号唯一性(如果提供了编号)
                if (StrUtil.isNotBlank(role.getCode())) {
                        checkCodeUnique(role.getCode(), null);
                } else {
                        // 3. 生成角色编号
                        role.setCode(generateRoleCode());
                }

                // 4. 设置默认值
                if (role.getStatus() == null) {
                        role.setStatus(1); // 默认启用
                }
                if (role.getDataScope() == null) {
                        role.setDataScope(1); // 默认全部数据权限
                }

                // 5. 保存角色
                return super.save(role);
        }

        @Override
        @Transactional(rollbackFor = Exception.class)
        public boolean updateById(SysRole role) {
                // 1. 检查角色是否存在
                SysRole existRole = this.getById(role.getId());
                if (existRole == null) {
                        throw new BusinessException("角色不存在");
                }

                // 2. 校验角色名称唯一性(如果修改了名称)
                if (StrUtil.isNotBlank(role.getName())
                                && !role.getName().equals(existRole.getName())) {
                        checkNameUnique(role.getName(), role.getId());
                }

                // 3. 校验角色编号唯一性(如果修改了编号)
                if (StrUtil.isNotBlank(role.getCode())
                                && !role.getCode().equals(existRole.getCode())) {
                        checkCodeUnique(role.getCode(), role.getId());
                }

                // 4. 更新角色
                return super.updateById(role);
        }

        @Transactional(rollbackFor = Exception.class)
        public boolean removeById(Long id) {
                // 1. 检查角色是否存在
                SysRole role = this.getById(id);
                if (role == null) {
                        throw new BusinessException("角色不存在");
                }

                // 2. 检查是否有用户使用该角色
                LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(SysUserRole::getRoleId, id);
                long count = userRoleMapper.selectCount(wrapper);
                if (count > 0) {
                        throw new BusinessException("该角色下还有用户,不允许删除");
                }

                // 3. 删除角色菜单关联
                LambdaQueryWrapper<SysRoleMenu> menuWrapper = new LambdaQueryWrapper<>();
                menuWrapper.eq(SysRoleMenu::getRoleId, id);
                roleMenuMapper.delete(menuWrapper);

                // 4. 逻辑删除角色
                return super.removeById(id);
        }

        @Override
        @Transactional(rollbackFor = Exception.class)
        public boolean assignMenus(Long roleId, List<Long> menuIds) {
                // 1. 检查角色是否存在
                SysRole role = this.getById(roleId);
                if (role == null) {
                        throw new BusinessException("角色不存在");
                }

                // 2. 删除角色原有的菜单关联
                LambdaQueryWrapper<SysRoleMenu> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(SysRoleMenu::getRoleId, roleId);
                roleMenuMapper.delete(wrapper);

                // 3. 如果菜单列表为空,则只删除不添加
                if (menuIds == null || menuIds.isEmpty()) {
                        return true;
                }

                // 4. 批量插入新的菜单关联
                List<SysRoleMenu> roleMenus = menuIds.stream()
                                .map(menuId -> {
                                        SysRoleMenu roleMenu = new SysRoleMenu();
                                        roleMenu.setRoleId(roleId);
                                        roleMenu.setMenuId(menuId);
                                        return roleMenu;
                                })
                                .collect(Collectors.toList());

                // 5. 批量插入
                roleMenus.forEach(roleMenuMapper::insert);

                return true;
        }

        @Override
        public List<Long> getRoleMenuIds(Long roleId) {
                // 查询角色的菜单ID列表
                LambdaQueryWrapper<SysRoleMenu> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(SysRoleMenu::getRoleId, roleId);

                return roleMenuMapper.selectList(wrapper).stream()
                                .map(SysRoleMenu::getMenuId)
                                .collect(Collectors.toList());
        }

        @Override
        public List<SysRole> getAllRoles() {
                // 查询所有启用的角色
                LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(SysRole::getStatus, 1)
                                .orderByAsc(SysRole::getCode);

                return this.list(wrapper);
        }

        @Override
        public long countRoleUsers(Long roleId) {
                // 统计角色下的用户数量
                LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(SysUserRole::getRoleId, roleId);

                return userRoleMapper.selectCount(wrapper);
        }

        /**
         * 生成角色编号
         * 格式: R + 6位数字 (例如: R000001)
         * 
         * @return 角色编号
         */
        private String generateRoleCode() {
                // 查询最大的角色编号
                LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
                wrapper.orderByDesc(SysRole::getCode).last("LIMIT 1");
                SysRole lastRole = this.getOne(wrapper);

                if (lastRole == null || StrUtil.isBlank(lastRole.getCode())) {
                        // 如果没有角色,从R000001开始
                        return "R000001";
                }

                // 提取数字部分并加1
                String lastCode = lastRole.getCode();
                String numberPart = lastCode.substring(1); // 去掉前缀R
                int nextNumber = Integer.parseInt(numberPart) + 1;

                // 格式化为6位数字
                return String.format("R%06d", nextNumber);
        }

        /**
         * 校验角色名称唯一性
         * 
         * @param name   角色名称
         * @param roleId 角色ID(更新时传入,新增时传null)
         */
        private void checkNameUnique(String name, Long roleId) {
                LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(SysRole::getName, name);

                // 如果是更新操作,排除当前角色
                if (roleId != null) {
                        wrapper.ne(SysRole::getId, roleId);
                }

                long count = this.count(wrapper);
                if (count > 0) {
                        throw new BusinessException("角色名称已存在");
                }
        }

        /**
         * 校验角色编号唯一性
         * 注意: 需要查询所有记录(包括已删除的),避免编号冲突
         * 
         * @param code   角色编号
         * @param roleId 角色ID(更新时传入,新增时传null)
         */
        private void checkCodeUnique(String code, Long roleId) {
                // 使用baseMapper直接查询,绕过@TableLogic的自动过滤
                LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(SysRole::getCode, code);

                // 如果是更新操作,排除当前角色
                if (roleId != null) {
                        wrapper.ne(SysRole::getId, roleId);
                }

                // 查询所有记录(包括已删除的)
                Long count = baseMapper.selectCount(wrapper);
                if (count != null && count > 0) {
                        throw new BusinessException("角色编号已存在(包括已删除的记录)");
                }
        }
}

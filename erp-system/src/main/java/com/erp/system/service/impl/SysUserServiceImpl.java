package com.erp.system.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.common.core.page.PageResult;
import com.erp.common.exception.BusinessException;
import com.erp.common.util.PasswordUtil;
import com.erp.system.entity.SysUser;
import com.erp.system.entity.SysUserDept;
import com.erp.system.entity.SysUserRole;
import com.erp.system.mapper.SysUserDeptMapper;
import com.erp.system.mapper.SysUserMapper;
import com.erp.system.mapper.SysUserRoleMapper;
import com.erp.system.param.SysUserPageParam;
import com.erp.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户Service实现类
 * 
 * @author ERP System
 * @since 2025-12-12
 */
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

        private final SysUserRoleMapper userRoleMapper;
        private final SysUserDeptMapper userDeptMapper;

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

        @Override
        @Transactional(rollbackFor = Exception.class)
        public boolean save(SysUser user) {
                // 1. 校验用户名唯一性
                checkUsernameUnique(user.getUsername(), null);

                // 2. 校验手机号唯一性
                if (StrUtil.isNotBlank(user.getPhone())) {
                        checkPhoneUnique(user.getPhone(), null);
                }

                // 3. 生成用户编号
                user.setCode(generateUserCode());

                // 4. 加密密码
                if (StrUtil.isNotBlank(user.getPassword())) {
                        user.setPassword(PasswordUtil.encode(user.getPassword()));
                }

                // 5. 设置默认值
                if (user.getStatus() == null) {
                        user.setStatus(1); // 默认启用
                }
                if (user.getGender() == null) {
                        user.setGender(0); // 默认未知
                }

                // 6. 保存用户
                return super.save(user);
        }

        @Override
        @Transactional(rollbackFor = Exception.class)
        public boolean updateById(SysUser user) {
                // 1. 检查用户是否存在
                SysUser existUser = this.getById(user.getId());
                if (existUser == null) {
                        throw new BusinessException("用户不存在");
                }

                // 2. 校验用户名唯一性(如果修改了用户名)
                if (StrUtil.isNotBlank(user.getUsername())
                                && !user.getUsername().equals(existUser.getUsername())) {
                        checkUsernameUnique(user.getUsername(), user.getId());
                }

                // 3. 校验手机号唯一性(如果修改了手机号)
                if (StrUtil.isNotBlank(user.getPhone())
                                && !user.getPhone().equals(existUser.getPhone())) {
                        checkPhoneUnique(user.getPhone(), user.getId());
                }

                // 4. 不允许直接修改密码(需要通过专门的修改密码接口)
                user.setPassword(null);

                // 5. 更新用户
                return super.updateById(user);
        }

        @Transactional(rollbackFor = Exception.class)
        public boolean removeById(Long id) {
                // 1. 检查用户是否存在
                SysUser user = this.getById(id);
                if (user == null) {
                        throw new BusinessException("用户不存在");
                }

                // 2. 不允许删除管理员账号
                if ("admin".equals(user.getUsername())) {
                        throw new BusinessException("不允许删除管理员账号");
                }

                // 3. 逻辑删除用户(MyBatis-Plus会自动处理@TableLogic)
                return super.removeById(id);
        }

        @Override
        @Transactional(rollbackFor = Exception.class)
        public boolean resetPassword(Long id, String newPassword) {
                // 1. 检查用户是否存在
                SysUser user = this.getById(id);
                if (user == null) {
                        throw new BusinessException("用户不存在");
                }

                // 2. 校验新密码
                if (StrUtil.isBlank(newPassword)) {
                        throw new BusinessException("新密码不能为空");
                }

                // 3. 加密新密码
                // String encodedPassword = PasswordUtil.encode(newPassword);

                // 4. 更新密码
                SysUser updateUser = new SysUser();
                updateUser.setId(id);
                updateUser.setPassword(newPassword);

                return this.updateById(updateUser);
        }

        @Override
        @Transactional(rollbackFor = Exception.class)
        public boolean changePassword(Long id, String oldPassword, String newPassword) {
                // 1. 检查用户是否存在
                SysUser user = this.getById(id);
                if (user == null) {
                        throw new BusinessException("用户不存在");
                }

                // 2. 验证旧密码
                if (!PasswordUtil.matches(oldPassword, user.getPassword())) {
                        throw new BusinessException("原密码错误");
                }

                // 3. 校验新密码
                if (StrUtil.isBlank(newPassword)) {
                        throw new BusinessException("新密码不能为空");
                }

                // 4. 新旧密码不能相同
                if (oldPassword.equals(newPassword)) {
                        throw new BusinessException("新密码不能与旧密码相同");
                }

                // 5. 加密新密码
                // String encodedPassword = PasswordUtil.encode(newPassword);

                // 6. 更新密码
                SysUser updateUser = new SysUser();
                updateUser.setId(id);
                updateUser.setPassword(newPassword);

                return this.updateById(updateUser);
        }

        @Override
        @Transactional(rollbackFor = Exception.class)
        public boolean assignRoles(Long userId, List<Long> roleIds) {
                // 1. 检查用户是否存在
                SysUser user = this.getById(userId);
                if (user == null) {
                        throw new BusinessException("用户不存在");
                }

                // 2. 删除用户原有的角色关联
                LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(SysUserRole::getUserId, userId);
                userRoleMapper.delete(wrapper);

                // 3. 如果角色列表为空,则只删除不添加
                if (roleIds == null || roleIds.isEmpty()) {
                        return true;
                }

                // 4. 批量插入新的角色关联
                List<SysUserRole> userRoles = roleIds.stream()
                                .map(roleId -> {
                                        SysUserRole userRole = new SysUserRole();
                                        userRole.setUserId(userId);
                                        userRole.setRoleId(roleId);
                                        return userRole;
                                })
                                .collect(Collectors.toList());

                // 5. 批量插入
                userRoles.forEach(userRoleMapper::insert);

                return true;
        }

        @Override
        @Transactional(rollbackFor = Exception.class)
        public boolean assignDepts(Long userId, List<Long> deptIds, Long mainDeptId) {
                // 1. 检查用户是否存在
                SysUser user = this.getById(userId);
                if (user == null) {
                        throw new BusinessException("用户不存在");
                }

                // 2. 删除用户原有的部门关联
                LambdaQueryWrapper<SysUserDept> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(SysUserDept::getUserId, userId);
                userDeptMapper.delete(wrapper);

                // 3. 如果部门列表为空,则只删除不添加
                if (deptIds == null || deptIds.isEmpty()) {
                        return true;
                }

                // 4. 校验主部门必须在部门列表中
                if (mainDeptId != null && !deptIds.contains(mainDeptId)) {
                        throw new BusinessException("主部门必须在部门列表中");
                }

                // 5. 批量插入新的部门关联
                List<SysUserDept> userDepts = deptIds.stream()
                                .map(deptId -> {
                                        SysUserDept userDept = new SysUserDept();
                                        userDept.setUserId(userId);
                                        userDept.setDeptId(deptId);
                                        // 设置是否为主部门
                                        userDept.setIsMain(deptId.equals(mainDeptId) ? 1 : 0);
                                        return userDept;
                                })
                                .collect(Collectors.toList());

                // 6. 批量插入
                userDepts.forEach(userDeptMapper::insert);

                return true;
        }

        @Override
        public List<Long> getUserRoleIds(Long userId) {
                // 查询用户的角色ID列表
                LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(SysUserRole::getUserId, userId);

                return userRoleMapper.selectList(wrapper).stream()
                                .map(SysUserRole::getRoleId)
                                .collect(Collectors.toList());
        }

        @Override
        public List<Long> getUserDeptIds(Long userId) {
                // 查询用户的部门ID列表
                LambdaQueryWrapper<SysUserDept> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(SysUserDept::getUserId, userId);

                return userDeptMapper.selectList(wrapper).stream()
                                .map(SysUserDept::getDeptId)
                                .collect(Collectors.toList());
        }

        /**
         * 生成用户编号
         * 格式: U + 6位数字 (例如: U000001)
         * 
         * @return 用户编号
         */
        private String generateUserCode() {
                // 查询最大的用户编号
                LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
                wrapper.orderByDesc(SysUser::getCode).last("LIMIT 1");
                SysUser lastUser = this.getOne(wrapper);

                if (lastUser == null || StrUtil.isBlank(lastUser.getCode())) {
                        // 如果没有用户,从U000001开始
                        return "U000001";
                }

                // 提取数字部分并加1
                String lastCode = lastUser.getCode();
                String numberPart = lastCode.substring(1); // 去掉前缀U
                int nextNumber = Integer.parseInt(numberPart) + 1;

                // 格式化为6位数字
                return String.format("U%06d", nextNumber);
        }

        /**
         * 校验用户名唯一性
         * 
         * @param username 用户名
         * @param userId   用户ID(更新时传入,新增时传null)
         */
        private void checkUsernameUnique(String username, Long userId) {
                LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(SysUser::getUsername, username);

                // 如果是更新操作,排除当前用户
                if (userId != null) {
                        wrapper.ne(SysUser::getId, userId);
                }

                long count = this.count(wrapper);
                if (count > 0) {
                        throw new BusinessException("用户名已存在");
                }
        }

        /**
         * 校验手机号唯一性
         * 
         * @param phone  手机号
         * @param userId 用户ID(更新时传入,新增时传null)
         */
        private void checkPhoneUnique(String phone, Long userId) {
                LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(SysUser::getPhone, phone);

                // 如果是更新操作,排除当前用户
                if (userId != null) {
                        wrapper.ne(SysUser::getId, userId);
                }

                long count = this.count(wrapper);
                if (count > 0) {
                        throw new BusinessException("手机号已存在");
                }
        }
}

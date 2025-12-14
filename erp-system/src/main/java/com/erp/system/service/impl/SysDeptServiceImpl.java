package com.erp.system.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.common.exception.BusinessException;
import com.erp.system.entity.SysDept;
import com.erp.system.entity.SysUserDept;
import com.erp.system.mapper.SysDeptMapper;
import com.erp.system.mapper.SysUserDeptMapper;
import com.erp.system.param.SysDeptQueryParam;
import com.erp.system.service.ISysDeptService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 部门Service实现类
 * 
 * @author ERP System
 * @since 2025-12-12
 */
@Service
@RequiredArgsConstructor
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept> implements ISysDeptService {

    private final SysUserDeptMapper userDeptMapper;

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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean save(SysDept dept) {
        // 1. 生成部门编号
        if (StrUtil.isBlank(dept.getCode())) {
            dept.setCode(generateDeptCode());
        }

        // 2. 设置祖级列表
        dept.setAncestors(buildAncestors(dept.getParentId()));

        // 3. 设置默认值
        if (dept.getStatus() == null) {
            dept.setStatus(1); // 默认启用
        }
        if (dept.getSort() == null) {
            dept.setSort(0);
        }

        // 4. 保存部门
        return super.save(dept);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateById(SysDept dept) {
        // 1. 检查部门是否存在
        SysDept existDept = this.getById(dept.getId());
        if (existDept == null) {
            throw new BusinessException("部门不存在");
        }

        // 2. 如果修改了父部门,需要更新祖级列表
        if (dept.getParentId() != null && !dept.getParentId().equals(existDept.getParentId())) {
            // 检查不能将部门设置为自己的子部门
            if (isChildDept(dept.getId(), dept.getParentId())) {
                throw new BusinessException("不能将部门设置为自己的子部门");
            }

            // 更新祖级列表
            dept.setAncestors(buildAncestors(dept.getParentId()));

            // 更新所有子部门的祖级列表
            updateChildrenAncestors(dept.getId());
        }

        // 3. 更新部门
        return super.updateById(dept);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean removeById(Long id) {
        // 1. 检查部门是否存在
        SysDept dept = this.getById(id);
        if (dept == null) {
            throw new BusinessException("部门不存在");
        }

        // 2. 检查是否有子部门
        LambdaQueryWrapper<SysDept> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDept::getParentId, id);
        long childCount = this.count(wrapper);
        if (childCount > 0) {
            throw new BusinessException("该部门下还有子部门,不允许删除");
        }

        // 3. 检查是否有用户
        LambdaQueryWrapper<SysUserDept> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(SysUserDept::getDeptId, id);
        long userCount = userDeptMapper.selectCount(userWrapper);
        if (userCount > 0) {
            throw new BusinessException("该部门下还有用户,不允许删除");
        }

        // 4. 逻辑删除部门
        return super.removeById(id);
    }

    @Override
    public List<SysDept> buildTree() {
        // 1. 查询所有部门
        List<SysDept> allDepts = this.list();

        // 2. 构建树形结构(这里返回顶级部门列表,前端自行构建树)
        return allDepts.stream()
                .filter(dept -> dept.getParentId() == null)
                .collect(Collectors.toList());
    }

    @Override
    public long countDeptUsers(Long deptId) {
        // 统计部门下的用户数量
        LambdaQueryWrapper<SysUserDept> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserDept::getDeptId, deptId);

        return userDeptMapper.selectCount(wrapper);
    }

    /**
     * 生成部门编号
     * 格式: D + 6位数字 (例如: D000001)
     * 
     * @return 部门编号
     */
    private String generateDeptCode() {
        // 查询最大的部门编号
        LambdaQueryWrapper<SysDept> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(SysDept::getCode).last("LIMIT 1");
        SysDept lastDept = this.getOne(wrapper);

        if (lastDept == null || StrUtil.isBlank(lastDept.getCode())) {
            // 如果没有部门,从D000001开始
            return "D000001";
        }

        // 提取数字部分并加1
        String lastCode = lastDept.getCode();
        String numberPart = lastCode.substring(1); // 去掉前缀D
        int nextNumber = Integer.parseInt(numberPart) + 1;

        // 格式化为6位数字
        return String.format("D%06d", nextNumber);
    }

    /**
     * 构建祖级列表
     * 
     * @param parentId 父部门ID
     * @return 祖级列表字符串
     */
    private String buildAncestors(Long parentId) {
        if (parentId == null) {
            return "0";
        }

        SysDept parent = this.getById(parentId);
        if (parent == null) {
            return "0";
        }

        // 父部门的祖级列表 + 父部门ID
        return parent.getAncestors() + "," + parentId;
    }

    /**
     * 检查是否为子部门
     * 
     * @param deptId         部门ID
     * @param targetParentId 目标父部门ID
     * @return 是否为子部门
     */
    private boolean isChildDept(Long deptId, Long targetParentId) {
        SysDept targetParent = this.getById(targetParentId);
        if (targetParent == null) {
            return false;
        }

        // 检查目标父部门的祖级列表中是否包含当前部门ID
        String ancestors = targetParent.getAncestors();
        return ancestors != null && ancestors.contains(deptId.toString());
    }

    /**
     * 更新所有子部门的祖级列表
     * 
     * @param deptId 部门ID
     */
    private void updateChildrenAncestors(Long deptId) {
        // 查询所有子部门
        LambdaQueryWrapper<SysDept> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDept::getParentId, deptId);
        List<SysDept> children = this.list(wrapper);

        // 递归更新子部门的祖级列表
        for (SysDept child : children) {
            child.setAncestors(buildAncestors(deptId));
            this.updateById(child);

            // 递归更新子部门的子部门
            updateChildrenAncestors(child.getId());
        }
    }
}

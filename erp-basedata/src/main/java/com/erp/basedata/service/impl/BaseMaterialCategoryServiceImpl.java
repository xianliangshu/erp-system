package com.erp.basedata.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.basedata.entity.BaseMaterialCategory;
import com.erp.basedata.mapper.BaseMaterialCategoryMapper;
import com.erp.basedata.service.IBaseMaterialCategoryService;
import com.erp.common.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 物料分类Service实现类
 *
 * @author ERP System
 * @since 2025-12-14
 */
@Service
public class BaseMaterialCategoryServiceImpl extends ServiceImpl<BaseMaterialCategoryMapper, BaseMaterialCategory>
        implements IBaseMaterialCategoryService {

    @Override
    public List<BaseMaterialCategory> getCategoryTree() {
        // 查询所有分类
        LambdaQueryWrapper<BaseMaterialCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(BaseMaterialCategory::getSort)
                .orderByAsc(BaseMaterialCategory::getId);
        List<BaseMaterialCategory> allCategories = this.list(wrapper);

        // 构建树形结构
        return buildTree(allCategories, 0L);
    }

    @Override
    public BaseMaterialCategory getById(Long id) {
        return super.getById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveCategory(BaseMaterialCategory category) {
        // 1. 生成分类编号(如果未提供)
        if (StrUtil.isBlank(category.getCode())) {
            category.setCode(generateCategoryCode());
        }

        // 2. 校验编号唯一性
        checkCodeUnique(category.getCode(), null);

        // 3. 设置默认值
        if (category.getParentId() == null) {
            category.setParentId(0L);
        }
        if (category.getSort() == null) {
            category.setSort(0);
        }
        if (category.getStatus() == null) {
            category.setStatus(1);
        }

        // 4. 保存分类
        return super.save(category);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateCategory(BaseMaterialCategory category) {
        // 1. 校验分类是否存在
        BaseMaterialCategory existCategory = this.getById(category.getId());
        if (existCategory == null) {
            throw new BusinessException("分类不存在");
        }

        // 2. 校验编号唯一性
        if (StrUtil.isNotBlank(category.getCode())) {
            checkCodeUnique(category.getCode(), category.getId());
        }

        // 3. 不允许将父分类设置为自己或自己的子分类
        if (category.getParentId() != null && category.getParentId().equals(category.getId())) {
            throw new BusinessException("不能将父分类设置为自己");
        }

        // 4. 更新分类
        return super.updateById(category);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteCategory(Long id) {
        // 1. 校验分类是否存在
        BaseMaterialCategory category = this.getById(id);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }

        // 2. 检查是否有子分类
        LambdaQueryWrapper<BaseMaterialCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BaseMaterialCategory::getParentId, id);
        long count = this.count(wrapper);
        if (count > 0) {
            throw new BusinessException("该分类下有子分类,不能删除");
        }

        // 3. TODO: 检查是否有物料使用该分类

        // 4. 删除分类(逻辑删除)
        return super.removeById(id);
    }

    /**
     * 构建树形结构
     *
     * @param allCategories 所有分类
     * @param parentId      父分类ID
     * @return 树形结构列表
     */
    private List<BaseMaterialCategory> buildTree(List<BaseMaterialCategory> allCategories, Long parentId) {
        return allCategories.stream()
                .filter(category -> parentId.equals(category.getParentId()))
                .peek(category -> {
                    List<BaseMaterialCategory> children = buildTree(allCategories, category.getId());
                    category.setChildren(children.isEmpty() ? null : children);
                })
                .collect(Collectors.toList());
    }

    /**
     * 生成分类编号
     * 格式: MC + 6位数字 (例如: MC000001)
     *
     * @return 分类编号
     */
    private String generateCategoryCode() {
        // 查询最大的分类编号
        LambdaQueryWrapper<BaseMaterialCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(BaseMaterialCategory::getCode).last("LIMIT 1");
        BaseMaterialCategory lastCategory = this.getOne(wrapper);

        if (lastCategory == null || StrUtil.isBlank(lastCategory.getCode())) {
            // 如果没有分类,从MC000001开始
            return "MC000001";
        }

        // 提取数字部分并加1
        String lastCode = lastCategory.getCode();
        String numberPart = lastCode.substring(2); // 去掉前缀MC
        int nextNumber = Integer.parseInt(numberPart) + 1;

        // 格式化为6位数字
        return String.format("MC%06d", nextNumber);
    }

    /**
     * 校验分类编号唯一性
     * 注意: 需要查询所有记录(包括已删除的),避免编号冲突
     *
     * @param code       分类编号
     * @param categoryId 分类ID(更新时传入)
     */
    private void checkCodeUnique(String code, Long categoryId) {
        // 使用baseMapper直接查询,绕过@TableLogic的自动过滤
        LambdaQueryWrapper<BaseMaterialCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BaseMaterialCategory::getCode, code);
        if (categoryId != null) {
            wrapper.ne(BaseMaterialCategory::getId, categoryId);
        }
        // 查询所有记录(包括已删除的)
        Long count = baseMapper.selectCount(wrapper);
        if (count != null && count > 0) {
            throw new BusinessException("分类编号已存在(包括已删除的记录)");
        }
    }
}

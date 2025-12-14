package com.erp.basedata.service;

import com.erp.basedata.entity.BaseMaterialCategory;

import java.util.List;

/**
 * 物料分类Service接口
 *
 * @author ERP System
 * @since 2025-12-14
 */
public interface IBaseMaterialCategoryService {

    /**
     * 获取分类树
     *
     * @return 分类树列表
     */
    List<BaseMaterialCategory> getCategoryTree();

    /**
     * 根据ID获取分类
     *
     * @param id 分类ID
     * @return 分类信息
     */
    BaseMaterialCategory getById(Long id);

    /**
     * 新增分类
     *
     * @param category 分类信息
     * @return 是否成功
     */
    boolean saveCategory(BaseMaterialCategory category);

    /**
     * 更新分类
     *
     * @param category 分类信息
     * @return 是否成功
     */
    boolean updateCategory(BaseMaterialCategory category);

    /**
     * 删除分类
     *
     * @param id 分类ID
     * @return 是否成功
     */
    boolean deleteCategory(Long id);
}

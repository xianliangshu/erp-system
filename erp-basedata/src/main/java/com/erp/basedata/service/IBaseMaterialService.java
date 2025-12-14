package com.erp.basedata.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.basedata.dto.MaterialPageParam;
import com.erp.basedata.entity.BaseMaterial;

import java.util.List;

/**
 * 物料信息Service接口
 *
 * @author ERP System
 * @since 2025-12-14
 */
public interface IBaseMaterialService {

    /**
     * 分页查询物料
     *
     * @param param 查询参数
     * @return 分页结果
     */
    Page<BaseMaterial> getMaterialPage(MaterialPageParam param);

    /**
     * 根据ID获取物料
     *
     * @param id 物料ID
     * @return 物料信息
     */
    BaseMaterial getById(Long id);

    /**
     * 根据分类ID获取物料列表
     *
     * @param categoryId 分类ID
     * @return 物料列表
     */
    List<BaseMaterial> getByCategory(Long categoryId);

    /**
     * 新增物料
     *
     * @param material 物料信息
     * @return 是否成功
     */
    boolean saveMaterial(BaseMaterial material);

    /**
     * 更新物料
     *
     * @param material 物料信息
     * @return 是否成功
     */
    boolean updateMaterial(BaseMaterial material);

    /**
     * 删除物料
     *
     * @param id 物料ID
     * @return 是否成功
     */
    boolean deleteMaterial(Long id);
}

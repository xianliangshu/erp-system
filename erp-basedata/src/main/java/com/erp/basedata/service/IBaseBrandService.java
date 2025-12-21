package com.erp.basedata.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.basedata.dto.BrandPageParam;
import com.erp.basedata.entity.BaseBrand;

import java.util.List;

/**
 * 品牌信息Service接口
 *
 * @author ERP System
 * @since 2025-12-21
 */
public interface IBaseBrandService extends IService<BaseBrand> {

    /**
     * 分页查询品牌
     *
     * @param param 查询参数
     * @return 分页结果
     */
    Page<BaseBrand> getBrandPage(BrandPageParam param);

    /**
     * 获取所有启用的品牌
     *
     * @return 品牌列表
     */
    List<BaseBrand> getAllEnabledBrands();

    /**
     * 根据ID获取品牌
     *
     * @param id 品牌ID
     * @return 品牌信息
     */
    BaseBrand getById(Long id);

    /**
     * 保存品牌
     *
     * @param brand 品牌信息
     * @return 是否成功
     */
    boolean saveBrand(BaseBrand brand);

    /**
     * 更新品牌
     *
     * @param brand 品牌信息
     * @return 是否成功
     */
    boolean updateBrand(BaseBrand brand);

    /**
     * 删除品牌
     *
     * @param id 品牌ID
     * @return 是否成功
     */
    boolean deleteBrand(Long id);
}

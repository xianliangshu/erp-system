package com.erp.basedata.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.basedata.dto.SupplierPageParam;
import com.erp.basedata.entity.BaseSupplier;

import java.util.List;

/**
 * 供应商信息Service接口
 *
 * @author ERP System
 * @since 2025-12-16
 */
public interface IBaseSupplierService extends IService<BaseSupplier> {

    /**
     * 分页查询供应商
     *
     * @param param 查询参数
     * @return 分页结果
     */
    Page<BaseSupplier> getSupplierPage(SupplierPageParam param);

    /**
     * 获取所有启用的供应商
     *
     * @return 供应商列表
     */
    List<BaseSupplier> getAllEnabledSuppliers();

    /**
     * 根据ID获取供应商
     *
     * @param id 供应商ID
     * @return 供应商信息
     */
    BaseSupplier getById(Long id);

    /**
     * 保存供应商
     *
     * @param supplier 供应商信息
     * @return 是否成功
     */
    boolean saveSupplier(BaseSupplier supplier);

    /**
     * 更新供应商
     *
     * @param supplier 供应商信息
     * @return 是否成功
     */
    boolean updateSupplier(BaseSupplier supplier);

    /**
     * 删除供应商
     *
     * @param id 供应商ID
     * @return 是否成功
     */
    boolean deleteSupplier(Long id);
}

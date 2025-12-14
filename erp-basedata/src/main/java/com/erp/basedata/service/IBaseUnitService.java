package com.erp.basedata.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.basedata.dto.UnitPageParam;
import com.erp.basedata.entity.BaseUnit;

import java.util.List;

/**
 * 计量单位Service接口
 *
 * @author ERP System
 * @since 2025-12-14
 */
public interface IBaseUnitService {

    /**
     * 分页查询单位
     *
     * @param param 查询参数
     * @return 分页结果
     */
    Page<BaseUnit> getUnitPage(UnitPageParam param);

    /**
     * 获取所有启用的单位
     *
     * @return 单位列表
     */
    List<BaseUnit> getAllEnabledUnits();

    /**
     * 根据ID获取单位
     *
     * @param id 单位ID
     * @return 单位信息
     */
    BaseUnit getById(Long id);

    /**
     * 新增单位
     *
     * @param unit 单位信息
     * @return 是否成功
     */
    boolean saveUnit(BaseUnit unit);

    /**
     * 更新单位
     *
     * @param unit 单位信息
     * @return 是否成功
     */
    boolean updateUnit(BaseUnit unit);

    /**
     * 删除单位
     *
     * @param id 单位ID
     * @return 是否成功
     */
    boolean deleteUnit(Long id);
}

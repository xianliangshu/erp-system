package com.erp.basedata.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.basedata.dto.SalePropertyPageParam;
import com.erp.basedata.entity.BaseSaleProperty;
import com.erp.basedata.entity.BaseSalePropertyItem;

import java.util.List;

/**
 * 商品销售属性组 Service 接口
 *
 * @author ERP System
 * @since 2026-10-03
 */
public interface ISalePropertyService extends IService<BaseSaleProperty> {

    /**
     * 分页查询属性组（含属性值）
     *
     * @param param 查询参数
     * @return 分页结果
     */
    Page<BaseSaleProperty> getPropertyPage(SalePropertyPageParam param);

    /**
     * 获取所有启用的属性组（含属性值，下拉/勾选面板用）
     *
     * @return 属性组列表
     */
    List<BaseSaleProperty> listWithItems();

    /**
     * 查询某属性组下的属性值
     *
     * @param propertyId 属性组ID
     * @return 属性值列表
     */
    List<BaseSalePropertyItem> listItems(Long propertyId);

    /**
     * 新增属性组（同时保存其属性值）
     *
     * @param property 属性组（items 非空时一并写入）
     * @return 是否成功
     */
    boolean saveProperty(BaseSaleProperty property);

    /**
     * 更新属性组（同时覆盖其属性值）
     *
     * @param property 属性组
     * @return 是否成功
     */
    boolean updateProperty(BaseSaleProperty property);

    /**
     * 删除属性组（连带删除其属性值）。已被 SKU 使用时拒绝删除。
     *
     * @param id 属性组ID
     * @return 是否成功
     */
    boolean deleteProperty(Long id);
}

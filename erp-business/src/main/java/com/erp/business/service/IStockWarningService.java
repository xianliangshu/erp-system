package com.erp.business.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.business.entity.StockWarning;

import java.util.List;

/**
 * 库存预警 Service
 *
 * @author ERP System
 */
public interface IStockWarningService extends IService<StockWarning> {

    /**
     * 分页查询预警配置
     */
    IPage<StockWarning> pageList(Long current, Long size, Long scId, String productName, Integer status);

    /**
     * 实时命中查询：返回当前已低于下限 / 高于上限的预警配置
     */
    List<StockWarning> hitList(Long scId, String productName);

    /**
     * 新增预警配置（同仓库同商品判重）
     */
    boolean addWarning(StockWarning warning);

    /**
     * 修改预警配置（同仓库同商品判重，排除自身）
     */
    boolean modifyWarning(StockWarning warning);
}

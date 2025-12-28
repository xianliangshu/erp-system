package com.erp.business.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.business.entity.StockAdjustReason;

/**
 * 库存调整原因 Service
 */
public interface IStockAdjustReasonService extends IService<StockAdjustReason> {

    /**
     * 分页查询
     */
    IPage<StockAdjustReason> page(IPage<StockAdjustReason> page, String code, String name, Integer status);

    /**
     * 生成编号
     */
    String generateCode();

    /**
     * 新增
     */
    boolean add(StockAdjustReason reason);

    /**
     * 修改
     */
    boolean modify(StockAdjustReason reason);
}

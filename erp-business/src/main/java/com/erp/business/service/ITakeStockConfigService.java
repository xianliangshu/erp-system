package com.erp.business.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.business.entity.TakeStockConfig;

/**
 * 库存盘点参数 Service
 *
 * @author ERP System
 */
public interface ITakeStockConfigService extends IService<TakeStockConfig> {

    /**
     * 获取盘点参数（单行；不存在时返回默认值，不落库）
     */
    TakeStockConfig getConfig();

    /**
     * 保存盘点参数（始终写 id = 1 这一行）
     */
    boolean saveConfig(TakeStockConfig config);
}

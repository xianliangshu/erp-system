package com.erp.business.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.business.entity.TakeStockConfig;
import com.erp.business.mapper.TakeStockConfigMapper;
import com.erp.business.service.ITakeStockConfigService;
import org.springframework.stereotype.Service;

/**
 * 库存盘点参数 Service 实现
 *
 * @author ERP System
 */
@Service
public class TakeStockConfigServiceImpl extends ServiceImpl<TakeStockConfigMapper, TakeStockConfig>
        implements ITakeStockConfigService {

    @Override
    public TakeStockConfig getConfig() {
        TakeStockConfig config = getById(TakeStockConfig.SINGLETON_ID);
        if (config != null) {
            return config;
        }
        // 兜底：种子数据缺失时返回默认值（不落库，保存时再写）
        TakeStockConfig defaults = new TakeStockConfig();
        defaults.setId(TakeStockConfig.SINGLETON_ID);
        defaults.setShowProduct(1);
        defaults.setShowStock(1);
        defaults.setAutoChangeStock(0);
        defaults.setAllowChangeNum(1);
        defaults.setCancelHours(24);
        return defaults;
    }

    @Override
    public boolean saveConfig(TakeStockConfig config) {
        // 前端可能只传部分字段，逐项兜底，避免把已配置的开关刷成 null
        TakeStockConfig current = getConfig();
        if (config.getShowProduct() == null) {
            config.setShowProduct(current.getShowProduct());
        }
        if (config.getShowStock() == null) {
            config.setShowStock(current.getShowStock());
        }
        if (config.getAutoChangeStock() == null) {
            config.setAutoChangeStock(current.getAutoChangeStock());
        }
        if (config.getAllowChangeNum() == null) {
            config.setAllowChangeNum(current.getAllowChangeNum());
        }
        if (config.getCancelHours() == null || config.getCancelHours() < 0) {
            config.setCancelHours(current.getCancelHours());
        }
        config.setId(TakeStockConfig.SINGLETON_ID);
        return saveOrUpdate(config);
    }
}

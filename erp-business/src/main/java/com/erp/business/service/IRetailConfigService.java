package com.erp.business.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.business.entity.RetailConfig;

/**
 * 零售配置 Service
 */
public interface IRetailConfigService extends IService<RetailConfig> {

    /**
     * 获取零售配置（如果不存在则创建）
     */
    RetailConfig getRetailConfig();

    /**
     * 更新零售配置
     */
    void updateRetailConfig(RetailConfig config);
}

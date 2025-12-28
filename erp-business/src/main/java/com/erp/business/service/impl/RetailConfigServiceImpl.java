package com.erp.business.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.business.entity.RetailConfig;
import com.erp.business.mapper.RetailConfigMapper;
import com.erp.business.service.IRetailConfigService;
import org.springframework.stereotype.Service;

/**
 * 零售配置 Service 实现
 */
@Service
public class RetailConfigServiceImpl extends ServiceImpl<RetailConfigMapper, RetailConfig>
        implements IRetailConfigService {

    @Override
    public RetailConfig getRetailConfig() {
        RetailConfig config = this.getOne(null);
        if (config == null) {
            config = new RetailConfig();
            config.setOutStockUnApprove(false);
            config.setReturnStockUnApprove(false);
            this.save(config);
        }
        return config;
    }

    @Override
    public void updateRetailConfig(RetailConfig config) {
        this.updateById(config);
    }
}

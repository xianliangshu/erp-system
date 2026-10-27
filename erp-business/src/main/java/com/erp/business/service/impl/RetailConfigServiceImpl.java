package com.erp.business.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.business.entity.RetailConfig;
import com.erp.business.mapper.RetailConfigMapper;
import com.erp.business.service.IRetailConfigService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 零售配置 Service 实现
 */
@Service
public class RetailConfigServiceImpl extends ServiceImpl<RetailConfigMapper, RetailConfig>
        implements IRetailConfigService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RetailConfig getRetailConfig() {
        RetailConfig config = this.getOne(null);
        if (config == null) {
            config = new RetailConfig();
            config.setOutStockUnApprove(false);
            config.setReturnStockUnApprove(false);
            this.save(config);
            // 回查一次，补上 createTime / createBy / deleted 这些由数据库或填充器写入的值，
            // 否则直接返回内存对象会得到一个 deleted = null 的脏结果
            config = this.getById(config.getId());
        }
        return config;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRetailConfig(RetailConfig config) {
        // 零售配置是全表唯一的一行记录，而前端表单只声明了两个开关字段、
        // 提交时不会带上主键，因此这里显式兜底：id 为空时定位到已存在的那行再更新。
        if (config.getId() == null) {
            RetailConfig exists = this.getOne(null);
            if (exists == null) {
                this.save(config);
                return;
            }
            config.setId(exists.getId());
        }
        // MyBatis-Plus 的 updateById 在 id 查不到记录时只会静默返回 false，
        // 必须显式判断并抛错，否则前端会收到"保存成功"但数据其实没变。
        if (!this.updateById(config)) {
            throw new RuntimeException("零售配置保存失败：未找到 id=" + config.getId() + " 的配置记录");
        }
    }
}

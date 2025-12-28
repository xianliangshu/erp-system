package com.erp.business.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.business.dto.SettleInOutItemQueryDTO;
import com.erp.business.entity.SettleInOutItem;

import java.util.List;

/**
 * 收支项目 Service
 */
public interface ISettleInOutItemService extends IService<SettleInOutItem> {

    /**
     * 分页查询
     */
    Page<SettleInOutItem> queryPage(Long current, Long size, SettleInOutItemQueryDTO queryDTO);

    /**
     * 获取所有启用状态的项目
     */
    List<SettleInOutItem> listEnabled(Integer itemType);

    /**
     * 生成编号
     */
    String generateCode();
}

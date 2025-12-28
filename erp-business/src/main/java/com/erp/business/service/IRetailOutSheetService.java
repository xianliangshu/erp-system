package com.erp.business.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.business.entity.RetailOutSheet;
import java.util.Map;

/**
 * 零售出库单 Service
 */
public interface IRetailOutSheetService extends IService<RetailOutSheet> {

    /**
     * 分页查询
     */
    IPage<RetailOutSheet> getPage(Page<RetailOutSheet> page, Map<String, Object> params);

    /**
     * 根据ID获取详情（包含明细）
     */
    Map<String, Object> getDetailById(Long id);

    /**
     * 新增
     */
    void add(Map<String, Object> data);

    /**
     * 修改
     */
    void update(Map<String, Object> data);

    /**
     * 审核通过
     */
    void approve(Long id);

    /**
     * 审核拒绝
     */
    void refuse(Long id, String refuseReason);

    /**
     * 删除
     */
    void deleteById(Long id);
}

package com.erp.business.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.business.dto.SettleFeeSheetDTO;
import com.erp.business.dto.SettleFeeSheetQueryDTO;
import com.erp.business.entity.SettleFeeSheet;

import java.util.Map;

/**
 * 供应商费用单 Service
 */
public interface ISettleFeeSheetService extends IService<SettleFeeSheet> {

    /**
     * 分页查询
     */
    Page<SettleFeeSheet> queryPage(Long current, Long size, SettleFeeSheetQueryDTO queryDTO);

    /**
     * 获取详情（含明细）
     */
    Map<String, Object> getDetailById(Long id);

    /**
     * 创建费用单
     */
    Long create(SettleFeeSheetDTO dto);

    /**
     * 更新费用单
     */
    void update(SettleFeeSheetDTO dto);

    /**
     * 审核通过
     */
    void approve(Long id);

    /**
     * 审核拒绝
     */
    void refuse(Long id, String reason);

    /**
     * 删除
     */
    void deleteById(Long id);

    /**
     * 生成单据编号
     */
    String generateCode();
}

package com.erp.business.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.business.entity.StockAdjustSheet;
import com.erp.business.entity.StockAdjustSheetDetail;
import com.erp.business.enums.StockAdjustBizType;
import com.erp.business.enums.StockAdjustSheetStatus;

import java.util.List;

/**
 * 库存调整单 Service
 */
public interface IStockAdjustSheetService extends IService<StockAdjustSheet> {

    /**
     * 分页查询
     */
    IPage<StockAdjustSheet> page(IPage<StockAdjustSheet> page, String code, Long scId,
            Long reasonId, StockAdjustBizType bizType, StockAdjustSheetStatus status);

    /**
     * 生成编号
     */
    String generateCode();

    /**
     * 新增调整单（含明细）
     */
    boolean add(StockAdjustSheet sheet, List<StockAdjustSheetDetail> details);

    /**
     * 修改调整单（含明细）
     */
    boolean modify(StockAdjustSheet sheet, List<StockAdjustSheetDetail> details);

    /**
     * 审核通过
     */
    boolean approvePass(Long id);

    /**
     * 审核拒绝
     */
    boolean approveRefuse(Long id, String refuseReason);

    /**
     * 删除调整单
     */
    boolean deleteById(Long id);
}

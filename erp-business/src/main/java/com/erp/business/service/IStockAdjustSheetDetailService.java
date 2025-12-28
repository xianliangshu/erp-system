package com.erp.business.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.business.entity.StockAdjustSheetDetail;

import java.util.List;

/**
 * 库存调整单明细 Service
 */
public interface IStockAdjustSheetDetailService extends IService<StockAdjustSheetDetail> {

    /**
     * 根据调整单ID查询明细列表
     */
    List<StockAdjustSheetDetail> listBySheetId(Long sheetId);

    /**
     * 根据调整单ID删除明细
     */
    boolean deleteBySheetId(Long sheetId);
}

package com.erp.business.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.business.entity.RetailOutSheetDetail;
import java.util.List;

/**
 * 零售出库单明细 Service
 */
public interface IRetailOutSheetDetailService extends IService<RetailOutSheetDetail> {

    /**
     * 根据主表ID查询明细
     */
    List<RetailOutSheetDetail> listBySheetId(Long sheetId);

    /**
     * 根据主表ID删除明细
     */
    void deleteBySheetId(Long sheetId);
}

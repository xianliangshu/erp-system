package com.erp.business.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.business.entity.PreTakeStockSheet;
import com.erp.business.entity.PreTakeStockSheetDetail;

import java.util.List;

/**
 * 库存预先盘点单 Service
 *
 * @author ERP System
 */
public interface IPreTakeStockService extends IService<PreTakeStockSheet> {

    /**
     * 分页查询
     */
    IPage<PreTakeStockSheet> pageList(Long current, Long size, Long scId, Integer takeStatus, String code);

    /**
     * 查询详情（含明细与商品信息）
     */
    PreTakeStockSheet getDetail(Long id);

    /**
     * 下拉选项（供库存盘点单「关联预先盘点单」使用），按创建时间倒序
     */
    List<PreTakeStockSheet> listOptions();

    /**
     * 新增（主表 + 明细）
     */
    Long create(PreTakeStockSheet sheet, List<PreTakeStockSheetDetail> details);

    /**
     * 修改（主表 + 明细整体覆盖）
     */
    boolean modify(PreTakeStockSheet sheet, List<PreTakeStockSheetDetail> details);

    /**
     * 删除（连带明细）
     */
    boolean deleteById(Long id);
}

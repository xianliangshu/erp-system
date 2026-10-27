package com.erp.business.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.business.entity.StockCostAdjustSheet;
import com.erp.business.entity.StockCostAdjustSheetDetail;

import java.util.List;

/**
 * 库存成本调整单 Service
 *
 * @author ERP System
 */
public interface IStockCostAdjustService extends IService<StockCostAdjustSheet> {

    /**
     * 分页查询
     */
    IPage<StockCostAdjustSheet> pageList(Long current, Long size, String code, Long scId, Integer status);

    /**
     * 查询详情（主表 + 明细）
     */
    StockCostAdjustSheet getDetail(Long id);

    /**
     * 查询某商品在某仓库的当前库存与成本价（制单时展示用）
     *
     * @return stockNum / costPrice / purchasePrice / productCode / productName
     */
    java.util.Map<String, Object> getStockInfo(Long scId, Long productId);

    /**
     * 新增（含明细）
     */
    boolean add(StockCostAdjustSheet sheet, List<StockCostAdjustSheetDetail> details);

    /**
     * 修改（含明细，仅待审核状态可改）
     */
    boolean modify(StockCostAdjustSheet sheet, List<StockCostAdjustSheetDetail> details);

    /**
     * 审核通过：回写 product_stock 成本价与库存金额（数量不变），并写 COST_ADJUST 流水
     */
    boolean approvePass(Long id, String operator);

    /**
     * 审核拒绝：只改状态，库存不动
     */
    boolean approveRefuse(Long id, String refuseReason, String operator);

    /**
     * 删除（已审核通过的单据不允许删除）
     */
    boolean deleteById(Long id);
}

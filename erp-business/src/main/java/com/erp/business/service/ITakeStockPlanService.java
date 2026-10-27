package com.erp.business.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.business.entity.TakeStockPlan;
import com.erp.business.entity.TakeStockPlanDetail;

import java.util.List;

/**
 * 库存盘点任务 Service
 *
 * @author ERP System
 */
public interface ITakeStockPlanService extends IService<TakeStockPlan> {

    /**
     * 分页查询（附带明细总数与已录入数）
     */
    IPage<TakeStockPlan> pageList(Long current, Long size, Long scId, Integer takeStatus, Integer takeType, String code);

    /**
     * 查询详情（含明细、商品信息、实时库存与差异）
     */
    TakeStockPlan getDetail(Long id);

    /**
     * 创建盘点任务：按盘点类别解析商品范围，并写入当时的<b>库存快照</b>
     *
     * @return 任务ID
     */
    Long createPlan(TakeStockPlan plan);

    /**
     * 录入盘点数量（仅「任务生成」状态可改）
     */
    boolean saveTakeNums(Long planId, List<TakeStockPlanDetail> details);

    /**
     * 生成差异：按盘点结果生成一张库存盘点单并回填 {@code plan_id}。
     *
     * <p>无差异时直接把任务置为「盘点完成」并返回 {@code null}。</p>
     *
     * @return 生成的盘点单ID，无差异时返回 null
     */
    Long generateDiff(Long planId);

    /**
     * 手工作废（仅「任务生成」状态可作废）
     */
    boolean cancel(Long planId);

    /**
     * 按 {@code take_stock_config.cancel_hours} 把超时未完成的任务批量置为已作废。
     *
     * @return 本次作废条数
     */
    int cancelExpired();

    /**
     * 未完成的任务列表（供库存盘点单「关联盘点任务」下拉使用）
     */
    List<TakeStockPlan> listActiveOptions();
}

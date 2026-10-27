package com.erp.business.dto;

import com.erp.business.entity.TakeStockPlanDetail;
import lombok.Data;

import java.util.List;

/**
 * 盘点任务「录入盘点数量」请求参数
 *
 * <p>必须用强类型参数接收：如果用 {@code Map<String, Object>} 接收，
 * Jackson 会把 {@code details} 反序列化成 {@code List<LinkedHashMap>}，
 * 后续按 {@link TakeStockPlanDetail} 迭代时抛 {@code ClassCastException}（已踩过）。</p>
 *
 * @author ERP System
 */
@Data
public class TakeStockNumsParam {

    /**
     * 盘点任务ID
     */
    private Long planId;

    /**
     * 盘点明细（只需 id / takeNum / description）
     */
    private List<TakeStockPlanDetail> details;
}

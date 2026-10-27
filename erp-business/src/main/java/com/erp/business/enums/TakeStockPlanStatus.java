package com.erp.business.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 盘点任务状态
 *
 * <p>编码沿用 xingyun 的 0/6/9/12 语义：</p>
 * <ul>
 *   <li>0 任务生成 —— 明细已按库存快照生成，等待录入盘点数量</li>
 *   <li>6 差异已生成 —— 已按盘点结果生成库存盘点单（{@code stock_check.plan_id} 回填），后续走盘点单审核</li>
 *   <li>9 盘点完成 —— 保留给「无差异直接完成」的场景</li>
 *   <li>12 已作废 —— 手工作废或超过 {@code take_stock_config.cancel_hours} 自动作废</li>
 * </ul>
 *
 * @author ERP System
 */
@Getter
public enum TakeStockPlanStatus {

    CREATED(0, "任务生成"),
    DIFF_GENERATED(6, "差异已生成"),
    FINISHED(9, "盘点完成"),
    CANCELLED(12, "已作废");

    @EnumValue
    @JsonValue
    private final Integer code;

    private final String desc;

    TakeStockPlanStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}

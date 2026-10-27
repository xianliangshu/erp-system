package com.erp.business.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 库存成本调整单状态
 *
 * <p>编码与 {@link StockAdjustSheetStatus} 保持一致（0/1/2），前端处理逻辑可复用。</p>
 *
 * @author ERP System
 */
@Getter
public enum StockCostAdjustStatus {

    CREATED(0, "待审核"),
    APPROVE_PASS(1, "审核通过"),
    APPROVE_REFUSE(2, "审核拒绝");

    @EnumValue
    @JsonValue
    private final Integer code;

    private final String desc;

    StockCostAdjustStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}

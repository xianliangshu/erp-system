package com.erp.business.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 库存调整单状态
 */
@Getter
public enum StockAdjustSheetStatus {

    CREATED(0, "待审核"),
    APPROVE_PASS(1, "审核通过"),
    APPROVE_REFUSE(2, "审核拒绝");

    @EnumValue
    @JsonValue
    private final Integer code;

    private final String desc;

    StockAdjustSheetStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}

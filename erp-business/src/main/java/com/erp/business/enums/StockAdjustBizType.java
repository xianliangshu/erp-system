package com.erp.business.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 库存调整业务类型
 */
@Getter
public enum StockAdjustBizType {

    IN(0, "入库调整"),
    OUT(1, "出库调整");

    @EnumValue
    @JsonValue
    private final Integer code;

    private final String desc;

    StockAdjustBizType(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}

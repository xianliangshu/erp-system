package com.erp.business.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 销售订单状态
 */
@Getter
@AllArgsConstructor
public enum SaleOrderStatus implements IEnum<Integer> {

    PENDING(0, "待审核"),
    APPROVED(1, "已审核"),
    REJECTED(2, "已拒绝"),
    COMPLETED(3, "已完成"),
    CANCELLED(4, "已取消");

    @EnumValue
    @JsonValue
    private final Integer value;
    private final String desc;

    @Override
    public Integer getValue() {
        return this.value;
    }
}

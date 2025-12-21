package com.erp.business.enums;

import com.baomidou.mybatisplus.annotation.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 采购订单状态
 */
@Getter
@AllArgsConstructor
public enum PurchaseOrderStatus implements IEnum<Integer> {

    PENDING(0, "待审核"),
    APPROVED(1, "已审核"),
    REJECTED(2, "已拒绝"),
    COMPLETED(3, "已完成"),
    CANCELLED(4, "已取消");

    private final Integer value;
    private final String desc;

    @Override
    public Integer getValue() {
        return this.value;
    }
}

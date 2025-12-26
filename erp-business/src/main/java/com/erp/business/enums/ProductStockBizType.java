package com.erp.business.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 库存业务类型
 */
@Getter
@AllArgsConstructor
public enum ProductStockBizType implements IEnum<Integer> {

    PURCHASE_RECEIPT(1, "采购收货"),
    PURCHASE_RETURN(2, "采购退货"),
    SALES_DELIVERY(3, "销售出库"),
    SALES_RETURN(4, "销售退货"),
    STOCK_ADJUST(5, "库存调整"),
    STOCK_TRANSFER(6, "库存调拨"),
    STOCK_CHECK_PROFIT(7, "盘盈入库"),
    STOCK_CHECK_LOSS(8, "盘亏出库");

    @EnumValue
    @JsonValue
    private final Integer value;
    private final String desc;

    @Override
    public Integer getValue() {
        return this.value;
    }
}

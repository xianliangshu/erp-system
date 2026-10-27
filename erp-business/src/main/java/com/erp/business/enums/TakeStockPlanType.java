package com.erp.business.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 盘点类别
 *
 * <p>决定创建盘点任务时按什么范围解析商品：</p>
 * <ul>
 *   <li>全场 —— 该仓库库存表里现有的全部商品</li>
 *   <li>单品 / 分类 / 品牌 —— 按 ID 串筛选商品档案，无库存记录的商品按 0 计入</li>
 * </ul>
 *
 * @author ERP System
 */
@Getter
public enum TakeStockPlanType {

    ALL(0, "全场"),
    PRODUCT(1, "单品"),
    CATEGORY(2, "分类"),
    BRAND(3, "品牌");

    @EnumValue
    @JsonValue
    private final Integer code;

    private final String desc;

    TakeStockPlanType(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}

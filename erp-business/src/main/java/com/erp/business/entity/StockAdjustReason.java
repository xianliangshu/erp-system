package com.erp.business.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 库存调整原因
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("stock_adjust_reason")
public class StockAdjustReason extends BaseBusinessEntity {

    /**
     * 编号
     */
    private String code;

    /**
     * 名称
     */
    private String name;

    /**
     * 状态: 0-禁用 1-启用
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;
}

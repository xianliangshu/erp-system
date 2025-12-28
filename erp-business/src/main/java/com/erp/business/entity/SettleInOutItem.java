package com.erp.business.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 收支项目
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("settle_in_out_item")
public class SettleInOutItem extends BaseBusinessEntity {

    /**
     * 编号
     */
    private String code;

    /**
     * 名称
     */
    private String name;

    /**
     * 项目类型: 1-收入, 2-支出
     */
    private Integer itemType;

    /**
     * 状态: 0-禁用, 1-启用
     */
    private Integer status;

    /**
     * 备注
     */
    private String description;
}

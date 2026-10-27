package com.erp.basedata.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 商品销售属性组实体类（如"颜色"、"尺码"）
 *
 * <p>勾选属性组后，系统按笛卡尔积为 SPU 生成 SKU 矩阵。</p>
 *
 * @author ERP System
 * @since 2026-10-03
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("base_sale_property")
public class BaseSaleProperty extends BaseDataEntity {

    /**
     * 属性组编码
     */
    private String code;

    /**
     * 属性组名称
     */
    private String name;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 状态(0=禁用,1=启用)
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 属性值列表(非数据库字段)
     */
    @TableField(exist = false)
    private List<BaseSalePropertyItem> items;
}

package com.erp.basedata.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 物料信息实体类
 *
 * @author ERP System
 * @since 2025-12-14
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("base_material")
public class BaseMaterial extends BaseDataEntity {

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 物料编号
     */
    private String code;

    /**
     * 物料名称
     */
    private String name;

    /**
     * 物料简称
     */
    private String shortName;

    /**
     * 规格型号
     */
    private String specification;

    /**
     * 计量单位ID
     */
    private Long unitId;

    /**
     * 采购价格
     */
    private BigDecimal purchasePrice;

    /**
     * 销售价格
     */
    private BigDecimal salePrice;

    /**
     * 零售价格
     */
    private BigDecimal retailPrice;

    /**
     * 最低库存
     */
    private BigDecimal minStock;

    /**
     * 最高库存
     */
    private BigDecimal maxStock;

    /**
     * 状态(0=禁用,1=启用)
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 分类名称(非数据库字段)
     */
    @TableField(exist = false)
    private String categoryName;

    /**
     * 单位名称(非数据库字段)
     */
    @TableField(exist = false)
    private String unitName;
}

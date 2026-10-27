package com.erp.basedata.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

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
     * SPU ID（所属商品；可为空，表示该 SKU 尚未归入任何 SPU）
     */
    private Long spuId;

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
     * 销售属性文本(如:颜色:红 / 尺码:XL)
     */
    private String salePropertyText;

    /**
     * SKU图片
     */
    private String mainImage;

    /**
     * 是否一品多码(0=否,1=是)
     */
    private Integer isMultiCode;

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
     * 所属SPU名称(非数据库字段)
     */
    @TableField(exist = false)
    private String spuName;

    /**
     * 该 SKU 绑定的销售属性值ID列表(非数据库字段)，按属性组一一对应
     */
    @TableField(exist = false)
    private List<Long> propertyItemIds;

    /**
     * 单位名称(非数据库字段)
     */
    @TableField(exist = false)
    private String unitName;
}

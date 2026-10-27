package com.erp.basedata.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * SPU 商品主表实体类
 *
 * <p>SPU 是商品中心的最上层聚合维度，描述"这个商品是什么"；
 * 其下的 {@link BaseMaterial} 才是真正的 SKU（库存与单据的锚点）。</p>
 *
 * @author ERP System
 * @since 2026-10-03
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("base_spu")
public class BaseSpu extends BaseDataEntity {

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 品牌ID
     */
    private Long brandId;

    /**
     * 基本计量单位ID
     */
    private Long unitId;

    /**
     * SPU编号
     */
    private String code;

    /**
     * 商品名称
     */
    private String name;

    /**
     * 商品简称
     */
    private String shortName;

    /**
     * 主体规格描述
     */
    private String specification;

    /**
     * 商品主图
     */
    private String mainImage;

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
     * 品牌名称(非数据库字段)
     */
    @TableField(exist = false)
    private String brandName;

    /**
     * 单位名称(非数据库字段)
     */
    @TableField(exist = false)
    private String unitName;

    /**
     * 该 SPU 下的 SKU 数量(非数据库字段)
     */
    @TableField(exist = false)
    private Integer skuCount;

    /**
     * 该 SPU 绑定的销售属性组ID列表(非数据库字段)
     */
    @TableField(exist = false)
    private List<Long> propertyIds;
}

package com.erp.basedata.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 物料信息实体类
 *
 * @author ERP System
 * @since 2025-12-14
 */
@Data
@TableName("base_material")
public class BaseMaterial implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 物料ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

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
     * 删除标记(0=未删除,1=已删除)
     */
    @TableLogic
    private Integer deleted;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

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

package com.erp.basedata.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * 物料分类实体类
 *
 * @author ERP System
 * @since 2025-12-14
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("base_material_category")
public class BaseMaterialCategory extends BaseDataEntity {

    /**
     * 父分类ID(0表示顶级分类)
     */
    private Long parentId;

    /**
     * 分类编号
     */
    private String code;

    /**
     * 分类名称
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
     * 子分类列表(非数据库字段)
     */
    @TableField(exist = false)
    private List<BaseMaterialCategory> children;
}

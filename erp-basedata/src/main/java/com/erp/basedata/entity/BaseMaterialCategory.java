package com.erp.basedata.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 物料分类实体类
 *
 * @author ERP System
 * @since 2025-12-14
 */
@Data
@TableName("base_material_category")
public class BaseMaterialCategory implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 分类ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

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
     * 子分类列表(非数据库字段)
     */
    @TableField(exist = false)
    private List<BaseMaterialCategory> children;
}

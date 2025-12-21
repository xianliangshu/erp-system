package com.erp.basedata.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 品牌信息实体类
 *
 * @author ERP System
 * @since 2025-12-21
 */
@Data
@TableName("base_brand")
public class BaseBrand implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 品牌编号
     */
    private String code;

    /**
     * 品牌名称
     */
    private String name;

    /**
     * 品牌简称
     */
    private String shortName;

    /**
     * 品牌Logo
     */
    private String logo;

    /**
     * 状态: 0-禁用 1-启用
     */
    private Integer status;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    /**
     * 更新人
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    /**
     * 删除标记: 0-未删除 1-已删除
     */
    @TableLogic
    private Integer deleted;
}

package com.erp.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 部门实体类
 * 
 * @author ERP System
 * @since 2025-12-12
 */
@Data
@TableName("sys_dept")
public class SysDept implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 部门ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 父部门ID(顶级为NULL)
     */
    private Long parentId;

    /**
     * 祖级列表(逗号分隔)
     */
    private String ancestors;

    /**
     * 部门编号
     */
    private String code;

    /**
     * 部门名称
     */
    private String name;

    /**
     * 部门简称
     */
    private String shortName;

    /**
     * 负责人
     */
    private String leader;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 显示顺序
     */
    private Integer sort;

    /**
     * 状态: 0-禁用 1-启用
     */
    private Integer status;

    /**
     * 删除标记: 0-未删除 1-已删除
     */
    @TableLogic
    private Integer isDeleted;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建人
     */
    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

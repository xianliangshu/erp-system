package com.erp.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 角色实体类
 * 
 * @author ERP System
 * @since 2025-12-12
 */
@Data
@TableName("sys_role")
public class SysRole implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 角色ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 角色编号
     */
    private String code;

    /**
     * 角色名称
     */
    private String name;

    /**
     * 权限字符串(用于注解鉴权)
     */
    private String permissionCode;

    /**
     * 数据权限范围: 1-全部 2-本部门及以下 3-本部门 4-仅本人
     */
    private Integer dataScope;

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

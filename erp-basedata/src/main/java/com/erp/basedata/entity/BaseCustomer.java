package com.erp.basedata.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 客户信息实体类
 *
 * @author ERP System
 * @since 2025-12-21
 */
@Data
@TableName("base_customer")
public class BaseCustomer implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 客户编号
     */
    private String code;

    /**
     * 客户名称
     */
    private String name;

    /**
     * 客户简称
     */
    private String shortName;

    /**
     * 客户类型: 1-普通客户 2-VIP客户 3-分销商
     */
    private Integer type;

    /**
     * 客户等级: 1-一级 2-二级 3-三级
     */
    private Integer level;

    /**
     * 联系人
     */
    private String contactPerson;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 联系邮箱
     */
    private String contactEmail;

    /**
     * 联系地址
     */
    private String address;

    /**
     * 结算方式: 1-现金 2-月结 3-季结 4-账期
     */
    private Integer settlementMethod;

    /**
     * 账期天数
     */
    private Integer paymentDays;

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

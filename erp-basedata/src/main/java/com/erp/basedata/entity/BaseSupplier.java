package com.erp.basedata.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 供应商信息实体类
 *
 * @author ERP System
 * @since 2025-12-16
 */
@Data
@TableName("base_supplier")
public class BaseSupplier implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 供应商编号
     */
    private String code;

    /**
     * 供应商名称
     */
    private String name;

    /**
     * 供应商简称
     */
    private String shortName;

    /**
     * 供应商类型: 1-原材料供应商 2-设备供应商 3-服务供应商
     */
    private Integer type;

    /**
     * 信用等级: 1-优秀 2-良好 3-一般 4-较差 5-差
     */
    private Integer creditLevel;

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
     * 网站
     */
    private String website;

    /**
     * 结算方式: 1-现金 2-月结 3-季结 4-账期
     */
    private Integer settlementMethod;

    /**
     * 账期天数
     */
    private Integer paymentDays;

    /**
     * 开户银行
     */
    private String bankName;

    /**
     * 银行账号
     */
    private String bankAccount;

    /**
     * 税号
     */
    private String taxNumber;

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

package com.erp.basedata.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 客户信息实体类
 *
 * @author ERP System
 * @since 2025-12-21
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("base_customer")
public class BaseCustomer extends BaseDataEntity {

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
}

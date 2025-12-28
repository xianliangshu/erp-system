package com.erp.basedata.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 仓库实体类
 * 
 * @author ERP System
 * @since 2025-12-14
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("base_warehouse")
public class BaseWarehouse extends BaseDataEntity {

    /**
     * 仓库编号
     */
    private String code;

    /**
     * 仓库名称
     */
    private String name;

    /**
     * 联系人
     */
    private String contact;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 仓库地址
     */
    private String address;

    /**
     * 是否默认仓库: 0-否 1-是
     */
    private Integer isDefault;

    /**
     * 状态: 0-禁用 1-启用
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;
}

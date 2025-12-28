package com.erp.basedata.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 品牌信息实体类
 *
 * @author ERP System
 * @since 2025-12-21
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("base_brand")
public class BaseBrand extends BaseDataEntity {

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
}

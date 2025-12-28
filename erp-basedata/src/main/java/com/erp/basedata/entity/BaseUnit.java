package com.erp.basedata.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 计量单位实体类
 *
 * @author ERP System
 * @since 2025-12-14
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("base_unit")
public class BaseUnit extends BaseDataEntity {

    /**
     * 单位编号
     */
    private String code;

    /**
     * 单位名称
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
}

package com.erp.basedata.dto;

import lombok.Data;

/**
 * 客户分页查询参数
 *
 * @author ERP System
 * @since 2025-12-21
 */
@Data
public class CustomerPageParam {

    /**
     * 当前页
     */
    private Long current = 1L;

    /**
     * 每页大小
     */
    private Long size = 10L;

    /**
     * 客户名称(模糊查询)
     */
    private String name;

    /**
     * 客户编号(模糊查询)
     */
    private String code;

    /**
     * 客户类型
     */
    private Integer type;

    /**
     * 客户等级
     */
    private Integer level;

    /**
     * 状态
     */
    private Integer status;
}

package com.erp.basedata.dto;

import lombok.Data;

/**
 * 计量单位分页查询参数
 *
 * @author ERP System
 * @since 2025-12-14
 */
@Data
public class UnitPageParam {

    /**
     * 当前页
     */
    private Long current = 1L;

    /**
     * 每页大小
     */
    private Long size = 10L;

    /**
     * 单位名称(模糊查询)
     */
    private String name;

    /**
     * 状态(0=禁用,1=启用)
     */
    private Integer status;
}

package com.erp.basedata.dto;

import lombok.Data;

/**
 * 供应商分页查询参数
 *
 * @author ERP System
 * @since 2025-12-16
 */
@Data
public class SupplierPageParam {

    /**
     * 当前页
     */
    private Long current = 1L;

    /**
     * 每页大小
     */
    private Long size = 10L;

    /**
     * 供应商名称(模糊查询)
     */
    private String name;

    /**
     * 供应商编号(模糊查询)
     */
    private String code;

    /**
     * 供应商类型
     */
    private Integer type;

    /**
     * 状态
     */
    private Integer status;
}

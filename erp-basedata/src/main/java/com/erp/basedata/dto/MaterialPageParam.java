package com.erp.basedata.dto;

import lombok.Data;

/**
 * 物料信息分页查询参数
 *
 * @author ERP System
 * @since 2025-12-14
 */
@Data
public class MaterialPageParam {

    /**
     * 当前页
     */
    private Long current = 1L;

    /**
     * 每页大小
     */
    private Long size = 10L;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 物料名称(模糊查询)
     */
    private String name;

    /**
     * 物料编号(模糊查询)
     */
    private String code;

    /**
     * 状态(0=禁用,1=启用)
     */
    private Integer status;
}

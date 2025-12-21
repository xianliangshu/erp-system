package com.erp.basedata.dto;

import lombok.Data;

/**
 * 品牌分页查询参数
 *
 * @author ERP System
 * @since 2025-12-21
 */
@Data
public class BrandPageParam {

    /**
     * 当前页
     */
    private Long current = 1L;

    /**
     * 每页大小
     */
    private Long size = 10L;

    /**
     * 品牌名称(模糊查询)
     */
    private String name;

    /**
     * 品牌编号(模糊查询)
     */
    private String code;

    /**
     * 状态
     */
    private Integer status;
}

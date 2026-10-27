package com.erp.basedata.dto;

import lombok.Data;

/**
 * SPU 分页查询参数
 *
 * @author ERP System
 * @since 2026-10-03
 */
@Data
public class SpuPageParam {

    /**
     * 当前页
     */
    private Long current = 1L;

    /**
     * 每页大小
     */
    private Long size = 10L;

    /**
     * 商品名称(模糊查询)
     */
    private String name;

    /**
     * SPU编号(模糊查询)
     */
    private String code;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 品牌ID
     */
    private Long brandId;

    /**
     * 状态
     */
    private Integer status;
}

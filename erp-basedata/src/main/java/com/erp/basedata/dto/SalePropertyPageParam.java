package com.erp.basedata.dto;

import lombok.Data;

/**
 * 销售属性组分页查询参数
 *
 * @author ERP System
 * @since 2026-10-03
 */
@Data
public class SalePropertyPageParam {

    /**
     * 当前页
     */
    private Long current = 1L;

    /**
     * 每页大小
     */
    private Long size = 10L;

    /**
     * 属性组名称(模糊查询)
     */
    private String name;

    /**
     * 属性组编码(模糊查询)
     */
    private String code;

    /**
     * 状态
     */
    private Integer status;
}

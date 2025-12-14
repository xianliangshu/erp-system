package com.erp.basedata.dto;

import lombok.Data;
import java.io.Serializable;

/**
 * 仓库分页查询参数DTO
 * 
 * @author ERP System
 * @since 2025-12-14
 */
@Data
public class WarehousePageParam implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前页
     */
    private Integer current;

    /**
     * 每页大小
     */
    private Integer size;

    /**
     * 仓库名称(模糊查询)
     */
    private String name;

    /**
     * 状态
     */
    private Integer status;
}

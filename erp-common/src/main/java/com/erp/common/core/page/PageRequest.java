package com.erp.common.core.page;

import lombok.Data;

import java.io.Serializable;

/**
 * 分页请求基类
 * 
 * @author ERP System
 * @since 2025-12-12
 */
@Data
public class PageRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前页码,默认第1页
     */
    private Long current = 1L;

    /**
     * 每页大小,默认10条
     */
    private Long size = 10L;
}

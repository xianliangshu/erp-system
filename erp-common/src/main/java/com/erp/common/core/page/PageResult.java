package com.erp.common.core.page;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * 分页结果类
 * 
 * @author ERP System
 * @since 2025-12-12
 */
@Data
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 当前页码
     */
    private Long current;

    /**
     * 每页大小
     */
    private Long size;

    /**
     * 总页数
     */
    private Long pages;

    /**
     * 数据列表
     */
    private List<T> records;

    public PageResult() {
    }

    public PageResult(Long total, Long current, Long size, List<T> records) {
        this.total = total;
        this.current = current;
        this.size = size;
        this.records = records;
        this.pages = (total + size - 1) / size;
    }

    /**
     * 构建分页结果
     */
    public static <T> PageResult<T> build(Long total, Long current, Long size, List<T> records) {
        return new PageResult<>(total, current, size, records);
    }
}

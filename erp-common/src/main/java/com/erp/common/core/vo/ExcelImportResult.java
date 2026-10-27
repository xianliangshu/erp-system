package com.erp.common.core.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Excel 批量导入结果
 *
 * <p>导入采用「逐行校验、整体回滚」策略：任一行不合法则全部不落库，
 * 避免出现「导入了一半」的中间状态。错误行会带行号返回，便于用户修正后重传。</p>
 *
 * @author ERP System
 * @since 2026-10-03
 */
@Data
public class ExcelImportResult {

    /**
     * 解析到的总行数
     */
    private int total;

    /**
     * 成功导入数
     */
    private int success;

    /**
     * 失败数
     */
    private int failed;

    /**
     * 错误明细（含行号）
     */
    private List<String> errors = new ArrayList<>();

    public void addError(int rowNum, String message) {
        this.errors.add("第 " + rowNum + " 行: " + message);
        this.failed++;
    }

    /**
     * 是否全部成功
     */
    public boolean isAllSuccess() {
        return failed == 0;
    }
}

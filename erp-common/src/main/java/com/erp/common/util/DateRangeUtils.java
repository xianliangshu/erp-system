package com.erp.common.util;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 日期区间解析工具
 *
 * <p>列表页的日期筛选前端传的都是 {@code yyyy-MM-dd} 字符串，而库里是
 * {@code datetime}。这里统一把「起始日期」解析成当天 00:00:00、
 * 「结束日期」解析成当天 23:59:59，避免各处在 SQL 里拼字符串补时分秒。</p>
 *
 * <p>解析失败一律返回 {@code null}，调用方据此跳过该筛选条件 ——
 * 前端传了脏数据时应该「不过滤」而不是直接报错。</p>
 *
 * @author ERP System
 * @since 2026-10-03
 */
public final class DateRangeUtils {

    private DateRangeUtils() {
    }

    /**
     * 起始日期 → 当天 00:00:00
     *
     * @param date yyyy-MM-dd，空或非法返回 null
     * @return 起始时间
     */
    public static LocalDateTime startOfDay(String date) {
        LocalDate parsed = parse(date);
        return parsed == null ? null : parsed.atStartOfDay();
    }

    /**
     * 结束日期 → 当天 23:59:59
     *
     * @param date yyyy-MM-dd，空或非法返回 null
     * @return 结束时间
     */
    public static LocalDateTime endOfDay(String date) {
        LocalDate parsed = parse(date);
        return parsed == null ? null : parsed.atTime(23, 59, 59);
    }

    /**
     * 宽松解析 yyyy-MM-dd
     */
    private static LocalDate parse(String date) {
        if (date == null || date.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(date.trim());
        } catch (Exception e) {
            return null;
        }
    }
}

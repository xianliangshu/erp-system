package com.erp.system.enums;

import lombok.Getter;

/**
 * 操作业务类型
 *
 * <p>取值与 {@code sys_operation_log.business_type} 的注释保持一致，
 * 改动时两边要同步。</p>
 *
 * @author ERP System
 * @since 2026-10-03
 */
@Getter
public enum BusinessType {

    /** 其它 */
    OTHER(0, "其它"),

    /** 新增 */
    INSERT(1, "新增"),

    /** 修改 */
    UPDATE(2, "修改"),

    /** 删除 */
    DELETE(3, "删除"),

    /** 查询 */
    QUERY(4, "查询"),

    /** 导出 */
    EXPORT(5, "导出"),

    /** 导入 */
    IMPORT(6, "导入");

    private final Integer value;

    private final String label;

    BusinessType(Integer value, String label) {
        this.value = value;
        this.label = label;
    }

    /**
     * 按数值反查枚举，用于日志列表页把数字转成中文
     *
     * @param value 数值
     * @return 匹配的枚举，无匹配时返回 {@link #OTHER}
     */
    public static BusinessType of(Integer value) {
        if (value != null) {
            for (BusinessType type : values()) {
                if (type.value.equals(value)) {
                    return type;
                }
            }
        }
        return OTHER;
    }
}

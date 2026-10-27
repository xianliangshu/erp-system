package com.erp.system.enums;

import lombok.Getter;

/**
 * 操作人类别
 *
 * <p>取值与 {@code sys_operation_log.operator_type} 的注释保持一致。</p>
 *
 * @author ERP System
 * @since 2026-10-03
 */
@Getter
public enum OperatorType {

    /** 其它 */
    OTHER(0, "其它"),

    /** 后台用户 */
    MANAGE(1, "后台用户"),

    /** 手机端用户 */
    MOBILE(2, "手机端用户");

    private final Integer value;

    private final String label;

    OperatorType(Integer value, String label) {
        this.value = value;
        this.label = label;
    }
}

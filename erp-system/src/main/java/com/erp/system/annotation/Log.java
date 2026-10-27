package com.erp.system.annotation;

import com.erp.system.enums.BusinessType;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作日志注解
 *
 * <p><b>这个注解是「覆盖项」而不是「开关」</b>：切面默认就会记录所有非 GET 请求
 * 以及 {@code /export} 请求，标题按 Controller 自动推导。只有当自动推导的结果
 * 不合适时才需要标注本注解，典型场景有两类：</p>
 * <ul>
 *   <li>标题要更精确，例如「分配菜单权限」比自动推导出的「角色管理」更有审计价值；</li>
 *   <li>请求里带敏感字段，必须用 {@code saveParam = false} 关掉参数记录，
 *       例如修改密码。</li>
 * </ul>
 *
 * <p>可以标注在方法上，也可以标注在 Controller 类上（类级作为该类所有方法的默认值，
 * 方法级优先）。</p>
 *
 * @author ERP System
 * @since 2026-10-03
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {

    /**
     * 操作模块名称，留空时按 Controller 自动推导
     */
    String title() default "";

    /**
     * 业务类型，留空时按 HTTP 方法自动推导（POST=新增 / PUT=修改 / DELETE=删除）
     */
    BusinessType businessType() default BusinessType.OTHER;

    /**
     * 是否记录请求参数。请求体里含密码等敏感信息时必须置为 false
     */
    boolean saveParam() default true;

    /**
     * 是否记录返回结果。默认关闭；返回体较大的查询类接口尤其不要打开
     */
    boolean saveResult() default false;
}

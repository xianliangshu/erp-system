package com.erp.system.param;

import lombok.Data;

/**
 * 日志分页查询参数
 *
 * <p>登录日志与操作日志共用：各自只取自己需要的字段，用不到的留空即可。</p>
 *
 * @author ERP System
 * @since 2026-10-03
 */
@Data
public class LogQueryParam {

    /**
     * 当前页
     */
    private Long current = 1L;

    /**
     * 每页大小
     */
    private Long size = 10L;

    /**
     * 用户账号 / 操作人员（模糊查询）
     */
    private String username;

    /**
     * 操作模块（模糊查询），仅操作日志使用
     */
    private String title;

    /**
     * 状态：登录日志 0-成功 1-失败；操作日志 0-正常 1-异常
     */
    private Integer status;

    /**
     * 业务类型，仅操作日志使用
     */
    private Integer businessType;

    /**
     * 起始日期，格式 yyyy-MM-dd
     */
    private String beginDate;

    /**
     * 结束日期，格式 yyyy-MM-dd
     */
    private String endDate;
}

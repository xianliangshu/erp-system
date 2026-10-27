package com.erp.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.system.entity.SysLoginLog;
import com.erp.system.param.LogQueryParam;

/**
 * 登录日志Service接口
 *
 * @author ERP System
 * @since 2026-10-03
 */
public interface ISysLoginLogService {

    /**
     * 写入一条登录日志
     *
     * <p>由 {@code AuthServiceImpl#login} 调用。<b>本方法必须吞掉自身异常</b>：
     * 记日志失败不能导致用户登不进来。</p>
     *
     * @param loginLog 日志内容
     */
    void record(SysLoginLog loginLog);

    /**
     * 分页查询登录日志
     *
     * @param param 查询参数
     * @return 分页结果
     */
    Page<SysLoginLog> getPage(LogQueryParam param);

    /**
     * 清空登录日志（TRUNCATE，同时重置自增 ID）
     *
     * @return 影响行数，TRUNCATE 恒返回 0
     */
    int clean();
}

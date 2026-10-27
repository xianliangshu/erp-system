package com.erp.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.system.entity.SysOperationLog;
import com.erp.system.param.LogQueryParam;

/**
 * 操作日志Service接口
 *
 * @author ERP System
 * @since 2026-10-03
 */
public interface ISysOperationLogService {

    /**
     * 写入一条操作日志
     *
     * <p>由切面在方法执行前后调用。<b>本方法必须吞掉自身异常</b>：
     * 记日志失败绝不能把业务操作一起带崩。</p>
     *
     * @param operationLog 日志内容
     */
    void record(SysOperationLog operationLog);

    /**
     * 分页查询操作日志
     *
     * @param param 查询参数
     * @return 分页结果
     */
    Page<SysOperationLog> getPage(LogQueryParam param);

    /**
     * 清空操作日志（TRUNCATE，同时重置自增 ID）
     *
     * @return 影响行数，TRUNCATE 恒返回 0
     */
    int clean();
}

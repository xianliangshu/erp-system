package com.erp.system.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.common.util.DateRangeUtils;
import com.erp.system.entity.SysOperationLog;
import com.erp.system.mapper.SysOperationLogMapper;
import com.erp.system.param.LogQueryParam;
import com.erp.system.service.ISysOperationLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 操作日志Service实现类
 *
 * @author ERP System
 * @since 2026-10-03
 */
@Slf4j
@Service
public class SysOperationLogServiceImpl extends ServiceImpl<SysOperationLogMapper, SysOperationLog>
        implements ISysOperationLogService {

    /**
     * 写入日志
     *
     * <p>{@code REQUIRES_NEW} 是刻意为之：审计日志要能独立于业务事务存下来。
     * 假如业务方法执行到一半其实已经报错、外层事务注定回滚，日志若跟它同一个事务
     * 也会被一起回滚，那就等于没有记录了。</p>
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void record(SysOperationLog operationLog) {
        try {
            if (operationLog.getOperTime() == null) {
                operationLog.setOperTime(LocalDateTime.now());
            }
            baseMapper.insert(operationLog);
        } catch (Exception e) {
            // 记日志失败不能影响业务，这里只能降级成应用日志
            log.error("操作日志写入失败: url={}, title={}", operationLog.getOperUrl(),
                    operationLog.getTitle(), e);
        }
    }

    @Override
    public Page<SysOperationLog> getPage(LogQueryParam param) {
        Page<SysOperationLog> page = new Page<>(param.getCurrent(), param.getSize());

        LocalDateTime begin = DateRangeUtils.startOfDay(param.getBeginDate());
        LocalDateTime end = DateRangeUtils.endOfDay(param.getEndDate());

        LambdaQueryWrapper<SysOperationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(param.getUsername()), SysOperationLog::getOperName, param.getUsername())
                .like(StrUtil.isNotBlank(param.getTitle()), SysOperationLog::getTitle, param.getTitle())
                .eq(param.getStatus() != null, SysOperationLog::getStatus, param.getStatus())
                .eq(param.getBusinessType() != null, SysOperationLog::getBusinessType, param.getBusinessType())
                .ge(begin != null, SysOperationLog::getOperTime, begin)
                .le(end != null, SysOperationLog::getOperTime, end)
                .orderByDesc(SysOperationLog::getId);

        return this.page(page, wrapper);
    }

    @Override
    public int clean() {
        baseMapper.truncateTable();
        return 0;
    }
}

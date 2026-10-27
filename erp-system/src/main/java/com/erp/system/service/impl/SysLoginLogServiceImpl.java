package com.erp.system.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.common.util.DateRangeUtils;
import com.erp.system.entity.SysLoginLog;
import com.erp.system.mapper.SysLoginLogMapper;
import com.erp.system.param.LogQueryParam;
import com.erp.system.service.ISysLoginLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 登录日志Service实现类
 *
 * @author ERP System
 * @since 2026-10-03
 */
@Slf4j
@Service
public class SysLoginLogServiceImpl extends ServiceImpl<SysLoginLogMapper, SysLoginLog>
        implements ISysLoginLogService {

    /**
     * 写入日志
     *
     * <p>{@code REQUIRES_NEW} 的原因见
     * {@link SysOperationLogServiceImpl#record}：审计日志要独立于业务事务。</p>
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void record(SysLoginLog loginLog) {
        try {
            if (loginLog.getLoginTime() == null) {
                loginLog.setLoginTime(LocalDateTime.now());
            }
            baseMapper.insert(loginLog);
        } catch (Exception e) {
            // 登录日志写失败不能把登录本身搞挂
            log.error("登录日志写入失败: username={}", loginLog.getUsername(), e);
        }
    }

    @Override
    public Page<SysLoginLog> getPage(LogQueryParam param) {
        Page<SysLoginLog> page = new Page<>(param.getCurrent(), param.getSize());

        LocalDateTime begin = DateRangeUtils.startOfDay(param.getBeginDate());
        LocalDateTime end = DateRangeUtils.endOfDay(param.getEndDate());

        LambdaQueryWrapper<SysLoginLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(param.getUsername()), SysLoginLog::getUsername, param.getUsername())
                .eq(param.getStatus() != null, SysLoginLog::getStatus, param.getStatus())
                .ge(begin != null, SysLoginLog::getLoginTime, begin)
                .le(end != null, SysLoginLog::getLoginTime, end)
                .orderByDesc(SysLoginLog::getId);

        return this.page(page, wrapper);
    }

    @Override
    public int clean() {
        baseMapper.truncateTable();
        return 0;
    }
}

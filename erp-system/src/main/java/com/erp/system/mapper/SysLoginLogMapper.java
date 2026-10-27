package com.erp.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.system.entity.SysLoginLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

/**
 * 登录日志Mapper接口
 * 
 * @author ERP System
 * @since 2025-12-12
 */
@Mapper
public interface SysLoginLogMapper extends BaseMapper<SysLoginLog> {

    /**
     * 清空登录日志表
     *
     * <p>TRUNCATE 的取舍同 {@link SysOperationLogMapper#truncateTable()}。</p>
     */
    @Update("TRUNCATE TABLE sys_login_log")
    void truncateTable();
}

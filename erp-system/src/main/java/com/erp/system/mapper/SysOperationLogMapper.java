package com.erp.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.system.entity.SysOperationLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

/**
 * 操作日志Mapper接口
 * 
 * @author ERP System
 * @since 2025-12-12
 */
@Mapper
public interface SysOperationLogMapper extends BaseMapper<SysOperationLog> {

    /**
     * 清空操作日志表
     *
     * <p>用 TRUNCATE 而不是 DELETE：日志表只需「一键清空」，顺带把自增 ID 归零，
     * 避免删光后再记录时 ID 已经涨得很大。</p>
     */
    @Update("TRUNCATE TABLE sys_operation_log")
    void truncateTable();
}

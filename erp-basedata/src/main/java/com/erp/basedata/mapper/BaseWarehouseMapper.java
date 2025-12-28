package com.erp.basedata.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.basedata.entity.BaseWarehouse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 仓库Mapper接口
 * 
 * @author ERP System
 * @since 2025-12-14
 */
@Mapper
public interface BaseWarehouseMapper extends BaseMapper<BaseWarehouse> {

    /**
     * 查询最大编号（忽略软删除条件）
     * 
     * @return 最大编号
     */
    @Select("SELECT MAX(code) FROM base_warehouse")
    String selectMaxCode();
}

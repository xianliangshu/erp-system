package com.erp.basedata.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.basedata.entity.BaseUnit;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 计量单位Mapper接口
 *
 * @author ERP System
 * @since 2025-12-14
 */
@Mapper
public interface BaseUnitMapper extends BaseMapper<BaseUnit> {

    /**
     * 查询最大编号（忽略软删除条件）
     * 
     * @return 最大编号
     */
    @Select("SELECT MAX(code) FROM base_unit")
    String selectMaxCode();
}

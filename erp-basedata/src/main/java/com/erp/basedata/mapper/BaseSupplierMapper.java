package com.erp.basedata.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.basedata.entity.BaseSupplier;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 供应商信息Mapper接口
 *
 * @author ERP System
 * @since 2025-12-16
 */
@Mapper
public interface BaseSupplierMapper extends BaseMapper<BaseSupplier> {

    /**
     * 查询最大编号（忽略软删除条件）
     * 
     * @return 最大编号
     */
    @Select("SELECT MAX(code) FROM base_supplier")
    String selectMaxCode();
}

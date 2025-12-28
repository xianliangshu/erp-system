package com.erp.basedata.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.basedata.entity.BaseCustomer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 客户信息 Mapper 接口
 *
 * @author ERP System
 * @since 2025-12-21
 */
@Mapper
public interface BaseCustomerMapper extends BaseMapper<BaseCustomer> {

    /**
     * 查询最大编号（忽略软删除条件）
     * 
     * @return 最大编号
     */
    @Select("SELECT MAX(code) FROM base_customer")
    String selectMaxCode();
}

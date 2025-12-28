package com.erp.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.business.entity.RetailReturn;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 零售退货单 Mapper
 */
@Mapper
public interface RetailReturnMapper extends BaseMapper<RetailReturn> {

    /**
     * 查询最大编号
     */
    @Select("SELECT MAX(code) FROM retail_return")
    String selectMaxCode();
}

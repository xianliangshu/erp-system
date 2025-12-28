package com.erp.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.business.entity.RetailOutSheet;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 零售出库单 Mapper
 */
@Mapper
public interface RetailOutSheetMapper extends BaseMapper<RetailOutSheet> {

    /**
     * 查询最大编号
     */
    @Select("SELECT MAX(code) FROM retail_out_sheet")
    String selectMaxCode();
}

package com.erp.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.business.entity.SettleFeeSheet;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 供应商费用单Mapper
 */
@Mapper
public interface SettleFeeSheetMapper extends BaseMapper<SettleFeeSheet> {

    /**
     * 根据前缀查询最大编号（不考虑逻辑删除）
     */
    @Select("SELECT MAX(code) FROM settle_fee_sheet WHERE code LIKE CONCAT(#{prefix}, '%')")
    String selectMaxCodeByPrefix(@Param("prefix") String prefix);
}

package com.erp.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.business.entity.SettleInOutItem;
import org.apache.ibatis.annotations.Mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 收支项目Mapper
 */
@Mapper
public interface SettleInOutItemMapper extends BaseMapper<SettleInOutItem> {

    /**
     * 根据前缀查询最大编号（不考虑逻辑删除）
     */
    @Select("SELECT MAX(code) FROM settle_in_out_item WHERE code LIKE CONCAT(#{prefix}, '%')")
    String selectMaxCodeByPrefix(@Param("prefix") String prefix);
}

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
}

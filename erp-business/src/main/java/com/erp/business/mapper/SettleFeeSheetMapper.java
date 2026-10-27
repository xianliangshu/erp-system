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
}

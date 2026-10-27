package com.erp.basedata.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.basedata.entity.BaseMaterialCode;
import org.apache.ibatis.annotations.Mapper;

/**
 * SKU 一品多码 Mapper 接口
 *
 * @author ERP System
 * @since 2026-10-03
 */
@Mapper
public interface BaseMaterialCodeMapper extends BaseMapper<BaseMaterialCode> {
}

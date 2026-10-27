package com.erp.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.business.entity.StockAdjustSheet;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 库存调整单 Mapper
 */
@Mapper
public interface StockAdjustSheetMapper extends BaseMapper<StockAdjustSheet> {
}

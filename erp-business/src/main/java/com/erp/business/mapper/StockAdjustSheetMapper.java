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

    /**
     * 查询最大编号（忽略软删除）
     */
    @Select("SELECT MAX(code) FROM stock_adjust_sheet")
    String selectMaxCode();
}

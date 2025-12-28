package com.erp.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.business.entity.StockCheck;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 库存盘点Mapper
 */
@Mapper
public interface StockCheckMapper extends BaseMapper<StockCheck> {

    /**
     * 查询指定前缀的最大编号（忽略软删除条件）
     * 
     * @param prefix 编号前缀
     * @return 最大编号
     */
    @Select("SELECT MAX(code) FROM stock_check WHERE code LIKE CONCAT(#{prefix}, '%')")
    String selectMaxCodeByPrefix(@Param("prefix") String prefix);
}

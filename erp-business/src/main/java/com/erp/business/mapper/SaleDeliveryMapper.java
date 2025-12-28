package com.erp.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.business.entity.SaleDelivery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 销售出库Mapper
 */
@Mapper
public interface SaleDeliveryMapper extends BaseMapper<SaleDelivery> {

    /**
     * 查询指定前缀的最大编号（忽略软删除条件）
     * 
     * @param prefix 编号前缀
     * @return 最大编号
     */
    @Select("SELECT MAX(code) FROM sale_delivery WHERE code LIKE CONCAT(#{prefix}, '%')")
    String selectMaxCodeByPrefix(@Param("prefix") String prefix);
}

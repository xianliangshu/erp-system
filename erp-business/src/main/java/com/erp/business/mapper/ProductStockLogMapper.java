package com.erp.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.business.entity.ProductStockLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品库存日志Mapper
 */
@Mapper
public interface ProductStockLogMapper extends BaseMapper<ProductStockLog> {
}

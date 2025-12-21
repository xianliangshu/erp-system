package com.erp.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.business.entity.ProductStock;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品库存Mapper
 */
@Mapper
public interface ProductStockMapper extends BaseMapper<ProductStock> {
}

package com.erp.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.business.entity.PurchaseOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 采购订单Mapper
 */
@Mapper
public interface PurchaseOrderMapper extends BaseMapper<PurchaseOrder> {
}

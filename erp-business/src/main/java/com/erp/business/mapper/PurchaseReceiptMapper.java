package com.erp.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.business.entity.PurchaseReceipt;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 采购收货Mapper
 */
@Mapper
public interface PurchaseReceiptMapper extends BaseMapper<PurchaseReceipt> {
}

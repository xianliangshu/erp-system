package com.erp.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.business.entity.TakeStockPlan;
import org.apache.ibatis.annotations.Mapper;

/**
 * 库存盘点任务 Mapper
 *
 * @author ERP System
 */
@Mapper
public interface TakeStockPlanMapper extends BaseMapper<TakeStockPlan> {
}

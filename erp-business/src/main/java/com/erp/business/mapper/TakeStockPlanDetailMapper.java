package com.erp.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.business.entity.TakeStockPlanDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 库存盘点任务明细 Mapper
 *
 * @author ERP System
 */
@Mapper
public interface TakeStockPlanDetailMapper extends BaseMapper<TakeStockPlanDetail> {

    /**
     * 差异单生成后，把盘点单 ID 记到明细上便于追溯。
     * <p>这里存的是「这张差异单由哪条明细产生」，不改变明细自身的数量语义。</p>
     */
    @Update("UPDATE take_stock_plan_detail SET description = #{description} " +
            "WHERE id = #{id}")
    int updateDescription(@Param("id") Long id, @Param("description") String description);
}

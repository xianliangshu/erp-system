package com.erp.basedata.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.basedata.entity.BaseMaterialCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 物料分类Mapper接口
 *
 * @author ERP System
 * @since 2025-12-14
 */
@Mapper
public interface BaseMaterialCategoryMapper extends BaseMapper<BaseMaterialCategory> {

    /**
     * 查询最大编号（忽略软删除条件）
     * 
     * @return 最大编号
     */
    @Select("SELECT MAX(code) FROM base_material_category")
    String selectMaxCode();
}

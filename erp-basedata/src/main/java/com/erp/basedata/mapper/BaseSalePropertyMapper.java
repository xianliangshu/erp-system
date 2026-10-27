package com.erp.basedata.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.basedata.entity.BaseSaleProperty;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 商品销售属性组 Mapper 接口
 *
 * @author ERP System
 * @since 2026-10-03
 */
@Mapper
public interface BaseSalePropertyMapper extends BaseMapper<BaseSaleProperty> {

    /**
     * 校验属性组编码是否已存在（<b>包含已逻辑删除的记录</b>），理由见 BaseSpuMapper 同名方法。
     *
     * @param code      属性组编码
     * @param excludeId 需要排除的属性组 ID（更新时传入，新增传 null）
     * @return 占用条数
     */
    @Select("SELECT COUNT(*) FROM base_sale_property WHERE code = #{code} AND (#{excludeId} IS NULL OR id <> #{excludeId})")
    Long countByCodeIncludeDeleted(@Param("code") String code, @Param("excludeId") Long excludeId);
}

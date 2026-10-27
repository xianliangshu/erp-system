package com.erp.basedata.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.basedata.entity.BaseSpu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * SPU 商品 Mapper 接口
 *
 * @author ERP System
 * @since 2026-10-03
 */
@Mapper
public interface BaseSpuMapper extends BaseMapper<BaseSpu> {

    /**
     * 校验 SPU 编号是否已存在（<b>包含已逻辑删除的记录</b>）。
     *
     * <p>唯一键 uk_code 不包含 deleted 列，因此逻辑删除后重建同编号仍会撞唯一键，
     * 必须绕过 MyBatis-Plus 的 @TableLogic 自动过滤来做前置校验。</p>
     *
     * @param code      SPU 编号
     * @param excludeId 需要排除的 SPU ID（更新时传入，新增传 null）
     * @return 占用条数
     */
    @Select("SELECT COUNT(*) FROM base_spu WHERE code = #{code} AND (#{excludeId} IS NULL OR id <> #{excludeId})")
    Long countByCodeIncludeDeleted(@Param("code") String code, @Param("excludeId") Long excludeId);
}

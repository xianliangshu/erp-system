package com.erp.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.common.entity.CodeRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 单据编号规则 Mapper
 *
 * @author ERP System
 * @since 2026-10-03
 */
@Mapper
public interface CodeRuleMapper extends BaseMapper<CodeRule> {

    /**
     * 行级锁读取规则（必须在事务中调用）。
     *
     * <p>这是并发安全的关键：把「读当前值 → 计算下一个值 → 写回」串行化。
     * 原来的实现是「SELECT MAX(code) 再 +1」，两个并发请求会拿到同一个流水号，
     * 导致单据编号重复。</p>
     *
     * <p>注意：自定义 SQL 不会被 MyBatis-Plus 自动追加逻辑删除条件，需显式写
     * {@code is_deleted = 0}。</p>
     *
     * @param ruleCode 规则编码
     * @return 规则记录，不存在时返回 null
     */
    @Select("SELECT * FROM sys_code_rule WHERE rule_code = #{ruleCode} AND is_deleted = 0 FOR UPDATE")
    CodeRule selectForUpdate(@Param("ruleCode") String ruleCode);

    /**
     * 按规则编码查询（不加锁）
     *
     * @param ruleCode 规则编码
     * @return 规则记录，不存在时返回 null
     */
    @Select("SELECT * FROM sys_code_rule WHERE rule_code = #{ruleCode} AND is_deleted = 0")
    CodeRule selectByRuleCode(@Param("ruleCode") String ruleCode);
}

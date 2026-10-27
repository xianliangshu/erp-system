package com.erp.common.service;

import com.erp.common.entity.CodeRule;

import java.util.List;

/**
 * 统一编号生成服务
 *
 * <p>取代原先散落在 28 个 ServiceImpl 中的 {@code generateXxxCode()} 方法。
 * 所有单据编号一律通过 {@link #generate(String)} 生成，规则存在
 * {@code sys_code_rule} 表中，可在线调整而无需改代码。</p>
 *
 * @author ERP System
 * @since 2026-10-03
 */
public interface ICodeGenerateService {

    /**
     * 生成下一个编号（并发安全，内部对规则行加锁）
     *
     * @param ruleCode 规则编码，如 {@code PURCHASE_ORDER}、{@code BASE_MATERIAL}
     * @return 生成的编号，如 {@code PO202610030001}
     */
    String generate(String ruleCode);

    /**
     * 查询全部启用的编号规则
     *
     * @return 规则列表
     */
    List<CodeRule> listRules();

    /**
     * 按规则编码查询规则
     *
     * @param ruleCode 规则编码
     * @return 规则记录，不存在时返回 null
     */
    CodeRule getRule(String ruleCode);
}

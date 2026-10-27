package com.erp.common.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.erp.common.entity.CodeRule;
import com.erp.common.exception.BusinessException;
import com.erp.common.mapper.CodeRuleMapper;
import com.erp.common.service.ICodeGenerateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 统一编号生成服务实现
 *
 * <p>并发安全依赖两件事：</p>
 * <ol>
 *   <li>{@code SELECT ... FOR UPDATE} 对规则行加排他锁，把「读值 → 计算 → 写回」串行化；</li>
 *   <li>方法带 {@code @Transactional}，保证锁在整个「读改写」期间有效。
 *       若没有事务，行锁会在语句结束后立即释放，等于没有加锁。</li>
 * </ol>
 *
 * @author ERP System
 * @since 2026-10-03
 */
@Slf4j
@Service
public class CodeGenerateServiceImpl implements ICodeGenerateService {

    /**
     * 日切判定统一用 yyyyMMdd，与编号里日期段的显示格式无关
     */
    private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final CodeRuleMapper codeRuleMapper;

    public CodeGenerateServiceImpl(CodeRuleMapper codeRuleMapper) {
        this.codeRuleMapper = codeRuleMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String generate(String ruleCode) {
        if (StrUtil.isBlank(ruleCode)) {
            throw new BusinessException("编号规则编码不能为空");
        }

        CodeRule rule = codeRuleMapper.selectForUpdate(ruleCode);
        if (rule == null) {
            throw new BusinessException("编号规则未配置: " + ruleCode + "（请在 sys_code_rule 中登记）");
        }
        if (!Integer.valueOf(1).equals(rule.getStatus())) {
            throw new BusinessException("编号规则已停用: " + ruleCode);
        }

        LocalDate today = LocalDate.now();
        String todayStr = today.format(DAY_FORMATTER);

        int seqLength = rule.getSeqLength() == null ? 4 : rule.getSeqLength();
        int seqStart = rule.getSeqStart() == null ? 1 : rule.getSeqStart();
        int seqStep = rule.getSeqStep() == null ? 1 : rule.getSeqStep();
        int current = rule.getCurrentValue() == null ? 0 : rule.getCurrentValue();

        // 按日重置：只有「当前值所属日期」不是今天时才回到起始值。
        // 同一天内持续递增（不撞号），跨天后从起始值开始（不跳号）。
        String seqDate = rule.getSeqDate() == null ? "" : rule.getSeqDate();
        boolean needReset = Integer.valueOf(1).equals(rule.getResetByDate()) && !todayStr.equals(seqDate);

        int next = needReset ? seqStart : current + seqStep;
        if (next < seqStart) {
            next = seqStart;
        }
        if (String.valueOf(next).length() > seqLength) {
            throw new BusinessException(
                    rule.getRuleName() + " 编号流水号已超出 " + seqLength + " 位上限，请调整规则位数");
        }

        StringBuilder code = new StringBuilder(rule.getPrefix() == null ? "" : rule.getPrefix());
        if (StrUtil.isNotBlank(rule.getDateFormat())) {
            code.append(today.format(DateTimeFormatter.ofPattern(rule.getDateFormat())));
        }
        code.append(String.format("%0" + seqLength + "d", next));

        rule.setCurrentValue(next);
        rule.setSeqDate(todayStr);
        codeRuleMapper.updateById(rule);

        String result = code.toString();
        log.debug("生成编号: rule={} code={} value={} date={}", ruleCode, result, next, todayStr);
        return result;
    }

    @Override
    public List<CodeRule> listRules() {
        LambdaQueryWrapper<CodeRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(CodeRule::getId);
        return codeRuleMapper.selectList(wrapper);
    }

    @Override
    public CodeRule getRule(String ruleCode) {
        return codeRuleMapper.selectByRuleCode(ruleCode);
    }
}

package com.erp.common.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 单据编号规则实体
 *
 * <p>对应表 {@code sys_code_rule}。把原来散落在 28 个 ServiceImpl 里的
 * 「前缀 + 日期格式 + 流水位数」配置集中到一张表，改规则不需要改代码。</p>
 *
 * @author ERP System
 * @since 2026-10-03
 */
@Data
@TableName("sys_code_rule")
public class CodeRule implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 规则编码(业务唯一标识, 如 PURCHASE_ORDER)
     */
    private String ruleCode;

    /**
     * 单据/对象名称
     */
    private String ruleName;

    /**
     * 编号前缀
     */
    private String prefix;

    /**
     * 日期段格式(如 yyyyMMdd), 为空表示编号不含日期段
     */
    private String dateFormat;

    /**
     * 流水号位数
     */
    private Integer seqLength;

    /**
     * 起始流水值
     */
    private Integer seqStart;

    /**
     * 步长
     */
    private Integer seqStep;

    /**
     * 当前流水值(0 表示未初始化)
     */
    private Integer currentValue;

    /**
     * 流水号所属日期(yyyyMMdd, 用于按日重置判定)
     *
     * <p>⚠️ 列名不能叫 {@code current_date} —— 它是 MySQL 保留字（无括号的日期函数），
     * 生成的 {@code SET current_date=?} 会直接报语法错误。</p>
     */
    private String seqDate;

    /**
     * 是否按日重置: 1-是 0-否(主数据编码用 0)
     */
    private Integer resetByDate;

    /**
     * 状态: 1-启用 0-停用
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 删除标记: 0-未删除 1-已删除
     */
    @TableLogic
    private Integer isDeleted;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    /**
     * 更新人
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;
}

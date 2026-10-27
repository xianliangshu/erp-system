package com.erp.business.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.erp.business.enums.TakeStockPlanStatus;
import com.erp.business.enums.TakeStockPlanType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 库存盘点任务
 *
 * <p>业务含义：按「全场 / 单品 / 分类 / 品牌」批量发起盘点，创建时把
 * <b>当时的系统库存写进明细作为快照</b>（{@link TakeStockPlanDetail#getStockNum()}），
 * 这样盘点期间的出入库不会污染比对基准。</p>
 *
 * <p>盘点结果最终汇成一张 {@code stock_check}（回填 {@code plan_id}），
 * 后续走盘点单已有的审核流程调整库存 —— 本任务自身不改库存。</p>
 *
 * @author ERP System
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("take_stock_plan")
public class TakeStockPlan extends BaseBusinessEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 任务编号（编号规则 STOCK_TAKE_PLAN → PDyyyyMMdd0001）
     */
    private String code;

    /**
     * 仓库ID
     */
    private Long scId;

    /**
     * 盘点类别(0=全场,1=单品,2=分类,3=品牌)
     */
    private TakeStockPlanType takeType;

    /**
     * 盘点范围ID串（逗号分隔：分类ID / 品牌ID / 商品ID）；全场时为空
     */
    private String bizId;

    /**
     * 状态(0=任务生成,6=差异已生成,9=盘点完成,12=已作废)
     */
    private TakeStockPlanStatus takeStatus;

    /**
     * 备注
     */
    private String description;

    // ==================== 以下为非数据库字段 ====================

    /**
     * 仓库名称
     */
    @TableField(exist = false)
    private String scName;

    /**
     * 明细总数
     */
    @TableField(exist = false)
    private Long detailCount;

    /**
     * 已录入盘点数量的明细数
     */
    @TableField(exist = false)
    private Long enteredCount;

    /**
     * 生成的盘点单ID（差异已生成后回填展示）
     */
    @TableField(exist = false)
    private Long checkId;

    /**
     * 明细列表
     */
    @TableField(exist = false)
    private List<TakeStockPlanDetail> details;
}

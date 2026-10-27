package com.erp.business.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.erp.business.enums.StockCostAdjustStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 库存成本调整单
 *
 * <p>业务含义：<b>只改成本价、不改数量</b>。用于成本录错、批次成本重估。
 * 与 {@link StockAdjustSheet}（库存调整单）的分工是：调整单改数量，本单改价格。</p>
 *
 * <p>审核通过时直接回写 {@code product_stock.tax_price / tax_amount}，
 * <b>绝不走 addStock / subStock</b> —— 数量没变，走库存入口会把 stock_num 加/减一遍。</p>
 *
 * @author ERP System
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("stock_cost_adjust_sheet")
public class StockCostAdjustSheet extends BaseBusinessEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 单据编号
     */
    private String code;

    /**
     * 仓库ID
     */
    private Long scId;

    /**
     * 调价品种数
     */
    private Integer productNum;

    /**
     * 库存调价差额合计
     */
    private BigDecimal diffAmount;

    /**
     * 状态(0=待审核,1=审核通过,2=审核拒绝)
     */
    private StockCostAdjustStatus status;

    /**
     * 备注
     */
    private String description;

    /**
     * 审核人
     */
    private String approveBy;

    /**
     * 审核时间
     */
    private LocalDateTime approveTime;

    /**
     * 拒绝原因
     */
    private String refuseReason;

    // ==================== 以下为非数据库字段 ====================

    /**
     * 仓库名称
     */
    @TableField(exist = false)
    private String scName;

    /**
     * 明细列表
     */
    @TableField(exist = false)
    private List<StockCostAdjustSheetDetail> details;
}

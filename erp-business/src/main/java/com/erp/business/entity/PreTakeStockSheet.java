package com.erp.business.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 库存预先盘点单
 *
 * <p>业务含义：<b>正式盘点单之前的分次清点记录</b>（初盘 → 复盘 → 抽盘），用于交叉校验。
 * 本单据<b>不产生任何库存变动</b>，纯记录；差异最终由「库存盘点单」或「盘点任务」处理。</p>
 *
 * @author ERP System
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pre_take_stock_sheet")
public class PreTakeStockSheet extends BaseBusinessEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 单据编号（编号规则 STOCK_TAKE_PRE → YPDyyyyMMdd0001）
     */
    private String code;

    /**
     * 仓库ID
     */
    private Long scId;

    /**
     * 盘点轮次(0=初盘,1=复盘,2=抽盘)
     */
    private Integer takeStatus;

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
     * 明细列表
     */
    @TableField(exist = false)
    private List<PreTakeStockSheetDetail> details;
}

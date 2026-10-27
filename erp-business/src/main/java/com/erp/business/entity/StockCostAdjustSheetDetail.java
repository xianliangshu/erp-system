package com.erp.business.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 库存成本调整单明细
 *
 * <p>明细表不继承实体基类（项目约定：所有 *_detail 表都是独立实现，无 deleted 列）。</p>
 *
 * @author ERP System
 */
@Data
@TableName("stock_cost_adjust_sheet_detail")
public class StockCostAdjustSheetDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 单据ID
     */
    private Long sheetId;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 库存数量（制单时快照，审核前会与当前库存二次校验）
     */
    private BigDecimal stockNum;

    /**
     * 档案采购价（参考值）
     */
    private BigDecimal purchasePrice;

    /**
     * 调整前成本价
     */
    private BigDecimal oriPrice;

    /**
     * 调整后成本价
     */
    private BigDecimal price;

    /**
     * 调价差额 = (price - oriPrice) × stockNum
     */
    private BigDecimal diffAmount;

    /**
     * 备注
     */
    private String description;

    /**
     * 排序
     */
    private Integer orderNo;

    // ==================== 以下为非数据库字段 ====================

    /**
     * 商品编号
     */
    @TableField(exist = false)
    private String productCode;

    /**
     * 商品名称
     */
    @TableField(exist = false)
    private String productName;

    /**
     * 规格型号
     */
    @TableField(exist = false)
    private String specification;

    /**
     * 销售属性文本（SKU）
     */
    @TableField(exist = false)
    private String salePropertyText;
}

package com.erp.business.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 库存预先盘点单明细
 *
 * <p>明细表不继承实体基类（项目约定：所有 *_detail 表都是独立实现，无 deleted 列）。</p>
 *
 * <p>三个数量列对应三轮清点结果，<b>互不影响</b>，允许只填其中一轮：</p>
 * <ul>
 *   <li>{@code firstNum} 初盘数量</li>
 *   <li>{@code secondNum} 复盘数量</li>
 *   <li>{@code randNum} 抽盘数量</li>
 * </ul>
 *
 * @author ERP System
 */
@Data
@TableName("pre_take_stock_sheet_detail")
public class PreTakeStockSheetDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 预先盘点单ID
     */
    private Long sheetId;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 初盘数量
     */
    private BigDecimal firstNum;

    /**
     * 复盘数量
     */
    private BigDecimal secondNum;

    /**
     * 抽盘数量
     */
    private BigDecimal randNum;

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

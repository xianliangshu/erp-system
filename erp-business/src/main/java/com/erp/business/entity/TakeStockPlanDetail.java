package com.erp.business.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 库存盘点任务明细
 *
 * <p>明细表不继承实体基类（项目约定：所有 *_detail 表都是独立实现，无 deleted 列）。</p>
 *
 * <p>数量语义：</p>
 * <ul>
 *   <li>{@code stockNum} —— <b>创建任务时的库存快照</b>，不随出入库变化，是差异比对的基准</li>
 *   <li>{@code oriTakeNum} —— 盘点数量（原始录入）</li>
 *   <li>{@code takeNum} —— 修正后盘点数量（生成差异以它为准）</li>
 *   <li>{@code totalOutNum / totalInNum} —— 盘点期间该商品的出/入库累计，供「动碰盘点」参考</li>
 * </ul>
 *
 * <p>表上刻意<b>不加</b> {@code uk(plan_id, product_id)}：唯一键不含 deleted 列，
 * 逻辑删除后重建必撞键（同 SKU 关联表的教训），去重放应用层。</p>
 *
 * @author ERP System
 */
@Data
@TableName("take_stock_plan_detail")
public class TakeStockPlanDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 盘点任务ID
     */
    private Long planId;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 创建任务时的系统库存快照
     */
    private BigDecimal stockNum;

    /**
     * 盘点数量（原始录入）
     */
    private BigDecimal oriTakeNum;

    /**
     * 修正后盘点数量
     */
    private BigDecimal takeNum;

    /**
     * 盘点期间出库累计
     */
    private BigDecimal totalOutNum;

    /**
     * 盘点期间入库累计
     */
    private BigDecimal totalInNum;

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

    /**
     * 当前系统库存（与快照对比用，展示层实时回填，不落库）
     */
    @TableField(exist = false)
    private BigDecimal currentStockNum;

    /**
     * 差异数量 = 修正后盘点数量 - 库存快照（展示用，服务端计算）
     */
    @TableField(exist = false)
    private BigDecimal diffNum;
}

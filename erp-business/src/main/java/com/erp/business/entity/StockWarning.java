package com.erp.business.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 库存预警配置（仓库 × 商品 的上下限）
 *
 * @author ERP System
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("stock_warning")
public class StockWarning extends BaseBusinessEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 仓库ID
     */
    private Long scId;

    /**
     * 商品ID（即 SKU，沿用 base_material.id）
     */
    private Long productId;

    /**
     * 预警上限，NULL = 不限上限
     */
    private BigDecimal maxLimit;

    /**
     * 预警下限，NULL = 不限下限
     */
    private BigDecimal minLimit;

    /**
     * 状态(0=停用,1=启用)
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    // ==================== 以下为非数据库字段 ====================

    /**
     * 仓库名称
     */
    @TableField(exist = false)
    private String scName;

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
     * 当前库存数量
     */
    @TableField(exist = false)
    private BigDecimal stockNum;

    /**
     * 命中类型(1=低于下限,2=高于上限)，仅 hit-list 接口返回
     */
    @TableField(exist = false)
    private Integer hitType;
}

package com.erp.business.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.erp.business.enums.ProductStockBizType;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品库存日志
 */
@Data
@TableName("product_stock_log")
public class ProductStockLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 仓库ID
     */
    private Long scId;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 原库存数量
     */
    private BigDecimal oriStockNum;

    /**
     * 现库存数量
     */
    private BigDecimal curStockNum;

    /**
     * 变动库存数量
     */
    private BigDecimal stockNum;

    /**
     * 业务单据ID
     */
    private Long bizId;

    /**
     * 业务单据号
     */
    private String bizCode;

    /**
     * 业务类型
     */
    private ProductStockBizType bizType;

    /**
     * 创建者
     */
    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

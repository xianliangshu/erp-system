package com.erp.business.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 库存盘点明细
 */
@Data
@TableName("stock_check_detail")
public class StockCheckDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 盘点单ID */
    private Long checkId;

    /** 商品ID */
    private Long productId;

    /** 账面数量 */
    private BigDecimal stockNum;

    /** 实盘数量 */
    private BigDecimal actualNum;

    /** 差异数量 */
    private BigDecimal diffNum;

    /** 成本单价 */
    private BigDecimal costPrice;

    /** 差异金额 */
    private BigDecimal diffAmount;

    /** 备注 */
    private String description;
}

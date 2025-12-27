package com.erp.business.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 库存调拨明细
 */
@Data
@TableName("stock_transfer_detail")
public class StockTransferDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 调拨单ID */
    private Long transferId;

    /** 商品ID */
    private Long productId;

    /** 调拨数量 */
    private BigDecimal transferNum;

    /** 成本单价 */
    private BigDecimal costPrice;

    /** 备注 */
    private String description;
}

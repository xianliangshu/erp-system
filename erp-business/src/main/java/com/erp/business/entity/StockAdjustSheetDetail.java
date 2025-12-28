package com.erp.business.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 库存调整单明细
 */
@Data
@TableName("stock_adjust_sheet_detail")
public class StockAdjustSheetDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 调整单ID
     */
    private Long sheetId;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 调整库存数量
     */
    private BigDecimal stockNum;

    /**
     * 备注
     */
    private String description;

    /**
     * 排序
     */
    private Integer sort;
}

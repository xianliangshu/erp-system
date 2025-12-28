package com.erp.business.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 零售出库单明细
 */
@Data
@TableName("retail_out_sheet_detail")
public class RetailOutSheetDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 主表ID
     */
    private Long sheetId;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 出库数量
     */
    private BigDecimal outNum;

    /**
     * 含税单价
     */
    private BigDecimal taxPrice;

    /**
     * 含税金额
     */
    private BigDecimal taxAmount;

    /**
     * 备注
     */
    private String description;
}

package com.erp.business.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 供应商费用单明细
 */
@Data
@TableName("settle_fee_sheet_detail")
public class SettleFeeSheetDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 费用单ID
     */
    private Long sheetId;

    /**
     * 收支项目ID
     */
    private Long itemId;

    /**
     * 金额
     */
    private BigDecimal amount;

    /**
     * 排序
     */
    private Integer orderNo;
}

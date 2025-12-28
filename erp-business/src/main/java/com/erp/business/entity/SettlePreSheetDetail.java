package com.erp.business.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 供应商预付款单明细
 */
@Data
@TableName("settle_pre_sheet_detail")
public class SettlePreSheetDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 预付款单ID
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

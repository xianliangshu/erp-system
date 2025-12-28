package com.erp.business.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 供应商对账单明细
 */
@Data
@TableName("settle_check_sheet_detail")
public class SettleCheckSheetDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long sheetId;
    private Long bizId;
    private Integer bizType; // 1-采购入库, 2-采购退货, 3-费用单, 4-预付款
    private String bizCode;
    private BigDecimal payAmount;
    private String description;
    private Integer orderNo;
}

package com.erp.business.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@TableName("customer_settle_sheet_detail")
public class CustomerSettleSheetDetail implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long sheetId;
    private Long bizId;
    private String bizCode;
    private BigDecimal payAmount;
    private BigDecimal discountAmount;
    private String description;
    private Integer orderNo;
}

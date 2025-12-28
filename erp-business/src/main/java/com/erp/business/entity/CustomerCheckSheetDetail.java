package com.erp.business.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@TableName("customer_check_sheet_detail")
public class CustomerCheckSheetDetail implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long sheetId;
    private Long bizId;
    private Integer bizType;
    private String bizCode;
    private BigDecimal payAmount;
    private String description;
    private Integer orderNo;
}

package com.erp.business.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 库存盘点
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("stock_check")
public class StockCheck extends BaseBusinessEntity {

    /** 盘点单编号 */
    private String code;

    /** 仓库ID */
    private Long scId;

    /** 盘点日期 */
    private LocalDate checkDate;

    /** 盘盈数量 */
    private BigDecimal totalProfitNum;

    /** 盘亏数量 */
    private BigDecimal totalLossNum;

    /** 状态 */
    private Integer status;

    /** 备注 */
    private String description;
}

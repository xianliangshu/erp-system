package com.erp.business.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 库存调拨
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("stock_transfer")
public class StockTransfer extends BaseBusinessEntity {

    /** 调拨单编号 */
    private String code;

    /** 调出仓库ID */
    private Long outScId;

    /** 调入仓库ID */
    private Long inScId;

    /** 调拨日期 */
    private LocalDate transferDate;

    /** 调拨总数量 */
    private BigDecimal totalNum;

    /** 状态: 0-待确认 1-已确认 */
    private Integer status;

    /** 备注 */
    private String description;
}

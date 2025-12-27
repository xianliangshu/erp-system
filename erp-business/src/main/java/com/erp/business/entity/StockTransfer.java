package com.erp.business.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 库存调拨
 */
@Data
@TableName("stock_transfer")
public class StockTransfer implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

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

    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}

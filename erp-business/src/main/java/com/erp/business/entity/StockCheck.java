package com.erp.business.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 库存盘点
 */
@Data
@TableName("stock_check")
public class StockCheck implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

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

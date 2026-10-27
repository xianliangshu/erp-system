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

    /**
     * 关联盘点任务ID（可空）
     *
     * <p>由「盘点任务 → 生成差异」产生的盘点单会回填此列，便于从盘点单回溯到任务。
     * 手工创建的盘点单保持为 NULL，行为与改造前完全一致。</p>
     */
    private Long planId;

    /**
     * 关联预先盘点单ID（可空）
     */
    private Long preSheetId;

    /** 备注 */
    private String description;
}

package com.erp.business.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.erp.business.enums.StockAdjustBizType;
import com.erp.business.enums.StockAdjustSheetStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 库存调整单
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("stock_adjust_sheet")
public class StockAdjustSheet extends BaseBusinessEntity {

    /**
     * 调整单编号
     */
    private String code;

    /**
     * 仓库ID
     */
    private Long scId;

    /**
     * 调整原因ID
     */
    private Long reasonId;

    /**
     * 业务类型: 0-入库调整 1-出库调整
     */
    private StockAdjustBizType bizType;

    /**
     * 状态: 0-待审核 1-审核通过 2-审核拒绝
     */
    private StockAdjustSheetStatus status;

    /**
     * 备注
     */
    private String description;

    /**
     * 审核人
     */
    private String approveBy;

    /**
     * 审核时间
     */
    private LocalDateTime approveTime;

    /**
     * 拒绝原因
     */
    private String refuseReason;
}

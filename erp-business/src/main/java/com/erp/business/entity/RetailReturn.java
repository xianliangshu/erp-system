package com.erp.business.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

/**
 * 零售退货单
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("retail_return")
public class RetailReturn extends BaseBusinessEntity {

    /**
     * 单据编号
     */
    private String code;

    /**
     * 关联出库单ID
     */
    private Long outSheetId;

    /**
     * 仓库ID
     */
    private Long scId;

    /**
     * 客户ID
     */
    private Long customerId;

    /**
     * 总数量
     */
    private BigDecimal totalNum;

    /**
     * 总金额
     */
    private BigDecimal totalAmount;

    /**
     * 状态: 0-待审核, 1-已审核, 2-已拒绝
     */
    private Integer status;

    /**
     * 备注
     */
    private String description;
}

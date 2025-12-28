package com.erp.business.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

/**
 * 零售出库单
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("retail_out_sheet")
public class RetailOutSheet extends BaseBusinessEntity {

    /**
     * 单据编号
     */
    private String code;

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

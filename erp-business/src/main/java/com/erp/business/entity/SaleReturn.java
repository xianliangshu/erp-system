package com.erp.business.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 销售退货
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sale_return")
public class SaleReturn extends BaseBusinessEntity {

    /** 退货单编号 */
    private String code;

    /** 出库单ID */
    private Long deliveryId;

    /** 出库单编号 */
    private String deliveryCode;

    /** 仓库ID */
    private Long scId;

    /** 客户ID */
    private Long customerId;

    /** 退货数量 */
    private BigDecimal totalNum;

    /** 退货金额 */
    private BigDecimal totalAmount;

    /** 状态: 0-待确认 1-已确认 */
    private Integer status;

    /** 备注 */
    private String description;
}

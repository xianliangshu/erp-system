package com.erp.business.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 销售出库明细
 */
@Data
@TableName("sale_delivery_detail")
public class SaleDeliveryDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 出库单ID */
    private Long deliveryId;

    /** 订单明细ID */
    private Long orderDetailId;

    /** 商品ID */
    private Long productId;

    /** 订单数量 */
    private BigDecimal orderNum;

    /** 出库数量 */
    private BigDecimal deliveryNum;

    /** 含税单价 */
    private BigDecimal taxPrice;

    /** 含税金额 */
    private BigDecimal taxAmount;

    /** 备注 */
    private String description;
}

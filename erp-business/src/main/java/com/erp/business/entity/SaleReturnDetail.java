package com.erp.business.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 销售退货明细
 */
@Data
@TableName("sale_return_detail")
public class SaleReturnDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 退货单ID */
    private Long returnId;

    /** 出库明细ID */
    private Long deliveryDetailId;

    /** 商品ID */
    private Long productId;

    /** 出库数量 */
    private BigDecimal deliveryNum;

    /** 退货数量 */
    private BigDecimal returnNum;

    /** 含税单价 */
    private BigDecimal taxPrice;

    /** 含税金额 */
    private BigDecimal taxAmount;

    /** 备注 */
    private String description;
}

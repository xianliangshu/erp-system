package com.erp.business.vo;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 库存查询VO (包含关联信息)
 */
@Data
public class ProductStockVO {
    private Long id;
    private Long scId;
    private String scName; // 仓库名称
    private Long productId;
    private String productCode; // 商品编号
    private String productName; // 商品名称
    private String productSpec; // 规格
    private String unitName; // 单位
    private BigDecimal stockNum;
    private BigDecimal taxPrice;
    private BigDecimal taxAmount;
}

package com.erp.business.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 库存流水VO (包含关联的仓库与商品信息)
 */
@Data
public class ProductStockLogVO {

    private Long id;

    private Long scId;

    /** 仓库名称 */
    private String scName;

    private Long productId;

    /** 商品编号 */
    private String productCode;

    /** 商品名称 */
    private String productName;

    /** 规格型号 */
    private String productSpec;

    /** 计量单位 */
    private String unitName;

    /** 原库存数量 */
    private BigDecimal oriStockNum;

    /** 变动数量 (正数入库 / 负数出库) */
    private BigDecimal stockNum;

    /** 现库存数量 */
    private BigDecimal curStockNum;

    /** 业务单据ID */
    private Long bizId;

    /** 业务单据号 */
    private String bizCode;

    /** 业务类型值 */
    private Integer bizType;

    /** 业务类型描述 */
    private String bizTypeDesc;

    /** 操作人 */
    private String createBy;

    /** 操作时间 */
    private LocalDateTime createTime;
}

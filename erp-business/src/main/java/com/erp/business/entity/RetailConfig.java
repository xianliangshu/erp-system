package com.erp.business.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 零售配置
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("retail_config")
public class RetailConfig extends BaseBusinessEntity {

    /**
     * 零售出库单是否自动审核
     */
    private Boolean outStockUnApprove;

    /**
     * 零售退货单是否自动审核
     */
    private Boolean returnStockUnApprove;
}

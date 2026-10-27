package com.erp.business.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 库存盘点参数设置（固定单行，id = 1）
 *
 * <p>控制盘点任务与盘点单的行为，避免这些开关散落在代码里硬编码。</p>
 *
 * @author ERP System
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("take_stock_config")
public class TakeStockConfig extends BaseBusinessEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 配置ID（固定为 1）
     */
    public static final Long SINGLETON_ID = 1L;

    /**
     * 盘点单关联盘点任务后，是否带出任务中的商品明细(0=否,1=是)
     */
    private Integer showProduct;

    /**
     * 是否显示盘点任务创建时的系统库存快照(0=否,1=是)
     */
    private Integer showStock;

    /**
     * 差异生成时是否自动调整库存(0=否,1=是)
     */
    private Integer autoChangeStock;

    /**
     * 盘点数量是否允许手工修改(0=否,1=是)
     */
    private Integer allowChangeNum;

    /**
     * 盘点任务创建后多少小时未完成自动作废
     */
    private Integer cancelHours;
}

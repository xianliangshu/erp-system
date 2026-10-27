package com.erp.basedata.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * SPU 与销售属性组绑定实体类
 *
 * <p>记录某个 SPU 选用哪几个属性组来生成 SKU 矩阵。</p>
 *
 * <p><b>注意</b>：本类<b>不继承 BaseDataEntity</b>、不使用逻辑删除。
 * 原因是表的唯一键 (spu_id, property_id) 不含 deleted 列，逻辑删除后重新绑定会撞唯一键；
 * 关联表用物理删除语义更干净（与项目里 *_detail 明细表的处理一致）。</p>
 *
 * @author ERP System
 * @since 2026-10-03
 */
@Data
@TableName("base_spu_sale_property")
public class BaseSpuSaleProperty implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * SPU ID
     */
    private Long spuId;

    /**
     * 属性组ID
     */
    private Long propertyId;
}

package com.erp.basedata.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * SKU 与销售属性值绑定实体类
 *
 * <p>每条记录表示某 SKU 在某个属性组上取的值，例如「颜色 = 红色」。
 * 一个 SKU 在每个属性组上只能取一个值（由唯一键 uk_mat_property 约束）。</p>
 *
 * <p><b>注意</b>：本类不继承 BaseDataEntity、不使用逻辑删除，理由同
 * {@link BaseSpuSaleProperty}（唯一键不含 deleted 列）。</p>
 *
 * @author ERP System
 * @since 2026-10-03
 */
@Data
@TableName("base_material_property_rel")
public class BaseMaterialPropertyRel implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * SKU ID(=base_material.id)
     */
    private Long materialId;

    /**
     * 属性组ID
     */
    private Long propertyId;

    /**
     * 属性值ID
     */
    private Long propertyItemId;
}

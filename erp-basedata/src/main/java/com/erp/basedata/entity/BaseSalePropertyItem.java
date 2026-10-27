package com.erp.basedata.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 商品销售属性值实体类（如"红色"、"XL"）
 *
 * <p><b>注意</b>：本类不继承 BaseDataEntity、不使用逻辑删除。属性值的维护方式是
 * 「随属性组整体覆盖」，物理删除语义更简单；且唯一键 (property_id, code) 不含 deleted 列。</p>
 *
 * @author ERP System
 * @since 2026-10-03
 */
@Data
@TableName("base_sale_property_item")
public class BaseSalePropertyItem implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 属性组ID
     */
    private Long propertyId;

    /**
     * 属性值编码
     */
    private String code;

    /**
     * 属性值名称
     */
    private String name;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 状态(0=禁用,1=启用)
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 属性组名称(非数据库字段)
     */
    @TableField(exist = false)
    private String propertyName;
}

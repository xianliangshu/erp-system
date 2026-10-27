package com.erp.basedata.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * SKU 一品多码实体类
 *
 * <p>一个 SKU 可挂多个编码：内部主编号、厂商条码、门店自编码等，
 * 扫码查询时任意一个码都能定位到同一个 SKU。</p>
 *
 * <p><b>注意</b>：本类不继承 BaseDataEntity、不使用逻辑删除，理由同
 * {@link BaseSpuSaleProperty}（唯一键 uk_code_type 不含 deleted 列）。</p>
 *
 * @author ERP System
 * @since 2026-10-03
 */
@Data
@TableName("base_material_code")
public class BaseMaterialCode implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 编码类型：主编号
     */
    public static final int TYPE_MAIN = 1;

    /**
     * 编码类型：条码
     */
    public static final int TYPE_BARCODE = 2;

    /**
     * 编码类型：其他
     */
    public static final int TYPE_OTHER = 3;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * SKU ID
     */
    private Long materialId;

    /**
     * 编码/条码
     */
    private String code;

    /**
     * 类型(1=主编号,2=条码,3=其他)
     */
    private Integer codeType;

    /**
     * 是否主码(0=否,1=是)
     */
    private Integer isMain;

    /**
     * 备注
     */
    private String remark;

    /**
     * SKU 名称(非数据库字段)
     */
    @TableField(exist = false)
    private String materialName;

    /**
     * SKU 编号(非数据库字段)
     */
    @TableField(exist = false)
    private String materialCode;
}

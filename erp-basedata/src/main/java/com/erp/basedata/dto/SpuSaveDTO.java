package com.erp.basedata.dto;

import lombok.Data;

import java.util.List;

/**
 * SPU 保存参数（含 SKU 矩阵生成所需的属性勾选）
 *
 * @author ERP System
 * @since 2026-10-03
 */
@Data
public class SpuSaveDTO {

    /**
     * SPU ID（更新时必填）
     */
    private Long id;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 品牌ID
     */
    private Long brandId;

    /**
     * 基本计量单位ID
     */
    private Long unitId;

    /**
     * SPU编号（留空则自动生成）
     */
    private String code;

    /**
     * 商品名称
     */
    private String name;

    /**
     * 商品简称
     */
    private String shortName;

    /**
     * 主体规格描述
     */
    private String specification;

    /**
     * 商品主图
     */
    private String mainImage;

    /**
     * 状态(0=禁用,1=启用)
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 勾选的属性组及各组选中的属性值。
     *
     * <p>后端按各组取值做<b>笛卡尔积</b>生成 SKU 矩阵，例如
     * {颜色:[红,蓝]} × {尺码:[M,L]} ⇒ 4 个 SKU。</p>
     */
    private List<PropertySelection> selections;

    /**
     * 单个属性组的勾选结果
     */
    @Data
    public static class PropertySelection {

        /**
         * 属性组ID
         */
        private Long propertyId;

        /**
         * 该属性组下选中的属性值ID列表
         */
        private List<Long> itemIds;
    }
}

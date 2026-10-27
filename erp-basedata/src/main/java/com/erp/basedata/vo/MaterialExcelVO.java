package com.erp.basedata.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 物料 Excel 导入导出模型
 *
 * <p>同一个类既用于导出（填充 {@code code} 等全部字段），也用于导入
 * （{@code code} 留空则自动生成，分类/单位按<strong>名称</strong>匹配）。</p>
 *
 * @author ERP System
 * @since 2026-10-03
 */
@Data
public class MaterialExcelVO {

    @ExcelProperty("物料编码")
    @ColumnWidth(16)
    private String code;

    @ExcelProperty("物料名称")
    @ColumnWidth(22)
    private String name;

    @ExcelProperty("简称")
    @ColumnWidth(14)
    private String shortName;

    @ExcelProperty("规格")
    @ColumnWidth(20)
    private String specification;

    @ExcelProperty("分类")
    @ColumnWidth(14)
    private String categoryName;

    @ExcelProperty("计量单位")
    @ColumnWidth(12)
    private String unitName;

    @ExcelProperty("采购价")
    @ColumnWidth(12)
    private BigDecimal purchasePrice;

    @ExcelProperty("销售价")
    @ColumnWidth(12)
    private BigDecimal salePrice;

    @ExcelProperty("零售价")
    @ColumnWidth(12)
    private BigDecimal retailPrice;

    @ExcelProperty("最低库存")
    @ColumnWidth(12)
    private BigDecimal minStock;

    @ExcelProperty("最高库存")
    @ColumnWidth(12)
    private BigDecimal maxStock;

    @ExcelProperty("状态")
    @ColumnWidth(10)
    private String statusText;

    @ExcelProperty("备注")
    @ColumnWidth(20)
    private String remark;
}

package com.erp.basedata.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

/**
 * 客户 Excel 导入导出模型
 *
 * <p>{@code code} 留空则自动生成。{@code type}/{@code level}/{@code settlementMethod}
 * 沿用系统内的数字取值（不做文本映射，避免与页面语义不一致）；{@code status} 用中文，
 * 导入时同时接受「启用/禁用」与「1/0」。</p>
 *
 * @author ERP System
 * @since 2026-10-03
 */
@Data
public class CustomerExcelVO {

    @ExcelProperty("客户编号")
    @ColumnWidth(16)
    private String code;

    @ExcelProperty("客户名称")
    @ColumnWidth(28)
    private String name;

    @ExcelProperty("简称")
    @ColumnWidth(14)
    private String shortName;

    @ExcelProperty("类型")
    @ColumnWidth(10)
    private Integer type;

    @ExcelProperty("级别")
    @ColumnWidth(10)
    private Integer level;

    @ExcelProperty("联系人")
    @ColumnWidth(12)
    private String contactPerson;

    @ExcelProperty("联系电话")
    @ColumnWidth(18)
    private String contactPhone;

    @ExcelProperty("邮箱")
    @ColumnWidth(22)
    private String contactEmail;

    @ExcelProperty("地址")
    @ColumnWidth(30)
    private String address;

    @ExcelProperty("结算方式")
    @ColumnWidth(12)
    private Integer settlementMethod;

    @ExcelProperty("账期(天)")
    @ColumnWidth(10)
    private Integer paymentDays;

    @ExcelProperty("状态")
    @ColumnWidth(10)
    private String statusText;

    @ExcelProperty("备注")
    @ColumnWidth(20)
    private String remark;
}

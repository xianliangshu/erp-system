package com.erp.basedata.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

/**
 * 供应商 Excel 导入导出模型
 *
 * <p>字段取值约定同 {@link CustomerExcelVO}：枚举用系统内数字，状态用中文。</p>
 *
 * @author ERP System
 * @since 2026-10-03
 */
@Data
public class SupplierExcelVO {

    @ExcelProperty("供应商编号")
    @ColumnWidth(16)
    private String code;

    @ExcelProperty("供应商名称")
    @ColumnWidth(28)
    private String name;

    @ExcelProperty("简称")
    @ColumnWidth(14)
    private String shortName;

    @ExcelProperty("类型")
    @ColumnWidth(10)
    private Integer type;

    @ExcelProperty("信用等级")
    @ColumnWidth(10)
    private Integer creditLevel;

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

    @ExcelProperty("开户行")
    @ColumnWidth(20)
    private String bankName;

    @ExcelProperty("银行账号")
    @ColumnWidth(22)
    private String bankAccount;

    @ExcelProperty("税号")
    @ColumnWidth(22)
    private String taxNumber;

    @ExcelProperty("状态")
    @ColumnWidth(10)
    private String statusText;

    @ExcelProperty("备注")
    @ColumnWidth(20)
    private String remark;
}

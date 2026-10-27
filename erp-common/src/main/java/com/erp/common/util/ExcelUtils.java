package com.erp.common.util;

import com.alibaba.excel.EasyExcel;
import com.erp.common.exception.BusinessException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Excel 导入导出工具（注解驱动）
 *
 * <p>导出模型只需在字段上标注 {@code @ExcelProperty("列名")}，无需逐列写代码：
 * <pre>
 * public class MaterialExportVO {
 *     &#64;ExcelProperty("物料编码") private String code;
 *     &#64;ExcelProperty("物料名称") private String name;
 * }
 * </pre>
 * 模板下载与导出共用 {@link #export}：传入空列表就只输出表头。</p>
 *
 * @author ERP System
 * @since 2026-10-03
 */
@Slf4j
public final class ExcelUtils {

    private static final String XLSX_CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private ExcelUtils() {
    }

    /**
     * 把列表写入 HTTP 响应流，浏览器直接下载。
     *
     * @param response  响应对象
     * @param fileName  文件名（不含扩展名，中文会被正确编码）
     * @param sheetName sheet 名称
     * @param clazz     导出模型类（字段上是 {@code @ExcelProperty}）
     * @param data      数据；传空列表即生成「只有表头」的导入模板
     */
    public static <T> void export(HttpServletResponse response, String fileName, String sheetName,
                                  Class<T> clazz, List<T> data) {
        try {
            response.setContentType(XLSX_CONTENT_TYPE);
            response.setCharacterEncoding("utf-8");
            // filename*=utf-8''xxx 可正确传递中文文件名；URLEncoder 会把空格编成 +，需还原
            String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
            response.setHeader("Content-Disposition", "attachment;filename*=utf-8''" + encoded + ".xlsx");
            EasyExcel.write(response.getOutputStream(), clazz).sheet(sheetName).doWrite(data);
        } catch (IOException e) {
            log.error("Excel 导出失败: {}", fileName, e);
            throw new BusinessException("Excel 导出失败: " + e.getMessage());
        }
    }

    /**
     * 解析上传的 Excel 为对象列表（同步读取，适合中小批量导入）
     *
     * @param in    上传文件流
     * @param clazz 导入模型类
     * @return 解析结果；格式错误会抛 BusinessException
     */
    public static <T> List<T> read(InputStream in, Class<T> clazz) {
        try {
            return EasyExcel.read(in).head(clazz).sheet().doReadSync();
        } catch (Exception e) {
            log.error("Excel 解析失败", e);
            throw new BusinessException("Excel 解析失败，请确认使用了正确的导入模板: " + e.getMessage());
        }
    }

    /**
     * 状态值转中文（导出用）
     *
     * @param status 1-启用 0-禁用，null 视为启用
     * @return 中文文本
     */
    public static String statusText(Integer status) {
        return Integer.valueOf(0).equals(status) ? "禁用" : "启用";
    }

    /**
     * 中文/数字状态转状态值（导入用）
     *
     * <p>同时接受「启用 / 禁用」与「1 / 0」两种写法，留空时返回默认值 ——
     * 用户从导出文件里改数据再传回来，或直接写数字，都能通过。</p>
     *
     * @param text         单元格文本
     * @param defaultValue 留空或无法识别时的默认值
     * @return 1 或 0
     */
    public static Integer parseStatus(String text, Integer defaultValue) {
        if (text == null) {
            return defaultValue;
        }
        String t = text.trim();
        if (t.isEmpty()) {
            return defaultValue;
        }
        if ("1".equals(t) || "启用".equals(t) || "是".equals(t)) {
            return 1;
        }
        if ("0".equals(t) || "禁用".equals(t) || "否".equals(t)) {
            return 0;
        }
        return defaultValue;
    }
}

package com.erp.basedata.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.basedata.dto.SupplierPageParam;
import com.erp.basedata.entity.BaseSupplier;
import com.erp.basedata.vo.SupplierExcelVO;
import com.erp.common.core.vo.ExcelImportResult;

import java.io.InputStream;
import java.util.List;

/**
 * 供应商信息Service接口
 *
 * @author ERP System
 * @since 2025-12-16
 */
public interface IBaseSupplierService extends IService<BaseSupplier> {

    /**
     * 分页查询供应商
     *
     * @param param 查询参数
     * @return 分页结果
     */
    Page<BaseSupplier> getSupplierPage(SupplierPageParam param);

    /**
     * 获取所有启用的供应商
     *
     * @return 供应商列表
     */
    List<BaseSupplier> getAllEnabledSuppliers();

    /**
     * 根据ID获取供应商
     *
     * @param id 供应商ID
     * @return 供应商信息
     */
    BaseSupplier getById(Long id);

    /**
     * 保存供应商
     *
     * @param supplier 供应商信息
     * @return 是否成功
     */
    boolean saveSupplier(BaseSupplier supplier);

    /**
     * 更新供应商
     *
     * @param supplier 供应商信息
     * @return 是否成功
     */
    boolean updateSupplier(BaseSupplier supplier);

    /**
     * 删除供应商
     *
     * @param id 供应商ID
     * @return 是否成功
     */
    boolean deleteSupplier(Long id);

    /**
     * 查询需要导出的供应商（按与列表页相同的筛选条件，但<b>不分页</b>）
     *
     * @param param 查询参数（只取筛选条件，忽略分页字段）
     * @return Excel 行数据，按排序号、ID 升序
     */
    List<SupplierExcelVO> listForExport(SupplierPageParam param);

    /**
     * Excel 批量导入供应商
     *
     * <p>策略是<b>逐行校验、一行不合法则整体不落库</b>。校验规则：名称为必填且不可重复；
     * 编号留空则自动生成，填了则要求唯一；类型/信用等级/结算方式沿用系统内数字取值；
     * 状态接受「启用/禁用」或「1/0」。</p>
     *
     * @param in 上传文件流
     * @return 导入结果（含总行数、成功数、失败数、带行号的错误明细）
     */
    ExcelImportResult importExcel(InputStream in);
}

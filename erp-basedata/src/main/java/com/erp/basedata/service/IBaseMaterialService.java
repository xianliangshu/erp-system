package com.erp.basedata.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.basedata.dto.MaterialPageParam;
import com.erp.basedata.entity.BaseMaterial;
import com.erp.basedata.vo.MaterialExcelVO;
import com.erp.common.core.vo.ExcelImportResult;

import java.io.InputStream;
import java.util.List;

/**
 * 物料信息Service接口
 *
 * @author ERP System
 * @since 2025-12-14
 */
public interface IBaseMaterialService {

    /**
     * 分页查询物料
     *
     * @param param 查询参数
     * @return 分页结果
     */
    Page<BaseMaterial> getMaterialPage(MaterialPageParam param);

    /**
     * 根据ID获取物料
     *
     * @param id 物料ID
     * @return 物料信息
     */
    BaseMaterial getById(Long id);

    /**
     * 根据分类ID获取物料列表
     *
     * @param categoryId 分类ID
     * @return 物料列表
     */
    List<BaseMaterial> getByCategory(Long categoryId);

    /**
     * 获取所有物料列表（下拉选择用）
     *
     * @return 物料列表
     */
    List<BaseMaterial> getAll();

    /**
     * 新增物料
     *
     * @param material 物料信息
     * @return 是否成功
     */
    boolean saveMaterial(BaseMaterial material);

    /**
     * 更新物料
     *
     * @param material 物料信息
     * @return 是否成功
     */
    boolean updateMaterial(BaseMaterial material);

    /**
     * 删除物料
     *
     * @param id 物料ID
     * @return 是否成功
     */
    boolean deleteMaterial(Long id);

    /**
     * 查询需要导出的物料（按与列表页相同的筛选条件，但<b>不分页</b>）
     *
     * <p>返回的是 Excel 模型而非实体：分类与计量单位导出为<b>名称</b>，
     * 状态导出为「启用/禁用」，这样导出文件可以直接当导入模板改完再传回来。</p>
     *
     * @param param 查询参数（只取筛选条件，忽略分页字段）
     * @return Excel 行数据，按编码升序
     */
    List<MaterialExcelVO> listForExport(MaterialPageParam param);

    /**
     * Excel 批量导入物料
     *
     * <p>策略是<b>逐行校验、一行不合法则整体不落库</b>：先全部校验并收集错误行，
     * 只有零错误时才统一写入。避免出现「导入了一半」的脏数据。</p>
     *
     * <p>校验规则：名称为必填且不可与库内/文件内重复；分类与计量单位必填，
     * 按<b>名称</b>匹配已有档案；编码留空则由编号规则自动生成，填了则要求唯一。</p>
     *
     * @param in 上传文件流
     * @return 导入结果（含总行数、成功数、失败数、带行号的错误明细）
     */
    ExcelImportResult importExcel(InputStream in);
}

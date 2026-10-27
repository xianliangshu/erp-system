import request from '@/utils/request';
import { downloadFile } from '@/utils/download';

// 物料信息API

/**
 * 分页查询物料
 */
export const getMaterialPage = (params: any) => {
    return request.get('/basedata/material/page', { params });
};

/**
 * 根据ID获取物料
 */
export const getMaterialById = (id: number) => {
    return request.get(`/basedata/material/${id}`);
};

/**
 * 获取所有物料列表（下拉选择用）
 */
export const getMaterialList = () => {
    return request.get('/basedata/material/list');
};

/**
 * 根据分类ID获取物料列表
 */
export const getMaterialByCategory = (categoryId: number) => {
    return request.get(`/basedata/material/by-category/${categoryId}`);
};

/**
 * 新增物料
 */
export const saveMaterial = (data: any) => {
    return request.post('/basedata/material', data);
};

/**
 * 更新物料
 */
export const updateMaterial = (id: number, data: any) => {
    return request.put(`/basedata/material/${id}`, data);
};

/**
 * 删除物料
 */
export const deleteMaterial = (id: number) => {
    return request.delete(`/basedata/material/${id}`);
};

/**
 * 导出物料（按当前筛选条件，不分页）。直接触发浏览器下载，无返回值
 */
export const exportMaterial = (params: any) => {
    return downloadFile('/basedata/material/export', params, '物料列表.xlsx');
};

/**
 * 下载物料导入模板（只有表头）
 */
export const downloadMaterialTemplate = () => {
    return downloadFile('/basedata/material/template', undefined, '物料导入模板.xlsx');
};

/**
 * 批量导入物料
 *
 * 返回 ExcelImportResult：{ total, success, failed, errors }
 * 任一行校验失败则整体不落库，errors 里带 Excel 行号
 */
export const importMaterial = (file: File) => {
    const formData = new FormData();
    formData.append('file', file);
    return request.post('/basedata/material/import', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
    });
};

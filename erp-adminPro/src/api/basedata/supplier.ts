import request from '@/utils/request';
import { downloadFile } from '@/utils/download';

/**
 * 供应商管理API
 */

// 分页查询供应商
export const getSupplierPage = (params: any) => {
    return request({
        url: '/basedata/supplier/page',
        method: 'get',
        params
    });
};

// 获取所有启用的供应商
export const getSupplierList = () => {
    return request({
        url: '/basedata/supplier/list',
        method: 'get'
    });
};

// 根据ID获取供应商
export const getSupplierById = (id: number) => {
    return request({
        url: `/basedata/supplier/${id}`,
        method: 'get'
    });
};

// 新增供应商
export const saveSupplier = (data: any) => {
    return request({
        url: '/basedata/supplier',
        method: 'post',
        data
    });
};

// 更新供应商
export const updateSupplier = (id: number, data: any) => {
    return request({
        url: `/basedata/supplier/${id}`,
        method: 'put',
        data
    });
};

// 删除供应商
export const deleteSupplier = (id: number) => {
    return request({
        url: `/basedata/supplier/${id}`,
        method: 'delete'
    });
};

// 导出供应商（按当前筛选条件，不分页）
export const exportSupplier = (params: any) => {
    return downloadFile('/basedata/supplier/export', params, '供应商列表.xlsx');
};

// 下载供应商导入模板
export const downloadSupplierTemplate = () => {
    return downloadFile('/basedata/supplier/template', undefined, '供应商导入模板.xlsx');
};

// 批量导入供应商，返回 { total, success, failed, errors }
export const importSupplier = (file: File) => {
    const formData = new FormData();
    formData.append('file', file);
    return request({
        url: '/basedata/supplier/import',
        method: 'post',
        data: formData,
        headers: { 'Content-Type': 'multipart/form-data' }
    });
};

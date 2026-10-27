import request from '@/utils/request';
import { downloadFile } from '@/utils/download';
import { PageResult } from '@/types';

/**
 * 客户管理API
 */

// 分页查询客户
export const getCustomerPage = (params: any): Promise<PageResult<any>> => {
    return request({
        url: '/basedata/customer/page',
        method: 'get',
        params
    });
};

// 获取所有启用的客户
export const getCustomerList = () => {
    return request({
        url: '/basedata/customer/list',
        method: 'get'
    });
};

// 根据ID获取客户
export const getCustomerById = (id: number) => {
    return request({
        url: `/basedata/customer/${id}`,
        method: 'get'
    });
};

// 新增客户
export const saveCustomer = (data: any) => {
    return request({
        url: '/basedata/customer',
        method: 'post',
        data
    });
};

// 更新客户
export const updateCustomer = (id: number, data: any) => {
    return request({
        url: `/basedata/customer/${id}`,
        method: 'put',
        data
    });
};

// 删除客户
export const deleteCustomer = (id: number) => {
    return request({
        url: `/basedata/customer/${id}`,
        method: 'delete'
    });
};

// 导出客户（按当前筛选条件，不分页）
export const exportCustomer = (params: any) => {
    return downloadFile('/basedata/customer/export', params, '客户列表.xlsx');
};

// 下载客户导入模板
export const downloadCustomerTemplate = () => {
    return downloadFile('/basedata/customer/template', undefined, '客户导入模板.xlsx');
};

// 批量导入客户，返回 { total, success, failed, errors }
export const importCustomer = (file: File) => {
    const formData = new FormData();
    formData.append('file', file);
    return request({
        url: '/basedata/customer/import',
        method: 'post',
        data: formData,
        headers: { 'Content-Type': 'multipart/form-data' }
    });
};

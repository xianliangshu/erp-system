import request from '@/utils/request';

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

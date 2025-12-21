import request from '@/utils/request';
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

import request from '@/utils/request';
import { PageResult } from '@/types';

/**
 * 销售订单API
 */

// 分页查询销售订单
export const getSaleOrderPage = (params: any): Promise<PageResult<any>> => {
    return request({
        url: '/business/sale/order/page',
        method: 'get',
        params
    });
};

// 获取订单详情（包含明细）
export const getSaleOrderById = (id: number) => {
    return request({
        url: `/business/sale/order/${id}`,
        method: 'get'
    });
};

// 创建销售订单
export const createSaleOrder = (data: any) => {
    return request({
        url: '/business/sale/order',
        method: 'post',
        data
    });
};

// 更新销售订单
export const updateSaleOrder = (data: any) => {
    return request({
        url: '/business/sale/order',
        method: 'put',
        data
    });
};

// 审核通过
export const approveSaleOrder = (id: number) => {
    return request({
        url: `/business/sale/order/${id}/approve`,
        method: 'post'
    });
};

// 审核拒绝
export const rejectSaleOrder = (id: number, reason: string) => {
    return request({
        url: `/business/sale/order/${id}/reject`,
        method: 'post',
        params: { reason }
    });
};

// 取消订单
export const cancelSaleOrder = (id: number) => {
    return request({
        url: `/business/sale/order/${id}/cancel`,
        method: 'post'
    });
};

// 删除订单
export const deleteSaleOrder = (id: number) => {
    return request({
        url: `/business/sale/order/${id}`,
        method: 'delete'
    });
};

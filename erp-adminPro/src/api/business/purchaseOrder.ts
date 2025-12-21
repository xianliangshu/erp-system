import request from '@/utils/request';
import { PageResult } from '@/types';

/**
 * 采购订单API
 */

// 分页查询采购订单
export const getPurchaseOrderPage = (params: any): Promise<PageResult<any>> => {
    return request({
        url: '/business/purchase/order/page',
        method: 'get',
        params
    });
};

// 获取订单详情（包含明细）
export const getPurchaseOrderById = (id: number) => {
    return request({
        url: `/business/purchase/order/${id}`,
        method: 'get'
    });
};

// 创建采购订单
export const createPurchaseOrder = (data: any) => {
    return request({
        url: '/business/purchase/order',
        method: 'post',
        data
    });
};

// 更新采购订单
export const updatePurchaseOrder = (data: any) => {
    return request({
        url: '/business/purchase/order',
        method: 'put',
        data
    });
};

// 审核通过
export const approvePurchaseOrder = (id: number) => {
    return request({
        url: `/business/purchase/order/${id}/approve`,
        method: 'post'
    });
};

// 审核拒绝
export const rejectPurchaseOrder = (id: number, reason: string) => {
    return request({
        url: `/business/purchase/order/${id}/reject`,
        method: 'post',
        params: { reason }
    });
};

// 取消订单
export const cancelPurchaseOrder = (id: number) => {
    return request({
        url: `/business/purchase/order/${id}/cancel`,
        method: 'post'
    });
};

// 删除订单
export const deletePurchaseOrder = (id: number) => {
    return request({
        url: `/business/purchase/order/${id}`,
        method: 'delete'
    });
};

import request from '@/utils/request';
import { PageResult } from '@/types';

/**
 * 采购收货API
 */

// 分页查询采购收货
export const getPurchaseReceiptPage = (params: any): Promise<PageResult<any>> => {
    return request({
        url: '/business/purchase/receipt/page',
        method: 'get',
        params
    });
};

// 获取收货单详情（包含明细）
export const getPurchaseReceiptById = (id: number) => {
    return request({
        url: `/business/purchase/receipt/${id}`,
        method: 'get'
    });
};

// 创建采购收货
export const createPurchaseReceipt = (data: any) => {
    return request({
        url: '/business/purchase/receipt',
        method: 'post',
        data
    });
};

// 更新采购收货
export const updatePurchaseReceipt = (data: any) => {
    return request({
        url: '/business/purchase/receipt',
        method: 'put',
        data
    });
};

// 确认收货
export const confirmPurchaseReceipt = (id: number) => {
    return request({
        url: `/business/purchase/receipt/${id}/confirm`,
        method: 'post'
    });
};

// 删除收货单
export const deletePurchaseReceipt = (id: number) => {
    return request({
        url: `/business/purchase/receipt/${id}`,
        method: 'delete'
    });
};

// 获取待收货的采购订单
export const getPendingReceiveOrders = (params: any) => {
    return request({
        url: '/business/purchase/receipt/pending-orders',
        method: 'get',
        params
    });
};

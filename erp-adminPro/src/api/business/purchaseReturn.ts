import request from '@/utils/request';
import { PageResult } from '@/types';

/**
 * 采购退货API
 */

// 分页查询采购退货
export const getPurchaseReturnPage = (params: any): Promise<PageResult<any>> => {
    return request({
        url: '/business/purchase/return/page',
        method: 'get',
        params
    });
};

// 获取退货单详情（包含明细）
export const getPurchaseReturnById = (id: number) => {
    return request({
        url: `/business/purchase/return/${id}`,
        method: 'get'
    });
};

// 创建采购退货
export const createPurchaseReturn = (data: any) => {
    return request({
        url: '/business/purchase/return',
        method: 'post',
        data
    });
};

// 更新采购退货
export const updatePurchaseReturn = (data: any) => {
    return request({
        url: '/business/purchase/return',
        method: 'put',
        data
    });
};

// 确认退货
export const confirmPurchaseReturn = (id: number) => {
    return request({
        url: `/business/purchase/return/${id}/confirm`,
        method: 'post'
    });
};

// 删除退货单
export const deletePurchaseReturn = (id: number) => {
    return request({
        url: `/business/purchase/return/${id}`,
        method: 'delete'
    });
};

// 获取可退货的收货单列表
export const getPendingReturnReceipts = (params: any) => {
    return request({
        url: '/business/purchase/return/pending-receipts',
        method: 'get',
        params
    });
};

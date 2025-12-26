import request from '@/utils/request';
import { PageResult } from '@/types';

// 分页查询销售出库
export const getSaleDeliveryPage = (params: any): Promise<PageResult<any>> => {
    return request({ url: '/business/sale/delivery/page', method: 'get', params });
};

// 获取出库单详情（包含明细）
export const getSaleDeliveryById = (id: number) => {
    return request({ url: `/business/sale/delivery/${id}`, method: 'get' });
};

// 创建销售出库
export const createSaleDelivery = (data: any) => {
    return request({ url: '/business/sale/delivery', method: 'post', data });
};

// 更新销售出库
export const updateSaleDelivery = (data: any) => {
    return request({ url: '/business/sale/delivery', method: 'put', data });
};

// 确认出库
export const confirmSaleDelivery = (id: number) => {
    return request({ url: `/business/sale/delivery/${id}/confirm`, method: 'post' });
};

// 删除出库单
export const deleteSaleDelivery = (id: number) => {
    return request({ url: `/business/sale/delivery/${id}`, method: 'delete' });
};

// 获取待出库的销售订单列表
export const getPendingDeliveryOrders = (params: any) => {
    return request({ url: '/business/sale/delivery/pending-orders', method: 'get', params });
};

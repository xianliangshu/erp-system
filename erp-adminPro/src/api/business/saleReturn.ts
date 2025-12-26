import request from '@/utils/request';
import { PageResult } from '@/types';

export const getSaleReturnPage = (params: any): Promise<PageResult<any>> => {
    return request({ url: '/business/sale/return/page', method: 'get', params });
};

export const getSaleReturnById = (id: number) => {
    return request({ url: `/business/sale/return/${id}`, method: 'get' });
};

export const createSaleReturn = (data: any) => {
    return request({ url: '/business/sale/return', method: 'post', data });
};

export const updateSaleReturn = (data: any) => {
    return request({ url: '/business/sale/return', method: 'put', data });
};

export const confirmSaleReturn = (id: number) => {
    return request({ url: `/business/sale/return/${id}/confirm`, method: 'post' });
};

export const deleteSaleReturn = (id: number) => {
    return request({ url: `/business/sale/return/${id}`, method: 'delete' });
};

export const getPendingReturnDeliveries = (params: any) => {
    return request({ url: '/business/sale/return/pending-deliveries', method: 'get', params });
};

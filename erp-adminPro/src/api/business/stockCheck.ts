import request from '@/utils/request';
import { PageResult } from '@/types';

export const getStockCheckPage = (params: any): Promise<PageResult<any>> => {
    return request({ url: '/business/stock/check/page', method: 'get', params });
};

export const getStockCheckById = (id: number) => {
    return request({ url: `/business/stock/check/${id}`, method: 'get' });
};

export const createStockCheck = (data: any) => {
    return request({ url: '/business/stock/check', method: 'post', data });
};

export const updateStockCheck = (data: any) => {
    return request({ url: '/business/stock/check', method: 'put', data });
};

export const approveStockCheck = (id: number) => {
    return request({ url: `/business/stock/check/${id}/approve`, method: 'post' });
};

export const deleteStockCheck = (id: number) => {
    return request({ url: `/business/stock/check/${id}`, method: 'delete' });
};

import request from '@/utils/request';
import { PageResult } from '@/types';

/**
 * 库存管理API
 */

// 分页查询商品库存
export const getProductStockPage = (params: any): Promise<PageResult<any>> => {
    return request({
        url: '/business/stock/page',
        method: 'get',
        params
    });
};

// 分页查询库存日志
export const getProductStockLogPage = (params: any): Promise<PageResult<any>> => {
    return request({
        url: '/business/stock/log/page',
        method: 'get',
        params
    });
};

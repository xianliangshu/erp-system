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

// 注意：库存流水分页查询见 '@/api/business/stockLog'（getProductStockLogPage）

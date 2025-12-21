import request from '@/utils/request';
import { PageResult } from '@/types';

/**
 * 品牌管理API
 */

// 分页查询品牌
export const getBrandPage = (params: any): Promise<PageResult<any>> => {
    return request({
        url: '/basedata/brand/page',
        method: 'get',
        params
    });
};

// 获取所有启用的品牌
export const getBrandList = () => {
    return request({
        url: '/basedata/brand/list',
        method: 'get'
    });
};

// 根据ID获取品牌
export const getBrandById = (id: number) => {
    return request({
        url: `/basedata/brand/${id}`,
        method: 'get'
    });
};

// 新增品牌
export const saveBrand = (data: any) => {
    return request({
        url: '/basedata/brand',
        method: 'post',
        data
    });
};

// 更新品牌
export const updateBrand = (id: number, data: any) => {
    return request({
        url: `/basedata/brand/${id}`,
        method: 'put',
        data
    });
};

// 删除品牌
export const deleteBrand = (id: number) => {
    return request({
        url: `/basedata/brand/${id}`,
        method: 'delete'
    });
};

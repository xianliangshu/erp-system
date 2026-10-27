import request from '@/utils/request';

/**
 * 商品销售属性 API
 */

// 分页查询属性组（含属性值）
export const getSalePropertyPage = (params: any) => {
    return request({
        url: '/basedata/sale-property/page',
        method: 'get',
        params
    });
};

// 获取所有启用的属性组（含属性值，勾选面板用）
export const getSalePropertyList = () => {
    return request({
        url: '/basedata/sale-property/list',
        method: 'get'
    });
};

// 查询某属性组下的属性值
export const getSalePropertyItems = (id: number) => {
    return request({
        url: `/basedata/sale-property/${id}/items`,
        method: 'get'
    });
};

// 新增属性组（items 一并写入）
export const saveSaleProperty = (data: any) => {
    return request({
        url: '/basedata/sale-property',
        method: 'post',
        data
    });
};

// 更新属性组（属性值差异更新，ID 保持稳定）
export const updateSaleProperty = (id: number, data: any) => {
    return request({
        url: `/basedata/sale-property/${id}`,
        method: 'put',
        data
    });
};

// 删除属性组
export const deleteSaleProperty = (id: number) => {
    return request({
        url: `/basedata/sale-property/${id}`,
        method: 'delete'
    });
};

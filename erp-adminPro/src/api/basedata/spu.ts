import request from '@/utils/request';

/**
 * 商品 SPU API
 */

// 分页查询 SPU
export const getSpuPage = (params: any) => {
    return request({
        url: '/basedata/spu/page',
        method: 'get',
        params
    });
};

// 获取所有启用的 SPU（下拉）
export const getSpuList = () => {
    return request({
        url: '/basedata/spu/list',
        method: 'get'
    });
};

// 根据ID获取 SPU 详情（含已绑定属性组）
export const getSpuById = (id: number) => {
    return request({
        url: `/basedata/spu/${id}`,
        method: 'get'
    });
};

// 查询某 SPU 下的 SKU 列表
export const getSpuSkus = (id: number) => {
    return request({
        url: `/basedata/spu/${id}/skus`,
        method: 'get'
    });
};

// 新增 SPU（selection 决定生成哪些 SKU）
export const saveSpu = (data: any) => {
    return request({
        url: '/basedata/spu',
        method: 'post',
        data
    });
};

// 更新 SPU（属性组合只增不删）
export const updateSpu = (id: number, data: any) => {
    return request({
        url: `/basedata/spu/${id}`,
        method: 'put',
        data
    });
};

// 删除 SPU
export const deleteSpu = (id: number) => {
    return request({
        url: `/basedata/spu/${id}`,
        method: 'delete'
    });
};

// 仅补生成 SKU 矩阵（不新增 SPU）
export const generateSkus = (id: number, data: any) => {
    return request({
        url: `/basedata/spu/${id}/generate-skus`,
        method: 'post',
        data
    });
};

import request from '@/utils/request';

// 物料信息API

/**
 * 分页查询物料
 */
export const getMaterialPage = (params: any) => {
    return request.get('/basedata/material/page', { params });
};

/**
 * 根据ID获取物料
 */
export const getMaterialById = (id: number) => {
    return request.get(`/basedata/material/${id}`);
};

/**
 * 根据分类ID获取物料列表
 */
export const getMaterialByCategory = (categoryId: number) => {
    return request.get(`/basedata/material/by-category/${categoryId}`);
};

/**
 * 新增物料
 */
export const saveMaterial = (data: any) => {
    return request.post('/basedata/material', data);
};

/**
 * 更新物料
 */
export const updateMaterial = (id: number, data: any) => {
    return request.put(`/basedata/material/${id}`, data);
};

/**
 * 删除物料
 */
export const deleteMaterial = (id: number) => {
    return request.delete(`/basedata/material/${id}`);
};

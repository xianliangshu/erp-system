import request from '@/utils/request';

// 计量单位API

/**
 * 分页查询单位
 */
export const getUnitPage = (params: any) => {
    return request.get('/basedata/unit/page', { params });
};

/**
 * 获取所有启用的单位
 */
export const getUnitList = () => {
    return request.get('/basedata/unit/list');
};

/**
 * 根据ID获取单位
 */
export const getUnitById = (id: number) => {
    return request.get(`/basedata/unit/${id}`);
};

/**
 * 新增单位
 */
export const saveUnit = (data: any) => {
    return request.post('/basedata/unit', data);
};

/**
 * 更新单位
 */
export const updateUnit = (id: number, data: any) => {
    return request.put(`/basedata/unit/${id}`, data);
};

/**
 * 删除单位
 */
export const deleteUnit = (id: number) => {
    return request.delete(`/basedata/unit/${id}`);
};

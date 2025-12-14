import request from '@/utils/request';

// 物料分类API

/**
 * 获取分类树
 */
export const getCategoryTree = () => {
    return request.get('/api/basedata/material-category/tree');
};

/**
 * 根据ID获取分类
 */
export const getCategoryById = (id: number) => {
    return request.get(`/api/basedata/material-category/${id}`);
};

/**
 * 新增分类
 */
export const saveCategory = (data: any) => {
    return request.post('/api/basedata/material-category', data);
};

/**
 * 更新分类
 */
export const updateCategory = (id: number, data: any) => {
    return request.put(`/api/basedata/material-category/${id}`, data);
};

/**
 * 删除分类
 */
export const deleteCategory = (id: number) => {
    return request.delete(`/api/basedata/material-category/${id}`);
};

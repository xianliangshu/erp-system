import request from '@/utils/request';

/**
 * 库存预警 API
 */

// 分页查询预警配置
export function getStockWarningPage(params: {
    current?: number;
    size?: number;
    scId?: number;
    productName?: string;
    status?: number;
}) {
    return request.get('/stock/warning/page', { params });
}

// 实时命中查询（当前已低于下限 / 高于上限的商品）
export function getStockWarningHitList(params?: { scId?: number; productName?: string }) {
    return request.get('/stock/warning/hit-list', { params });
}

// 根据ID查询
export function getStockWarningById(id: number) {
    return request.get(`/stock/warning/${id}`);
}

// 新增
export function addStockWarning(data: any) {
    return request.post('/stock/warning', data);
}

// 修改
export function updateStockWarning(data: any) {
    return request.put('/stock/warning', data);
}

// 删除
export function deleteStockWarning(id: number) {
    return request.delete(`/stock/warning/${id}`);
}

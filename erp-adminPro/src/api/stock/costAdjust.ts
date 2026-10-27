import request from '@/utils/request';

/**
 * 库存成本调整单 API
 */

// 分页查询
export function getCostAdjustPage(params: {
    current?: number;
    size?: number;
    code?: string;
    scId?: number;
    status?: number;
}) {
    return request.get('/stock/adjust/cost/page', { params });
}

// 查询某商品在某仓库的当前库存与成本价（制单时展示用）
export function getCostAdjustStockInfo(scId: number, productId: number) {
    return request.get('/stock/adjust/cost/stock-info', { params: { scId, productId } });
}

// 查询详情（含明细）
export function getCostAdjustById(id: number) {
    return request.get(`/stock/adjust/cost/${id}`);
}

// 新增
export function addCostAdjust(data: any) {
    return request.post('/stock/adjust/cost', data);
}

// 修改
export function updateCostAdjust(data: any) {
    return request.put('/stock/adjust/cost', data);
}

// 删除
export function deleteCostAdjust(id: number) {
    return request.delete(`/stock/adjust/cost/${id}`);
}

// 审核通过
export function approveCostAdjust(id: number) {
    return request.post(`/stock/adjust/cost/approve/${id}`);
}

// 审核拒绝
export function refuseCostAdjust(id: number, refuseReason: string) {
    return request.post(`/stock/adjust/cost/refuse/${id}`, { refuseReason });
}

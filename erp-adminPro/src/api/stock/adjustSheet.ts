import request from '@/utils/request';

/**
 * 库存调整单 API
 */

// 分页查询
export function getAdjustSheetPage(params: {
    current?: number;
    size?: number;
    code?: string;
    scId?: number;
    reasonId?: number;
    bizType?: number;
    status?: number;
}) {
    return request.get('/stock/adjust/sheet/page', { params });
}

// 根据ID查询详情（包含明细）
export function getAdjustSheetById(id: number) {
    return request.get(`/stock/adjust/sheet/${id}`);
}

// 新增
export function addAdjustSheet(data: {
    scId: number;
    reasonId: number;
    bizType: number;
    description?: string;
    details: Array<{
        productId: number;
        stockNum: number;
        description?: string;
    }>;
}) {
    return request.post('/stock/adjust/sheet', data);
}

// 修改
export function updateAdjustSheet(data: {
    id: number;
    scId: number;
    reasonId: number;
    bizType: number;
    description?: string;
    details: Array<{
        productId: number;
        stockNum: number;
        description?: string;
    }>;
}) {
    return request.put('/stock/adjust/sheet', data);
}

// 删除
export function deleteAdjustSheet(id: number) {
    return request.delete(`/stock/adjust/sheet/${id}`);
}

// 审核通过
export function approveAdjustSheet(id: number) {
    return request.post(`/stock/adjust/sheet/approve/${id}`);
}

// 审核拒绝
export function refuseAdjustSheet(id: number, refuseReason: string) {
    return request.post(`/stock/adjust/sheet/refuse/${id}`, { refuseReason });
}

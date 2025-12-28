import request from '@/utils/request';

/**
 * 库存调整原因 API
 */

// 分页查询
export function getAdjustReasonPage(params: {
    current?: number;
    size?: number;
    code?: string;
    name?: string;
    status?: number;
}) {
    return request.get('/stock/adjust/reason/page', { params });
}

// 查询所有启用的调整原因（下拉选择用）
export function getAdjustReasonList() {
    return request.get('/stock/adjust/reason/list');
}

// 根据ID查询
export function getAdjustReasonById(id: number) {
    return request.get(`/stock/adjust/reason/${id}`);
}

// 新增
export function addAdjustReason(data: {
    code?: string;
    name: string;
    status: number;
    remark?: string;
}) {
    return request.post('/stock/adjust/reason', data);
}

// 修改
export function updateAdjustReason(data: {
    id: number;
    code?: string;
    name: string;
    status: number;
    remark?: string;
}) {
    return request.put('/stock/adjust/reason', data);
}

// 删除
export function deleteAdjustReason(id: number) {
    return request.delete(`/stock/adjust/reason/${id}`);
}

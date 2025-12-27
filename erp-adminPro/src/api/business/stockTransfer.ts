import request from '@/utils/request';

// 分页查询调拨单
export function getStockTransferPage(params: {
    current?: number;
    size?: number;
    outScId?: number;
    inScId?: number;
    status?: number;
}) {
    return request.get('/business/stock/transfer/page', { params });
}

// 获取调拨单详情
export function getStockTransferById(id: number) {
    return request.get(`/business/stock/transfer/${id}`);
}

// 创建调拨单
export function createStockTransfer(data: any) {
    return request.post('/business/stock/transfer', data);
}

// 更新调拨单
export function updateStockTransfer(id: number, data: any) {
    return request.put(`/business/stock/transfer/${id}`, data);
}

// 确认调拨
export function confirmStockTransfer(id: number) {
    return request.post(`/business/stock/transfer/${id}/confirm`);
}

// 删除调拨单
export function deleteStockTransfer(id: number) {
    return request.delete(`/business/stock/transfer/${id}`);
}

import request from '@/utils/request';

// 仪表盘数据
export function getDashboard() {
    return request({ url: '/chart/dashboard', method: 'get' });
}

// 采购统计
export function getPurchaseStats(params?: { startDate?: string; endDate?: string }) {
    return request({ url: '/chart/purchase', method: 'get', params });
}

// 销售统计
export function getSalesStats(params?: { startDate?: string; endDate?: string }) {
    return request({ url: '/chart/sales', method: 'get', params });
}

// 库存报表
export function getStockReport(params?: { warehouseId?: number }) {
    return request({ url: '/chart/stock', method: 'get', params });
}

// 进销存汇总
export function getSummaryReport(params?: { startDate?: string; endDate?: string }) {
    return request({ url: '/chart/summary', method: 'get', params });
}

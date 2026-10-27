import request from '@/utils/request';

/**
 * 库存盘点参数 API（单行配置）
 */

// 获取盘点参数
export function getTakeStockConfig() {
    return request.get('/stock/take/config');
}

// 保存盘点参数
export function saveTakeStockConfig(data: any) {
    return request.put('/stock/take/config', data);
}

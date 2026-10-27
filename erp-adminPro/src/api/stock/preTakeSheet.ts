import request from '@/utils/request';

/**
 * 库存预先盘点单 API
 *
 * <p>预先盘点单是正式盘点之前的分次清点记录（初盘 / 复盘 / 抽盘，三个数量各自独立保存），
 * <b>不产生任何库存变动</b>。</p>
 */

// 分页查询
export function getPreTakePage(params: {
    current?: number;
    size?: number;
    code?: string;
    scId?: number;
    takeStatus?: number;
}) {
    return request.get('/stock/take/pre/page', { params });
}

// 查询详情（含明细）
export function getPreTakeById(id: number) {
    return request.get(`/stock/take/pre/${id}`);
}

// 下拉选项（供库存盘点单「关联预先盘点单」使用）
export function getPreTakeOptions() {
    return request.get('/stock/take/pre/options');
}

// 新增
export function addPreTake(data: any) {
    return request.post('/stock/take/pre', data);
}

// 修改（明细整体覆盖）
export function updatePreTake(data: any) {
    return request.put('/stock/take/pre', data);
}

// 删除
export function deletePreTake(id: number) {
    return request.delete(`/stock/take/pre/${id}`);
}

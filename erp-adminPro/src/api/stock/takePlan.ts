import request from '@/utils/request';

/**
 * 库存盘点任务 API
 *
 * <p>任务创建时按「全场/单品/分类/品牌」解析商品范围，并把当时的库存写成快照；
 * 录入盘点数量后生成差异 → 差异落成一张库存盘点单（走盘点单已有的审核流程）。</p>
 */

// 分页查询
export function getTakePlanPage(params: {
    current?: number;
    size?: number;
    code?: string;
    scId?: number;
    takeStatus?: number;
    takeType?: number;
}) {
    return request.get('/stock/take/plan/page', { params });
}

// 查询详情（含明细、商品信息、实时库存与差异）
export function getTakePlanById(id: number) {
    return request.get(`/stock/take/plan/${id}`);
}

// 未完成任务下拉（供盘点单「关联盘点任务」使用）
export function getTakePlanOptions() {
    return request.get('/stock/take/plan/options');
}

// 某任务的明细（供盘点单「关联任务后带出商品」使用）
export function getTakePlanDetails(id: number) {
    return request.get(`/stock/take/plan/${id}/details`);
}

// 创建盘点任务（后端会按盘点类别解析商品范围并写库存快照）
export function addTakePlan(data: any) {
    return request.post('/stock/take/plan', data);
}

// 录入盘点数量
export function saveTakeNums(planId: number, details: any[]) {
    return request.put('/stock/take/plan/take-nums', { planId, details });
}

// 生成差异 → 落成库存盘点单；返回盘点单ID（无差异时返回 null 且任务置为「盘点完成」）
export function generateTakeDiff(id: number) {
    return request.post(`/stock/take/plan/generate-diff/${id}`);
}

// 作废
export function cancelTakePlan(id: number) {
    return request.post(`/stock/take/plan/cancel/${id}`);
}

// 删除
export function deleteTakePlan(id: number) {
    return request.delete(`/stock/take/plan/${id}`);
}

import request from '@/utils/request'
import type { PageResult } from '@/types'

// ==================== 类型定义 ====================

export interface StockLog {
    id: number
    scId: number
    scName?: string
    productId: number
    productCode?: string
    productName?: string
    productSpec?: string
    unitName?: string
    /** 变动前库存 */
    oriStockNum: number
    /** 变动数量（正数入库 / 负数出库） */
    stockNum: number
    /** 变动后库存 */
    curStockNum: number
    bizId?: number
    bizCode?: string
    bizType?: number
    bizTypeDesc?: string
    createBy?: string
    createTime?: string
}

export interface StockLogPageParam {
    current?: number
    size?: number
    scId?: number
    productName?: string
    bizType?: number
    startDate?: string
    endDate?: string
}

export interface BizTypeOption {
    value: number
    label: string
}

// ==================== API方法 ====================

/**
 * 分页查询库存流水
 */
export const getProductStockLogPage = (params: StockLogPageParam): Promise<PageResult<StockLog>> => {
    return request.get('/business/stock/log/page', { params }) as unknown as Promise<PageResult<StockLog>>
}

/**
 * 获取库存业务类型字典（筛选下拉）
 */
export const getStockBizTypes = (): Promise<BizTypeOption[]> => {
    return request.get('/business/stock/log/biz-types') as unknown as Promise<BizTypeOption[]>
}

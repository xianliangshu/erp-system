import request from '@/utils/request'
import type { PageResult } from '@/types'

// ==================== 类型定义 ====================

export interface DictType {
    id: number
    dictName: string
    dictType: string
    status: number
    remark?: string
    createTime?: string
    updateTime?: string
}

export interface DictData {
    id: number
    dictSort: number
    dictLabel: string
    dictValue: string
    dictType: string
    cssClass?: string
    listClass?: string
    isDefault?: number
    status: number
    remark?: string
    createTime?: string
    updateTime?: string
}

export interface DictTypePageParam {
    current?: number
    size?: number
    dictName?: string
    status?: number
}

// ==================== API方法 ====================

/**
 * 分页查询字典类型
 */
export const getDictTypePage = (params: DictTypePageParam): Promise<PageResult<DictType>> => {
    return request.get('/system/dict/type/page', { params }) as unknown as Promise<PageResult<DictType>>
}

/**
 * 根据字典类型查询字典数据列表
 */
export const getDictDataList = (dictType: string): Promise<DictData[]> => {
    return request.get(`/system/dict/data/list/${dictType}`) as unknown as Promise<DictData[]>
}

/**
 * 新增字典类型
 */
export const createDictType = (data: Partial<DictType>) => {
    return request.post('/system/dict/type', data)
}

/**
 * 修改字典类型
 */
export const updateDictType = (data: DictType) => {
    return request.put('/system/dict/type', data)
}

/**
 * 删除字典类型
 */
export const deleteDictType = (id: number) => {
    return request.delete(`/system/dict/type/${id}`)
}

/**
 * 新增字典数据
 */
export const createDictData = (data: Partial<DictData>) => {
    return request.post('/system/dict/data', data)
}

/**
 * 修改字典数据
 */
export const updateDictData = (data: DictData) => {
    return request.put('/system/dict/data', data)
}

/**
 * 删除字典数据
 */
export const deleteDictData = (id: number) => {
    return request.delete(`/system/dict/data/${id}`)
}

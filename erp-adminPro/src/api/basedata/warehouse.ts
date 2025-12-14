import request from '@/utils/request'

// ==================== 类型定义 ====================

export interface Warehouse {
    id: number
    code: string
    name: string
    contact?: string
    phone?: string
    address?: string
    isDefault: number
    status: number
    remark?: string
    createTime?: string
    updateTime?: string
}

export interface WarehousePageParam {
    current: number
    size: number
    name?: string
    status?: number
}

export interface PageResult<T> {
    records: T[]
    total: number
    current: number
    size: number
}

// ==================== API方法 ====================

/**
 * 分页查询仓库
 */
export const getWarehousePage = (params: WarehousePageParam) => {
    return request.get<PageResult<Warehouse>>('/basedata/warehouse/page', { params })
}

/**
 * 根据ID查询仓库
 */
export const getWarehouseById = (id: number) => {
    return request.get<Warehouse>(`/basedata/warehouse/${id}`)
}

/**
 * 新增仓库
 */
export const createWarehouse = (data: Partial<Warehouse>) => {
    return request.post('/basedata/warehouse', data)
}

/**
 * 更新仓库
 */
export const updateWarehouse = (data: Warehouse) => {
    return request.put('/basedata/warehouse', data)
}

/**
 * 删除仓库
 */
export const deleteWarehouse = (id: number) => {
    return request.delete(`/basedata/warehouse/${id}`)
}

/**
 * 设置默认仓库
 */
export const setDefaultWarehouse = (id: number) => {
    return request.post(`/basedata/warehouse/${id}/set-default`)
}

/**
 * 获取所有启用的仓库
 */
export const getAllWarehouses = () => {
    return request.get<Warehouse[]>('/basedata/warehouse/all')
}

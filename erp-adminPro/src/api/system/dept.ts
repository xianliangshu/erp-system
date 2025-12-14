import request from '@/utils/request'

// ==================== 类型定义 ====================

export interface Dept {
    id: number
    parentId?: number
    ancestors?: string
    code: string
    name: string
    shortName?: string
    leader?: string
    phone?: string
    email?: string
    sort?: number
    status: number
    remark?: string
    createTime?: string
    updateTime?: string
    children?: Dept[]
}

export interface DeptQueryParam {
    name?: string
    status?: number
}

// ==================== API方法 ====================

/**
 * 查询部门列表
 */
export const getDeptList = (params?: DeptQueryParam) => {
    return request.get<Dept[]>('/system/dept/list', { params })
}

/**
 * 获取部门树
 */
export const getDeptTree = () => {
    return request.get<Dept[]>('/system/dept/tree')
}

/**
 * 根据ID查询部门
 */
export const getDeptById = (id: number) => {
    return request.get<Dept>(`/system/dept/${id}`)
}

/**
 * 新增部门
 */
export const createDept = (data: Partial<Dept>) => {
    return request.post('/system/dept', data)
}

/**
 * 更新部门
 */
export const updateDept = (data: Dept) => {
    return request.put('/system/dept', data)
}

/**
 * 删除部门
 */
export const deleteDept = (id: number) => {
    return request.delete(`/system/dept/${id}`)
}

/**
 * 统计部门用户数量
 */
export const countDeptUsers = (id: number) => {
    return request.get<number>(`/system/dept/${id}/user-count`)
}

import request from '@/utils/request'

// ==================== 类型定义 ====================

export interface User {
    id: number
    code: string
    username: string
    password?: string
    nickname?: string
    realName?: string
    email?: string
    phone?: string
    gender?: number
    avatar?: string
    status: number
    remark?: string
    createTime?: string
    updateTime?: string
    lastLoginTime?: string
    lastLoginIp?: string
}

export interface UserPageParam {
    current: number
    size: number
    username?: string
    phone?: string
    status?: number
}

export interface PageResult<T> {
    records: T[]
    total: number
    current: number
    size: number
}

export interface ResetPasswordParam {
    newPassword: string
}

export interface ChangePasswordParam {
    userId: number
    oldPassword: string
    newPassword: string
}

export interface AssignRolesParam {
    roleIds: number[]
}

export interface AssignDeptsParam {
    deptIds: number[]
    mainDeptId?: number
}

// ==================== API方法 ====================

/**
 * 分页查询用户
 */
export const getUserPage = (params: UserPageParam) => {
    return request.get<PageResult<User>>('/system/user/page', { params })
}

/**
 * 根据ID查询用户
 */
export const getUserById = (id: number) => {
    return request.get<User>(`/system/user/${id}`)
}

/**
 * 新增用户
 */
export const createUser = (data: Partial<User>) => {
    return request.post('/system/user', data)
}

/**
 * 更新用户
 */
export const updateUser = (data: User) => {
    return request.put('/system/user', data)
}

/**
 * 删除用户
 */
export const deleteUser = (id: number) => {
    return request.delete(`/system/user/${id}`)
}

/**
 * 批量删除用户
 */
export const batchDeleteUsers = (ids: number[]) => {
    return request.delete('/system/user/batch', { data: ids })
}

/**
 * 重置密码
 */
export const resetPassword = (id: number, newPassword: string) => {
    return request.post(`/system/user/${id}/reset-password`, { newPassword })
}

/**
 * 修改密码
 */
export const changePassword = (data: ChangePasswordParam) => {
    return request.post('/system/user/change-password', data)
}

/**
 * 分配角色
 */
export const assignRoles = (id: number, roleIds: number[]) => {
    return request.post(`/system/user/${id}/roles`, { roleIds })
}

/**
 * 获取用户角色
 */
export const getUserRoles = (id: number) => {
    return request.get<number[]>(`/system/user/${id}/roles`)
}

/**
 * 分配部门
 */
export const assignDepts = (id: number, data: AssignDeptsParam) => {
    return request.post(`/system/user/${id}/depts`, data)
}

/**
 * 获取用户部门
 */
export const getUserDepts = (id: number) => {
    return request.get<number[]>(`/system/user/${id}/depts`)
}

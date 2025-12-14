import request from '@/utils/request'

export interface UserPageParams {
    current: number
    size: number
    username?: string
    phone?: string
    status?: number
}

export interface User {
    id: number
    username: string
    nickname: string
    phone: string
    email: string
    status: number
    createTime: string
}

export interface PageResult<T> {
    total: number
    current: number
    size: number
    pages: number
    records: T[]
}

// 分页查询用户列表
export const getUserPage = (params: UserPageParams) => {
    return request.get<any, PageResult<User>>('/system/user/page', { params })
}

// 获取用户详情
export const getUserById = (id: number) => {
    return request.get<any, User>(`/system/user/${id}`)
}

// 新增用户
export const addUser = (data: Partial<User>) => {
    return request.post('/system/user', data)
}

// 更新用户
export const updateUser = (data: Partial<User>) => {
    return request.put('/system/user', data)
}

// 删除用户
export const deleteUser = (id: number) => {
    return request.delete(`/system/user/${id}`)
}

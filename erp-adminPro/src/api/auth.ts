import request from '@/utils/request'
import type { User } from '@/types'

export interface LoginParams {
    username: string
    password: string
}

export interface LoginResult {
    token: string
    user: User
}

// 登录
export const login = (data: LoginParams) => {
    return request.post<any, LoginResult>('/auth/login', data)
}

// 退出登录
export const logout = () => {
    return request.post('/auth/logout')
}

// 获取当前用户信息
export const getCurrentUser = () => {
    return request.get('/auth/user')
}

// 通用响应类型
export interface Result<T = any> {
    code: number
    message: string
    data: T
    timestamp: number
}

// 分页结果类型
export interface PageResult<T> {
    total: number
    current: number
    size: number
    pages: number
    records: T[]
}

// 用户类型
export interface User {
    id: number
    code: string
    username: string
    password?: string
    nickname: string
    realName: string
    email: string
    phone: string
    gender: number
    avatar?: string
    status: number
    createBy: string
    createTime: string
    updateBy?: string
    updateTime?: string
}

// 角色类型
export interface Role {
    id: number
    code: string
    name: string
    description?: string
    status: number
    createTime: string
}

// 部门类型
export interface Dept {
    id: number
    parentId: number
    name: string
    sort: number
    leader?: string
    phone?: string
    status: number
    children?: Dept[]
}

// 菜单类型
export interface Menu {
    id: number
    parentId: number
    name: string
    path?: string
    component?: string
    icon?: string
    sort: number
    type: number
    status: number
    children?: Menu[]
}

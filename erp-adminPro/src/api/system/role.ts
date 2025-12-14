import request from '@/utils/request'
import type { PageResult } from './user'

// ==================== 类型定义 ====================

export interface Role {
    id: number
    code: string
    name: string
    permissionCode?: string
    dataScope?: number
    status: number
    remark?: string
    createTime?: string
    updateTime?: string
}

export interface RolePageParam {
    current: number
    size: number
    name?: string
    status?: number
}

export interface AssignMenusParam {
    menuIds: number[]
}

// ==================== API方法 ====================

/**
 * 分页查询角色
 */
export const getRolePage = (params: RolePageParam) => {
    return request.get<PageResult<Role>>('/system/role/page', { params })
}

/**
 * 根据ID查询角色
 */
export const getRoleById = (id: number) => {
    return request.get<Role>(`/system/role/${id}`)
}

/**
 * 新增角色
 */
export const createRole = (data: Partial<Role>) => {
    return request.post('/system/role', data)
}

/**
 * 更新角色
 */
export const updateRole = (data: Role) => {
    return request.put('/system/role', data)
}

/**
 * 删除角色
 */
export const deleteRole = (id: number) => {
    return request.delete(`/system/role/${id}`)
}

/**
 * 获取所有角色(下拉选择用)
 */
export const getAllRoles = () => {
    return request.get<Role[]>('/system/role/all')
}

/**
 * 分配菜单权限
 */
export const assignMenus = (id: number, menuIds: number[]) => {
    return request.post(`/system/role/${id}/menus`, { menuIds })
}

/**
 * 获取角色菜单
 */
export const getRoleMenus = (id: number) => {
    return request.get<number[]>(`/system/role/${id}/menus`)
}

/**
 * 统计角色用户数量
 */
export const countRoleUsers = (id: number) => {
    return request.get<number>(`/system/role/${id}/user-count`)
}

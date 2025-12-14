import request from '@/utils/request'

// ==================== 类型定义 ====================

export interface Menu {
    id: number
    parentId?: number
    code: string
    name: string
    title?: string
    menuType: number  // 0-目录 1-菜单 2-按钮
    path?: string
    component?: string
    permission?: string
    icon?: string
    sort?: number
    visible?: number  // 0-隐藏 1-显示
    status: number
    remark?: string
    createTime?: string
    updateTime?: string
    children?: Menu[]
}

export interface MenuQueryParam {
    name?: string
    status?: number
}

// ==================== API方法 ====================

/**
 * 查询菜单列表
 */
export const getMenuList = (params?: MenuQueryParam) => {
    return request.get<Menu[]>('/system/menu/list', { params })
}

/**
 * 获取菜单树
 */
export const getMenuTree = () => {
    return request.get<Menu[]>('/system/menu/tree')
}

/**
 * 获取用户菜单
 */
export const getUserMenus = (userId: number) => {
    return request.get<Menu[]>('/system/menu/user-menus', { params: { userId } })
}

/**
 * 根据ID查询菜单
 */
export const getMenuById = (id: number) => {
    return request.get<Menu>(`/system/menu/${id}`)
}

/**
 * 新增菜单
 */
export const createMenu = (data: Partial<Menu>) => {
    return request.post('/system/menu', data)
}

/**
 * 更新菜单
 */
export const updateMenu = (data: Menu) => {
    return request.put('/system/menu', data)
}

/**
 * 删除菜单
 */
export const deleteMenu = (id: number) => {
    return request.delete(`/system/menu/${id}`)
}

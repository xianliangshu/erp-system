import type { Menu } from '@/api/system/menu'
import type { MenuProps } from 'antd'
import * as Icons from '@ant-design/icons'

type MenuItem = Required<MenuProps>['items'][number]

/**
 * 将后端菜单数据转换为Ant Design Menu组件所需格式
 */
export const transformMenuToAntdFormat = (menus: Menu[]): MenuItem[] => {
    console.log('🔍 transformMenuToAntdFormat input:', menus)
    const result = menus.map(menu => {
        console.log('🔍 Transforming menu:', menu.name, menu)
        const item: MenuItem = {
            key: menu.path || menu.id.toString(),
            label: menu.title || menu.name,
            icon: menu.icon ? getIconComponent(menu.icon) : undefined,
            children: menu.children && menu.children.length > 0
                ? transformMenuToAntdFormat(menu.children)
                : undefined
        }
        console.log('🔍 Transformed item:', item)
        return item
    })
    console.log('🔍 transformMenuToAntdFormat output:', result)
    return result
}

/**
 * 过滤隐藏菜单和按钮权限
 */
export const filterVisibleMenus = (menus: Menu[]): Menu[] => {
    console.log('🔍 filterVisibleMenus input:', menus)
    const filtered = menus
        .filter(menu => {
            // 过滤隐藏菜单
            if (menu.visible === 0) {
                console.log('🔍 Filtering out hidden menu:', menu.name)
                return false
            }
            // 过滤按钮权限 (menuType: 0-目录 1-菜单 2-按钮)
            if (menu.menuType === 2) {
                console.log('🔍 Filtering out button permission:', menu.name)
                return false
            }
            return true
        })
        .map(menu => ({
            ...menu,
            children: menu.children ? filterVisibleMenus(menu.children) : undefined
        }))
    console.log('🔍 filterVisibleMenus output:', filtered)
    return filtered
}

/**
 * 根据路径查找菜单项(用于面包屑)
 */
export const findMenuByPath = (menus: Menu[], path: string): Menu | null => {
    for (const menu of menus) {
        if (menu.path === path) {
            return menu
        }
        if (menu.children) {
            const found = findMenuByPath(menu.children, path)
            if (found) return found
        }
    }
    return null
}

/**
 * 获取图标组件
 * 支持Ant Design图标格式: "ant-design:user-outlined"
 */
const getIconComponent = (iconName: string) => {
    if (!iconName) return undefined

    try {
        // 处理 ant-design:icon-name 格式
        // 例如: "ant-design:user-outlined" -> "UserOutlined"
        const iconKey = iconName
            .replace('ant-design:', '')
            .split('-')
            .map(word => word.charAt(0).toUpperCase() + word.slice(1))
            .join('')

        console.log(`Converting icon: ${iconName} -> ${iconKey}`)

        const IconComponent = (Icons as any)[iconKey]
        if (!IconComponent) {
            console.warn(`Icon component not found for: ${iconKey}`)
            return undefined
        }
        return <IconComponent />
    } catch (error) {
        console.error(`Error loading icon ${iconName}:`, error)
        return undefined
    }
}

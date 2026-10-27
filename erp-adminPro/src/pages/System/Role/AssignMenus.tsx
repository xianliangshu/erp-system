import React, { useEffect, useState } from 'react'
import { Modal, Tree, message } from 'antd'
import type { DataNode } from 'antd/es/tree'
import { getMenuTree, type Menu } from '@/api/system/menu'
import { assignMenus, getRoleMenus, type Role } from '@/api/system/role'

interface AssignMenusProps {
    visible: boolean
    role: Role
    onCancel: () => void
    onSuccess: () => void
}

/**
 * 收集"有子节点"的菜单ID。
 *
 * antd Tree 在非严格勾选模式(checkStrictly=false)下会对受控 checkedKeys 做**向下传导**：
 * 只要把父级ID放进 checkedKeys，它下面所有子节点都会被自动勾上。
 * 因此回填时必须只给"叶子节点"，父级交给组件自行推导为半选/全选，
 * 否则会凭空授予用户没勾过的子菜单（越权）。
 */
const collectParentIds = (menus: Menu[], acc: Set<number> = new Set<number>()): Set<number> => {
    menus.forEach(menu => {
        const children = menu.children || []
        if (children.length > 0) {
            acc.add(menu.id)
            collectParentIds(children, acc)
        }
    })
    return acc
}

/** 后端返回的是嵌套结构，直接映射为 antd 需要的 DataNode */
const buildTreeData = (menus: Menu[]): DataNode[] =>
    menus.map(menu => {
        const children = menu.children || []
        return {
            key: menu.id,
            title: menu.title || menu.name,
            children: children.length > 0 ? buildTreeData(children) : undefined
        }
    })

/**
 * 客户端补齐祖先目录。
 *
 * 后端 SysRoleServiceImpl.assignMenus() 也会做同样的闭包，
 * 这里再算一次是为了让提交内容与界面语义完全一致，不依赖服务端隐式纠偏。
 */
const withAncestors = (keys: React.Key[], allMenus: Menu[]): number[] => {
    const parentMap = new Map<number, number>()
    const walk = (menus: Menu[]) => {
        menus.forEach(menu => {
            parentMap.set(menu.id, menu.parentId ?? 0)
            walk(menu.children || [])
        })
    }
    walk(allMenus)

    const result = new Set<number>(keys.map(Number))
    Array.from(result).forEach(id => {
        let cursor = parentMap.get(id)
        let depth = 0
        while (cursor && cursor !== 0 && depth++ < 50) {
            result.add(cursor)
            cursor = parentMap.get(cursor)
        }
    })
    return Array.from(result)
}

const AssignMenus: React.FC<AssignMenusProps> = ({ visible, role, onCancel, onSuccess }) => {
    const [allMenus, setAllMenus] = useState<Menu[]>([])
    const [treeData, setTreeData] = useState<DataNode[]>([])
    const [checkedKeys, setCheckedKeys] = useState<React.Key[]>([])
    const [loading, setLoading] = useState(false)
    const [submitting, setSubmitting] = useState(false)

    useEffect(() => {
        if (visible) {
            loadData()
        }
        // 切换角色时重新加载
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [visible, role.id])

    const loadData = async () => {
        setLoading(true)
        try {
            // 全量菜单树用 /system/menu/tree，**不能**用 /system/menu/user-menus：
            // 后者是按当前登录用户授权过滤后的结果，非超管打开时只能看到自己已有的菜单，
            // 会把"分配权限"变成"复制我自己的权限"，且隐藏菜单永远无法被授权。
            const menusRes = await getMenuTree() as any
            const menus: Menu[] = Array.isArray(menusRes) ? menusRes : []
            setAllMenus(menus)
            setTreeData(buildTreeData(menus))

            // 加载角色已有菜单
            const roleMenusRes = await getRoleMenus(role.id) as any
            const menuIds: number[] = Array.isArray(roleMenusRes) ? roleMenusRes : []

            // 只回填叶子节点，父级由 Tree 自行推导，避免向下传导造成越权
            const parentIds = collectParentIds(menus)
            setCheckedKeys(menuIds.filter(id => !parentIds.has(id)))
        } catch (error) {
            console.error('加载数据失败:', error)
            message.error('加载数据失败')
        } finally {
            setLoading(false)
        }
    }

    const handleCheck = (checked: React.Key[] | { checked: React.Key[]; halfChecked: React.Key[] }) => {
        // 受控状态只接受"实际被勾选"的节点：
        // 非严格模式返回 Key[]（其中已含"因子节点全选而变全选"的父级）；严格模式返回对象。
        // 半选的父级不能放进受控值，否则会触发向下传导、把未授权的子菜单一起勾上。
        const keys = Array.isArray(checked) ? checked : checked.checked
        setCheckedKeys(keys)
    }

    const handleSubmit = async () => {
        setSubmitting(true)
        try {
            await assignMenus(role.id, withAncestors(checkedKeys, allMenus))
            message.success('分配权限成功')
            onSuccess()
        } catch (error) {
            message.error('分配权限失败')
        } finally {
            setSubmitting(false)
        }
    }

    return (
        <Modal
            title="分配菜单权限"
            open={visible}
            onOk={handleSubmit}
            onCancel={onCancel}
            confirmLoading={submitting}
            width={500}
            destroyOnClose
        >
            <div style={{ marginBottom: 16 }}>
                <span>角色: {role.name}</span>
            </div>
            <Tree
                checkable
                treeData={treeData}
                checkedKeys={checkedKeys}
                onCheck={handleCheck}
                height={400}
                disabled={loading}
            />
        </Modal>
    )
}

export default AssignMenus

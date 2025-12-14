import React, { useEffect, useState } from 'react'
import { Modal, Tree, message } from 'antd'
import type { DataNode } from 'antd/es/tree'
import { getUserMenus, type Menu } from '@/api/system/menu'
import { assignMenus, getRoleMenus, type Role } from '@/api/system/role'
import { useUserStore } from '@/store/userStore'

interface AssignMenusProps {
    visible: boolean
    role: Role
    onCancel: () => void
    onSuccess: () => void
}

const AssignMenus: React.FC<AssignMenusProps> = ({ visible, role, onCancel, onSuccess }) => {
    const { user } = useUserStore()
    const [treeData, setTreeData] = useState<DataNode[]>([])
    const [checkedKeys, setCheckedKeys] = useState<React.Key[]>([])
    const [loading, setLoading] = useState(false)

    useEffect(() => {
        if (visible && user) {
            loadData()
        }
    }, [visible])

    const loadData = async () => {
        setLoading(true)
        try {
            // 加载所有菜单
            const menusRes = await getUserMenus(user!.id) as any
            console.log('🔍 All menus response:', menusRes)
            const tree = buildTreeData(Array.isArray(menusRes) ? menusRes : [])
            setTreeData(tree)

            // 加载角色已有菜单
            const roleMenusRes = await getRoleMenus(role.id) as any
            console.log('🔍 Role menus response:', roleMenusRes)
            const menuIds = Array.isArray(roleMenusRes) ? roleMenusRes : []
            setCheckedKeys(menuIds)
        } catch (error) {
            console.error('加载数据失败:', error)
            message.error('加载数据失败')
        } finally {
            setLoading(false)
        }
    }

    const buildTreeData = (menus: Menu[]): DataNode[] => {
        return menus.map(menu => ({
            key: menu.id,
            title: menu.name,
            children: menu.children ? buildTreeData(menu.children) : []
        }))
    }

    const handleCheck = (checked: React.Key[] | { checked: React.Key[]; halfChecked: React.Key[] }) => {
        const keys = Array.isArray(checked) ? checked : checked.checked
        setCheckedKeys(keys)
    }

    const handleSubmit = async () => {
        try {
            await assignMenus(role.id, checkedKeys as number[])
            message.success('分配权限成功')
            onSuccess()
        } catch (error) {
            message.error('分配权限失败')
        }
    }

    return (
        <Modal
            title="分配菜单权限"
            open={visible}
            onOk={handleSubmit}
            onCancel={onCancel}
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
            />
        </Modal>
    )
}

export default AssignMenus

import React, { useEffect, useState } from 'react'
import { Modal, Transfer, message } from 'antd'
import type { TransferDirection } from 'antd/es/transfer'
import { getAllRoles, type Role } from '@/api/system/role'
import { assignRoles, getUserRoles, type User } from '@/api/system/user'

interface AssignRoleProps {
    visible: boolean
    user: User
    onCancel: () => void
    onSuccess: () => void
}

const AssignRole: React.FC<AssignRoleProps> = ({ visible, user, onCancel, onSuccess }) => {
    const [allRoles, setAllRoles] = useState<Role[]>([])
    const [targetKeys, setTargetKeys] = useState<string[]>([])
    const [loading, setLoading] = useState(false)

    useEffect(() => {
        if (visible) {
            loadData()
        }
    }, [visible])

    const loadData = async () => {
        setLoading(true)
        try {
            // 加载所有角色
            const rolesRes = await getAllRoles() as any
            console.log('🔍 All roles response:', rolesRes)
            // rolesRes 已经是角色数组,因为 request.ts 拦截器已经提取了 data.data
            setAllRoles(Array.isArray(rolesRes) ? rolesRes : [])

            // 加载用户已有角色
            const userRolesRes = await getUserRoles(user.id) as any
            console.log('🔍 User roles response:', userRolesRes)
            // userRolesRes 已经是角色ID数组
            const roleIds = Array.isArray(userRolesRes) ? userRolesRes : []
            setTargetKeys(roleIds.map(String))
        } catch (error) {
            console.error('加载数据失败:', error)
            message.error('加载数据失败')
        } finally {
            setLoading(false)
        }
    }

    const handleChange = (newTargetKeys: string[]) => {
        setTargetKeys(newTargetKeys)
    }

    const handleSubmit = async () => {
        try {
            const roleIds = targetKeys.map(Number)
            await assignRoles(user.id, roleIds)
            message.success('分配角色成功')
            onSuccess()
        } catch (error) {
            message.error('分配角色失败')
        }
    }

    return (
        <Modal
            title="分配角色"
            open={visible}
            onOk={handleSubmit}
            onCancel={onCancel}
            width={600}
            destroyOnClose
        >
            <div style={{ marginBottom: 16 }}>
                <span>用户: {user.username}</span>
            </div>
            <Transfer
                dataSource={allRoles.map(role => ({
                    key: String(role.id),
                    title: role.name,
                    description: role.remark
                }))}
                titles={['可选角色', '已选角色']}
                targetKeys={targetKeys}
                onChange={handleChange}
                render={item => item.title}
                listStyle={{
                    width: 250,
                    height: 400
                }}
                loading={loading}
            />
        </Modal>
    )
}

export default AssignRole

import React, { useEffect, useState } from 'react'
import { Modal, Tree, message, Radio } from 'antd'
import type { DataNode } from 'antd/es/tree'
import { getDeptTree, type Dept } from '@/api/system/dept'
import { assignDepts, getUserDepts, type User } from '@/api/system/user'

interface AssignDeptProps {
    visible: boolean
    user: User
    onCancel: () => void
    onSuccess: () => void
}

const AssignDept: React.FC<AssignDeptProps> = ({ visible, user, onCancel, onSuccess }) => {
    const [treeData, setTreeData] = useState<DataNode[]>([])
    const [checkedKeys, setCheckedKeys] = useState<React.Key[]>([])
    const [mainDeptId, setMainDeptId] = useState<number>()
    const [loading, setLoading] = useState(false)

    useEffect(() => {
        if (visible) {
            loadData()
        }
    }, [visible])

    const loadData = async () => {
        setLoading(true)
        try {
            // 加载部门树
            const deptRes = await getDeptTree() as any
            console.log('🔍 Dept tree response:', deptRes)
            // deptRes 已经是部门树数组,因为 request.ts 拦截器已经提取了 data.data
            const tree = buildTreeData(Array.isArray(deptRes) ? deptRes : [])
            setTreeData(tree)

            // 加载用户已有部门
            const userDeptsRes = await getUserDepts(user.id) as any
            console.log('🔍 User depts response:', userDeptsRes)
            // userDeptsRes 已经是部门ID数组
            const deptIds = Array.isArray(userDeptsRes) ? userDeptsRes : []
            setCheckedKeys(deptIds)
            if (deptIds.length > 0) {
                setMainDeptId(deptIds[0])
            }
        } catch (error) {
            console.error('加载数据失败:', error)
            message.error('加载数据失败')
        } finally {
            setLoading(false)
        }
    }

    const buildTreeData = (depts: Dept[]): DataNode[] => {
        return depts.map(dept => ({
            key: dept.id,
            title: dept.name,
            children: dept.children ? buildTreeData(dept.children) : []
        }))
    }

    const handleCheck = (checked: React.Key[] | { checked: React.Key[]; halfChecked: React.Key[] }) => {
        const keys = Array.isArray(checked) ? checked : checked.checked
        setCheckedKeys(keys)
        if (keys.length > 0 && !keys.includes(mainDeptId as React.Key)) {
            setMainDeptId(keys[0] as number)
        }
    }

    const handleSubmit = async () => {
        if (checkedKeys.length === 0) {
            message.warning('请至少选择一个部门')
            return
        }
        if (!mainDeptId) {
            message.warning('请选择主部门')
            return
        }
        try {
            await assignDepts(user.id, {
                deptIds: checkedKeys as number[],
                mainDeptId
            })
            message.success('分配部门成功')
            onSuccess()
        } catch (error) {
            message.error('分配部门失败')
        }
    }

    return (
        <Modal
            title="分配部门"
            open={visible}
            onOk={handleSubmit}
            onCancel={onCancel}
            width={500}
            destroyOnClose
        >
            <div style={{ marginBottom: 16 }}>
                <span>用户: {user.username}</span>
            </div>
            <Tree
                checkable
                treeData={treeData}
                checkedKeys={checkedKeys}
                onCheck={handleCheck}
                height={300}
            />
            {checkedKeys.length > 0 && (
                <div style={{ marginTop: 16 }}>
                    <span>主部门: </span>
                    <Radio.Group
                        value={mainDeptId}
                        onChange={e => setMainDeptId(e.target.value)}
                    >
                        {checkedKeys.map(key => {
                            const findDept = (depts: Dept[], id: React.Key): Dept | null => {
                                for (const dept of depts) {
                                    if (dept.id === id) return dept
                                    if (dept.children) {
                                        const found = findDept(dept.children, id)
                                        if (found) return found
                                    }
                                }
                                return null
                            }
                            return (
                                <Radio key={key} value={key}>
                                    部门{key}
                                </Radio>
                            )
                        })}
                    </Radio.Group>
                </div>
            )}
        </Modal>
    )
}

export default AssignDept

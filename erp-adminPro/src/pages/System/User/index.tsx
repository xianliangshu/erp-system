import React, { useState, useEffect } from 'react'
import { Table, Button, Space, Tag, Input, Select, message, Modal, Form, App } from 'antd'
import { PlusOutlined, EditOutlined, DeleteOutlined, KeyOutlined, TeamOutlined, ApartmentOutlined } from '@ant-design/icons'
import type { ColumnsType } from 'antd/es/table'
import { getUserPage, deleteUser, batchDeleteUsers, type User, type UserPageParam } from '@/api/system/user'
import UserForm from './UserForm'
import ResetPassword from './ResetPassword'
import AssignRole from './AssignRole'
import AssignDept from './AssignDept'
import './index.css'

const { Search } = Input
const { Option } = Select

const UserList: React.FC = () => {
    // 使用 App.useApp() 获取 modal 实例
    const { modal } = App.useApp()
    // 状态定义
    const [loading, setLoading] = useState(false)
    const [dataSource, setDataSource] = useState<User[]>([])
    const [total, setTotal] = useState(0)
    const [current, setCurrent] = useState(1)
    const [pageSize, setPageSize] = useState(10)
    const [selectedRowKeys, setSelectedRowKeys] = useState<React.Key[]>([])

    // 查询条件
    const [searchParams, setSearchParams] = useState<Partial<UserPageParam>>({})

    // 弹窗状态
    const [formVisible, setFormVisible] = useState(false)
    const [resetPwdVisible, setResetPwdVisible] = useState(false)
    const [assignRoleVisible, setAssignRoleVisible] = useState(false)
    const [assignDeptVisible, setAssignDeptVisible] = useState(false)
    const [currentUser, setCurrentUser] = useState<User | null>(null)

    // 加载数据
    useEffect(() => {
        fetchData()
    }, [current, pageSize, searchParams])

    const fetchData = async () => {
        setLoading(true)
        try {
            const res = await getUserPage({
                current,
                size: pageSize,
                ...searchParams
            })
            console.log('🔍 User page API response:', res)
            // res 已经是分页对象 { total, current, size, pages, records }
            // 因为 request.ts 拦截器已经提取了 data.data
            setDataSource(res.records || [])
            setTotal(res.total || 0)
        } catch (error) {
            message.error('加载数据失败')
        } finally {
            setLoading(false)
        }
    }

    // 表格列定义
    const columns: ColumnsType<User> = [
        {
            title: '用户编号',
            dataIndex: 'code',
            key: 'code',
            width: 120
        },
        {
            title: '用户名',
            dataIndex: 'username',
            key: 'username',
            width: 120
        },
        {
            title: '昵称',
            dataIndex: 'nickname',
            key: 'nickname',
            width: 120
        },
        {
            title: '手机号',
            dataIndex: 'phone',
            key: 'phone',
            width: 130
        },
        {
            title: '邮箱',
            dataIndex: 'email',
            key: 'email',
            width: 180,
            ellipsis: true
        },
        {
            title: '状态',
            dataIndex: 'status',
            key: 'status',
            width: 80,
            render: (status: number) => (
                <Tag color={status === 1 ? 'success' : 'error'}>
                    {status === 1 ? '启用' : '禁用'}
                </Tag>
            )
        },
        {
            title: '创建时间',
            dataIndex: 'createTime',
            key: 'createTime',
            width: 180
        },
        {
            title: '操作',
            key: 'action',
            width: 280,
            fixed: 'right',
            render: (_, record) => (
                <Space size="small">
                    <Button
                        type="link"
                        size="small"
                        icon={<EditOutlined />}
                        onClick={() => handleEdit(record)}
                    >
                        编辑
                    </Button>
                    <Button
                        type="link"
                        size="small"
                        icon={<KeyOutlined />}
                        onClick={() => handleResetPassword(record)}
                    >
                        重置密码
                    </Button>
                    <Button
                        type="link"
                        size="small"
                        icon={<TeamOutlined />}
                        onClick={() => handleAssignRole(record)}
                    >
                        分配角色
                    </Button>
                    <Button
                        type="link"
                        size="small"
                        icon={<ApartmentOutlined />}
                        onClick={() => handleAssignDept(record)}
                    >
                        分配部门
                    </Button>
                    <Button
                        type="link"
                        size="small"
                        danger
                        icon={<DeleteOutlined />}
                        onClick={() => handleDelete(record)}
                    >
                        删除
                    </Button>
                </Space>
            )
        }
    ]

    // 事件处理
    const handleAdd = () => {
        setCurrentUser(null)
        setFormVisible(true)
    }

    const handleEdit = (record: User) => {
        setCurrentUser(record)
        setFormVisible(true)
    }

    const handleDelete = (record: User) => {
        modal.confirm({
            title: '确认删除',
            content: `确定要删除用户"${record.username}"吗？`,
            onOk: async () => {
                try {
                    await deleteUser(record.id)
                    message.success('删除成功')
                    fetchData()
                } catch (error) {
                    message.error('删除失败')
                }
            }
        })
    }

    const handleBatchDelete = () => {
        if (selectedRowKeys.length === 0) {
            message.warning('请选择要删除的用户')
            return
        }
        modal.confirm({
            title: '确认删除',
            content: `确定要删除选中的 ${selectedRowKeys.length} 个用户吗?`,
            okText: '确定',
            cancelText: '取消',
            onOk: async () => {
                try {
                    await batchDeleteUsers(selectedRowKeys as number[])
                    message.success('删除成功')
                    setSelectedRowKeys([])
                    fetchData()
                } catch (error) {
                    message.error('删除失败')
                }
            }
        })
    }

    const handleResetPassword = (record: User) => {
        setCurrentUser(record)
        setResetPwdVisible(true)
    }

    const handleAssignRole = (record: User) => {
        setCurrentUser(record)
        setAssignRoleVisible(true)
    }

    const handleAssignDept = (record: User) => {
        setCurrentUser(record)
        setAssignDeptVisible(true)
    }

    const handleSearch = (value: string) => {
        setSearchParams({ ...searchParams, username: value })
        setCurrent(1)
    }

    const handleStatusChange = (value: number | undefined) => {
        setSearchParams({ ...searchParams, status: value })
        setCurrent(1)
    }

    return (
        <div className="user-list-container">
            {/* 搜索栏 */}
            <div className="search-bar">
                <Space size="middle">
                    <Search
                        placeholder="请输入用户名"
                        allowClear
                        style={{ width: 200 }}
                        onSearch={handleSearch}
                    />
                    <Select
                        placeholder="用户状态"
                        allowClear
                        style={{ width: 120 }}
                        onChange={handleStatusChange}
                    >
                        <Option value={1}>启用</Option>
                        <Option value={0}>禁用</Option>
                    </Select>
                    <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
                        新增用户
                    </Button>
                    <Button danger onClick={handleBatchDelete}>
                        批量删除
                    </Button>
                </Space>
            </div>

            {/* 表格 */}
            <Table
                rowKey="id"
                loading={loading}
                dataSource={dataSource}
                columns={columns}
                scroll={{ x: 1400 }}
                rowSelection={{
                    selectedRowKeys,
                    onChange: setSelectedRowKeys
                }}
                pagination={{
                    current,
                    pageSize,
                    total,
                    showSizeChanger: true,
                    showQuickJumper: true,
                    showTotal: (total) => `共 ${total} 条`,
                    onChange: (page, size) => {
                        setCurrent(page)
                        setPageSize(size)
                    }
                }}
            />

            {/* 用户表单弹窗 */}
            <UserForm
                visible={formVisible}
                user={currentUser}
                onCancel={() => setFormVisible(false)}
                onSuccess={() => {
                    setFormVisible(false)
                    fetchData()
                }}
            />

            {/* 重置密码弹窗 */}
            {currentUser && (
                <ResetPassword
                    visible={resetPwdVisible}
                    user={currentUser}
                    onCancel={() => setResetPwdVisible(false)}
                    onSuccess={() => {
                        setResetPwdVisible(false)
                    }}
                />
            )}

            {/* 分配角色弹窗 */}
            {currentUser && (
                <AssignRole
                    visible={assignRoleVisible}
                    user={currentUser}
                    onCancel={() => setAssignRoleVisible(false)}
                    onSuccess={() => {
                        setAssignRoleVisible(false)
                    }}
                />
            )}

            {/* 分配部门弹窗 */}
            {currentUser && (
                <AssignDept
                    visible={assignDeptVisible}
                    user={currentUser}
                    onCancel={() => setAssignDeptVisible(false)}
                    onSuccess={() => {
                        setAssignDeptVisible(false)
                    }}
                />
            )}
        </div>
    )
}

export default UserList

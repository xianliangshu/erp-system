import React, { useState, useEffect } from 'react'
import { Table, Button, Space, Tag, Input, Select, message, App } from 'antd'
import { PlusOutlined, EditOutlined, DeleteOutlined, KeyOutlined } from '@ant-design/icons'
import type { ColumnsType } from 'antd/es/table'
import { getRolePage, deleteRole, type Role, type RolePageParam } from '@/api/system/role'
import RoleForm from './RoleForm'
import AssignMenus from './AssignMenus'
import './index.css'

const { Search } = Input
const { Option } = Select

const RoleList: React.FC = () => {
    const { modal } = App.useApp()

    // 状态定义
    const [loading, setLoading] = useState(false)
    const [dataSource, setDataSource] = useState<Role[]>([])
    const [total, setTotal] = useState(0)
    const [current, setCurrent] = useState(1)
    const [pageSize, setPageSize] = useState(10)
    const [selectedRowKeys, setSelectedRowKeys] = useState<React.Key[]>([])

    // 查询条件
    const [searchParams, setSearchParams] = useState<Partial<RolePageParam>>({})

    // 弹窗状态
    const [formVisible, setFormVisible] = useState(false)
    const [assignMenusVisible, setAssignMenusVisible] = useState(false)
    const [currentRole, setCurrentRole] = useState<Role | null>(null)

    // 加载数据
    useEffect(() => {
        fetchData()
    }, [current, pageSize, searchParams])

    const fetchData = async () => {
        setLoading(true)
        try {
            const res = await getRolePage({
                current,
                size: pageSize,
                ...searchParams
            }) as any
            console.log('🔍 Role page API response:', res)
            setDataSource(res.records || [])
            setTotal(res.total || 0)
        } catch (error) {
            message.error('加载数据失败')
        } finally {
            setLoading(false)
        }
    }

    // 表格列定义
    const columns: ColumnsType<Role> = [
        {
            title: '角色编号',
            dataIndex: 'code',
            key: 'code',
            width: 120
        },
        {
            title: '角色名称',
            dataIndex: 'name',
            key: 'name',
            width: 150
        },
        {
            title: '权限标识',
            dataIndex: 'permissionCode',
            key: 'permissionCode',
            width: 150
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
            title: '备注',
            dataIndex: 'remark',
            key: 'remark',
            ellipsis: true
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
            width: 220,
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
                        onClick={() => handleAssignMenus(record)}
                    >
                        分配权限
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
        setCurrentRole(null)
        setFormVisible(true)
    }

    const handleEdit = (record: Role) => {
        setCurrentRole(record)
        setFormVisible(true)
    }

    const handleDelete = (record: Role) => {
        modal.confirm({
            title: '确认删除',
            content: `确定要删除角色"${record.name}"吗?`,
            okText: '确定',
            cancelText: '取消',
            onOk: async () => {
                try {
                    await deleteRole(record.id)
                    message.success('删除成功')
                    fetchData()
                } catch (error) {
                    message.error('删除失败')
                }
            }
        })
    }

    const handleAssignMenus = (record: Role) => {
        setCurrentRole(record)
        setAssignMenusVisible(true)
    }

    const handleSearch = (value: string) => {
        setSearchParams({ ...searchParams, name: value })
        setCurrent(1)
    }

    const handleStatusChange = (value: number | undefined) => {
        setSearchParams({ ...searchParams, status: value })
        setCurrent(1)
    }

    return (
        <div className="role-list-container">
            {/* 搜索栏 */}
            <div className="search-bar">
                <Space size="middle">
                    <Search
                        placeholder="请输入角色名称"
                        allowClear
                        style={{ width: 200 }}
                        onSearch={handleSearch}
                    />
                    <Select
                        placeholder="角色状态"
                        allowClear
                        style={{ width: 120 }}
                        onChange={handleStatusChange}
                    >
                        <Option value={1}>启用</Option>
                        <Option value={0}>禁用</Option>
                    </Select>
                    <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
                        新增角色
                    </Button>
                </Space>
            </div>

            {/* 表格 */}
            <Table
                rowKey="id"
                loading={loading}
                dataSource={dataSource}
                columns={columns}
                scroll={{ x: 1200 }}
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

            {/* 角色表单弹窗 */}
            <RoleForm
                visible={formVisible}
                role={currentRole}
                onCancel={() => setFormVisible(false)}
                onSuccess={() => {
                    setFormVisible(false)
                    fetchData()
                }}
            />

            {/* 分配权限弹窗 */}
            {currentRole && (
                <AssignMenus
                    visible={assignMenusVisible}
                    role={currentRole}
                    onCancel={() => setAssignMenusVisible(false)}
                    onSuccess={() => {
                        setAssignMenusVisible(false)
                    }}
                />
            )}
        </div>
    )
}

export default RoleList

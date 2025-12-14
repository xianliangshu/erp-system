import React, { useState, useEffect } from 'react'
import { Table, Button, Space, Tag, Input, Select, message, App } from 'antd'
import { PlusOutlined, EditOutlined, DeleteOutlined, StarOutlined, StarFilled } from '@ant-design/icons'
import type { ColumnsType } from 'antd/es/table'
import { getWarehousePage, deleteWarehouse, setDefaultWarehouse, type Warehouse, type WarehousePageParam } from '@/api/basedata/warehouse'
import WarehouseForm from './WarehouseForm'
import './index.css'

const { Search } = Input
const { Option } = Select

const WarehouseList: React.FC = () => {
    const { modal } = App.useApp()

    // 状态定义
    const [loading, setLoading] = useState(false)
    const [dataSource, setDataSource] = useState<Warehouse[]>([])
    const [total, setTotal] = useState(0)
    const [current, setCurrent] = useState(1)
    const [pageSize, setPageSize] = useState(10)

    // 查询条件
    const [searchParams, setSearchParams] = useState<Partial<WarehousePageParam>>({})

    // 弹窗状态
    const [formVisible, setFormVisible] = useState(false)
    const [currentWarehouse, setCurrentWarehouse] = useState<Warehouse | null>(null)

    // 加载数据
    useEffect(() => {
        fetchData()
    }, [current, pageSize, searchParams])

    const fetchData = async () => {
        setLoading(true)
        try {
            const res = await getWarehousePage({
                current,
                size: pageSize,
                ...searchParams
            }) as any
            console.log('🔍 Warehouse page API response:', res)
            setDataSource(res.records || [])
            setTotal(res.total || 0)
        } catch (error) {
            message.error('加载数据失败')
        } finally {
            setLoading(false)
        }
    }

    // 表格列定义
    const columns: ColumnsType<Warehouse> = [
        {
            title: '仓库编号',
            dataIndex: 'code',
            key: 'code',
            width: 120
        },
        {
            title: '仓库名称',
            dataIndex: 'name',
            key: 'name',
            width: 150
        },
        {
            title: '联系人',
            dataIndex: 'contact',
            key: 'contact',
            width: 100
        },
        {
            title: '联系电话',
            dataIndex: 'phone',
            key: 'phone',
            width: 130
        },
        {
            title: '仓库地址',
            dataIndex: 'address',
            key: 'address',
            ellipsis: true
        },
        {
            title: '默认仓库',
            dataIndex: 'isDefault',
            key: 'isDefault',
            width: 100,
            render: (isDefault: number, record) => (
                isDefault === 1 ? (
                    <Tag icon={<StarFilled />} color="gold">默认</Tag>
                ) : (
                    <Button
                        type="link"
                        size="small"
                        icon={<StarOutlined />}
                        onClick={() => handleSetDefault(record)}
                    >
                        设为默认
                    </Button>
                )
            )
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
            width: 150,
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
                        danger
                        icon={<DeleteOutlined />}
                        onClick={() => handleDelete(record)}
                        disabled={record.isDefault === 1}
                    >
                        删除
                    </Button>
                </Space>
            )
        }
    ]

    // 事件处理
    const handleAdd = () => {
        setCurrentWarehouse(null)
        setFormVisible(true)
    }

    const handleEdit = (record: Warehouse) => {
        setCurrentWarehouse(record)
        setFormVisible(true)
    }

    const handleDelete = (record: Warehouse) => {
        modal.confirm({
            title: '确认删除',
            content: `确定要删除仓库"${record.name}"吗?`,
            okText: '确定',
            cancelText: '取消',
            onOk: async () => {
                try {
                    await deleteWarehouse(record.id)
                    message.success('删除成功')
                    fetchData()
                } catch (error) {
                    message.error('删除失败')
                }
            }
        })
    }

    const handleSetDefault = (record: Warehouse) => {
        modal.confirm({
            title: '确认设置',
            content: `确定要将"${record.name}"设为默认仓库吗?`,
            okText: '确定',
            cancelText: '取消',
            onOk: async () => {
                try {
                    await setDefaultWarehouse(record.id)
                    message.success('设置成功')
                    fetchData()
                } catch (error) {
                    message.error('设置失败')
                }
            }
        })
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
        <div className="warehouse-list-container">
            {/* 搜索栏 */}
            <div className="search-bar">
                <Space size="middle">
                    <Search
                        placeholder="请输入仓库名称"
                        allowClear
                        style={{ width: 200 }}
                        onSearch={handleSearch}
                    />
                    <Select
                        placeholder="仓库状态"
                        allowClear
                        style={{ width: 120 }}
                        onChange={handleStatusChange}
                    >
                        <Option value={1}>启用</Option>
                        <Option value={0}>禁用</Option>
                    </Select>
                    <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
                        新增仓库
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

            {/* 仓库表单弹窗 */}
            <WarehouseForm
                visible={formVisible}
                warehouse={currentWarehouse}
                onCancel={() => setFormVisible(false)}
                onSuccess={() => {
                    setFormVisible(false)
                    fetchData()
                }}
            />
        </div>
    )
}

export default WarehouseList

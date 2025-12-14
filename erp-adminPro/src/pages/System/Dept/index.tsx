import React, { useState, useEffect } from 'react'
import { Table, Button, Space, Tag, Input, Select, message, App } from 'antd'
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons'
import type { ColumnsType } from 'antd/es/table'
import { getDeptTree, deleteDept, type Dept } from '@/api/system/dept'
import DeptForm from './DeptForm'
import './index.css'

const { Search } = Input
const { Option } = Select

const DeptList: React.FC = () => {
    const { modal } = App.useApp()

    // 状态定义
    const [loading, setLoading] = useState(false)
    const [dataSource, setDataSource] = useState<Dept[]>([])
    const [expandedRowKeys, setExpandedRowKeys] = useState<React.Key[]>([])

    // 查询条件
    const [searchName, setSearchName] = useState<string>('')
    const [searchStatus, setSearchStatus] = useState<number | undefined>()

    // 弹窗状态
    const [formVisible, setFormVisible] = useState(false)
    const [currentDept, setCurrentDept] = useState<Dept | null>(null)

    // 加载数据
    useEffect(() => {
        fetchData()
    }, [])

    const fetchData = async () => {
        setLoading(true)
        try {
            const res = await getDeptTree() as any
            console.log('🔍 Dept tree API response:', res)
            const deptTree = Array.isArray(res) ? res : []
            setDataSource(deptTree)
            // 默认展开第一层
            const firstLevelKeys = deptTree.map(dept => dept.id)
            setExpandedRowKeys(firstLevelKeys)
        } catch (error) {
            message.error('加载数据失败')
        } finally {
            setLoading(false)
        }
    }

    // 过滤数据
    const filterData = (data: Dept[]): Dept[] => {
        return data.filter(item => {
            const matchName = !searchName || item.name.includes(searchName)
            const matchStatus = searchStatus === undefined || item.status === searchStatus

            if (matchName && matchStatus) {
                if (item.children && item.children.length > 0) {
                    item.children = filterData(item.children)
                }
                return true
            }
            return false
        })
    }

    const filteredData = searchName || searchStatus !== undefined ? filterData([...dataSource]) : dataSource

    // 表格列定义
    const columns: ColumnsType<Dept> = [
        {
            title: '部门名称',
            dataIndex: 'name',
            key: 'name',
            width: 200
        },
        {
            title: '部门编号',
            dataIndex: 'code',
            key: 'code',
            width: 120
        },
        {
            title: '负责人',
            dataIndex: 'leader',
            key: 'leader',
            width: 100
        },
        {
            title: '联系电话',
            dataIndex: 'phone',
            key: 'phone',
            width: 130
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
            title: '排序',
            dataIndex: 'sort',
            key: 'sort',
            width: 80
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
            width: 200,
            fixed: 'right',
            render: (_, record) => (
                <Space size="small">
                    <Button
                        type="link"
                        size="small"
                        icon={<PlusOutlined />}
                        onClick={() => handleAddChild(record)}
                    >
                        新增下级
                    </Button>
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
                    >
                        删除
                    </Button>
                </Space>
            )
        }
    ]

    // 事件处理
    const handleAdd = () => {
        setCurrentDept(null)
        setFormVisible(true)
    }

    const handleAddChild = (record: Dept) => {
        setCurrentDept({ ...record, id: 0, parentId: record.id } as any)
        setFormVisible(true)
    }

    const handleEdit = (record: Dept) => {
        setCurrentDept(record)
        setFormVisible(true)
    }

    const handleDelete = (record: Dept) => {
        modal.confirm({
            title: '确认删除',
            content: `确定要删除部门"${record.name}"吗? 删除后该部门下的子部门也将被删除。`,
            okText: '确定',
            cancelText: '取消',
            onOk: async () => {
                try {
                    await deleteDept(record.id)
                    message.success('删除成功')
                    fetchData()
                } catch (error) {
                    message.error('删除失败')
                }
            }
        })
    }

    const handleSearch = () => {
        // 搜索时重新过滤数据
        setExpandedRowKeys([])
    }

    const handleReset = () => {
        setSearchName('')
        setSearchStatus(undefined)
    }

    return (
        <div className="dept-list-container">
            {/* 搜索栏 */}
            <div className="search-bar">
                <Space size="middle">
                    <Input
                        placeholder="请输入部门名称"
                        allowClear
                        style={{ width: 200 }}
                        value={searchName}
                        onChange={e => setSearchName(e.target.value)}
                    />
                    <Select
                        placeholder="部门状态"
                        allowClear
                        style={{ width: 120 }}
                        value={searchStatus}
                        onChange={setSearchStatus}
                    >
                        <Option value={1}>启用</Option>
                        <Option value={0}>禁用</Option>
                    </Select>
                    <Button type="primary" onClick={handleSearch}>
                        搜索
                    </Button>
                    <Button onClick={handleReset}>
                        重置
                    </Button>
                    <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
                        新增部门
                    </Button>
                </Space>
            </div>

            {/* 表格 */}
            <Table
                rowKey="id"
                loading={loading}
                dataSource={filteredData}
                columns={columns}
                scroll={{ x: 1200 }}
                pagination={false}
                expandable={{
                    expandedRowKeys,
                    onExpandedRowsChange: (keys) => setExpandedRowKeys(keys)
                }}
            />

            {/* 部门表单弹窗 */}
            <DeptForm
                visible={formVisible}
                dept={currentDept}
                allDepts={dataSource}
                onCancel={() => setFormVisible(false)}
                onSuccess={() => {
                    setFormVisible(false)
                    fetchData()
                }}
            />
        </div>
    )
}

export default DeptList

import React, { useState, useEffect } from 'react'
import { Table, Button, Space, Tag, Input, Select, message, App } from 'antd'
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons'
import type { ColumnsType } from 'antd/es/table'
import { getMenuTree, deleteMenu, type Menu } from '@/api/system/menu'
import MenuForm from './MenuForm'
import './index.css'

const { Search } = Input
const { Option } = Select

const MenuList: React.FC = () => {
    const { modal } = App.useApp()

    // 状态定义
    const [loading, setLoading] = useState(false)
    const [dataSource, setDataSource] = useState<Menu[]>([])
    const [expandedRowKeys, setExpandedRowKeys] = useState<React.Key[]>([])

    // 查询条件
    const [searchName, setSearchName] = useState<string>('')
    const [searchVisible, setSearchVisible] = useState<number | undefined>()

    // 弹窗状态
    const [formVisible, setFormVisible] = useState(false)
    const [currentMenu, setCurrentMenu] = useState<Menu | null>(null)

    // 加载数据
    useEffect(() => {
        fetchData()
    }, [])

    const fetchData = async () => {
        setLoading(true)
        try {
            const res = await getMenuTree() as any
            console.log('🔍 Menu tree API response:', res)
            const menuTree = Array.isArray(res) ? res : []
            setDataSource(menuTree)
            // 默认展开第一层
            const firstLevelKeys = menuTree.map(menu => menu.id)
            setExpandedRowKeys(firstLevelKeys)
        } catch (error) {
            message.error('加载数据失败')
        } finally {
            setLoading(false)
        }
    }

    // 过滤数据
    const filterData = (data: Menu[]): Menu[] => {
        return data.filter(item => {
            const matchName = !searchName || item.name.includes(searchName)
            const matchVisible = searchVisible === undefined || item.visible === searchVisible

            if (matchName && matchVisible) {
                if (item.children && item.children.length > 0) {
                    item.children = filterData(item.children)
                }
                return true
            }
            return false
        })
    }

    const filteredData = searchName || searchVisible !== undefined ? filterData([...dataSource]) : dataSource

    // 菜单类型标签
    const getMenuTypeTag = (type: number) => {
        const typeMap = {
            0: { text: '目录', color: 'blue' },
            1: { text: '菜单', color: 'green' },
            2: { text: '按钮', color: 'orange' }
        }
        const config = typeMap[type as keyof typeof typeMap] || { text: '未知', color: 'default' }
        return <Tag color={config.color}>{config.text}</Tag>
    }

    // 表格列定义
    const columns: ColumnsType<Menu> = [
        {
            title: '菜单名称',
            dataIndex: 'name',
            key: 'name',
            width: 200
        },
        {
            title: '图标',
            dataIndex: 'icon',
            key: 'icon',
            width: 100,
            render: (icon: string) => icon || '-'
        },
        {
            title: '路径',
            dataIndex: 'path',
            key: 'path',
            width: 180
        },
        {
            title: '类型',
            dataIndex: 'menuType',
            key: 'menuType',
            width: 80,
            render: (type: number) => getMenuTypeTag(type)
        },
        {
            title: '权限标识',
            dataIndex: 'permission',
            key: 'permission',
            width: 150,
            render: (permission: string) => permission || '-'
        },
        {
            title: '可见',
            dataIndex: 'visible',
            key: 'visible',
            width: 80,
            render: (visible: number) => (
                <Tag color={visible === 1 ? 'success' : 'error'}>
                    {visible === 1 ? '是' : '否'}
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
        setCurrentMenu(null)
        setFormVisible(true)
    }

    const handleAddChild = (record: Menu) => {
        setCurrentMenu({ ...record, id: 0, parentId: record.id } as any)
        setFormVisible(true)
    }

    const handleEdit = (record: Menu) => {
        setCurrentMenu(record)
        setFormVisible(true)
    }

    const handleDelete = (record: Menu) => {
        modal.confirm({
            title: '确认删除',
            content: `确定要删除菜单"${record.name}"吗? 删除后该菜单下的子菜单也将被删除。`,
            okText: '确定',
            cancelText: '取消',
            onOk: async () => {
                try {
                    await deleteMenu(record.id)
                    message.success('删除成功')
                    fetchData()
                } catch (error) {
                    message.error('删除失败')
                }
            }
        })
    }

    const handleSearch = () => {
        setExpandedRowKeys([])
    }

    const handleReset = () => {
        setSearchName('')
        setSearchVisible(undefined)
    }

    return (
        <div className="menu-list-container">
            {/* 搜索栏 */}
            <div className="search-bar">
                <Space size="middle">
                    <Input
                        placeholder="请输入菜单名称"
                        allowClear
                        style={{ width: 200 }}
                        value={searchName}
                        onChange={e => setSearchName(e.target.value)}
                    />
                    <Select
                        placeholder="是否可见"
                        allowClear
                        style={{ width: 120 }}
                        value={searchVisible}
                        onChange={setSearchVisible}
                    >
                        <Option value={1}>是</Option>
                        <Option value={0}>否</Option>
                    </Select>
                    <Button type="primary" onClick={handleSearch}>
                        搜索
                    </Button>
                    <Button onClick={handleReset}>
                        重置
                    </Button>
                    <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
                        新增菜单
                    </Button>
                </Space>
            </div>

            {/* 表格 */}
            <Table
                rowKey="id"
                loading={loading}
                dataSource={filteredData}
                columns={columns}
                scroll={{ x: 1400 }}
                pagination={false}
                expandable={{
                    expandedRowKeys,
                    onExpandedRowsChange: (keys) => setExpandedRowKeys(keys)
                }}
            />

            {/* 菜单表单弹窗 */}
            <MenuForm
                visible={formVisible}
                menu={currentMenu}
                allMenus={dataSource}
                onCancel={() => setFormVisible(false)}
                onSuccess={() => {
                    setFormVisible(false)
                    fetchData()
                }}
            />
        </div>
    )
}

export default MenuList

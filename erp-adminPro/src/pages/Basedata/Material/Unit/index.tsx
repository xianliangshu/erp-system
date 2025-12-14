import React, { useState, useEffect } from 'react';
import { Table, Button, Space, Modal, message, Form, Input, InputNumber, Select, Card } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';
import { getUnitPage, saveUnit, updateUnit, deleteUnit } from '@/api/material/unit';

const { Search } = Input;

const UnitManagement: React.FC = () => {
    const [loading, setLoading] = useState(false);
    const [dataSource, setDataSource] = useState([]);
    const [total, setTotal] = useState(0);
    const [current, setCurrent] = useState(1);
    const [pageSize, setPageSize] = useState(10);
    const [searchName, setSearchName] = useState('');
    const [searchStatus, setSearchStatus] = useState<number | undefined>(undefined);

    const [modalVisible, setModalVisible] = useState(false);
    const [modalTitle, setModalTitle] = useState('');
    const [editingId, setEditingId] = useState<number | null>(null);
    const [form] = Form.useForm();

    // 加载数据
    const loadData = async () => {
        setLoading(true);
        try {
            const params = {
                current,
                size: pageSize,
                name: searchName || undefined,
                status: searchStatus,
            };
            const res = await getUnitPage(params);
            if (res.code === 200) {
                setDataSource(res.data.records);
                setTotal(res.data.total);
            }
        } catch (error) {
            message.error('加载数据失败');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadData();
    }, [current, pageSize]);

    // 搜索
    const handleSearch = () => {
        setCurrent(1);
        loadData();
    };

    // 重置
    const handleReset = () => {
        setSearchName('');
        setSearchStatus(undefined);
        setCurrent(1);
        loadData();
    };

    // 新增
    const handleAdd = () => {
        setModalTitle('新增单位');
        setEditingId(null);
        form.resetFields();
        setModalVisible(true);
    };

    // 编辑
    const handleEdit = (record: any) => {
        setModalTitle('编辑单位');
        setEditingId(record.id);
        form.setFieldsValue(record);
        setModalVisible(true);
    };

    // 删除
    const handleDelete = (id: number) => {
        Modal.confirm({
            title: '确认删除',
            content: '确定要删除这个单位吗?',
            onOk: async () => {
                try {
                    const res = await deleteUnit(id);
                    if (res.code === 200) {
                        message.success('删除成功');
                        loadData();
                    }
                } catch (error) {
                    message.error('删除失败');
                }
            },
        });
    };

    // 提交表单
    const handleSubmit = async () => {
        try {
            const values = await form.validateFields();
            if (editingId) {
                const res = await updateUnit(editingId, values);
                if (res.code === 200) {
                    message.success('更新成功');
                    setModalVisible(false);
                    loadData();
                }
            } else {
                const res = await saveUnit(values);
                if (res.code === 200) {
                    message.success('新增成功');
                    setModalVisible(false);
                    loadData();
                }
            }
        } catch (error) {
            console.error(error);
        }
    };

    const columns = [
        {
            title: '单位编号',
            dataIndex: 'code',
            key: 'code',
            width: 150,
        },
        {
            title: '单位名称',
            dataIndex: 'name',
            key: 'name',
            width: 150,
        },
        {
            title: '排序',
            dataIndex: 'sort',
            key: 'sort',
            width: 100,
        },
        {
            title: '状态',
            dataIndex: 'status',
            key: 'status',
            width: 100,
            render: (status: number) => (
                <span style={{ color: status === 1 ? '#52c41a' : '#ff4d4f' }}>
                    {status === 1 ? '启用' : '禁用'}
                </span>
            ),
        },
        {
            title: '备注',
            dataIndex: 'remark',
            key: 'remark',
            ellipsis: true,
        },
        {
            title: '操作',
            key: 'action',
            width: 150,
            fixed: 'right' as const,
            render: (_: any, record: any) => (
                <Space>
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
                        onClick={() => handleDelete(record.id)}
                    >
                        删除
                    </Button>
                </Space>
            ),
        },
    ];

    return (
        <div style={{ padding: '24px' }}>
            <Card>
                {/* 搜索栏 */}
                <Space style={{ marginBottom: 16 }}>
                    <Search
                        placeholder="单位名称"
                        value={searchName}
                        onChange={(e) => setSearchName(e.target.value)}
                        onSearch={handleSearch}
                        style={{ width: 200 }}
                    />
                    <Select
                        placeholder="状态"
                        value={searchStatus}
                        onChange={setSearchStatus}
                        allowClear
                        style={{ width: 120 }}
                    >
                        <Select.Option value={1}>启用</Select.Option>
                        <Select.Option value={0}>禁用</Select.Option>
                    </Select>
                    <Button onClick={handleSearch} type="primary">
                        搜索
                    </Button>
                    <Button onClick={handleReset}>重置</Button>
                    <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
                        新增单位
                    </Button>
                </Space>

                {/* 表格 */}
                <Table
                    loading={loading}
                    columns={columns}
                    dataSource={dataSource}
                    rowKey="id"
                    pagination={{
                        current,
                        pageSize,
                        total,
                        showSizeChanger: true,
                        showQuickJumper: true,
                        showTotal: (total) => `共 ${total} 条`,
                        onChange: (page, size) => {
                            setCurrent(page);
                            setPageSize(size);
                        },
                    }}
                />
            </Card>

            {/* 新增/编辑弹窗 */}
            <Modal
                title={modalTitle}
                open={modalVisible}
                onOk={handleSubmit}
                onCancel={() => setModalVisible(false)}
                width={600}
            >
                <Form form={form} labelCol={{ span: 6 }} wrapperCol={{ span: 16 }}>
                    <Form.Item
                        label="单位编号"
                        name="code"
                        tooltip="留空则自动生成"
                    >
                        <Input placeholder="留空自动生成" />
                    </Form.Item>
                    <Form.Item
                        label="单位名称"
                        name="name"
                        rules={[{ required: true, message: '请输入单位名称' }]}
                    >
                        <Input placeholder="请输入单位名称" />
                    </Form.Item>
                    <Form.Item label="排序" name="sort" initialValue={0}>
                        <InputNumber min={0} style={{ width: '100%' }} />
                    </Form.Item>
                    <Form.Item label="状态" name="status" initialValue={1}>
                        <Select>
                            <Select.Option value={1}>启用</Select.Option>
                            <Select.Option value={0}>禁用</Select.Option>
                        </Select>
                    </Form.Item>
                    <Form.Item label="备注" name="remark">
                        <Input.TextArea rows={4} placeholder="请输入备注" />
                    </Form.Item>
                </Form>
            </Modal>
        </div>
    );
};

export default UnitManagement;

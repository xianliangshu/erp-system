import React, { useState, useEffect } from 'react';
import { Table, Card, Space, Input, Select, Button, message, Tag, Modal, Form } from 'antd';
import { SearchOutlined, ReloadOutlined, PlusOutlined, EditOutlined, DeleteOutlined, ExclamationCircleOutlined } from '@ant-design/icons';
import { getSettleItemPage, addSettleItem, updateSettleItem, deleteSettleItem } from '@/api/business/settle';

const IncomeExpenseItemList: React.FC = () => {
    const [loading, setLoading] = useState(false);
    const [dataSource, setDataSource] = useState<any[]>([]);
    const [total, setTotal] = useState(0);
    const [current, setCurrent] = useState(1);
    const [pageSize, setPageSize] = useState(10);

    const [searchCode, setSearchCode] = useState('');
    const [searchName, setSearchName] = useState('');
    const [searchItemType, setSearchItemType] = useState<number | undefined>(undefined);
    const [searchStatus, setSearchStatus] = useState<number | undefined>(undefined);

    const [modalVisible, setModalVisible] = useState(false);
    const [modalTitle, setModalTitle] = useState('');
    const [form] = Form.useForm();
    const [editingId, setEditingId] = useState<number | null>(null);

    const loadData = async () => {
        setLoading(true);
        try {
            const res: any = await getSettleItemPage({
                current,
                size: pageSize,
                code: searchCode,
                name: searchName,
                itemType: searchItemType,
                status: searchStatus,
            });
            setDataSource(res?.records || []);
            setTotal(res?.total || 0);
        } catch (e) {
            message.error('加载数据失败');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadData();
    }, [current, pageSize]);

    const handleSearch = () => {
        setCurrent(1);
        loadData();
    };

    const handleReset = () => {
        setSearchCode('');
        setSearchName('');
        setSearchItemType(undefined);
        setSearchStatus(undefined);
        setCurrent(1);
        loadData();
    };

    const handleAdd = () => {
        setModalTitle('新增收支项目');
        setEditingId(null);
        form.resetFields();
        form.setFieldsValue({ status: 1 });
        setModalVisible(true);
    };

    const handleEdit = (record: any) => {
        setModalTitle('编辑收支项目');
        setEditingId(record.id);
        form.setFieldsValue(record);
        setModalVisible(true);
    };

    const handleDelete = (record: any) => {
        Modal.confirm({
            title: '确认删除',
            icon: <ExclamationCircleOutlined />,
            content: `确定要删除项目 "${record.name}" 吗？`,
            onOk: async () => {
                try {
                    await deleteSettleItem(record.id);
                    message.success('删除成功');
                    loadData();
                } catch (e) {
                    message.error('删除失败');
                }
            }
        });
    };

    const handleModalOk = async () => {
        try {
            const values = await form.validateFields();
            if (editingId) {
                await updateSettleItem({ ...values, id: editingId });
                message.success('修改成功');
            } else {
                await addSettleItem(values);
                message.success('新增成功');
            }
            setModalVisible(false);
            loadData();
        } catch (e) {
            console.error(e);
        }
    };

    const columns = [
        { title: '编号', dataIndex: 'code', key: 'code', width: 150 },
        { title: '名称', dataIndex: 'name', key: 'name' },
        {
            title: '项目类型',
            dataIndex: 'itemType',
            key: 'itemType',
            width: 120,
            render: (val: number) => (
                <Tag color={val === 1 ? 'green' : 'orange'}>
                    {val === 1 ? '收入' : '支出'}
                </Tag>
            )
        },
        {
            title: '状态',
            dataIndex: 'status',
            key: 'status',
            width: 100,
            render: (val: number) => (
                <Tag color={val === 1 ? 'success' : 'error'}>
                    {val === 1 ? '启用' : '禁用'}
                </Tag>
            )
        },
        { title: '备注', dataIndex: 'description', key: 'description', ellipsis: true },
        { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
        {
            title: '操作',
            key: 'action',
            width: 150,
            render: (_: any, record: any) => (
                <Space>
                    <Button type="link" size="small" icon={<EditOutlined />} onClick={() => handleEdit(record)}>编辑</Button>
                    <Button type="link" size="small" danger icon={<DeleteOutlined />} onClick={() => handleDelete(record)}>删除</Button>
                </Space>
            )
        },
    ];

    return (
        <div style={{ padding: '24px' }}>
            <Card title="收支项目管理">
                <Space style={{ marginBottom: 16 }} wrap>
                    <Input
                        placeholder="编号"
                        style={{ width: 150 }}
                        value={searchCode}
                        onChange={e => setSearchCode(e.target.value)}
                        onPressEnter={handleSearch}
                    />
                    <Input
                        placeholder="名称"
                        style={{ width: 180 }}
                        value={searchName}
                        onChange={e => setSearchName(e.target.value)}
                        onPressEnter={handleSearch}
                    />
                    <Select
                        placeholder="项目类型"
                        style={{ width: 120 }}
                        value={searchItemType}
                        onChange={setSearchItemType}
                        allowClear
                    >
                        <Select.Option value={1}>收入</Select.Option>
                        <Select.Option value={2}>支出</Select.Option>
                    </Select>
                    <Select
                        placeholder="状态"
                        style={{ width: 100 }}
                        value={searchStatus}
                        onChange={setSearchStatus}
                        allowClear
                    >
                        <Select.Option value={1}>启用</Select.Option>
                        <Select.Option value={0}>禁用</Select.Option>
                    </Select>
                    <Button type="primary" icon={<SearchOutlined />} onClick={handleSearch}>查询</Button>
                    <Button icon={<ReloadOutlined />} onClick={handleReset}>重置</Button>
                    <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>新增</Button>
                </Space>
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
                        onChange: (page, size) => {
                            setCurrent(page);
                            setPageSize(size);
                        }
                    }}
                />
            </Card>

            <Modal
                title={modalTitle}
                open={modalVisible}
                onOk={handleModalOk}
                onCancel={() => setModalVisible(false)}
                destroyOnClose
            >
                <Form form={form} layout="vertical">
                    <Form.Item name="code" label="编号" extra="留空将自动生成">
                        <Input placeholder="请输入编号" />
                    </Form.Item>
                    <Form.Item name="name" label="名称" rules={[{ required: true, message: '请输入名称' }]}>
                        <Input placeholder="请输入名称" />
                    </Form.Item>
                    <Form.Item name="itemType" label="项目类型" rules={[{ required: true, message: '请选择项目类型' }]}>
                        <Select placeholder="请选择项目类型">
                            <Select.Option value={1}>收入</Select.Option>
                            <Select.Option value={2}>支出</Select.Option>
                        </Select>
                    </Form.Item>
                    <Form.Item name="status" label="状态" rules={[{ required: true }]}>
                        <Select>
                            <Select.Option value={1}>启用</Select.Option>
                            <Select.Option value={0}>禁用</Select.Option>
                        </Select>
                    </Form.Item>
                    <Form.Item name="description" label="备注">
                        <Input.TextArea rows={3} placeholder="请输入备注" />
                    </Form.Item>
                </Form>
            </Modal>
        </div>
    );
};

export default IncomeExpenseItemList;

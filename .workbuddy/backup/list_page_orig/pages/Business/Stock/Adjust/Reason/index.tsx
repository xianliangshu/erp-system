import React, { useState, useEffect } from 'react';
import { Table, Card, Space, Input, Select, Button, message, Tag, Modal, Form, Switch } from 'antd';
import { SearchOutlined, ReloadOutlined, PlusOutlined, EditOutlined, DeleteOutlined, ExclamationCircleOutlined } from '@ant-design/icons';
import { getAdjustReasonPage, addAdjustReason, updateAdjustReason, deleteAdjustReason } from '@/api/stock/adjustReason';

const { Search } = Input;

const AdjustReasonList: React.FC = () => {
    const [loading, setLoading] = useState(false);
    const [dataSource, setDataSource] = useState<any[]>([]);
    const [total, setTotal] = useState(0);
    const [current, setCurrent] = useState(1);
    const [pageSize, setPageSize] = useState(10);

    // 搜索条件
    const [searchCode, setSearchCode] = useState('');
    const [searchName, setSearchName] = useState('');
    const [searchStatus, setSearchStatus] = useState<number | undefined>(undefined);

    // 表单相关
    const [formVisible, setFormVisible] = useState(false);
    const [formData, setFormData] = useState<any>(null);
    const [form] = Form.useForm();
    const [modal, contextHolder] = Modal.useModal();

    const loadData = async () => {
        setLoading(true);
        try {
            const params = {
                current,
                size: pageSize,
                code: searchCode || undefined,
                name: searchName || undefined,
                status: searchStatus,
            };
            const res: any = await getAdjustReasonPage(params);
            // 响应拦截器已返回 data.data，所以直接取 records
            setDataSource(res?.records || []);
            setTotal(res?.total || 0);
        } catch (error) {
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
        setSearchStatus(undefined);
        setCurrent(1);
        loadData();
    };

    const handleAdd = () => {
        setFormData(null);
        form.resetFields();
        form.setFieldsValue({ status: 1 });
        setFormVisible(true);
    };

    const handleEdit = (record: any) => {
        setFormData(record);
        form.setFieldsValue(record);
        setFormVisible(true);
    };

    const handleDelete = (record: any) => {
        modal.confirm({
            title: '确认删除',
            icon: <ExclamationCircleOutlined />,
            content: `确定要删除调整原因 "${record.name}" 吗？`,
            okText: '确定',
            cancelText: '取消',
            onOk: async () => {
                try {
                    await deleteAdjustReason(record.id);
                    message.success('删除成功');
                    loadData();
                } catch (error) {
                    message.error('删除失败');
                }
            },
        });
    };

    const handleSubmit = async () => {
        try {
            const values = await form.validateFields();
            if (formData?.id) {
                await updateAdjustReason({ ...values, id: formData.id });
                message.success('修改成功');
            } else {
                await addAdjustReason(values);
                message.success('新增成功');
            }
            setFormVisible(false);
            loadData();
        } catch (error) {
            console.error('提交失败', error);
        }
    };

    const columns = [
        { title: '编号', dataIndex: 'code', key: 'code', width: 100 },
        { title: '名称', dataIndex: 'name', key: 'name' },
        {
            title: '状态',
            dataIndex: 'status',
            key: 'status',
            width: 100,
            render: (status: number) => (
                <Tag color={status === 1 ? 'green' : 'red'}>
                    {status === 1 ? '启用' : '禁用'}
                </Tag>
            ),
        },
        { title: '备注', dataIndex: 'remark', key: 'remark' },
        { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
        {
            title: '操作',
            key: 'action',
            width: 150,
            render: (_: any, record: any) => (
                <Space>
                    <Button type="link" size="small" icon={<EditOutlined />} onClick={() => handleEdit(record)}>
                        编辑
                    </Button>
                    <Button type="link" size="small" danger icon={<DeleteOutlined />} onClick={() => handleDelete(record)}>
                        删除
                    </Button>
                </Space>
            ),
        },
    ];

    return (
        <div style={{ padding: '24px' }}>
            {contextHolder}
            <Card title="调整原因管理">
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
                        style={{ width: 150 }}
                        value={searchName}
                        onChange={e => setSearchName(e.target.value)}
                        onPressEnter={handleSearch}
                    />
                    <Select
                        placeholder="状态"
                        style={{ width: 120 }}
                        value={searchStatus}
                        onChange={setSearchStatus}
                        allowClear
                    >
                        <Select.Option value={1}>启用</Select.Option>
                        <Select.Option value={0}>禁用</Select.Option>
                    </Select>
                    <Button type="primary" icon={<SearchOutlined />} onClick={handleSearch}>
                        查询
                    </Button>
                    <Button icon={<ReloadOutlined />} onClick={handleReset}>
                        重置
                    </Button>
                    <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
                        新增
                    </Button>
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
                        showTotal: (total) => `共 ${total} 条`,
                        onChange: (page, size) => {
                            setCurrent(page);
                            setPageSize(size);
                        },
                    }}
                />
            </Card>

            {/* 新增/编辑表单弹窗 */}
            <Modal
                title={formData ? '编辑调整原因' : '新增调整原因'}
                open={formVisible}
                onOk={handleSubmit}
                onCancel={() => setFormVisible(false)}
                okText="确定"
                cancelText="取消"
            >
                <Form form={form} layout="vertical">
                    <Form.Item name="code" label="编号">
                        <Input placeholder="留空自动生成" />
                    </Form.Item>
                    <Form.Item name="name" label="名称" rules={[{ required: true, message: '请输入名称' }]}>
                        <Input placeholder="请输入名称" />
                    </Form.Item>
                    <Form.Item name="status" label="状态" valuePropName="checked" getValueFromEvent={(checked) => checked ? 1 : 0}>
                        <Switch checkedChildren="启用" unCheckedChildren="禁用" defaultChecked />
                    </Form.Item>
                    <Form.Item name="remark" label="备注">
                        <Input.TextArea rows={3} placeholder="请输入备注" />
                    </Form.Item>
                </Form>
            </Modal>
        </div>
    );
};

export default AdjustReasonList;

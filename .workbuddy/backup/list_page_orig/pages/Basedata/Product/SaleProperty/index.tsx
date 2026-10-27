import React, { useState, useEffect } from 'react';
import { Table, Button, Space, Modal, message, Form, Input, InputNumber, Select, Card, Tag, App } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, MinusCircleOutlined, ExclamationCircleOutlined } from '@ant-design/icons';
import {
    getSalePropertyPage,
    saveSaleProperty,
    updateSaleProperty,
    deleteSaleProperty,
} from '@/api/basedata/saleProperty';

const { Search } = Input;
const { TextArea } = Input;

/**
 * 商品销售属性维护
 *
 * 属性组决定 SKU 的维度（如「颜色」「尺码」），其下的属性值决定维度的取值（如「红」「XL」）。
 * 后端对属性值做差异更新，编辑时不会重建已有属性值，因此 SKU 的绑定关系不会断。
 */
const SalePropertyManagement: React.FC = () => {
    const { modal } = App.useApp();
    const [loading, setLoading] = useState(false);
    const [dataSource, setDataSource] = useState<any[]>([]);
    const [total, setTotal] = useState(0);
    const [current, setCurrent] = useState(1);
    const [pageSize, setPageSize] = useState(10);

    const [searchName, setSearchName] = useState('');
    const [searchCode, setSearchCode] = useState('');
    const [searchStatus, setSearchStatus] = useState<number | undefined>(undefined);

    const [modalVisible, setModalVisible] = useState(false);
    const [modalTitle, setModalTitle] = useState('');
    const [editingId, setEditingId] = useState<number | null>(null);
    const [form] = Form.useForm();

    const loadData = async () => {
        setLoading(true);
        try {
            const data = await getSalePropertyPage({
                current,
                size: pageSize,
                name: searchName || undefined,
                code: searchCode || undefined,
                status: searchStatus,
            });
            setDataSource(data.records || []);
            setTotal(data.total || 0);
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
        setSearchName('');
        setSearchCode('');
        setSearchStatus(undefined);
        setCurrent(1);
        loadData();
    };

    const handleAdd = () => {
        setModalTitle('新增销售属性');
        setEditingId(null);
        form.resetFields();
        form.setFieldsValue({ status: 1, sort: 0, items: [{ name: '' }] });
        setModalVisible(true);
    };

    const handleEdit = (record: any) => {
        setModalTitle('编辑销售属性');
        setEditingId(record.id);
        form.setFieldsValue({
            name: record.name,
            code: record.code,
            sort: record.sort,
            status: record.status,
            remark: record.remark,
            items: (record.items || []).map((i: any) => ({ name: i.name })),
        });
        setModalVisible(true);
    };

    const handleDelete = (id: number) => {
        modal.confirm({
            title: '确认删除',
            icon: <ExclamationCircleOutlined />,
            content: '确定要删除这个销售属性吗？已被 SKU 使用的属性不允许删除。',
            okText: '确定',
            cancelText: '取消',
            onOk: async () => {
                try {
                    await deleteSaleProperty(id);
                    message.success('删除成功');
                    loadData();
                } catch (error: any) {
                    message.error(error.message || '删除失败');
                }
            },
        });
    };

    const handleSubmit = async () => {
        try {
            const values = await form.validateFields();
            const payload = {
                ...values,
                items: (values.items || []).filter((i: any) => i && i.name),
            };
            if (editingId) {
                await updateSaleProperty(editingId, payload);
                message.success('更新成功');
            } else {
                await saveSaleProperty(payload);
                message.success('新增成功');
            }
            setModalVisible(false);
            loadData();
        } catch (error: any) {
            if (error?.errorFields) return;
            message.error(error.message || '操作失败');
        }
    };

    const columns = [
        { title: '属性编码', dataIndex: 'code', key: 'code', width: 130 },
        { title: '属性名称', dataIndex: 'name', key: 'name', width: 140 },
        {
            title: '属性值',
            dataIndex: 'items',
            key: 'items',
            render: (items: any[]) =>
                items && items.length ? (
                    <Space size={[4, 4]} wrap>
                        {items.map((i) => (
                            <Tag key={i.id} color="blue">
                                {i.name}
                            </Tag>
                        ))}
                    </Space>
                ) : (
                    <span style={{ color: '#999' }}>未维护</span>
                ),
        },
        { title: '排序', dataIndex: 'sort', key: 'sort', width: 80 },
        {
            title: '状态',
            dataIndex: 'status',
            key: 'status',
            width: 90,
            render: (status: number) => (
                <span style={{ color: status === 1 ? '#52c41a' : '#ff4d4f' }}>
                    {status === 1 ? '启用' : '禁用'}
                </span>
            ),
        },
        {
            title: '操作',
            key: 'action',
            width: 150,
            fixed: 'right' as const,
            render: (_: any, record: any) => (
                <Space>
                    <Button type="link" size="small" icon={<EditOutlined />} onClick={() => handleEdit(record)}>
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
                <Space style={{ marginBottom: 16 }} wrap>
                    <Search
                        placeholder="属性名称"
                        value={searchName}
                        onChange={(e) => setSearchName(e.target.value)}
                        onSearch={handleSearch}
                        style={{ width: 180 }}
                    />
                    <Search
                        placeholder="属性编码"
                        value={searchCode}
                        onChange={(e) => setSearchCode(e.target.value)}
                        onSearch={handleSearch}
                        style={{ width: 180 }}
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
                        新增销售属性
                    </Button>
                </Space>

                <Table
                    loading={loading}
                    columns={columns}
                    dataSource={dataSource}
                    rowKey="id"
                    scroll={{ x: 900 }}
                    pagination={{
                        current,
                        pageSize,
                        total,
                        showSizeChanger: true,
                        showTotal: (t) => `共 ${t} 条`,
                        onChange: (page, size) => {
                            setCurrent(page);
                            setPageSize(size);
                        },
                    }}
                />
            </Card>

            <Modal
                title={modalTitle}
                open={modalVisible}
                onOk={handleSubmit}
                onCancel={() => setModalVisible(false)}
                width={640}
                destroyOnClose
            >
                <Form form={form} labelCol={{ span: 5 }} wrapperCol={{ span: 18 }}>
                    <Form.Item label="属性名称" name="name" rules={[{ required: true, message: '请输入属性名称' }]}>
                        <Input placeholder="如：颜色、尺码" />
                    </Form.Item>
                    <Form.Item label="属性编码" name="code" tooltip="留空则自动生成，如 SP000001">
                        <Input placeholder="留空自动生成" />
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

                    <Form.Item label="属性值" required>
                        <Form.List name="items">
                            {(fields, { add, remove }) => (
                                <>
                                    {fields.map(({ key, name: fieldName, ...rest }) => (
                                        <Space key={key} align="baseline" style={{ display: 'flex', marginBottom: 8 }}>
                                            <Form.Item
                                                {...rest}
                                                name={[fieldName, 'name']}
                                                rules={[{ required: true, message: '请输入属性值' }]}
                                                style={{ marginBottom: 0 }}
                                            >
                                                <Input placeholder="属性值，如 红色 / XL" style={{ width: 380 }} />
                                            </Form.Item>
                                            {fields.length > 1 && (
                                                <MinusCircleOutlined onClick={() => remove(fieldName)} />
                                            )}
                                        </Space>
                                    ))}
                                    <Button type="dashed" onClick={() => add({ name: '' })} block icon={<PlusOutlined />}>
                                        添加属性值
                                    </Button>
                                </>
                            )}
                        </Form.List>
                    </Form.Item>

                    <Form.Item label="备注" name="remark">
                        <TextArea rows={3} placeholder="请输入备注" />
                    </Form.Item>
                </Form>
            </Modal>
        </div>
    );
};

export default SalePropertyManagement;

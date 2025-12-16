import React, { useState, useEffect } from 'react';
import { Table, Button, Space, Modal, message, Form, Input, InputNumber, Select, Card, Row, Col } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';
import { getSupplierPage, saveSupplier, updateSupplier, deleteSupplier } from '@/api/basedata/supplier';

const { Search } = Input;
const { TextArea } = Input;

const SupplierManagement: React.FC = () => {
    const [loading, setLoading] = useState(false);
    const [dataSource, setDataSource] = useState([]);
    const [total, setTotal] = useState(0);
    const [current, setCurrent] = useState(1);
    const [pageSize, setPageSize] = useState(10);

    // 搜索条件
    const [searchName, setSearchName] = useState('');
    const [searchCode, setSearchCode] = useState('');
    const [searchType, setSearchType] = useState<number | undefined>(undefined);
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
                code: searchCode || undefined,
                type: searchType,
                status: searchStatus,
            };
            const data = await getSupplierPage(params);
            setDataSource(data.records);
            setTotal(data.total);
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
        setSearchCode('');
        setSearchType(undefined);
        setSearchStatus(undefined);
        setCurrent(1);
        loadData();
    };

    // 新增
    const handleAdd = () => {
        setModalTitle('新增供应商');
        setEditingId(null);
        form.resetFields();
        setModalVisible(true);
    };

    // 编辑
    const handleEdit = (record: any) => {
        setModalTitle('编辑供应商');
        setEditingId(record.id);
        form.setFieldsValue(record);
        setModalVisible(true);
    };

    // 删除
    const handleDelete = (id: number) => {
        Modal.confirm({
            title: '确认删除',
            content: '确定要删除这个供应商吗?',
            onOk: async () => {
                try {
                    await deleteSupplier(id);
                    message.success('删除成功');
                    loadData();
                } catch (error: any) {
                    message.error(error.message || '删除失败');
                }
            },
        });
    };

    // 提交表单
    const handleSubmit = async () => {
        try {
            const values = await form.validateFields();
            if (editingId) {
                await updateSupplier(editingId, values);
                message.success('更新成功');
                setModalVisible(false);
                loadData();
            } else {
                await saveSupplier(values);
                message.success('新增成功');
                setModalVisible(false);
                loadData();
            }
        } catch (error: any) {
            message.error(error.message || '操作失败');
        }
    };

    const columns = [
        {
            title: '供应商编号',
            dataIndex: 'code',
            key: 'code',
            width: 120,
        },
        {
            title: '供应商名称',
            dataIndex: 'name',
            key: 'name',
            width: 150,
        },
        {
            title: '简称',
            dataIndex: 'shortName',
            key: 'shortName',
            width: 100,
        },
        {
            title: '类型',
            dataIndex: 'type',
            key: 'type',
            width: 100,
            render: (type: number) => {
                const typeMap: any = {
                    1: '原材料供应商',
                    2: '设备供应商',
                    3: '服务供应商',
                };
                return typeMap[type] || '-';
            },
        },
        {
            title: '信用等级',
            dataIndex: 'creditLevel',
            key: 'creditLevel',
            width: 100,
            render: (level: number) => {
                const levelMap: any = {
                    1: '优秀',
                    2: '良好',
                    3: '一般',
                    4: '较差',
                    5: '差',
                };
                const colorMap: any = {
                    1: '#52c41a',
                    2: '#1890ff',
                    3: '#faad14',
                    4: '#ff7a45',
                    5: '#ff4d4f',
                };
                return <span style={{ color: colorMap[level] }}>{levelMap[level] || '-'}</span>;
            },
        },
        {
            title: '联系人',
            dataIndex: 'contactPerson',
            key: 'contactPerson',
            width: 100,
        },
        {
            title: '联系电话',
            dataIndex: 'contactPhone',
            key: 'contactPhone',
            width: 120,
        },
        {
            title: '状态',
            dataIndex: 'status',
            key: 'status',
            width: 80,
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
                <Space style={{ marginBottom: 16 }} wrap>
                    <Search
                        placeholder="供应商名称"
                        value={searchName}
                        onChange={(e) => setSearchName(e.target.value)}
                        onSearch={handleSearch}
                        style={{ width: 180 }}
                    />
                    <Search
                        placeholder="供应商编号"
                        value={searchCode}
                        onChange={(e) => setSearchCode(e.target.value)}
                        onSearch={handleSearch}
                        style={{ width: 180 }}
                    />
                    <Select
                        placeholder="供应商类型"
                        value={searchType}
                        onChange={setSearchType}
                        allowClear
                        style={{ width: 150 }}
                    >
                        <Select.Option value={1}>原材料供应商</Select.Option>
                        <Select.Option value={2}>设备供应商</Select.Option>
                        <Select.Option value={3}>服务供应商</Select.Option>
                    </Select>
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
                        新增供应商
                    </Button>
                </Space>

                {/* 表格 */}
                <Table
                    loading={loading}
                    columns={columns}
                    dataSource={dataSource}
                    rowKey="id"
                    scroll={{ x: 1200 }}
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
                width={900}
            >
                <Form form={form} labelCol={{ span: 6 }} wrapperCol={{ span: 16 }}>
                    <Row gutter={16}>
                        <Col span={12}>
                            <Form.Item
                                label="供应商编号"
                                name="code"
                                tooltip="留空则自动生成"
                            >
                                <Input placeholder="留空自动生成" />
                            </Form.Item>
                        </Col>
                        <Col span={12}>
                            <Form.Item
                                label="供应商名称"
                                name="name"
                                rules={[{ required: true, message: '请输入供应商名称' }]}
                            >
                                <Input placeholder="请输入供应商名称" />
                            </Form.Item>
                        </Col>
                    </Row>
                    <Row gutter={16}>
                        <Col span={12}>
                            <Form.Item label="供应商简称" name="shortName">
                                <Input placeholder="请输入供应商简称" />
                            </Form.Item>
                        </Col>
                        <Col span={12}>
                            <Form.Item label="供应商类型" name="type" initialValue={1}>
                                <Select>
                                    <Select.Option value={1}>原材料供应商</Select.Option>
                                    <Select.Option value={2}>设备供应商</Select.Option>
                                    <Select.Option value={3}>服务供应商</Select.Option>
                                </Select>
                            </Form.Item>
                        </Col>
                    </Row>
                    <Row gutter={16}>
                        <Col span={12}>
                            <Form.Item label="信用等级" name="creditLevel" initialValue={3}>
                                <Select>
                                    <Select.Option value={1}>优秀</Select.Option>
                                    <Select.Option value={2}>良好</Select.Option>
                                    <Select.Option value={3}>一般</Select.Option>
                                    <Select.Option value={4}>较差</Select.Option>
                                    <Select.Option value={5}>差</Select.Option>
                                </Select>
                            </Form.Item>
                        </Col>
                        <Col span={12}>
                            <Form.Item label="联系人" name="contactPerson">
                                <Input placeholder="请输入联系人" />
                            </Form.Item>
                        </Col>
                    </Row>
                    <Row gutter={16}>
                        <Col span={12}>
                            <Form.Item label="联系电话" name="contactPhone">
                                <Input placeholder="请输入联系电话" />
                            </Form.Item>
                        </Col>
                        <Col span={12}>
                            <Form.Item label="联系邮箱" name="contactEmail">
                                <Input placeholder="请输入联系邮箱" />
                            </Form.Item>
                        </Col>
                    </Row>
                    <Form.Item label="联系地址" name="address" labelCol={{ span: 3 }} wrapperCol={{ span: 20 }}>
                        <Input placeholder="请输入联系地址" />
                    </Form.Item>
                    <Form.Item label="网站" name="website" labelCol={{ span: 3 }} wrapperCol={{ span: 20 }}>
                        <Input placeholder="请输入网站" />
                    </Form.Item>
                    <Row gutter={16}>
                        <Col span={12}>
                            <Form.Item label="结算方式" name="settlementMethod" initialValue={1}>
                                <Select>
                                    <Select.Option value={1}>现金</Select.Option>
                                    <Select.Option value={2}>月结</Select.Option>
                                    <Select.Option value={3}>季结</Select.Option>
                                    <Select.Option value={4}>账期</Select.Option>
                                </Select>
                            </Form.Item>
                        </Col>
                        <Col span={12}>
                            <Form.Item label="账期天数" name="paymentDays" initialValue={0}>
                                <InputNumber min={0} style={{ width: '100%' }} />
                            </Form.Item>
                        </Col>
                    </Row>
                    <Row gutter={16}>
                        <Col span={12}>
                            <Form.Item label="开户银行" name="bankName">
                                <Input placeholder="请输入开户银行" />
                            </Form.Item>
                        </Col>
                        <Col span={12}>
                            <Form.Item label="银行账号" name="bankAccount">
                                <Input placeholder="请输入银行账号" />
                            </Form.Item>
                        </Col>
                    </Row>
                    <Row gutter={16}>
                        <Col span={12}>
                            <Form.Item label="税号" name="taxNumber">
                                <Input placeholder="请输入税号" />
                            </Form.Item>
                        </Col>
                        <Col span={12}>
                            <Form.Item label="状态" name="status" initialValue={1}>
                                <Select>
                                    <Select.Option value={1}>启用</Select.Option>
                                    <Select.Option value={0}>禁用</Select.Option>
                                </Select>
                            </Form.Item>
                        </Col>
                    </Row>
                    <Form.Item label="备注" name="remark" labelCol={{ span: 3 }} wrapperCol={{ span: 20 }}>
                        <TextArea rows={4} placeholder="请输入备注" />
                    </Form.Item>
                </Form>
            </Modal>
        </div>
    );
};

export default SupplierManagement;

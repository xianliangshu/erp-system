import React, { useState, useEffect } from 'react';
import { Table, Button, Space, Modal, message, Form, Input, InputNumber, Select, Card, Row, Col, App, Tag } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, ExclamationCircleOutlined, ReloadOutlined } from '@ant-design/icons';
import {
    getDictTypePage,
    getDictDataList,
    createDictType,
    updateDictType,
    deleteDictType,
    createDictData,
    updateDictData,
    deleteDictData,
    type DictType,
    type DictData,
} from '@/api/system/dict';

const { Search } = Input;
const { TextArea } = Input;

const DictManagement: React.FC = () => {
    const { modal } = App.useApp();

    // ---------- 左侧：字典类型 ----------
    const [typeLoading, setTypeLoading] = useState(false);
    const [typeList, setTypeList] = useState<DictType[]>([]);
    const [typeTotal, setTypeTotal] = useState(0);
    const [typeCurrent, setTypeCurrent] = useState(1);
    const [typePageSize, setTypePageSize] = useState(10);
    const [searchDictName, setSearchDictName] = useState('');

    // ---------- 右侧：字典数据 ----------
    const [selectedType, setSelectedType] = useState<DictType | null>(null);
    const [dataLoading, setDataLoading] = useState(false);
    const [dataList, setDataList] = useState<DictData[]>([]);

    // ---------- 弹窗 ----------
    const [typeModalVisible, setTypeModalVisible] = useState(false);
    const [typeEditingId, setTypeEditingId] = useState<number | null>(null);
    const [typeForm] = Form.useForm();

    const [dataModalVisible, setDataModalVisible] = useState(false);
    const [dataEditingId, setDataEditingId] = useState<number | null>(null);
    const [dataForm] = Form.useForm();

    // ==================== 数据加载 ====================

    const loadTypes = async () => {
        setTypeLoading(true);
        try {
            const data = await getDictTypePage({
                current: typeCurrent,
                size: typePageSize,
                dictName: searchDictName || undefined,
            });
            setTypeList(data.records || []);
            setTypeTotal(data.total || 0);
        } catch (error) {
            message.error('加载字典类型失败');
        } finally {
            setTypeLoading(false);
        }
    };

    const loadDataList = async (dictType: string) => {
        setDataLoading(true);
        try {
            const data = await getDictDataList(dictType);
            setDataList(data || []);
        } catch (error) {
            message.error('加载字典数据失败');
        } finally {
            setDataLoading(false);
        }
    };

    useEffect(() => {
        loadTypes();
    }, [typeCurrent, typePageSize]);

    // 首次加载后自动选中第一个字典类型
    useEffect(() => {
        if (!selectedType && typeList.length > 0) {
            setSelectedType(typeList[0]);
            loadDataList(typeList[0].dictType);
        }
    }, [typeList]);

    const handleSearchType = () => {
        setTypeCurrent(1);
        loadTypes();
    };

    // ==================== 字典类型操作 ====================

    const handleAddType = () => {
        setTypeEditingId(null);
        typeForm.resetFields();
        typeForm.setFieldsValue({ status: 1 });
        setTypeModalVisible(true);
    };

    const handleEditType = (record: DictType) => {
        setTypeEditingId(record.id);
        typeForm.setFieldsValue(record);
        setTypeModalVisible(true);
    };

    const handleSubmitType = async () => {
        try {
            const values = await typeForm.validateFields();
            if (typeEditingId) {
                await updateDictType({ ...values, id: typeEditingId });
                message.success('更新成功');
            } else {
                await createDictType(values);
                message.success('新增成功');
            }
            setTypeModalVisible(false);
            loadTypes();
        } catch (error: any) {
            if (error?.message) message.error(error.message);
        }
    };

    const handleDeleteType = (record: DictType) => {
        modal.confirm({
            title: '确认删除',
            icon: <ExclamationCircleOutlined />,
            content: `确定要删除字典类型【${record.dictName}】吗?`,
            okText: '确定',
            cancelText: '取消',
            onOk: async () => {
                try {
                    await deleteDictType(record.id);
                    message.success('删除成功');
                    if (selectedType?.id === record.id) {
                        setSelectedType(null);
                        setDataList([]);
                    }
                    loadTypes();
                } catch (error: any) {
                    message.error(error.message || '删除失败');
                }
            },
        });
    };

    // ==================== 字典数据操作 ====================

    const handleSelectType = (record: DictType) => {
        setSelectedType(record);
        loadDataList(record.dictType);
    };

    const handleAddData = () => {
        if (!selectedType) {
            message.warning('请先在左侧选择一个字典类型');
            return;
        }
        setDataEditingId(null);
        dataForm.resetFields();
        dataForm.setFieldsValue({ status: 1, dictSort: dataList.length + 1, isDefault: 0 });
        setDataModalVisible(true);
    };

    const handleEditData = (record: DictData) => {
        setDataEditingId(record.id);
        dataForm.setFieldsValue(record);
        setDataModalVisible(true);
    };

    const handleSubmitData = async () => {
        try {
            const values = await dataForm.validateFields();
            const payload = { ...values, dictType: selectedType?.dictType };
            if (dataEditingId) {
                await updateDictData({ ...payload, id: dataEditingId });
                message.success('更新成功');
            } else {
                await createDictData(payload);
                message.success('新增成功');
            }
            setDataModalVisible(false);
            if (selectedType) loadDataList(selectedType.dictType);
        } catch (error: any) {
            if (error?.message) message.error(error.message);
        }
    };

    const handleDeleteData = (record: DictData) => {
        modal.confirm({
            title: '确认删除',
            icon: <ExclamationCircleOutlined />,
            content: `确定要删除字典数据【${record.dictLabel}】吗?`,
            okText: '确定',
            cancelText: '取消',
            onOk: async () => {
                try {
                    await deleteDictData(record.id);
                    message.success('删除成功');
                    if (selectedType) loadDataList(selectedType.dictType);
                } catch (error: any) {
                    message.error(error.message || '删除失败');
                }
            },
        });
    };

    // ==================== 列定义 ====================

    const typeColumns = [
        {
            title: '字典名称',
            dataIndex: 'dictName',
            key: 'dictName',
            ellipsis: true,
        },
        {
            title: '字典类型',
            dataIndex: 'dictType',
            key: 'dictType',
            ellipsis: true,
            render: (v: string) => <Tag>{v}</Tag>,
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
            width: 130,
            render: (_: any, record: DictType) => (
                <Space size={0}>
                    <Button
                        type="link"
                        size="small"
                        icon={<EditOutlined />}
                        onClick={(e) => {
                            e.stopPropagation();
                            handleEditType(record);
                        }}
                    />
                    <Button
                        type="link"
                        size="small"
                        danger
                        icon={<DeleteOutlined />}
                        onClick={(e) => {
                            e.stopPropagation();
                            handleDeleteType(record);
                        }}
                    />
                </Space>
            ),
        },
    ];

    const dataColumns = [
        {
            title: '排序',
            dataIndex: 'dictSort',
            key: 'dictSort',
            width: 70,
        },
        {
            title: '字典标签',
            dataIndex: 'dictLabel',
            key: 'dictLabel',
        },
        {
            title: '字典键值',
            dataIndex: 'dictValue',
            key: 'dictValue',
            render: (v: string) => <Tag color="blue">{v}</Tag>,
        },
        {
            title: '是否默认',
            dataIndex: 'isDefault',
            key: 'isDefault',
            width: 90,
            render: (v: number) => (v === 1 ? <Tag color="green">是</Tag> : '否'),
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
            title: '备注',
            dataIndex: 'remark',
            key: 'remark',
            ellipsis: true,
        },
        {
            title: '操作',
            key: 'action',
            width: 130,
            render: (_: any, record: DictData) => (
                <Space size={0}>
                    <Button type="link" size="small" icon={<EditOutlined />} onClick={() => handleEditData(record)} />
                    <Button type="link" size="small" danger icon={<DeleteOutlined />} onClick={() => handleDeleteData(record)} />
                </Space>
            ),
        },
    ];

    return (
        <div style={{ padding: '24px' }}>
            <Row gutter={16}>
                {/* 左侧：字典类型 */}
                <Col span={10}>
                    <Card
                        title="字典类型"
                        extra={
                            <Button type="primary" size="small" icon={<PlusOutlined />} onClick={handleAddType}>
                                新增
                            </Button>
                        }
                    >
                        <Space style={{ marginBottom: 16 }} wrap>
                            <Search
                                placeholder="字典名称"
                                value={searchDictName}
                                onChange={(e) => setSearchDictName(e.target.value)}
                                onSearch={handleSearchType}
                                style={{ width: 180 }}
                                allowClear
                            />
                            <Button icon={<ReloadOutlined />} onClick={handleSearchType}>
                                刷新
                            </Button>
                        </Space>

                        <Table
                            loading={typeLoading}
                            columns={typeColumns}
                            dataSource={typeList}
                            rowKey="id"
                            size="small"
                            rowClassName={(record) => (selectedType?.id === record.id ? 'ant-table-row-selected' : '')}
                            onRow={(record) => ({
                                onClick: () => handleSelectType(record),
                                style: { cursor: 'pointer' },
                            })}
                            pagination={{
                                current: typeCurrent,
                                pageSize: typePageSize,
                                total: typeTotal,
                                size: 'small',
                                showTotal: (t) => `共 ${t} 条`,
                                onChange: (page, size) => {
                                    setTypeCurrent(page);
                                    setTypePageSize(size);
                                },
                            }}
                        />
                    </Card>
                </Col>

                {/* 右侧：字典数据 */}
                <Col span={14}>
                    <Card
                        title={selectedType ? `字典数据 - ${selectedType.dictName}` : '字典数据'}
                        extra={
                            <Button
                                type="primary"
                                size="small"
                                icon={<PlusOutlined />}
                                disabled={!selectedType}
                                onClick={handleAddData}
                            >
                                新增
                            </Button>
                        }
                    >
                        {selectedType ? (
                            <Table
                                loading={dataLoading}
                                columns={dataColumns}
                                dataSource={dataList}
                                rowKey="id"
                                size="small"
                                pagination={false}
                            />
                        ) : (
                            <div style={{ padding: '40px 0', textAlign: 'center', color: '#999' }}>
                                请先在左侧选择一个字典类型
                            </div>
                        )}
                    </Card>
                </Col>
            </Row>

            {/* 字典类型弹窗 */}
            <Modal
                title={typeEditingId ? '编辑字典类型' : '新增字典类型'}
                open={typeModalVisible}
                onOk={handleSubmitType}
                onCancel={() => setTypeModalVisible(false)}
                width={560}
            >
                <Form form={typeForm} labelCol={{ span: 6 }} wrapperCol={{ span: 16 }}>
                    <Form.Item
                        label="字典名称"
                        name="dictName"
                        rules={[{ required: true, message: '请输入字典名称' }]}
                    >
                        <Input placeholder="如：用户性别" />
                    </Form.Item>
                    <Form.Item
                        label="字典类型"
                        name="dictType"
                        rules={[{ required: true, message: '请输入字典类型' }]}
                        tooltip="唯一标识，建议使用小写下划线，如 sys_user_gender"
                    >
                        <Input placeholder="如：sys_user_gender" disabled={!!typeEditingId} />
                    </Form.Item>
                    <Form.Item label="状态" name="status" initialValue={1}>
                        <Select>
                            <Select.Option value={1}>启用</Select.Option>
                            <Select.Option value={0}>禁用</Select.Option>
                        </Select>
                    </Form.Item>
                    <Form.Item label="备注" name="remark">
                        <TextArea rows={3} placeholder="请输入备注" />
                    </Form.Item>
                </Form>
            </Modal>

            {/* 字典数据弹窗 */}
            <Modal
                title={dataEditingId ? '编辑字典数据' : '新增字典数据'}
                open={dataModalVisible}
                onOk={handleSubmitData}
                onCancel={() => setDataModalVisible(false)}
                width={560}
            >
                <Form form={dataForm} labelCol={{ span: 6 }} wrapperCol={{ span: 16 }}>
                    <Form.Item label="所属字典" required>
                        <Input value={selectedType?.dictType} disabled />
                    </Form.Item>
                    <Form.Item label="字典标签" name="dictLabel" rules={[{ required: true, message: '请输入字典标签' }]}>
                        <Input placeholder="如：男" />
                    </Form.Item>
                    <Form.Item label="字典键值" name="dictValue" rules={[{ required: true, message: '请输入字典键值' }]}>
                        <Input placeholder="如：1" />
                    </Form.Item>
                    <Form.Item label="排序" name="dictSort" initialValue={0}>
                        <InputNumber min={0} style={{ width: '100%' }} />
                    </Form.Item>
                    <Form.Item label="是否默认" name="isDefault" initialValue={0}>
                        <Select>
                            <Select.Option value={1}>是</Select.Option>
                            <Select.Option value={0}>否</Select.Option>
                        </Select>
                    </Form.Item>
                    <Form.Item label="状态" name="status" initialValue={1}>
                        <Select>
                            <Select.Option value={1}>启用</Select.Option>
                            <Select.Option value={0}>禁用</Select.Option>
                        </Select>
                    </Form.Item>
                    <Form.Item label="备注" name="remark">
                        <TextArea rows={3} placeholder="请输入备注" />
                    </Form.Item>
                </Form>
            </Modal>
        </div>
    );
};

export default DictManagement;

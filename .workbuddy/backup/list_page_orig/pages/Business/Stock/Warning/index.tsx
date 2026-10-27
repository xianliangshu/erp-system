import React, { useEffect, useState } from 'react';
import {
    Card, Table, Space, Input, Select, Button, message, Tag, Modal, Form, InputNumber, Tabs, Switch,
} from 'antd';
import {
    SearchOutlined, ReloadOutlined, PlusOutlined, EditOutlined, DeleteOutlined,
    ExclamationCircleOutlined, WarningOutlined,
} from '@ant-design/icons';
import {
    getStockWarningPage, getStockWarningHitList, addStockWarning, updateStockWarning, deleteStockWarning,
} from '@/api/stock/stockWarning';
import { getAllWarehouses } from '@/api/basedata/warehouse';
import { getMaterialList } from '@/api/material/material';

const StockWarningPage: React.FC = () => {
    const [activeTab, setActiveTab] = useState('hit');

    // ========== 公共下拉 ==========
    const [warehouses, setWarehouses] = useState<any[]>([]);
    const [materials, setMaterials] = useState<any[]>([]);

    // ========== Tab 1：命中预警 ==========
    const [hitLoading, setHitLoading] = useState(false);
    const [hitList, setHitList] = useState<any[]>([]);
    const [hitScId, setHitScId] = useState<number | undefined>(undefined);
    const [hitProductName, setHitProductName] = useState('');

    // ========== Tab 2：预警配置 ==========
    const [loading, setLoading] = useState(false);
    const [dataSource, setDataSource] = useState<any[]>([]);
    const [total, setTotal] = useState(0);
    const [current, setCurrent] = useState(1);
    const [pageSize, setPageSize] = useState(10);
    const [searchScId, setSearchScId] = useState<number | undefined>(undefined);
    const [searchProductName, setSearchProductName] = useState('');
    const [searchStatus, setSearchStatus] = useState<number | undefined>(undefined);

    const [formVisible, setFormVisible] = useState(false);
    const [formData, setFormData] = useState<any>(null);
    const [form] = Form.useForm();
    const [modal, contextHolder] = Modal.useModal();

    const loadHitList = async () => {
        setHitLoading(true);
        try {
            const res: any = await getStockWarningHitList({
                scId: hitScId,
                productName: hitProductName || undefined,
            });
            setHitList(res || []);
        } catch (error) {
            message.error('加载命中预警失败');
        } finally {
            setHitLoading(false);
        }
    };

    const loadData = async () => {
        setLoading(true);
        try {
            const res: any = await getStockWarningPage({
                current,
                size: pageSize,
                scId: searchScId,
                productName: searchProductName || undefined,
                status: searchStatus,
            });
            setDataSource(res?.records || []);
            setTotal(res?.total || 0);
        } catch (error) {
            message.error('加载预警配置失败');
        } finally {
            setLoading(false);
        }
    };

    const loadOptions = async () => {
        try {
            const [wh, mt] = await Promise.all([getAllWarehouses(), getMaterialList()]);
            setWarehouses((wh as any) || []);
            setMaterials((mt as any) || []);
        } catch (error) {
            console.error('加载下拉选项失败', error);
        }
    };

    useEffect(() => {
        loadOptions();
    }, []);

    useEffect(() => {
        if (activeTab === 'hit') {
            loadHitList();
        } else {
            loadData();
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [activeTab, current, pageSize]);

    const handleHitSearch = () => {
        loadHitList();
    };

    const handleHitReset = () => {
        setHitScId(undefined);
        setHitProductName('');
        setTimeout(loadHitList, 0);
    };

    const handleSearch = () => {
        setCurrent(1);
        loadData();
    };

    const handleReset = () => {
        setSearchScId(undefined);
        setSearchProductName('');
        setSearchStatus(undefined);
        setCurrent(1);
        setTimeout(loadData, 0);
    };

    const handleAdd = () => {
        setFormData(null);
        form.resetFields();
        form.setFieldsValue({ status: 1 });
        setFormVisible(true);
    };

    const handleEdit = (record: any) => {
        setFormData(record);
        form.setFieldsValue({
            ...record,
            status: record.status === 1,
        });
        setFormVisible(true);
    };

    const handleDelete = (record: any) => {
        modal.confirm({
            title: '确认删除',
            icon: <ExclamationCircleOutlined />,
            content: `确定要删除「${record.productName || record.productId}」在「${record.scName || record.scId}」的预警配置吗？`,
            okText: '确定',
            cancelText: '取消',
            onOk: async () => {
                try {
                    await deleteStockWarning(record.id);
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
                await updateStockWarning({ ...values, id: formData.id });
                message.success('修改成功');
            } else {
                await addStockWarning(values);
                message.success('新增成功');
            }
            setFormVisible(false);
            loadData();
        } catch (error: any) {
            if (error?.errorFields) {
                return;
            }
            console.error('提交失败', error);
        }
    };

    const hitColumns = [
        {
            title: '预警类型',
            dataIndex: 'hitType',
            key: 'hitType',
            width: 110,
            render: (hitType: number) => (
                <Tag color={hitType === 1 ? 'red' : 'orange'}>
                    {hitType === 1 ? '低于下限' : '高于上限'}
                </Tag>
            ),
        },
        { title: '仓库', dataIndex: 'scName', key: 'scName', width: 120 },
        { title: '商品编号', dataIndex: 'productCode', key: 'productCode', width: 130 },
        { title: '商品名称', dataIndex: 'productName', key: 'productName' },
        {
            title: '规格 / 属性',
            key: 'spec',
            render: (_: any, record: any) =>
                [record.specification, record.salePropertyText].filter(Boolean).join(' / ') || '-',
        },
        {
            title: '当前库存',
            dataIndex: 'stockNum',
            key: 'stockNum',
            width: 110,
            render: (num: number) => <span style={{ fontWeight: 500 }}>{num}</span>,
        },
        { title: '预警下限', dataIndex: 'minLimit', key: 'minLimit', width: 100, render: (v: any) => v ?? '-' },
        { title: '预警上限', dataIndex: 'maxLimit', key: 'maxLimit', width: 100, render: (v: any) => v ?? '-' },
    ];

    const columns = [
        { title: '仓库', dataIndex: 'scName', key: 'scName', width: 120 },
        { title: '商品编号', dataIndex: 'productCode', key: 'productCode', width: 130 },
        { title: '商品名称', dataIndex: 'productName', key: 'productName' },
        {
            title: '规格 / 属性',
            key: 'spec',
            render: (_: any, record: any) =>
                [record.specification, record.salePropertyText].filter(Boolean).join(' / ') || '-',
        },
        { title: '当前库存', dataIndex: 'stockNum', key: 'stockNum', width: 100 },
        { title: '预警下限', dataIndex: 'minLimit', key: 'minLimit', width: 100, render: (v: any) => v ?? '-' },
        { title: '预警上限', dataIndex: 'maxLimit', key: 'maxLimit', width: 100, render: (v: any) => v ?? '-' },
        {
            title: '状态',
            dataIndex: 'status',
            key: 'status',
            width: 90,
            render: (status: number) => (
                <Tag color={status === 1 ? 'green' : 'default'}>{status === 1 ? '启用' : '停用'}</Tag>
            ),
        },
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

    const warehouseOptions = warehouses.map((w: any) => ({ value: w.id, label: w.name }));

    const hitTab = (
        <>
            <Space style={{ marginBottom: 16 }} wrap>
                <Select
                    placeholder="仓库"
                    style={{ width: 180 }}
                    value={hitScId}
                    onChange={setHitScId}
                    allowClear
                    options={warehouseOptions}
                />
                <Input
                    placeholder="商品名称"
                    style={{ width: 180 }}
                    value={hitProductName}
                    onChange={(e) => setHitProductName(e.target.value)}
                    onPressEnter={handleHitSearch}
                />
                <Button type="primary" icon={<SearchOutlined />} onClick={handleHitSearch}>
                    查询
                </Button>
                <Button icon={<ReloadOutlined />} onClick={handleHitReset}>
                    重置
                </Button>
            </Space>

            <Table
                loading={hitLoading}
                columns={hitColumns}
                dataSource={hitList}
                rowKey="id"
                pagination={{ pageSize: 10, showTotal: (t) => `共 ${t} 条` }}
            />
        </>
    );

    const configTab = (
        <>
            <Space style={{ marginBottom: 16 }} wrap>
                <Select
                    placeholder="仓库"
                    style={{ width: 180 }}
                    value={searchScId}
                    onChange={setSearchScId}
                    allowClear
                    options={warehouseOptions}
                />
                <Input
                    placeholder="商品名称"
                    style={{ width: 180 }}
                    value={searchProductName}
                    onChange={(e) => setSearchProductName(e.target.value)}
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
                    <Select.Option value={0}>停用</Select.Option>
                </Select>
                <Button type="primary" icon={<SearchOutlined />} onClick={handleSearch}>
                    查询
                </Button>
                <Button icon={<ReloadOutlined />} onClick={handleReset}>
                    重置
                </Button>
                <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
                    新增预警
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
                    showTotal: (t) => `共 ${t} 条`,
                    onChange: (page, size) => {
                        setCurrent(page);
                        setPageSize(size);
                    },
                }}
            />
        </>
    );

    return (
        <div style={{ padding: '24px' }}>
            {contextHolder}
            <Card title="库存预警">
                <Tabs
                    activeKey={activeTab}
                    onChange={setActiveTab}
                    items={[
                        {
                            key: 'hit',
                            label: (
                                <span>
                                    <WarningOutlined /> 命中预警
                                </span>
                            ),
                            children: hitTab,
                        },
                        { key: 'config', label: '预警配置', children: configTab },
                    ]}
                />
            </Card>

            <Modal
                title={formData ? '编辑预警配置' : '新增预警配置'}
                open={formVisible}
                onOk={handleSubmit}
                onCancel={() => setFormVisible(false)}
                okText="确定"
                cancelText="取消"
                width={560}
            >
                <Form form={form} layout="vertical">
                    <Form.Item name="scId" label="仓库" rules={[{ required: true, message: '请选择仓库' }]}>
                        <Select placeholder="请选择仓库" options={warehouseOptions} showSearch optionFilterProp="label" />
                    </Form.Item>
                    <Form.Item name="productId" label="商品" rules={[{ required: true, message: '请选择商品' }]}>
                        <Select
                            placeholder="请选择商品（支持名称/编号搜索）"
                            showSearch
                            optionFilterProp="label"
                            options={materials.map((m: any) => ({
                                value: m.id,
                                label: `${m.name}（${m.code}）${m.salePropertyText ? ' ' + m.salePropertyText : ''}`,
                            }))}
                        />
                    </Form.Item>
                    <Space size="large">
                        <Form.Item name="minLimit" label="预警下限">
                            <InputNumber min={0} style={{ width: 160 }} placeholder="低于则预警" />
                        </Form.Item>
                        <Form.Item name="maxLimit" label="预警上限">
                            <InputNumber min={0} style={{ width: 160 }} placeholder="高于则预警" />
                        </Form.Item>
                    </Space>
                    <Form.Item name="status" label="状态" valuePropName="checked" getValueFromEvent={(checked) => (checked ? 1 : 0)}>
                        <Switch checkedChildren="启用" unCheckedChildren="停用" defaultChecked />
                    </Form.Item>
                    <Form.Item name="remark" label="备注">
                        <Input.TextArea rows={2} placeholder="请输入备注" />
                    </Form.Item>
                </Form>
            </Modal>
        </div>
    );
};

export default StockWarningPage;

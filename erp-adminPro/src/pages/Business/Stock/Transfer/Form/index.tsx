import React, { useState, useEffect } from 'react';
import { Card, Form, Select, DatePicker, Input, Button, Table, InputNumber, Space, message, Popconfirm } from 'antd';
import { PlusOutlined, DeleteOutlined, ArrowLeftOutlined } from '@ant-design/icons';
import { useNavigate, useParams } from 'react-router-dom';
import { createStockTransfer, updateStockTransfer, getStockTransferById } from '@/api/business/stockTransfer';
import { getWarehouseList } from '@/api/basedata/warehouse';
import { getMaterialPage } from '@/api/material/material';
import dayjs from 'dayjs';

const { Option } = Select;


const StockTransferForm: React.FC = () => {
    const navigate = useNavigate();
    const { id } = useParams<{ id: string }>();
    const isEdit = !!id;

    const [form] = Form.useForm();
    const [loading, setLoading] = useState(false);
    const [warehouses, setWarehouses] = useState<any[]>([]);
    const [materials, setMaterials] = useState<any[]>([]);
    const [details, setDetails] = useState<any[]>([]);

    useEffect(() => {
        loadWarehouses();
        loadMaterials();
        if (isEdit) {
            loadTransferData();
        }
    }, [id]);

    const loadWarehouses = async () => {
        try {
            const data: any = await getWarehouseList();
            setWarehouses(Array.isArray(data) ? data : []);
        } catch (e) {
            console.error(e);
        }
    };

    const loadMaterials = async () => {
        try {
            const data: any = await getMaterialPage({ current: 1, size: 1000 });
            setMaterials(data.records || []);
        } catch (e) {
            console.error(e);
        }
    };

    const loadTransferData = async () => {
        try {
            const data: any = await getStockTransferById(Number(id));
            const transfer = data.transfer;
            form.setFieldsValue({
                outScId: transfer.outScId,
                inScId: transfer.inScId,
                transferDate: transfer.transferDate ? dayjs(transfer.transferDate) : null,
                description: transfer.description,
            });
            setDetails(data.details.map((d: any, index: number) => ({
                ...d,
                key: index,
            })));
        } catch (e) {
            message.error('加载数据失败');
        }
    };

    const handleAddDetail = () => {
        setDetails([...details, {
            key: Date.now(),
            productId: undefined,
            transferNum: 1,
            costPrice: 0,
            description: '',
        }]);
    };

    const handleRemoveDetail = (key: number) => {
        setDetails(details.filter(d => d.key !== key));
    };

    const handleDetailChange = (key: number, field: string, value: any) => {
        setDetails(details.map(d => d.key === key ? { ...d, [field]: value } : d));
    };

    const handleSubmit = async () => {
        try {
            await form.validateFields();
            const values = form.getFieldsValue();

            if (details.length === 0) {
                message.error('请添加调拨明细');
                return;
            }

            if (values.outScId === values.inScId) {
                message.error('调出仓库和调入仓库不能相同');
                return;
            }

            const data = {
                outScId: values.outScId,
                inScId: values.inScId,
                transferDate: values.transferDate?.format('YYYY-MM-DD'),
                description: values.description,
                details: details.map(d => ({
                    productId: d.productId,
                    transferNum: d.transferNum,
                    costPrice: d.costPrice,
                    description: d.description,
                })),
            };

            setLoading(true);
            if (isEdit) {
                await updateStockTransfer(Number(id), data);
                message.success('更新成功');
            } else {
                await createStockTransfer(data);
                message.success('创建成功');
            }
            navigate('/business/stock/transfer');
        } catch (e) {
            console.error(e);
        } finally {
            setLoading(false);
        }
    };

    const detailColumns = [
        {
            title: '商品', dataIndex: 'productId', width: 200,
            render: (_: any, record: any) => (
                <Select
                    style={{ width: '100%' }}
                    placeholder="选择商品"
                    value={record.productId}
                    onChange={(val) => handleDetailChange(record.key, 'productId', val)}
                    showSearch
                    filterOption={(input, option) =>
                        (option?.children as unknown as string)?.toLowerCase().includes(input.toLowerCase())
                    }
                >
                    {materials.map((m: any) => (
                        <Option key={m.id} value={m.id}>{m.name} ({m.code})</Option>
                    ))}
                </Select>
            )
        },
        {
            title: '调拨数量', dataIndex: 'transferNum', width: 120,
            render: (_: any, record: any) => (
                <InputNumber
                    min={0.01}
                    precision={2}
                    value={record.transferNum}
                    onChange={(val) => handleDetailChange(record.key, 'transferNum', val)}
                />
            )
        },
        {
            title: '成本单价', dataIndex: 'costPrice', width: 120,
            render: (_: any, record: any) => (
                <InputNumber
                    min={0}
                    precision={2}
                    value={record.costPrice}
                    onChange={(val) => handleDetailChange(record.key, 'costPrice', val)}
                />
            )
        },
        {
            title: '备注', dataIndex: 'description', width: 200,
            render: (_: any, record: any) => (
                <Input
                    value={record.description}
                    onChange={(e) => handleDetailChange(record.key, 'description', e.target.value)}
                />
            )
        },
        {
            title: '操作', width: 80,
            render: (_: any, record: any) => (
                <Popconfirm title="确定删除?" onConfirm={() => handleRemoveDetail(record.key)}>
                    <Button type="link" danger icon={<DeleteOutlined />} />
                </Popconfirm>
            )
        },
    ];

    return (
        <div style={{ padding: '24px' }}>
            <Card
                title={isEdit ? '编辑调拨单' : '新增调拨单'}
                extra={
                    <Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/business/stock/transfer')}>
                        返回
                    </Button>
                }
            >
                <Form form={form} layout="inline" style={{ marginBottom: 24 }}>
                    <Form.Item name="outScId" label="调出仓库" rules={[{ required: true, message: '请选择调出仓库' }]}>
                        <Select style={{ width: 180 }} placeholder="选择调出仓库">
                            {warehouses.map(w => <Option key={w.id} value={w.id}>{w.name}</Option>)}
                        </Select>
                    </Form.Item>
                    <Form.Item name="inScId" label="调入仓库" rules={[{ required: true, message: '请选择调入仓库' }]}>
                        <Select style={{ width: 180 }} placeholder="选择调入仓库">
                            {warehouses.map(w => <Option key={w.id} value={w.id}>{w.name}</Option>)}
                        </Select>
                    </Form.Item>
                    <Form.Item name="transferDate" label="调拨日期">
                        <DatePicker />
                    </Form.Item>
                    <Form.Item name="description" label="备注">
                        <Input style={{ width: 200 }} placeholder="备注" />
                    </Form.Item>
                </Form>

                <div style={{ marginBottom: 16 }}>
                    <Button type="dashed" icon={<PlusOutlined />} onClick={handleAddDetail}>
                        添加商品
                    </Button>
                </div>

                <Table
                    columns={detailColumns}
                    dataSource={details}
                    rowKey="key"
                    pagination={false}
                    size="small"
                />

                <div style={{ marginTop: 24, textAlign: 'center' }}>
                    <Space>
                        <Button onClick={() => navigate('/business/stock/transfer')}>取消</Button>
                        <Button type="primary" loading={loading} onClick={handleSubmit}>
                            {isEdit ? '保存' : '创建'}
                        </Button>
                    </Space>
                </div>
            </Card>
        </div>
    );
};

export default StockTransferForm;

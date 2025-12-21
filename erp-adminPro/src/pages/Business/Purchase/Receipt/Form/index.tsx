import React, { useState, useEffect } from 'react';
import { Form, Input, Select, InputNumber, Button, Card, Table, Space, message, Divider, Alert } from 'antd';
import { DeleteOutlined, ArrowLeftOutlined } from '@ant-design/icons';
import { useNavigate, useParams } from 'react-router-dom';
import { createPurchaseReceipt, updatePurchaseReceipt, getPurchaseReceiptById, getPendingReceiveOrders } from '@/api/business/purchaseReceipt';
import { getPurchaseOrderById } from '@/api/business/purchaseOrder';
import { getSupplierList } from '@/api/basedata/supplier';
import { getWarehouseList } from '@/api/basedata/warehouse';

const { Option } = Select;
const { TextArea } = Input;

const PurchaseReceiptForm: React.FC = () => {
    const navigate = useNavigate();
    const { id } = useParams<{ id: string }>();
    const isEdit = !!id;

    const [form] = Form.useForm();
    const [loading, setLoading] = useState(false);
    const [suppliers, setSuppliers] = useState<any[]>([]);
    const [warehouses, setWarehouses] = useState<any[]>([]);
    const [pendingOrders, setPendingOrders] = useState<any[]>([]);
    const [details, setDetails] = useState<any[]>([]);

    // 加载基础数据
    useEffect(() => {
        loadSuppliers();
        loadWarehouses();
        loadPendingOrders();
        if (isEdit) {
            loadReceiptData();
        }
    }, [id]);

    const loadSuppliers = async () => {
        try {
            const data: any = await getSupplierList();
            setSuppliers(Array.isArray(data) ? data : []);
        } catch (error) {
            console.error('加载供应商失败', error);
        }
    };

    const loadWarehouses = async () => {
        try {
            const data: any = await getWarehouseList();
            setWarehouses(Array.isArray(data) ? data : []);
        } catch (error) {
            console.error('加载仓库失败', error);
        }
    };

    const loadPendingOrders = async () => {
        try {
            const data: any = await getPendingReceiveOrders({ current: 1, size: 100 });
            setPendingOrders(data?.records || []);
        } catch (error) {
            console.error('加载待收货订单失败', error);
        }
    };

    const loadReceiptData = async () => {
        try {
            const data: any = await getPurchaseReceiptById(Number(id));
            if (data?.receipt) {
                form.setFieldsValue({
                    orderId: data.receipt.orderId,
                    scId: data.receipt.scId,
                    supplierId: data.receipt.supplierId,
                    description: data.receipt.description,
                });
                // 转换明细数据
                const detailList = (data.details || []).map((d: any) => ({
                    ...d,
                    key: d.id,
                }));
                setDetails(detailList);
            }
        } catch (error) {
            message.error('加载收货数据失败');
        }
    };

    // 选择采购订单时加载订单明细
    const handleOrderSelect = async (orderId: number) => {
        try {
            const data: any = await getPurchaseOrderById(orderId);
            if (data?.order) {
                form.setFieldsValue({
                    scId: data.order.scId,
                    supplierId: data.order.supplierId,
                });
                // 将订单明细转换为收货明细
                const detailList = (data.details || []).map((d: any) => ({
                    key: d.id,
                    orderDetailId: d.id,
                    productId: d.productId,
                    orderNum: d.orderNum,
                    receivedNum: d.receivedNum || 0,
                    pendingNum: (d.orderNum || 0) - (d.receivedNum || 0),
                    receiveNum: (d.orderNum || 0) - (d.receivedNum || 0), // 默认收货数量为待收数量
                    taxPrice: d.taxPrice,
                    description: d.description,
                }));
                setDetails(detailList);
            }
        } catch (error) {
            message.error('加载订单明细失败');
        }
    };

    // 更新明细行
    const handleDetailChange = (key: number, field: string, value: any) => {
        setDetails(details.map(d => {
            if (d.key === key || d.id === key) {
                const updated = { ...d, [field]: value };
                // 自动计算金额
                if (field === 'receiveNum') {
                    updated.taxAmount = (updated.receiveNum || 0) * (updated.taxPrice || 0);
                }
                return updated;
            }
            return d;
        }));
    };

    // 删除明细行
    const handleDeleteDetail = (key: number) => {
        setDetails(details.filter(d => d.key !== key && d.id !== key));
    };

    // 提交表单
    const handleSubmit = async () => {
        try {
            const values = await form.validateFields();

            if (details.length === 0) {
                message.warning('请添加至少一个收货明细');
                return;
            }

            // 检查收货数量是否合理
            for (const d of details) {
                if (d.receiveNum > d.pendingNum) {
                    message.warning('收货数量不能超过待收数量');
                    return;
                }
                if (d.receiveNum <= 0) {
                    message.warning('收货数量必须大于0');
                    return;
                }
            }

            setLoading(true);

            const receiptData = {
                id: isEdit ? Number(id) : undefined,
                orderId: values.orderId,
                scId: values.scId,
                supplierId: values.supplierId,
                description: values.description,
                details: details.map(d => ({
                    orderDetailId: d.orderDetailId,
                    productId: d.productId,
                    orderNum: d.orderNum,
                    receiveNum: d.receiveNum,
                    taxPrice: d.taxPrice,
                    description: d.description,
                })),
            };

            if (isEdit) {
                await updatePurchaseReceipt(receiptData);
                message.success('更新成功');
            } else {
                await createPurchaseReceipt(receiptData);
                message.success('创建成功');
            }

            navigate('/business/purchase/receipt');
        } catch (error) {
            message.error('操作失败');
        } finally {
            setLoading(false);
        }
    };

    // 计算合计
    const totalNum = details.reduce((sum, d) => sum + (d.receiveNum || 0), 0);
    const totalAmount = details.reduce((sum, d) => sum + ((d.receiveNum || 0) * (d.taxPrice || 0)), 0);

    const detailColumns = [
        {
            title: '商品ID',
            dataIndex: 'productId',
            key: 'productId',
            width: 100,
        },
        {
            title: '订单数量',
            dataIndex: 'orderNum',
            key: 'orderNum',
            width: 100,
        },
        {
            title: '已收数量',
            dataIndex: 'receivedNum',
            key: 'receivedNum',
            width: 100,
        },
        {
            title: '待收数量',
            dataIndex: 'pendingNum',
            key: 'pendingNum',
            width: 100,
        },
        {
            title: '本次收货',
            dataIndex: 'receiveNum',
            key: 'receiveNum',
            width: 120,
            render: (value: number, record: any) => (
                <InputNumber
                    min={0}
                    max={record.pendingNum}
                    value={value}
                    onChange={(v) => handleDetailChange(record.key || record.id, 'receiveNum', v)}
                />
            ),
        },
        {
            title: '含税单价',
            dataIndex: 'taxPrice',
            key: 'taxPrice',
            width: 100,
            render: (value: number) => `￥${value?.toFixed(2) || '0.00'}`,
        },
        {
            title: '金额',
            dataIndex: 'taxAmount',
            key: 'taxAmount',
            width: 120,
            render: (_: any, record: any) => `￥${((record.receiveNum || 0) * (record.taxPrice || 0)).toFixed(2)}`,
        },
        {
            title: '操作',
            key: 'action',
            width: 80,
            render: (_: any, record: any) => (
                <Button
                    type="link"
                    danger
                    icon={<DeleteOutlined />}
                    onClick={() => handleDeleteDetail(record.key || record.id)}
                >
                    删除
                </Button>
            ),
        },
    ];

    return (
        <div style={{ padding: '24px' }}>
            <Card
                title={
                    <Space>
                        <Button icon={<ArrowLeftOutlined />} onClick={() => navigate(-1)}>返回</Button>
                        <span>{isEdit ? '编辑收货单' : '新增收货单'}</span>
                    </Space>
                }
            >
                <Form form={form} layout="vertical">
                    <div style={{ display: 'flex', gap: 24 }}>
                        <Form.Item
                            name="orderId"
                            label="采购订单"
                            rules={[{ required: true, message: '请选择采购订单' }]}
                            style={{ flex: 1 }}
                        >
                            <Select
                                placeholder="选择采购订单"
                                onChange={handleOrderSelect}
                                disabled={isEdit}
                            >
                                {pendingOrders.map((o: any) => (
                                    <Option key={o.id} value={o.id}>
                                        {o.code} - {suppliers.find(s => s.id === o.supplierId)?.name || '未知供应商'}
                                    </Option>
                                ))}
                            </Select>
                        </Form.Item>
                        <Form.Item
                            name="scId"
                            label="仓库"
                            rules={[{ required: true, message: '请选择仓库' }]}
                            style={{ flex: 1 }}
                        >
                            <Select placeholder="选择仓库" disabled>
                                {warehouses.map((w: any) => (
                                    <Option key={w.id} value={w.id}>{w.name}</Option>
                                ))}
                            </Select>
                        </Form.Item>
                        <Form.Item
                            name="supplierId"
                            label="供应商"
                            style={{ flex: 1 }}
                        >
                            <Select placeholder="选择供应商" disabled>
                                {suppliers.map((s: any) => (
                                    <Option key={s.id} value={s.id}>{s.name}</Option>
                                ))}
                            </Select>
                        </Form.Item>
                    </div>
                    <Form.Item name="description" label="备注">
                        <TextArea rows={2} placeholder="收货备注" />
                    </Form.Item>
                </Form>

                <Divider>收货明细</Divider>

                {details.length === 0 ? (
                    <Alert message="请先选择采购订单" type="info" showIcon style={{ marginBottom: 16 }} />
                ) : (
                    <Table
                        columns={detailColumns}
                        dataSource={details}
                        rowKey={(record) => record.key || record.id}
                        pagination={false}
                        summary={() => (
                            <Table.Summary>
                                <Table.Summary.Row>
                                    <Table.Summary.Cell index={0}>合计</Table.Summary.Cell>
                                    <Table.Summary.Cell index={1}>-</Table.Summary.Cell>
                                    <Table.Summary.Cell index={2}>-</Table.Summary.Cell>
                                    <Table.Summary.Cell index={3}>-</Table.Summary.Cell>
                                    <Table.Summary.Cell index={4}>{totalNum}</Table.Summary.Cell>
                                    <Table.Summary.Cell index={5}>-</Table.Summary.Cell>
                                    <Table.Summary.Cell index={6}>￥{totalAmount.toFixed(2)}</Table.Summary.Cell>
                                    <Table.Summary.Cell index={7}>-</Table.Summary.Cell>
                                </Table.Summary.Row>
                            </Table.Summary>
                        )}
                    />
                )}

                <div style={{ marginTop: 24, textAlign: 'center' }}>
                    <Space>
                        <Button onClick={() => navigate(-1)}>取消</Button>
                        <Button type="primary" loading={loading} onClick={handleSubmit}>
                            {isEdit ? '保存修改' : '创建收货单'}
                        </Button>
                    </Space>
                </div>
            </Card>
        </div>
    );
};

export default PurchaseReceiptForm;

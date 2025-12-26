import React, { useState, useEffect } from 'react';
import { Form, Select, InputNumber, Button, Card, Table, Space, message, Divider, Input } from 'antd';
import { DeleteOutlined, ArrowLeftOutlined } from '@ant-design/icons';
import { useNavigate, useParams } from 'react-router-dom';
import { createSaleDelivery, updateSaleDelivery, getSaleDeliveryById, getPendingDeliveryOrders } from '@/api/business/saleDelivery';
import { getSaleOrderById } from '@/api/business/saleOrder';
import { getCustomerList } from '@/api/basedata/customer';
import { getMaterialPage } from '@/api/material/material';

const { Option } = Select;
const { TextArea } = Input;

const SaleDeliveryForm: React.FC = () => {
    const navigate = useNavigate();
    const { id } = useParams<{ id: string }>();
    const isEdit = !!id;

    const [form] = Form.useForm();
    const [loading, setLoading] = useState(false);
    const [customers, setCustomers] = useState<any[]>([]);
    const [orders, setOrders] = useState<any[]>([]);
    const [materials, setMaterials] = useState<any[]>([]);
    const [details, setDetails] = useState<any[]>([]);

    useEffect(() => { loadBasicData(); if (isEdit) loadDeliveryData(); }, [id]);

    const loadBasicData = async () => {
        try {
            const [customerData, orderData, materialData] = await Promise.all([
                getCustomerList(), getPendingDeliveryOrders({ current: 1, size: 1000 }), getMaterialPage({ current: 1, size: 1000 })
            ]);
            setCustomers(Array.isArray(customerData) ? customerData : []);
            setOrders((orderData as any)?.records || []);
            setMaterials((materialData as any)?.records || []);
        } catch (error) { console.error('加载基础数据失败', error); }
    };

    const loadDeliveryData = async () => {
        try {
            const data: any = await getSaleDeliveryById(Number(id));
            if (data?.delivery) {
                form.setFieldsValue({ orderId: data.delivery.orderId, description: data.delivery.description });
                setDetails(data.details?.map((d: any, i: number) => ({ ...d, key: Date.now() + i })) || []);
            }
        } catch (error) { message.error('加载出库单数据失败'); }
    };

    const handleOrderChange = async (orderId: number) => {
        try {
            const data: any = await getSaleOrderById(orderId);
            if (data?.details) {
                const deliveryDetails = data.details.map((d: any, index: number) => ({
                    key: Date.now() + index, orderDetailId: d.id, productId: d.productId,
                    orderNum: d.orderNum, deliveryNum: 0, taxPrice: d.taxPrice, description: '',
                }));
                setDetails(deliveryDetails);
            }
            if (data?.order) {
                form.setFieldsValue({ customerId: data.order.customerId, scId: data.order.scId });
            }
        } catch (error) { message.error('加载订单明细失败'); }
    };

    const getMaterialName = (productId: number) => materials.find(m => m.id === productId)?.name || productId;

    const handleDetailChange = (key: number, field: string, value: any) => {
        setDetails(details.map(d => {
            if (d.key === key || d.id === key) {
                const updated = { ...d, [field]: value };
                if (field === 'deliveryNum' || field === 'taxPrice') updated.taxAmount = (updated.deliveryNum || 0) * (updated.taxPrice || 0);
                return updated;
            }
            return d;
        }));
    };

    const handleDeleteDetail = (key: number) => { setDetails(details.filter(d => d.key !== key && d.id !== key)); };

    const handleSubmit = async () => {
        try {
            const values = await form.validateFields();
            if (details.length === 0) { message.warning('请添加至少一个出库明细'); return; }
            if (!details.some(d => d.deliveryNum > 0)) { message.warning('请填写出库数量'); return; }
            setLoading(true);
            const deliveryData = {
                id: isEdit ? Number(id) : undefined, orderId: values.orderId, scId: values.scId, customerId: values.customerId, description: values.description,
                details: details.filter(d => d.deliveryNum > 0).map(d => ({
                    orderDetailId: d.orderDetailId, productId: d.productId, orderNum: d.orderNum, deliveryNum: d.deliveryNum, taxPrice: d.taxPrice, description: d.description,
                })),
            };
            if (isEdit) { await updateSaleDelivery(deliveryData); message.success('更新成功'); }
            else { await createSaleDelivery(deliveryData); message.success('创建成功'); }
            navigate('/business/sale/delivery');
        } catch (error) { message.error('操作失败'); }
        finally { setLoading(false); }
    };

    const totalNum = details.reduce((sum, d) => sum + (d.deliveryNum || 0), 0);
    const totalAmount = details.reduce((sum, d) => sum + ((d.deliveryNum || 0) * (d.taxPrice || 0)), 0);

    const detailColumns = [
        { title: '商品', dataIndex: 'productId', key: 'productId', width: 200, render: (productId: number) => getMaterialName(productId) },
        { title: '订单数量', dataIndex: 'orderNum', key: 'orderNum', width: 100 },
        {
            title: '出库数量', dataIndex: 'deliveryNum', key: 'deliveryNum', width: 120, render: (value: number, record: any) => (
                <InputNumber min={0} max={record.orderNum} value={value} onChange={(v) => handleDetailChange(record.key || record.id, 'deliveryNum', v)} />
            )
        },
        { title: '含税单价', dataIndex: 'taxPrice', key: 'taxPrice', width: 120, render: (price: number) => `￥${(price || 0).toFixed(2)}` },
        { title: '金额', key: 'taxAmount', width: 120, render: (_: any, record: any) => `￥${((record.deliveryNum || 0) * (record.taxPrice || 0)).toFixed(2)}` },
        {
            title: '备注', dataIndex: 'description', key: 'description', render: (value: string, record: any) => (
                <Input value={value} onChange={(e) => handleDetailChange(record.key || record.id, 'description', e.target.value)} placeholder="备注" />
            )
        },
        {
            title: '操作', key: 'action', width: 80, render: (_: any, record: any) => (
                <Button type="link" danger icon={<DeleteOutlined />} onClick={() => handleDeleteDetail(record.key || record.id)}>删除</Button>
            )
        },
    ];

    return (
        <div style={{ padding: '24px' }}>
            <Card title={<Space><Button icon={<ArrowLeftOutlined />} onClick={() => navigate(-1)}>返回</Button><span>{isEdit ? '编辑销售出库' : '新增销售出库'}</span></Space>}>
                <Form form={form} layout="vertical">
                    <div style={{ display: 'flex', gap: 24 }}>
                        <Form.Item name="orderId" label="选择销售订单" rules={[{ required: true, message: '请选择销售订单' }]} style={{ flex: 1 }}>
                            <Select placeholder="选择已审核的销售订单" onChange={handleOrderChange} disabled={isEdit} showSearch
                                filterOption={(input, option) => (option?.children as unknown as string)?.toLowerCase()?.includes(input.toLowerCase())}>
                                {orders.map((o: any) => <Option key={o.id} value={o.id}>{o.code} - {customers.find(c => c.id === o.customerId)?.name || ''}</Option>)}
                            </Select>
                        </Form.Item>
                        <Form.Item name="customerId" label="客户" style={{ flex: 1 }}>
                            <Select placeholder="客户" disabled>
                                {customers.map((c: any) => <Option key={c.id} value={c.id}>{c.name}</Option>)}
                            </Select>
                        </Form.Item>
                    </div>
                    <Form.Item name="description" label="备注"><TextArea rows={2} placeholder="出库备注" /></Form.Item>
                </Form>
                <Divider>出库明细</Divider>
                <Table columns={detailColumns} dataSource={details} rowKey={(record) => record.key || record.id} pagination={false}
                    summary={() => (<Table.Summary><Table.Summary.Row>
                        <Table.Summary.Cell index={0}>合计</Table.Summary.Cell><Table.Summary.Cell index={1}>-</Table.Summary.Cell>
                        <Table.Summary.Cell index={2}>{totalNum}</Table.Summary.Cell><Table.Summary.Cell index={3}>-</Table.Summary.Cell>
                        <Table.Summary.Cell index={4}>￥{totalAmount.toFixed(2)}</Table.Summary.Cell><Table.Summary.Cell index={5}>-</Table.Summary.Cell>
                        <Table.Summary.Cell index={6}>-</Table.Summary.Cell>
                    </Table.Summary.Row></Table.Summary>)}
                />
                <div style={{ marginTop: 24, textAlign: 'center' }}>
                    <Space><Button onClick={() => navigate(-1)}>取消</Button><Button type="primary" loading={loading} onClick={handleSubmit}>{isEdit ? '保存修改' : '创建出库单'}</Button></Space>
                </div>
            </Card>
        </div>
    );
};

export default SaleDeliveryForm;

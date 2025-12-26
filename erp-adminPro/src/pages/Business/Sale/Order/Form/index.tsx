import React, { useState, useEffect } from 'react';
import { Form, Select, DatePicker, InputNumber, Button, Card, Table, Space, message, Divider, Input } from 'antd';
import { PlusOutlined, DeleteOutlined, ArrowLeftOutlined } from '@ant-design/icons';
import { useNavigate, useParams } from 'react-router-dom';
import { createSaleOrder, updateSaleOrder, getSaleOrderById } from '@/api/business/saleOrder';
import { getCustomerList } from '@/api/basedata/customer';
import { getWarehouseList } from '@/api/basedata/warehouse';
import { getMaterialPage } from '@/api/material/material';
import dayjs from 'dayjs';

const { Option } = Select;
const { TextArea } = Input;

const SaleOrderForm: React.FC = () => {
    const navigate = useNavigate();
    const { id } = useParams<{ id: string }>();
    const isEdit = !!id;

    const [form] = Form.useForm();
    const [loading, setLoading] = useState(false);
    const [customers, setCustomers] = useState<any[]>([]);
    const [warehouses, setWarehouses] = useState<any[]>([]);
    const [materials, setMaterials] = useState<any[]>([]);
    const [details, setDetails] = useState<any[]>([]);

    useEffect(() => {
        loadBasicData();
        if (isEdit) loadOrderData();
    }, [id]);

    const loadBasicData = async () => {
        try {
            const [customerData, warehouseData, materialData] = await Promise.all([
                getCustomerList(), getWarehouseList(), getMaterialPage({ current: 1, size: 1000 })
            ]);
            setCustomers(Array.isArray(customerData) ? customerData : []);
            setWarehouses(Array.isArray(warehouseData) ? warehouseData : []);
            setMaterials((materialData as any)?.records || []);
        } catch (error) { console.error('加载基础数据失败', error); }
    };

    const loadOrderData = async () => {
        try {
            const data: any = await getSaleOrderById(Number(id));
            if (data?.order) {
                form.setFieldsValue({
                    customerId: data.order.customerId, scId: data.order.scId, salerId: data.order.salerId,
                    expectDeliveryDate: data.order.expectDeliveryDate ? dayjs(data.order.expectDeliveryDate) : null,
                    description: data.order.description,
                });
                setDetails(data.details?.map((d: any, i: number) => ({ ...d, key: Date.now() + i })) || []);
            }
        } catch (error) { message.error('加载订单数据失败'); }
    };

    const handleAddDetail = () => {
        setDetails([...details, { key: Date.now(), productId: undefined, orderNum: 1, taxPrice: 0, taxAmount: 0, description: '' }]);
    };

    const handleDetailChange = (key: number, field: string, value: any) => {
        setDetails(details.map(d => {
            if (d.key === key || d.id === key) {
                const updated = { ...d, [field]: value };
                if (field === 'orderNum' || field === 'taxPrice') {
                    updated.taxAmount = (updated.orderNum || 0) * (updated.taxPrice || 0);
                }
                return updated;
            }
            return d;
        }));
    };

    const handleDeleteDetail = (key: number) => { setDetails(details.filter(d => d.key !== key && d.id !== key)); };

    const handleSubmit = async () => {
        try {
            const values = await form.validateFields();
            if (details.length === 0) { message.warning('请添加至少一个订单明细'); return; }
            setLoading(true);
            const orderData = {
                id: isEdit ? Number(id) : undefined,
                customerId: values.customerId, scId: values.scId, salerId: values.salerId,
                expectDeliveryDate: values.expectDeliveryDate?.format('YYYY-MM-DD'),
                description: values.description,
                details: details.map(d => ({ productId: d.productId, orderNum: d.orderNum, taxPrice: d.taxPrice, description: d.description })),
            };
            if (isEdit) { await updateSaleOrder(orderData); message.success('更新成功'); }
            else { await createSaleOrder(orderData); message.success('创建成功'); }
            navigate('/business/sale/order');
        } catch (error) { message.error('操作失败'); }
        finally { setLoading(false); }
    };

    const totalNum = details.reduce((sum, d) => sum + (d.orderNum || 0), 0);
    const totalAmount = details.reduce((sum, d) => sum + ((d.orderNum || 0) * (d.taxPrice || 0)), 0);

    const detailColumns = [
        {
            title: '商品', dataIndex: 'productId', key: 'productId', width: 200, render: (value: number, record: any) => (
                <Select value={value} onChange={(v) => handleDetailChange(record.key || record.id, 'productId', v)} style={{ width: '100%' }}
                    placeholder="选择商品" showSearch filterOption={(input, option) => (option?.children as unknown as string)?.toLowerCase().includes(input.toLowerCase())}>
                    {materials.map((m: any) => <Option key={m.id} value={m.id}>{m.name}</Option>)}
                </Select>
            )
        },
        {
            title: '数量', dataIndex: 'orderNum', key: 'orderNum', width: 120, render: (value: number, record: any) => (
                <InputNumber min={1} value={value} onChange={(v) => handleDetailChange(record.key || record.id, 'orderNum', v)} />
            )
        },
        {
            title: '含税单价', dataIndex: 'taxPrice', key: 'taxPrice', width: 120, render: (value: number, record: any) => (
                <InputNumber min={0} precision={2} value={value} onChange={(v) => handleDetailChange(record.key || record.id, 'taxPrice', v)} />
            )
        },
        { title: '金额', key: 'taxAmount', width: 120, render: (_: any, record: any) => `￥${((record.orderNum || 0) * (record.taxPrice || 0)).toFixed(2)}` },
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
            <Card title={<Space><Button icon={<ArrowLeftOutlined />} onClick={() => navigate(-1)}>返回</Button><span>{isEdit ? '编辑销售订单' : '新增销售订单'}</span></Space>}>
                <Form form={form} layout="vertical">
                    <div style={{ display: 'flex', gap: 24 }}>
                        <Form.Item name="customerId" label="客户" rules={[{ required: true, message: '请选择客户' }]} style={{ flex: 1 }}>
                            <Select placeholder="选择客户" showSearch filterOption={(input, option) => (option?.children as unknown as string)?.toLowerCase().includes(input.toLowerCase())}>
                                {customers.map((c: any) => <Option key={c.id} value={c.id}>{c.name}</Option>)}
                            </Select>
                        </Form.Item>
                        <Form.Item name="scId" label="仓库" rules={[{ required: true, message: '请选择仓库' }]} style={{ flex: 1 }}>
                            <Select placeholder="选择仓库">
                                {warehouses.map((w: any) => <Option key={w.id} value={w.id}>{w.name}</Option>)}
                            </Select>
                        </Form.Item>
                        <Form.Item name="expectDeliveryDate" label="预计发货日期" style={{ flex: 1 }}>
                            <DatePicker style={{ width: '100%' }} />
                        </Form.Item>
                    </div>
                    <Form.Item name="description" label="备注"><TextArea rows={2} placeholder="订单备注" /></Form.Item>
                </Form>
                <Divider>订单明细</Divider>
                <Button type="dashed" onClick={handleAddDetail} style={{ marginBottom: 16 }} icon={<PlusOutlined />}>添加商品</Button>
                <Table columns={detailColumns} dataSource={details} rowKey={(record) => record.key || record.id} pagination={false}
                    summary={() => (<Table.Summary><Table.Summary.Row>
                        <Table.Summary.Cell index={0}>合计</Table.Summary.Cell><Table.Summary.Cell index={1}>{totalNum}</Table.Summary.Cell>
                        <Table.Summary.Cell index={2}>-</Table.Summary.Cell><Table.Summary.Cell index={3}>￥{totalAmount.toFixed(2)}</Table.Summary.Cell>
                        <Table.Summary.Cell index={4}>-</Table.Summary.Cell><Table.Summary.Cell index={5}>-</Table.Summary.Cell>
                    </Table.Summary.Row></Table.Summary>)}
                />
                <div style={{ marginTop: 24, textAlign: 'center' }}>
                    <Space><Button onClick={() => navigate(-1)}>取消</Button><Button type="primary" loading={loading} onClick={handleSubmit}>{isEdit ? '保存修改' : '创建订单'}</Button></Space>
                </div>
            </Card>
        </div>
    );
};

export default SaleOrderForm;

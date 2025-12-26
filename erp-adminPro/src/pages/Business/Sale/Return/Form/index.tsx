import React, { useState, useEffect } from 'react';
import { Form, Select, InputNumber, Button, Card, Table, Space, message, Divider, Input } from 'antd';
import { DeleteOutlined, ArrowLeftOutlined } from '@ant-design/icons';
import { useNavigate, useParams } from 'react-router-dom';
import { createSaleReturn, updateSaleReturn, getSaleReturnById, getPendingReturnDeliveries } from '@/api/business/saleReturn';
import { getSaleDeliveryById } from '@/api/business/saleDelivery';
import { getCustomerList } from '@/api/basedata/customer';
import { getMaterialPage } from '@/api/material/material';

const { Option } = Select;
const { TextArea } = Input;

const SaleReturnForm: React.FC = () => {
    const navigate = useNavigate();
    const { id } = useParams<{ id: string }>();
    const isEdit = !!id;

    const [form] = Form.useForm();
    const [loading, setLoading] = useState(false);
    const [customers, setCustomers] = useState<any[]>([]);
    const [deliveries, setDeliveries] = useState<any[]>([]);
    const [materials, setMaterials] = useState<any[]>([]);
    const [details, setDetails] = useState<any[]>([]);

    useEffect(() => { loadBasicData(); if (isEdit) loadReturnData(); }, [id]);

    const loadBasicData = async () => {
        try {
            const [customerData, deliveryData, materialData] = await Promise.all([
                getCustomerList(), getPendingReturnDeliveries({ current: 1, size: 1000 }), getMaterialPage({ current: 1, size: 1000 })
            ]);
            setCustomers(Array.isArray(customerData) ? customerData : []);
            setDeliveries((deliveryData as any)?.records || []);
            setMaterials((materialData as any)?.records || []);
        } catch (error) { console.error('加载基础数据失败', error); }
    };

    const loadReturnData = async () => {
        try {
            const data: any = await getSaleReturnById(Number(id));
            if (data?.saleReturn) {
                form.setFieldsValue({ deliveryId: data.saleReturn.deliveryId, description: data.saleReturn.description });
                setDetails(data.details?.map((d: any, i: number) => ({ ...d, key: Date.now() + i })) || []);
            }
        } catch (error) { message.error('加载退货单数据失败'); }
    };

    const handleDeliveryChange = async (deliveryId: number) => {
        try {
            const data: any = await getSaleDeliveryById(deliveryId);
            if (data?.details) {
                const returnDetails = data.details.map((d: any, index: number) => ({
                    key: Date.now() + index, deliveryDetailId: d.id, productId: d.productId,
                    deliveryNum: d.deliveryNum, returnNum: 0, taxPrice: d.taxPrice, description: '',
                }));
                setDetails(returnDetails);
            }
            if (data?.delivery) form.setFieldsValue({ customerId: data.delivery.customerId, scId: data.delivery.scId });
        } catch (error) { message.error('加载出库单明细失败'); }
    };

    const getMaterialName = (productId: number) => materials.find(m => m.id === productId)?.name || productId;
    const handleDetailChange = (key: number, field: string, value: any) => {
        setDetails(details.map(d => {
            if (d.key === key || d.id === key) {
                const updated = { ...d, [field]: value };
                if (field === 'returnNum' || field === 'taxPrice') updated.taxAmount = (updated.returnNum || 0) * (updated.taxPrice || 0);
                return updated;
            }
            return d;
        }));
    };
    const handleDeleteDetail = (key: number) => { setDetails(details.filter(d => d.key !== key && d.id !== key)); };

    const handleSubmit = async () => {
        try {
            const values = await form.validateFields();
            if (details.length === 0) { message.warning('请添加退货明细'); return; }
            if (!details.some(d => d.returnNum > 0)) { message.warning('请填写退货数量'); return; }
            setLoading(true);
            const returnData = {
                id: isEdit ? Number(id) : undefined, deliveryId: values.deliveryId, scId: values.scId, customerId: values.customerId, description: values.description,
                details: details.filter(d => d.returnNum > 0).map(d => ({
                    deliveryDetailId: d.deliveryDetailId, productId: d.productId, deliveryNum: d.deliveryNum, returnNum: d.returnNum, taxPrice: d.taxPrice, description: d.description,
                })),
            };
            if (isEdit) { await updateSaleReturn(returnData); message.success('更新成功'); }
            else { await createSaleReturn(returnData); message.success('创建成功'); }
            navigate('/business/sale/return');
        } catch (error) { message.error('操作失败'); } finally { setLoading(false); }
    };

    const totalNum = details.reduce((sum, d) => sum + (d.returnNum || 0), 0);
    const totalAmount = details.reduce((sum, d) => sum + ((d.returnNum || 0) * (d.taxPrice || 0)), 0);

    const detailColumns = [
        { title: '商品', dataIndex: 'productId', key: 'productId', width: 200, render: (productId: number) => getMaterialName(productId) },
        { title: '出库数量', dataIndex: 'deliveryNum', key: 'deliveryNum', width: 100 },
        {
            title: '退货数量', dataIndex: 'returnNum', key: 'returnNum', width: 120, render: (value: number, record: any) => (
                <InputNumber min={0} max={record.deliveryNum} value={value} onChange={(v) => handleDetailChange(record.key || record.id, 'returnNum', v)} />
            )
        },
        { title: '含税单价', dataIndex: 'taxPrice', key: 'taxPrice', width: 120, render: (price: number) => `￥${(price || 0).toFixed(2)}` },
        { title: '金额', key: 'taxAmount', width: 120, render: (_: any, record: any) => `￥${((record.returnNum || 0) * (record.taxPrice || 0)).toFixed(2)}` },
        {
            title: '备注', dataIndex: 'description', key: 'description', render: (value: string, record: any) => (
                <Input value={value} onChange={(e) => handleDetailChange(record.key || record.id, 'description', e.target.value)} placeholder="退货原因" />
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
            <Card title={<Space><Button icon={<ArrowLeftOutlined />} onClick={() => navigate(-1)}>返回</Button><span>{isEdit ? '编辑销售退货' : '新增销售退货'}</span></Space>}>
                <Form form={form} layout="vertical">
                    <div style={{ display: 'flex', gap: 24 }}>
                        <Form.Item name="deliveryId" label="选择出库单" rules={[{ required: true, message: '请选择出库单' }]} style={{ flex: 1 }}>
                            <Select placeholder="选择已确认的出库单" onChange={handleDeliveryChange} disabled={isEdit} showSearch
                                filterOption={(input, option) => (option?.children as unknown as string)?.toLowerCase()?.includes(input.toLowerCase())}>
                                {deliveries.map((d: any) => <Option key={d.id} value={d.id}>{d.code} - {customers.find(c => c.id === d.customerId)?.name || ''}</Option>)}
                            </Select>
                        </Form.Item>
                        <Form.Item name="customerId" label="客户" style={{ flex: 1 }}>
                            <Select placeholder="客户" disabled>{customers.map((c: any) => <Option key={c.id} value={c.id}>{c.name}</Option>)}</Select>
                        </Form.Item>
                    </div>
                    <Form.Item name="description" label="备注"><TextArea rows={2} placeholder="退货备注" /></Form.Item>
                </Form>
                <Divider>退货明细</Divider>
                <Table columns={detailColumns} dataSource={details} rowKey={(record) => record.key || record.id} pagination={false}
                    summary={() => (<Table.Summary><Table.Summary.Row>
                        <Table.Summary.Cell index={0}>合计</Table.Summary.Cell><Table.Summary.Cell index={1}>-</Table.Summary.Cell>
                        <Table.Summary.Cell index={2}>{totalNum}</Table.Summary.Cell><Table.Summary.Cell index={3}>-</Table.Summary.Cell>
                        <Table.Summary.Cell index={4}>￥{totalAmount.toFixed(2)}</Table.Summary.Cell><Table.Summary.Cell index={5}>-</Table.Summary.Cell>
                        <Table.Summary.Cell index={6}>-</Table.Summary.Cell>
                    </Table.Summary.Row></Table.Summary>)}
                />
                <div style={{ marginTop: 24, textAlign: 'center' }}>
                    <Space><Button onClick={() => navigate(-1)}>取消</Button><Button type="primary" loading={loading} onClick={handleSubmit}>{isEdit ? '保存修改' : '创建退货单'}</Button></Space>
                </div>
            </Card>
        </div>
    );
};

export default SaleReturnForm;

import React, { useState, useEffect } from 'react';
import { Card, Form, Input, Select, Button, message, Table, InputNumber, Space } from 'antd';
import { SaveOutlined, ArrowLeftOutlined, PlusOutlined, DeleteOutlined } from '@ant-design/icons';
import { useNavigate, useParams } from 'react-router-dom';
import { getCustomerFeeSheetById, addCustomerFeeSheet, updateCustomerFeeSheet } from '@/api/business/settle';
import { getCustomerList } from '@/api/basedata/customer';
import { getSettleInOutItemList } from '@/api/business/settle';

const CustomerFeeSheetForm: React.FC = () => {
    const navigate = useNavigate();
    const { id } = useParams<{ id: string }>();
    const isEdit = !!id;
    const [form] = Form.useForm();
    const [loading, setLoading] = useState(false);
    const [submitting, setSubmitting] = useState(false);
    const [customers, setCustomers] = useState<any[]>([]);
    const [items, setItems] = useState<any[]>([]);
    const [details, setDetails] = useState<any[]>([{ key: Date.now(), itemId: undefined, amount: 0 }]);

    const loadOptions = async () => {
        try { const res: any = await getCustomerList(); setCustomers(res || []); } catch (e) { console.error(e); }
        try { const res: any = await getSettleInOutItemList(); setItems(res || []); } catch (e) { console.error(e); }
    };
    const loadData = async () => {
        if (!id) return;
        setLoading(true);
        try {
            const res: any = await getCustomerFeeSheetById(Number(id));
            if (res?.sheet) {
                form.setFieldsValue({ customerId: res.sheet.customerId, sheetType: res.sheet.sheetType, description: res.sheet.description });
                if (res.details?.length > 0) setDetails(res.details.map((d: any, idx: number) => ({ key: idx, itemId: d.itemId, amount: d.amount })));
            }
        } catch (e) { message.error('加载数据失败'); }
        finally { setLoading(false); }
    };

    useEffect(() => { loadOptions(); }, []);
    useEffect(() => { loadData(); }, [id]);

    const handleAddDetail = () => { setDetails([...details, { key: Date.now(), itemId: undefined, amount: 0 }]); };
    const handleRemoveDetail = (key: number) => { if (details.length <= 1) { message.warning('至少需要一条明细'); return; } setDetails(details.filter(d => d.key !== key)); };
    const handleDetailChange = (key: number, field: string, value: any) => { setDetails(details.map(d => d.key === key ? { ...d, [field]: value } : d)); };

    const onFinish = async (values: any) => {
        if (details.some(d => !d.itemId)) { message.error('请选择所有明细的收支项目'); return; }
        setSubmitting(true);
        try {
            const data = { ...values, id: id ? Number(id) : undefined, details: details.map(d => ({ itemId: d.itemId, amount: d.amount || 0 })) };
            console.log('提交数据:', data);
            if (isEdit) { await updateCustomerFeeSheet(data); message.success('修改成功'); }
            else { await addCustomerFeeSheet(data); message.success('新增成功'); }
            navigate('/business/settle/customer/fee');
        } catch (e: any) { console.error('提交失败:', e); message.error(e?.message || '操作失败'); }
        finally { setSubmitting(false); }
    };

    const detailColumns = [
        { title: '收支项目', dataIndex: 'itemId', render: (_: any, r: any) => <Select style={{ width: 200 }} value={r.itemId} onChange={v => handleDetailChange(r.key, 'itemId', v)} placeholder="请选择">{items.map(i => <Select.Option key={i.id} value={i.id}>{i.name}</Select.Option>)}</Select> },
        { title: '金额', dataIndex: 'amount', render: (_: any, r: any) => <InputNumber style={{ width: 120 }} value={r.amount} onChange={v => handleDetailChange(r.key, 'amount', v || 0)} min={0} precision={2} /> },
        { title: '操作', key: 'action', width: 80, render: (_: any, r: any) => <Button type="link" danger icon={<DeleteOutlined />} onClick={() => handleRemoveDetail(r.key)} /> },
    ];

    const totalAmount = details.reduce((sum, d) => sum + (d.amount || 0), 0);

    return (
        <div style={{ padding: '24px' }}>
            <Card title={isEdit ? '编辑客户费用单' : '新增客户费用单'} extra={<Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/business/settle/customer/fee')}>返回</Button>} loading={loading}>
                <Form form={form} layout="vertical" onFinish={onFinish} style={{ maxWidth: 800 }}>
                    <Space size="large">
                        <Form.Item name="customerId" label="客户" rules={[{ required: true, message: '请选择客户' }]}>
                            <Select placeholder="请选择" style={{ width: 200 }} showSearch optionFilterProp="children">{customers.map(c => <Select.Option key={c.id} value={c.id}>{c.name}</Select.Option>)}</Select>
                        </Form.Item>
                        <Form.Item name="sheetType" label="单据类型" rules={[{ required: true, message: '请选择类型' }]}>
                            <Select placeholder="请选择" style={{ width: 120 }}><Select.Option value={1}>收款</Select.Option><Select.Option value={2}>扣款</Select.Option></Select>
                        </Form.Item>
                    </Space>
                    <Form.Item name="description" label="备注"><Input.TextArea rows={2} /></Form.Item>
                </Form>
                <Card title="费用明细" size="small" style={{ marginTop: 16 }} extra={<Button type="dashed" icon={<PlusOutlined />} onClick={handleAddDetail}>添加</Button>}>
                    <Table columns={detailColumns} dataSource={details} rowKey="key" pagination={false} size="small" footer={() => <div style={{ textAlign: 'right' }}>合计: <strong>￥{totalAmount.toFixed(2)}</strong></div>} />
                </Card>
                <Space style={{ marginTop: 24 }}>
                    <Button type="primary" icon={<SaveOutlined />} loading={submitting} onClick={() => form.submit()}>保存</Button>
                    <Button onClick={() => navigate('/business/settle/customer/fee')}>取消</Button>
                </Space>
            </Card>
        </div>
    );
};

export default CustomerFeeSheetForm;

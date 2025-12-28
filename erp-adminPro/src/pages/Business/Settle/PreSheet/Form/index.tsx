import React, { useState, useEffect } from 'react';
import { Card, Form, Input, Select, Button, message, Table, InputNumber, Space } from 'antd';
import { SaveOutlined, ArrowLeftOutlined, PlusOutlined, DeleteOutlined } from '@ant-design/icons';
import { useNavigate, useParams } from 'react-router-dom';
import { getPreSheetById, addPreSheet, updatePreSheet, listEnabledSettleItems } from '@/api/business/settle';
import { getSupplierList } from '@/api/basedata/supplier';

const PreSheetForm: React.FC = () => {
    const navigate = useNavigate();
    const { id } = useParams<{ id: string }>();
    const isEdit = !!id;

    const [form] = Form.useForm();
    const [loading, setLoading] = useState(false);
    const [submitting, setSubmitting] = useState(false);

    const [suppliers, setSuppliers] = useState<any[]>([]);
    const [settleItems, setSettleItems] = useState<any[]>([]);
    const [details, setDetails] = useState<any[]>([{ key: Date.now(), itemId: undefined, amount: 0 }]);

    const loadOptions = async () => {
        try {
            const [suppRes, itemRes]: any[] = await Promise.all([getSupplierList(), listEnabledSettleItems()]);
            setSuppliers(suppRes || []);
            setSettleItems(itemRes || []);
        } catch (e) { console.error(e); }
    };

    const loadData = async () => {
        if (!id) return;
        setLoading(true);
        try {
            const res: any = await getPreSheetById(Number(id));
            if (res?.sheet) {
                form.setFieldsValue({ supplierId: res.sheet.supplierId, description: res.sheet.description });
                if (res.details && res.details.length > 0) {
                    setDetails(res.details.map((d: any, idx: number) => ({ key: idx, itemId: d.itemId, amount: d.amount })));
                }
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
        if (details.some(d => !d.itemId || d.amount <= 0)) { message.error('请完整填写明细信息'); return; }

        setSubmitting(true);
        try {
            const data = { ...values, id: id ? Number(id) : undefined, details: details.map(d => ({ itemId: d.itemId, amount: d.amount })) };
            if (isEdit) { await updatePreSheet(data); message.success('修改成功'); }
            else { await addPreSheet(data); message.success('新增成功'); }
            navigate('/business/settle/pre');
        } catch (e) { message.error('操作失败'); }
        finally { setSubmitting(false); }
    };

    const detailColumns = [
        {
            title: '收支项目', dataIndex: 'itemId', render: (_: any, record: any) => (
                <Select style={{ width: 200 }} value={record.itemId} onChange={v => handleDetailChange(record.key, 'itemId', v)} placeholder="请选择">
                    {settleItems.map(item => <Select.Option key={item.id} value={item.id}>{item.name}</Select.Option>)}
                </Select>
            )
        },
        {
            title: '金额', dataIndex: 'amount', render: (_: any, record: any) => (
                <InputNumber style={{ width: 150 }} value={record.amount} onChange={v => handleDetailChange(record.key, 'amount', v || 0)} min={0} precision={2} />
            )
        },
        { title: '操作', key: 'action', width: 80, render: (_: any, record: any) => <Button type="link" danger icon={<DeleteOutlined />} onClick={() => handleRemoveDetail(record.key)} /> },
    ];

    const totalAmount = details.reduce((sum, d) => sum + (d.amount || 0), 0);

    return (
        <div style={{ padding: '24px' }}>
            <Card title={isEdit ? '编辑供应商预付款单' : '新增供应商预付款单'} extra={<Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/business/settle/pre')}>返回</Button>} loading={loading}>
                <Form form={form} layout="vertical" onFinish={onFinish} style={{ maxWidth: 800 }}>
                    <Form.Item name="supplierId" label="供应商" rules={[{ required: true, message: '请选择供应商' }]}>
                        <Select placeholder="请选择供应商" showSearch optionFilterProp="children">
                            {suppliers.map(s => <Select.Option key={s.id} value={s.id}>{s.name}</Select.Option>)}
                        </Select>
                    </Form.Item>
                    <Form.Item name="description" label="备注"><Input.TextArea rows={3} placeholder="请输入备注" /></Form.Item>
                </Form>
                <Card title="预付款明细" size="small" style={{ marginTop: 16 }} extra={<Button type="dashed" icon={<PlusOutlined />} onClick={handleAddDetail}>添加明细</Button>}>
                    <Table columns={detailColumns} dataSource={details} rowKey="key" pagination={false} size="small" footer={() => <div style={{ textAlign: 'right' }}>总金额: <strong>￥{totalAmount.toFixed(2)}</strong></div>} />
                </Card>
                <Space style={{ marginTop: 24 }}>
                    <Button type="primary" icon={<SaveOutlined />} loading={submitting} onClick={() => form.submit()}>保存</Button>
                    <Button onClick={() => navigate('/business/settle/pre')}>取消</Button>
                </Space>
            </Card>
        </div>
    );
};

export default PreSheetForm;

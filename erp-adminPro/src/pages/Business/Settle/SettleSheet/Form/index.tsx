import React, { useState, useEffect } from 'react';
import { Card, Form, Input, Select, Button, message, Table, InputNumber, Space, DatePicker } from 'antd';
import { SaveOutlined, ArrowLeftOutlined, PlusOutlined, DeleteOutlined } from '@ant-design/icons';
import { useNavigate, useParams } from 'react-router-dom';
import { getSettleSheetById, addSettleSheet, updateSettleSheet } from '@/api/business/settle';
import { getSupplierList } from '@/api/basedata/supplier';
import dayjs from 'dayjs';

const SettleSheetForm: React.FC = () => {
    const navigate = useNavigate();
    const { id } = useParams<{ id: string }>();
    const isEdit = !!id;
    const [form] = Form.useForm();
    const [loading, setLoading] = useState(false);
    const [submitting, setSubmitting] = useState(false);
    const [suppliers, setSuppliers] = useState<any[]>([]);
    const [details, setDetails] = useState<any[]>([{ key: Date.now(), bizId: 0, bizCode: '', payAmount: 0, discountAmount: 0, description: '' }]);

    const loadOptions = async () => { try { const res: any = await getSupplierList(); setSuppliers(res || []); } catch (e) { console.error(e); } };
    const loadData = async () => {
        if (!id) return;
        setLoading(true);
        try {
            const res: any = await getSettleSheetById(Number(id));
            if (res?.sheet) {
                form.setFieldsValue({
                    supplierId: res.sheet.supplierId,
                    startDate: res.sheet.startDate ? dayjs(res.sheet.startDate) : null,
                    endDate: res.sheet.endDate ? dayjs(res.sheet.endDate) : null,
                    totalDiscountAmount: res.sheet.totalDiscountAmount,
                    description: res.sheet.description
                });
                if (res.details?.length > 0) {
                    setDetails(res.details.map((d: any, idx: number) => ({ key: idx, bizId: d.bizId, bizCode: d.bizCode, payAmount: d.payAmount, discountAmount: d.discountAmount, description: d.description })));
                }
            }
        } catch (e) { message.error('加载数据失败'); }
        finally { setLoading(false); }
    };

    useEffect(() => { loadOptions(); }, []);
    useEffect(() => { loadData(); }, [id]);

    const handleAddDetail = () => { setDetails([...details, { key: Date.now(), bizId: 0, bizCode: '', payAmount: 0, discountAmount: 0, description: '' }]); };
    const handleRemoveDetail = (key: number) => { if (details.length <= 1) { message.warning('至少需要一条明细'); return; } setDetails(details.filter(d => d.key !== key)); };
    const handleDetailChange = (key: number, field: string, value: any) => { setDetails(details.map(d => d.key === key ? { ...d, [field]: value } : d)); };

    const onFinish = async (values: any) => {
        if (details.some(d => !d.bizCode)) { message.error('请填写所有明细的对账单编号'); return; }
        setSubmitting(true);
        try {
            const data = {
                ...values, id: id ? Number(id) : undefined,
                startDate: values.startDate?.format('YYYY-MM-DD'),
                endDate: values.endDate?.format('YYYY-MM-DD'),
                details: details.map(d => ({ bizId: d.bizId || 0, bizCode: d.bizCode, payAmount: d.payAmount || 0, discountAmount: d.discountAmount || 0, description: d.description }))
            };
            console.log('提交数据:', data);
            if (isEdit) { await updateSettleSheet(data); message.success('修改成功'); }
            else { await addSettleSheet(data); message.success('新增成功'); }
            navigate('/business/settle/sheet');
        } catch (e: any) { console.error('提交失败:', e); message.error(e?.message || '操作失败'); }
        finally { setSubmitting(false); }
    };

    const detailColumns = [
        { title: '对账单编号', dataIndex: 'bizCode', render: (_: any, r: any) => <Input style={{ width: 150 }} value={r.bizCode} onChange={e => handleDetailChange(r.key, 'bizCode', e.target.value)} placeholder="输入对账单编号" /> },
        { title: '实付金额', dataIndex: 'payAmount', render: (_: any, r: any) => <InputNumber style={{ width: 120 }} value={r.payAmount} onChange={v => handleDetailChange(r.key, 'payAmount', v || 0)} min={0} precision={2} /> },
        { title: '优惠金额', dataIndex: 'discountAmount', render: (_: any, r: any) => <InputNumber style={{ width: 120 }} value={r.discountAmount} onChange={v => handleDetailChange(r.key, 'discountAmount', v || 0)} min={0} precision={2} /> },
        { title: '备注', dataIndex: 'description', render: (_: any, r: any) => <Input style={{ width: 150 }} value={r.description} onChange={e => handleDetailChange(r.key, 'description', e.target.value)} /> },
        { title: '操作', key: 'action', width: 80, render: (_: any, r: any) => <Button type="link" danger icon={<DeleteOutlined />} onClick={() => handleRemoveDetail(r.key)} /> },
    ];

    const totalAmount = details.reduce((sum, d) => sum + (d.payAmount || 0), 0);
    const totalDiscount = details.reduce((sum, d) => sum + (d.discountAmount || 0), 0);

    return (
        <div style={{ padding: '24px' }}>
            <Card title={isEdit ? '编辑供应商结算单' : '新增供应商结算单'} extra={<Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/business/settle/sheet')}>返回</Button>} loading={loading}>
                <Form form={form} layout="vertical" onFinish={onFinish} style={{ maxWidth: 900 }}>
                    <Space size="large">
                        <Form.Item name="supplierId" label="供应商" rules={[{ required: true, message: '请选择供应商' }]}>
                            <Select placeholder="请选择" style={{ width: 200 }} showSearch optionFilterProp="children">{suppliers.map(s => <Select.Option key={s.id} value={s.id}>{s.name}</Select.Option>)}</Select>
                        </Form.Item>
                        <Form.Item name="startDate" label="起始日期"><DatePicker /></Form.Item>
                        <Form.Item name="endDate" label="截止日期"><DatePicker /></Form.Item>
                    </Space>
                    <Form.Item name="description" label="备注"><Input.TextArea rows={2} /></Form.Item>
                </Form>
                <Card title="结算明细" size="small" style={{ marginTop: 16 }} extra={<Button type="dashed" icon={<PlusOutlined />} onClick={handleAddDetail}>添加</Button>}>
                    <Table columns={detailColumns} dataSource={details} rowKey="key" pagination={false} size="small" footer={() => <div style={{ textAlign: 'right' }}>实付总金额: <strong>￥{totalAmount.toFixed(2)}</strong> | 优惠总金额: <strong>￥{totalDiscount.toFixed(2)}</strong></div>} />
                </Card>
                <Space style={{ marginTop: 24 }}>
                    <Button type="primary" icon={<SaveOutlined />} loading={submitting} onClick={() => form.submit()}>保存</Button>
                    <Button onClick={() => navigate('/business/settle/sheet')}>取消</Button>
                </Space>
            </Card>
        </div>
    );
};

export default SettleSheetForm;

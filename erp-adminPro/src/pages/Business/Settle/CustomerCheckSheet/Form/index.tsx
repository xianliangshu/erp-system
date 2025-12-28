import React, { useState, useEffect } from 'react';
import { Card, Form, Input, Select, Button, message, InputNumber, Space, DatePicker } from 'antd';
import { SaveOutlined, ArrowLeftOutlined } from '@ant-design/icons';
import { useNavigate, useParams } from 'react-router-dom';
import { getCustomerCheckSheetById, addCustomerCheckSheet, updateCustomerCheckSheet } from '@/api/business/settle';
import { getCustomerList } from '@/api/basedata/customer';
import dayjs from 'dayjs';

const CustomerCheckSheetForm: React.FC = () => {
    const navigate = useNavigate();
    const { id } = useParams<{ id: string }>();
    const isEdit = !!id;
    const [form] = Form.useForm();
    const [loading, setLoading] = useState(false);
    const [submitting, setSubmitting] = useState(false);
    const [customers, setCustomers] = useState<any[]>([]);

    const loadOptions = async () => { try { const res: any = await getCustomerList(); setCustomers(res || []); } catch (e) { console.error(e); } };
    const loadData = async () => {
        if (!id) return;
        setLoading(true);
        try {
            const res: any = await getCustomerCheckSheetById(Number(id));
            if (res?.sheet) {
                form.setFieldsValue({
                    customerId: res.sheet.customerId,
                    startDate: res.sheet.startDate ? dayjs(res.sheet.startDate) : null,
                    endDate: res.sheet.endDate ? dayjs(res.sheet.endDate) : null,
                    totalDiscountAmount: res.sheet.totalDiscountAmount,
                    totalAmount: res.sheet.totalAmount,
                    description: res.sheet.description
                });
            }
        } catch (e) { message.error('加载数据失败'); }
        finally { setLoading(false); }
    };

    useEffect(() => { loadOptions(); }, []);
    useEffect(() => { loadData(); }, [id]);

    const onFinish = async (values: any) => {
        setSubmitting(true);
        try {
            const data = {
                id: id ? Number(id) : undefined,
                customerId: values.customerId,
                startDate: values.startDate?.format('YYYY-MM-DD'),
                endDate: values.endDate?.format('YYYY-MM-DD'),
                totalDiscountAmount: values.totalDiscountAmount || 0,
                description: values.description,
                details: []
            };
            console.log('提交数据:', data);
            if (isEdit) { await updateCustomerCheckSheet(data); message.success('修改成功'); }
            else { await addCustomerCheckSheet(data); message.success('新增成功'); }
            navigate('/business/settle/customer/check');
        } catch (e: any) { console.error('提交失败:', e); message.error(e?.message || '操作失败'); }
        finally { setSubmitting(false); }
    };

    return (
        <div style={{ padding: '24px' }}>
            <Card title={isEdit ? '编辑客户对账单' : '新增客户对账单'} extra={<Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/business/settle/customer/check')}>返回</Button>} loading={loading}>
                <Form form={form} layout="vertical" onFinish={onFinish} style={{ maxWidth: 800 }}>
                    <Form.Item name="customerId" label="客户" rules={[{ required: true, message: '请选择客户' }]}>
                        <Select placeholder="请选择" style={{ width: 200 }} showSearch optionFilterProp="children">{customers.map(c => <Select.Option key={c.id} value={c.id}>{c.name}</Select.Option>)}</Select>
                    </Form.Item>
                    <Space size="large">
                        <Form.Item name="startDate" label="起始日期"><DatePicker /></Form.Item>
                        <Form.Item name="endDate" label="截止日期"><DatePicker /></Form.Item>
                    </Space>
                    <Form.Item name="totalAmount" label="应付金额"><InputNumber style={{ width: 150 }} min={0} precision={2} prefix="￥" /></Form.Item>
                    <Form.Item name="totalDiscountAmount" label="优惠金额"><InputNumber style={{ width: 150 }} min={0} precision={2} prefix="￥" /></Form.Item>
                    <Form.Item name="description" label="备注"><Input.TextArea rows={2} /></Form.Item>
                    <Space style={{ marginTop: 24 }}>
                        <Button type="primary" icon={<SaveOutlined />} loading={submitting} htmlType="submit">保存</Button>
                        <Button onClick={() => navigate('/business/settle/customer/check')}>取消</Button>
                    </Space>
                </Form>
            </Card>
        </div>
    );
};

export default CustomerCheckSheetForm;

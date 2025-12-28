import React, { useState, useEffect } from 'react';
import { Card, Form, Select, Input, Button, Table, InputNumber, Space, message, Modal } from 'antd';
import { PlusOutlined, DeleteOutlined, ArrowLeftOutlined, SaveOutlined, ExclamationCircleOutlined } from '@ant-design/icons';
import { useNavigate, useParams } from 'react-router-dom';
import { addRetailReturn, updateRetailReturn, getRetailReturnById } from '@/api/business/retail';
import { getWarehouseList } from '@/api/basedata/warehouse';
import { getCustomerList } from '@/api/basedata/customer';
import { getMaterialList } from '@/api/material/material';

const RetailReturnForm: React.FC = () => {
    const navigate = useNavigate();
    const { id } = useParams<{ id: string }>();
    const isEdit = !!id;

    const [form] = Form.useForm();
    const [loading, setLoading] = useState(false);
    const [submitting, setSubmitting] = useState(false);
    const [details, setDetails] = useState<any[]>([]);

    const [warehouses, setWarehouses] = useState<any[]>([]);
    const [customers, setCustomers] = useState<any[]>([]);
    const [materials, setMaterials] = useState<any[]>([]);

    const loadOptions = async () => {
        try {
            const [whRes, custRes, matRes]: any[] = await Promise.all([getWarehouseList(), getCustomerList(), getMaterialList()]);
            setWarehouses(whRes || []);
            setCustomers(custRes || []);
            setMaterials(matRes || []);
        } catch (e) { console.error(e); }
    };

    const loadDetail = async () => {
        if (!id) return;
        setLoading(true);
        try {
            const res: any = await getRetailReturnById(Number(id));
            form.setFieldsValue(res.sheet);
            setDetails(res.details.map((d: any, i: number) => ({ ...d, key: i })));
        } catch (e) { message.error('加载详情失败'); }
        finally { setLoading(false); }
    };

    useEffect(() => { loadOptions(); loadDetail(); }, [id]);

    const handleAddRow = () => {
        setDetails([...details, { key: Date.now(), productId: undefined, returnNum: 1, taxPrice: 0, description: '' }]);
    };

    const handleDetailChange = (key: any, field: string, value: any) => {
        setDetails(details.map(d => d.key === key ? { ...d, [field]: value } : d));
    };

    const handleSubmit = async () => {
        try {
            const values = await form.validateFields();
            if (details.length === 0) { message.warning('请添加明细'); return; }
            if (details.some(d => !d.productId || !d.returnNum)) { message.warning('请完善明细信息'); return; }

            setSubmitting(true);
            const data = { ...values, id: id ? Number(id) : undefined, details };
            if (isEdit) { await updateRetailReturn(data); message.success('修改成功'); }
            else { await addRetailReturn(data); message.success('新增成功'); }
            navigate('/business/retail/return');
        } catch (e) { console.error(e); }
        finally { setSubmitting(false); }
    };

    const columns = [
        {
            title: '商品', dataIndex: 'productId', key: 'productId', width: 250, render: (val: any, record: any) => (
                <Select style={{ width: '100%' }} value={val} onChange={v => handleDetailChange(record.key, 'productId', v)} showSearch optionFilterProp="children">
                    {materials.map(m => <Select.Option key={m.id} value={m.id}>{m.code} - {m.name}</Select.Option>)}
                </Select>
            )
        },
        {
            title: '数量', dataIndex: 'returnNum', key: 'returnNum', width: 120, render: (val: any, record: any) => (
                <InputNumber style={{ width: '100%' }} value={val} min={0.01} onChange={v => handleDetailChange(record.key, 'returnNum', v)} />
            )
        },
        {
            title: '含税单价', dataIndex: 'taxPrice', key: 'taxPrice', width: 120, render: (val: any, record: any) => (
                <InputNumber style={{ width: '100%' }} value={val} min={0} precision={2} onChange={v => handleDetailChange(record.key, 'taxPrice', v)} />
            )
        },
        {
            title: '金额', key: 'amount', width: 120, render: (_: any, record: any) => `￥${((record.returnNum || 0) * (record.taxPrice || 0)).toFixed(2)}`
        },
        { title: '备注', dataIndex: 'description', key: 'description', render: (val: any, record: any) => <Input value={val} onChange={e => handleDetailChange(record.key, 'description', e.target.value)} /> },
        { title: '操作', key: 'action', width: 80, render: (_: any, record: any) => <Button type="link" danger icon={<DeleteOutlined />} onClick={() => setDetails(details.filter(d => d.key !== record.key))}>删除</Button> },
    ];

    return (
        <div style={{ padding: '24px' }}>
            <Card title={isEdit ? '编辑零售退货单' : '新增零售退货单'} loading={loading} extra={<Space><Button icon={<ArrowLeftOutlined />} onClick={() => navigate(-1)}>返回</Button><Button type="primary" icon={<SaveOutlined />} loading={submitting} onClick={handleSubmit}>保存</Button></Space>}>
                <Form form={form} layout="inline" style={{ marginBottom: 24 }}>
                    <Form.Item name="scId" label="仓库" rules={[{ required: true, message: '请选择仓库' }]}>
                        <Select style={{ width: 200 }} placeholder="请选择仓库">{warehouses.map(w => <Select.Option key={w.id} value={w.id}>{w.name}</Select.Option>)}</Select>
                    </Form.Item>
                    <Form.Item name="customerId" label="客户">
                        <Select style={{ width: 200 }} placeholder="请选择客户" allowClear>{customers.map(c => <Select.Option key={c.id} value={c.id}>{c.name}</Select.Option>)}</Select>
                    </Form.Item>
                    <Form.Item name="description" label="备注"><Input style={{ width: 300 }} placeholder="请输入备注" /></Form.Item>
                </Form>
                <Button type="dashed" icon={<PlusOutlined />} onClick={handleAddRow} block style={{ marginBottom: 16 }}>添加商品</Button>
                <Table columns={columns} dataSource={details} rowKey="key" pagination={false} size="small" />
            </Card>
        </div>
    );
};

export default RetailReturnForm;

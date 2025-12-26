import React, { useState, useEffect } from 'react';
import { Form, Select, DatePicker, InputNumber, Button, Card, Table, Space, message, Divider, Input } from 'antd';
import { PlusOutlined, DeleteOutlined, ArrowLeftOutlined } from '@ant-design/icons';
import { useNavigate, useParams } from 'react-router-dom';
import { createStockCheck, updateStockCheck, getStockCheckById } from '@/api/business/stockCheck';
import { getWarehouseList } from '@/api/basedata/warehouse';
import { getMaterialPage } from '@/api/material/material';
import dayjs from 'dayjs';

const { Option } = Select;
const { TextArea } = Input;

const StockCheckForm: React.FC = () => {
    const navigate = useNavigate();
    const { id } = useParams<{ id: string }>();
    const isEdit = !!id;

    const [form] = Form.useForm();
    const [loading, setLoading] = useState(false);
    const [warehouses, setWarehouses] = useState<any[]>([]);
    const [materials, setMaterials] = useState<any[]>([]);
    const [details, setDetails] = useState<any[]>([]);

    useEffect(() => { loadBasicData(); if (isEdit) loadCheckData(); }, [id]);

    const loadBasicData = async () => {
        try {
            const [warehouseData, materialData] = await Promise.all([getWarehouseList(), getMaterialPage({ current: 1, size: 1000 })]);
            setWarehouses(Array.isArray(warehouseData) ? warehouseData : []);
            setMaterials((materialData as any)?.records || []);
        } catch (error) { console.error('加载基础数据失败', error); }
    };

    const loadCheckData = async () => {
        try {
            const data: any = await getStockCheckById(Number(id));
            if (data?.stockCheck) {
                form.setFieldsValue({ scId: data.stockCheck.scId, checkDate: data.stockCheck.checkDate ? dayjs(data.stockCheck.checkDate) : null, description: data.stockCheck.description });
                setDetails(data.details?.map((d: any, i: number) => ({ ...d, key: Date.now() + i })) || []);
            }
        } catch (error) { message.error('加载盘点单数据失败'); }
    };

    const handleAddDetail = () => { setDetails([...details, { key: Date.now(), productId: undefined, stockNum: 0, actualNum: 0, costPrice: 0, description: '' }]); };
    const getMaterialName = (productId: number) => materials.find(m => m.id === productId)?.name || productId;
    const handleDetailChange = (key: number, field: string, value: any) => {
        setDetails(details.map(d => {
            if (d.key === key || d.id === key) {
                const updated = { ...d, [field]: value };
                updated.diffNum = (updated.actualNum || 0) - (updated.stockNum || 0);
                updated.diffAmount = updated.diffNum * (updated.costPrice || 0);
                return updated;
            }
            return d;
        }));
    };
    const handleDeleteDetail = (key: number) => { setDetails(details.filter(d => d.key !== key && d.id !== key)); };

    const handleSubmit = async () => {
        try {
            const values = await form.validateFields();
            if (details.length === 0) { message.warning('请添加盘点明细'); return; }
            setLoading(true);
            const checkData = {
                id: isEdit ? Number(id) : undefined, scId: values.scId, checkDate: values.checkDate?.format('YYYY-MM-DD'), description: values.description,
                details: details.map(d => ({ productId: d.productId, stockNum: d.stockNum, actualNum: d.actualNum, costPrice: d.costPrice, description: d.description })),
            };
            if (isEdit) { await updateStockCheck(checkData); message.success('更新成功'); }
            else { await createStockCheck(checkData); message.success('创建成功'); }
            navigate('/business/stock/check');
        } catch (error) { message.error('操作失败'); } finally { setLoading(false); }
    };

    const totalProfit = details.filter(d => (d.actualNum || 0) > (d.stockNum || 0)).reduce((sum, d) => sum + ((d.actualNum || 0) - (d.stockNum || 0)), 0);
    const totalLoss = details.filter(d => (d.actualNum || 0) < (d.stockNum || 0)).reduce((sum, d) => sum + ((d.stockNum || 0) - (d.actualNum || 0)), 0);

    const detailColumns = [
        {
            title: '商品', dataIndex: 'productId', key: 'productId', width: 200, render: (value: number, record: any) => (
                <Select value={value} onChange={(v) => handleDetailChange(record.key || record.id, 'productId', v)} style={{ width: '100%' }} placeholder="选择商品" showSearch
                    filterOption={(input, option) => (option?.children as unknown as string)?.toLowerCase().includes(input.toLowerCase())}>
                    {materials.map((m: any) => <Option key={m.id} value={m.id}>{m.name}</Option>)}
                </Select>
            )
        },
        {
            title: '账面数量', dataIndex: 'stockNum', key: 'stockNum', width: 120, render: (value: number, record: any) => (
                <InputNumber min={0} value={value} onChange={(v) => handleDetailChange(record.key || record.id, 'stockNum', v)} />
            )
        },
        {
            title: '实盘数量', dataIndex: 'actualNum', key: 'actualNum', width: 120, render: (value: number, record: any) => (
                <InputNumber min={0} value={value} onChange={(v) => handleDetailChange(record.key || record.id, 'actualNum', v)} />
            )
        },
        {
            title: '差异', key: 'diffNum', width: 100, render: (_: any, record: any) => {
                const diff = (record.actualNum || 0) - (record.stockNum || 0);
                return <span style={{ color: diff > 0 ? 'green' : diff < 0 ? 'red' : 'inherit' }}>{diff > 0 ? '+' : ''}{diff}</span>;
            }
        },
        {
            title: '成本单价', dataIndex: 'costPrice', key: 'costPrice', width: 120, render: (value: number, record: any) => (
                <InputNumber min={0} precision={2} value={value} onChange={(v) => handleDetailChange(record.key || record.id, 'costPrice', v)} />
            )
        },
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
            <Card title={<Space><Button icon={<ArrowLeftOutlined />} onClick={() => navigate(-1)}>返回</Button><span>{isEdit ? '编辑库存盘点' : '新增库存盘点'}</span></Space>}>
                <Form form={form} layout="vertical">
                    <div style={{ display: 'flex', gap: 24 }}>
                        <Form.Item name="scId" label="仓库" rules={[{ required: true, message: '请选择仓库' }]} style={{ flex: 1 }}>
                            <Select placeholder="选择仓库">{warehouses.map((w: any) => <Option key={w.id} value={w.id}>{w.name}</Option>)}</Select>
                        </Form.Item>
                        <Form.Item name="checkDate" label="盘点日期" style={{ flex: 1 }}><DatePicker style={{ width: '100%' }} /></Form.Item>
                    </div>
                    <Form.Item name="description" label="备注"><TextArea rows={2} placeholder="盘点备注" /></Form.Item>
                </Form>
                <Divider>盘点明细</Divider>
                <Button type="dashed" onClick={handleAddDetail} style={{ marginBottom: 16 }} icon={<PlusOutlined />}>添加商品</Button>
                <Table columns={detailColumns} dataSource={details} rowKey={(record) => record.key || record.id} pagination={false}
                    summary={() => (<Table.Summary><Table.Summary.Row>
                        <Table.Summary.Cell index={0}>合计</Table.Summary.Cell><Table.Summary.Cell index={1}>-</Table.Summary.Cell>
                        <Table.Summary.Cell index={2}>-</Table.Summary.Cell>
                        <Table.Summary.Cell index={3}><span style={{ color: 'green' }}>盈:{totalProfit}</span> / <span style={{ color: 'red' }}>亏:{totalLoss}</span></Table.Summary.Cell>
                        <Table.Summary.Cell index={4}>-</Table.Summary.Cell><Table.Summary.Cell index={5}>-</Table.Summary.Cell><Table.Summary.Cell index={6}>-</Table.Summary.Cell>
                    </Table.Summary.Row></Table.Summary>)}
                />
                <div style={{ marginTop: 24, textAlign: 'center' }}>
                    <Space><Button onClick={() => navigate(-1)}>取消</Button><Button type="primary" loading={loading} onClick={handleSubmit}>{isEdit ? '保存修改' : '创建盘点单'}</Button></Space>
                </div>
            </Card>
        </div>
    );
};

export default StockCheckForm;

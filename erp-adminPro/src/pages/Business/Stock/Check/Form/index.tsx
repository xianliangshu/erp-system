import React, { useState, useEffect } from 'react';
import { Form, Select, DatePicker, InputNumber, Button, Card, Table, Space, message, Divider, Input, Alert } from 'antd';
import { PlusOutlined, DeleteOutlined, ArrowLeftOutlined } from '@ant-design/icons';
import { useNavigate, useParams } from 'react-router-dom';
import { getStockCheckById, createStockCheck, updateStockCheck } from '@/api/business/stockCheck';
import { getWarehouseList } from '@/api/basedata/warehouse';
import { getMaterialList } from '@/api/material/material';
import { getTakePlanOptions, getTakePlanDetails } from '@/api/stock/takePlan';
import { getPreTakeOptions } from '@/api/stock/preTakeSheet';
import { getTakeStockConfig } from '@/api/stock/takeConfig';
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
    // 关联来源
    const [takePlans, setTakePlans] = useState<any[]>([]);
    const [preSheets, setPreSheets] = useState<any[]>([]);
    // 盘点参数：关联任务后是否带出商品明细
    const [showProduct, setShowProduct] = useState(false);

    useEffect(() => { loadBasicData(); if (isEdit) loadCheckData(); }, [id]);

    const loadBasicData = async () => {
        try {
            const [warehouseData, materialData, config, planOptions, preOptions] = await Promise.all([
                getWarehouseList(), getMaterialList(), getTakeStockConfig(),
                getTakePlanOptions(), getPreTakeOptions(),
            ]);
            setWarehouses(Array.isArray(warehouseData) ? warehouseData : []);
            setMaterials(Array.isArray(materialData) ? materialData : []);
            setShowProduct(Number((config as any)?.showProduct) === 1);
            setTakePlans(Array.isArray(planOptions) ? planOptions : []);
            setPreSheets(Array.isArray(preOptions) ? preOptions : []);
        } catch (error) { console.error('加载基础数据失败', error); }
    };

    const loadCheckData = async () => {
        try {
            const data: any = await getStockCheckById(Number(id));
            if (data?.stockCheck) {
                form.setFieldsValue({
                    scId: data.stockCheck.scId,
                    checkDate: data.stockCheck.checkDate ? dayjs(data.stockCheck.checkDate) : null,
                    description: data.stockCheck.description,
                    planId: data.stockCheck.planId ?? undefined,
                    preSheetId: data.stockCheck.preSheetId ?? undefined,
                });
                setDetails(data.details?.map((d: any, i: number) => ({ ...d, key: Date.now() + i })) || []);
            }
        } catch (error) { message.error('加载盘点单数据失败'); }
    };

    /**
     * 选择盘点任务后的联动。
     *
     * <p>是否带出商品由盘点参数 {@code showProduct} 开关控制；
     * 带出时「账面数量」用任务创建时的<b>库存快照</b>（任务存在的意义所在），
     * 「实盘数量」先用快照值占位，由盘点人改成实际数。</p>
     */
    const handlePlanChange = async (planId: number | undefined) => {
        if (!planId) { return; }
        if (!showProduct) {
            message.info('盘点参数未开启「带出任务商品明细」，可按需手工添加商品');
            return;
        }
        try {
            const list: any = await getTakePlanDetails(planId);
            if (!Array.isArray(list) || list.length === 0) {
                message.warning('该任务没有明细');
                return;
            }
            const rows = list.map((d: any, i: number) => ({
                key: `plan-${d.id ?? i}-${Date.now()}`,
                productId: d.productId,
                stockNum: Number(d.stockNum ?? 0),
                // 占位 = 快照数量，避免差异金额为负噪音；盘点人再改为实际数
                actualNum: Number(d.takeNum ?? d.stockNum ?? 0),
                costPrice: 0,
                description: d.description || '',
            }));
            setDetails(rows);
            message.success(`已带出 ${rows.length} 条任务商品明细`);
        } catch (error) { message.error('带出任务明细失败'); }
    };

    const handleAddDetail = () => { setDetails([...details, { key: Date.now(), productId: undefined, stockNum: 0, actualNum: 0, costPrice: 0, description: '' }]); };
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
                // 单据来源关联：仅在创建时由后端写入，修改时不覆盖
                planId: values.planId ?? undefined,
                preSheetId: values.preSheetId ?? undefined,
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
                    <div style={{ display: 'flex', gap: 24 }}>
                        <Form.Item
                            name="planId"
                            label="关联盘点任务"
                            style={{ flex: 1 }}
                            extra={showProduct ? '选择后将按任务的库存快照带出商品明细' : '盘点参数未开启「带出任务商品明细」，仅记录关联关系'}
                        >
                            <Select
                                placeholder="不关联可留空"
                                allowClear
                                showSearch
                                optionFilterProp="label"
                                onChange={handlePlanChange}
                                options={takePlans.map((p: any) => ({ value: p.id, label: `${p.code}（${p.scName || p.scId || ''}）` }))}
                            />
                        </Form.Item>
                        <Form.Item
                            name="preSheetId"
                            label="关联预先盘点单"
                            style={{ flex: 1 }}
                            extra="仅记录来源，用于追溯分次清点结果"
                        >
                            <Select
                                placeholder="不关联可留空"
                                allowClear
                                showSearch
                                optionFilterProp="label"
                                options={preSheets.map((s: any) => ({
                                    value: s.id,
                                    label: `${s.code}（${s.scName || s.scId || ''}·${{ 0: '初盘', 1: '复盘', 2: '抽盘' }[s.takeStatus as number] || '未知'}）`,
                                }))}
                            />
                        </Form.Item>
                    </div>
                    {isEdit && (
                        <Alert
                            type="info"
                            showIcon
                            style={{ marginBottom: 16 }}
                            message="盘点单的「单据来源」只在创建时写入，编辑不会改动既有来源，也不会重新带出商品。"
                        />
                    )}
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

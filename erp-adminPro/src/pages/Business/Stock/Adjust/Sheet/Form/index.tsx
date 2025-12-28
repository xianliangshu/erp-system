import React, { useState, useEffect } from 'react';
import { Card, Form, Select, Input, Button, Table, InputNumber, Space, message, Modal } from 'antd';
import { PlusOutlined, DeleteOutlined, ArrowLeftOutlined, SaveOutlined, ExclamationCircleOutlined } from '@ant-design/icons';
import { useNavigate, useParams } from 'react-router-dom';
import { addAdjustSheet, updateAdjustSheet, getAdjustSheetById } from '@/api/stock/adjustSheet';
import { getAdjustReasonList } from '@/api/stock/adjustReason';
import { getWarehouseList } from '@/api/basedata/warehouse';
import { getMaterialList } from '@/api/material/material';

interface DetailItem {
    key: string;
    productId?: number;
    productName?: string;
    stockNum?: number;
    description?: string;
}

const AdjustSheetForm: React.FC = () => {
    const navigate = useNavigate();
    const { id } = useParams<{ id: string }>();
    const isEdit = !!id;

    const [form] = Form.useForm();
    const [loading, setLoading] = useState(false);
    const [submitting, setSubmitting] = useState(false);
    const [details, setDetails] = useState<DetailItem[]>([]);

    // 下拉选项
    const [warehouses, setWarehouses] = useState<any[]>([]);
    const [reasons, setReasons] = useState<any[]>([]);
    const [materials, setMaterials] = useState<any[]>([]);

    const loadSelectOptions = async () => {
        try {
            const [whRes, reasonRes, materialRes]: any[] = await Promise.all([
                getWarehouseList(),
                getAdjustReasonList(),
                getMaterialList(),
            ]);
            setWarehouses(whRes || []);
            setReasons(reasonRes || []);
            setMaterials(materialRes || []);
        } catch (error) {
            console.error('加载选项失败', error);
        }
    };

    const loadDetail = async () => {
        if (!id) return;
        setLoading(true);
        try {
            const res: any = await getAdjustSheetById(Number(id));
            const sheet = res?.sheet;
            const detailList = res?.details || [];

            form.setFieldsValue({
                scId: sheet?.scId,
                reasonId: sheet?.reasonId,
                bizType: sheet?.bizType,
                description: sheet?.description,
            });

            setDetails(detailList.map((item: any, index: number) => ({
                key: `${index}`,
                productId: item.productId,
                stockNum: Number(item.stockNum),
                description: item.description,
            })));
        } catch (error) {
            message.error('加载详情失败');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadSelectOptions();
        loadDetail();
    }, [id]);

    const handleAddRow = () => {
        setDetails([
            ...details,
            { key: `${Date.now()}`, productId: undefined, stockNum: undefined, description: '' }
        ]);
    };

    const handleDeleteRow = (key: string) => {
        setDetails(details.filter(item => item.key !== key));
    };

    const handleDetailChange = (key: string, field: string, value: any) => {
        setDetails(details.map(item =>
            item.key === key ? { ...item, [field]: value } : item
        ));
    };

    const handleSubmit = async () => {
        try {
            const values = await form.validateFields();

            if (details.length === 0) {
                message.warning('请添加调整明细');
                return;
            }

            const invalidDetails = details.filter(d => !d.productId || !d.stockNum);
            if (invalidDetails.length > 0) {
                message.warning('请完善调整明细信息');
                return;
            }

            setSubmitting(true);

            const data = {
                ...values,
                id: id ? Number(id) : undefined,
                details: details.map(d => ({
                    productId: d.productId,
                    stockNum: d.stockNum,
                    description: d.description,
                })),
            };

            if (isEdit) {
                await updateAdjustSheet(data);
                message.success('修改成功');
            } else {
                await addAdjustSheet(data);
                message.success('新增成功');
            }

            navigate('/stock/adjust/sheet');
        } catch (error) {
            console.error('提交失败', error);
        } finally {
            setSubmitting(false);
        }
    };

    const handleBack = () => {
        Modal.confirm({
            title: '确认返回',
            icon: <ExclamationCircleOutlined />,
            content: '确定要返回吗？未保存的数据将丢失。',
            okText: '确定',
            cancelText: '取消',
            onOk: () => navigate('/stock/adjust/sheet'),
        });
    };

    const getMaterialName = (productId: number) => {
        return materials.find(m => m.id === productId)?.name || '-';
    };

    const columns = [
        {
            title: '商品',
            dataIndex: 'productId',
            key: 'productId',
            width: 250,
            render: (productId: number, record: DetailItem) => (
                <Select
                    style={{ width: '100%' }}
                    value={productId}
                    onChange={(val) => handleDetailChange(record.key, 'productId', val)}
                    placeholder="请选择商品"
                    showSearch
                    optionFilterProp="children"
                >
                    {materials.map(m => (
                        <Select.Option key={m.id} value={m.id}>{m.code} - {m.name}</Select.Option>
                    ))}
                </Select>
            ),
        },
        {
            title: '调整数量',
            dataIndex: 'stockNum',
            key: 'stockNum',
            width: 150,
            render: (stockNum: number, record: DetailItem) => (
                <InputNumber
                    style={{ width: '100%' }}
                    value={stockNum}
                    onChange={(val) => handleDetailChange(record.key, 'stockNum', val)}
                    placeholder="调整数量"
                    precision={2}
                    min={0.01}
                />
            ),
        },
        {
            title: '备注',
            dataIndex: 'description',
            key: 'description',
            render: (description: string, record: DetailItem) => (
                <Input
                    value={description}
                    onChange={(e) => handleDetailChange(record.key, 'description', e.target.value)}
                    placeholder="备注"
                />
            ),
        },
        {
            title: '操作',
            key: 'action',
            width: 80,
            render: (_: any, record: DetailItem) => (
                <Button type="link" danger icon={<DeleteOutlined />} onClick={() => handleDeleteRow(record.key)}>
                    删除
                </Button>
            ),
        },
    ];

    return (
        <div style={{ padding: '24px' }}>
            <Card
                title={isEdit ? '编辑库存调整单' : '新增库存调整单'}
                loading={loading}
                extra={
                    <Space>
                        <Button icon={<ArrowLeftOutlined />} onClick={handleBack}>返回</Button>
                        <Button type="primary" icon={<SaveOutlined />} loading={submitting} onClick={handleSubmit}>
                            保存
                        </Button>
                    </Space>
                }
            >
                <Form form={form} layout="inline" style={{ marginBottom: 24 }}>
                    <Form.Item name="scId" label="仓库" rules={[{ required: true, message: '请选择仓库' }]}>
                        <Select style={{ width: 200 }} placeholder="请选择仓库">
                            {warehouses.map(w => (
                                <Select.Option key={w.id} value={w.id}>{w.name}</Select.Option>
                            ))}
                        </Select>
                    </Form.Item>
                    <Form.Item name="reasonId" label="调整原因" rules={[{ required: true, message: '请选择调整原因' }]}>
                        <Select style={{ width: 200 }} placeholder="请选择调整原因">
                            {reasons.map(r => (
                                <Select.Option key={r.id} value={r.id}>{r.name}</Select.Option>
                            ))}
                        </Select>
                    </Form.Item>
                    <Form.Item name="bizType" label="业务类型" rules={[{ required: true, message: '请选择业务类型' }]}>
                        <Select style={{ width: 150 }} placeholder="请选择类型">
                            <Select.Option value={0}>入库调整</Select.Option>
                            <Select.Option value={1}>出库调整</Select.Option>
                        </Select>
                    </Form.Item>
                    <Form.Item name="description" label="备注">
                        <Input style={{ width: 300 }} placeholder="请输入备注" />
                    </Form.Item>
                </Form>

                <div style={{ marginBottom: 16 }}>
                    <Button type="dashed" icon={<PlusOutlined />} onClick={handleAddRow} block>
                        添加商品
                    </Button>
                </div>

                <Table
                    columns={columns}
                    dataSource={details}
                    rowKey="key"
                    pagination={false}
                    size="small"
                />
            </Card>
        </div>
    );
};

export default AdjustSheetForm;

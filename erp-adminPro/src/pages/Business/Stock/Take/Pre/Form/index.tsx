import React, { useEffect, useState } from 'react';
import {
    Form, Select, Button, Card, Table, Space, message, Divider, Input, InputNumber,
} from 'antd';
import { PlusOutlined, DeleteOutlined, ArrowLeftOutlined, SaveOutlined } from '@ant-design/icons';
import { useNavigate, useParams } from 'react-router-dom';
import { getPreTakeById, addPreTake, updatePreTake } from '@/api/stock/preTakeSheet';
import { getAllWarehouses } from '@/api/basedata/warehouse';
import { getMaterialList } from '@/api/material/material';

const { TextArea } = Input;

/** 盘点轮次（0=初盘 / 1=复盘 / 2=抽盘） */
const ROUND_OPTIONS = [
    { value: 0, label: '初盘' },
    { value: 1, label: '复盘' },
    { value: 2, label: '抽盘' },
];

/**
 * 预先盘点单编辑页
 *
 * <p>三个数量列（初盘 / 复盘 / 抽盘）<b>互不影响</b>，允许只填其中一轮，
 * 这是「分次清点交叉校验」的核心 —— 不做任何合并计算。</p>
 */
const PreTakeStockForm: React.FC = () => {
    const navigate = useNavigate();
    const { id } = useParams<{ id: string }>();
    const isEdit = !!id;

    const [form] = Form.useForm();
    const [loading, setLoading] = useState(false);
    const [warehouses, setWarehouses] = useState<any[]>([]);
    const [materials, setMaterials] = useState<any[]>([]);
    const [details, setDetails] = useState<any[]>([]);

    const loadBasicData = async () => {
        try {
            const [wh, mt] = await Promise.all([getAllWarehouses(), getMaterialList()]);
            setWarehouses(Array.isArray(wh) ? wh : []);
            setMaterials(Array.isArray(mt) ? mt : []);
        } catch (error) {
            console.error('加载基础数据失败', error);
        }
    };

    const loadSheetData = async () => {
        try {
            const data: any = await getPreTakeById(Number(id));
            if (data) {
                form.setFieldsValue({
                    scId: data.scId,
                    takeStatus: data.takeStatus,
                    description: data.description,
                });
                setDetails((data.details || []).map((d: any, i: number) => ({ ...d, key: d.id ?? `new-${i}` })));
            }
        } catch (error) {
            message.error('加载预先盘点单失败');
        }
    };

    useEffect(() => {
        loadBasicData();
        if (isEdit) {
            loadSheetData();
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [id]);

    const handleAddDetail = () => {
        setDetails((list) => [
            ...list,
            { key: `new-${Date.now()}`, productId: undefined, firstNum: null, secondNum: null, randNum: null },
        ]);
    };

    const handleDeleteDetail = (key: any) => {
        setDetails((list) => list.filter((d) => d.key !== key));
    };

    const handleDetailChange = (key: any, field: string, value: any) => {
        setDetails((list) => list.map((d) => (d.key === key ? { ...d, [field]: value } : d)));
    };

    const handleSubmit = async () => {
        try {
            const values = await form.validateFields();
            if (details.length === 0) {
                message.warning('请至少添加一条明细');
                return;
            }
            const productIds = details.map((d) => d.productId).filter((v) => v !== undefined && v !== null);
            if (productIds.length !== details.length) {
                message.warning('存在未选择商品的明细行');
                return;
            }
            if (new Set(productIds).size !== productIds.length) {
                message.warning('同一商品不能重复添加');
                return;
            }
            setLoading(true);
            const payload = {
                id: isEdit ? Number(id) : undefined,
                scId: values.scId,
                takeStatus: values.takeStatus,
                description: values.description,
                details: details.map((d) => ({
                    productId: d.productId,
                    firstNum: d.firstNum ?? null,
                    secondNum: d.secondNum ?? null,
                    randNum: d.randNum ?? null,
                })),
            };
            if (isEdit) {
                await updatePreTake(payload);
                message.success('保存成功');
            } else {
                await addPreTake(payload);
                message.success('创建成功');
            }
            navigate('/stock/take/pre');
        } catch (error: any) {
            if (error?.errorFields) {
                return;
            }
            console.error('保存预先盘点单失败', error);
        } finally {
            setLoading(false);
        }
    };

    const detailColumns = [
        {
            title: '商品',
            dataIndex: 'productId',
            key: 'productId',
            width: 280,
            render: (value: number, record: any) => (
                <Select
                    value={value}
                    style={{ width: '100%' }}
                    placeholder="选择商品（支持名称搜索）"
                    showSearch
                    optionFilterProp="label"
                    options={materials.map((m: any) => ({
                        value: m.id,
                        label: `${m.name}（${m.code}）${m.salePropertyText ? ' ' + m.salePropertyText : ''}`,
                    }))}
                    onChange={(v) => handleDetailChange(record.key, 'productId', v)}
                />
            ),
        },
        {
            title: '初盘数量',
            dataIndex: 'firstNum',
            key: 'firstNum',
            width: 140,
            render: (value: any, record: any) => (
                <InputNumber
                    min={0}
                    style={{ width: '100%' }}
                    value={value}
                    placeholder="未清点"
                    onChange={(v) => handleDetailChange(record.key, 'firstNum', v)}
                />
            ),
        },
        {
            title: '复盘数量',
            dataIndex: 'secondNum',
            key: 'secondNum',
            width: 140,
            render: (value: any, record: any) => (
                <InputNumber
                    min={0}
                    style={{ width: '100%' }}
                    value={value}
                    placeholder="未清点"
                    onChange={(v) => handleDetailChange(record.key, 'secondNum', v)}
                />
            ),
        },
        {
            title: '抽盘数量',
            dataIndex: 'randNum',
            key: 'randNum',
            width: 140,
            render: (value: any, record: any) => (
                <InputNumber
                    min={0}
                    style={{ width: '100%' }}
                    value={value}
                    placeholder="未清点"
                    onChange={(v) => handleDetailChange(record.key, 'randNum', v)}
                />
            ),
        },
        {
            title: '三轮是否一致',
            key: 'consistent',
            width: 130,
            render: (_: any, record: any) => {
                const nums = [record.firstNum, record.secondNum, record.randNum].filter(
                    (v: any) => v !== null && v !== undefined,
                );
                if (nums.length < 2) {
                    return <span style={{ color: '#8c8c8c' }}>数据不足</span>;
                }
                const same = nums.every((v: any) => Number(v) === Number(nums[0]));
                return same ? (
                    <span style={{ color: '#3f8600' }}>一致</span>
                ) : (
                    <span style={{ color: '#cf1322' }}>存在差异</span>
                );
            },
        },
        {
            title: '操作',
            key: 'action',
            width: 80,
            render: (_: any, record: any) => (
                <Button type="link" danger icon={<DeleteOutlined />} onClick={() => handleDeleteDetail(record.key)}>
                    删除
                </Button>
            ),
        },
    ];

    return (
        <div style={{ padding: '24px' }}>
            <Card
                title={
                    <Space>
                        <Button icon={<ArrowLeftOutlined />} onClick={() => navigate(-1)}>
                            返回
                        </Button>
                        <span>{isEdit ? '编辑预先盘点单' : '新增预先盘点单'}</span>
                    </Space>
                }
                extra={
                    <Space>
                        <Button onClick={() => navigate(-1)}>取消</Button>
                        <Button type="primary" icon={<SaveOutlined />} loading={loading} onClick={handleSubmit}>
                            {isEdit ? '保存修改' : '创建'}
                        </Button>
                    </Space>
                }
            >
                <Form form={form} layout="vertical">
                    <div style={{ display: 'flex', gap: 24 }}>
                        <Form.Item name="scId" label="仓库" rules={[{ required: true, message: '请选择仓库' }]} style={{ flex: 1 }}>
                            <Select
                                placeholder="请选择仓库"
                                showSearch
                                optionFilterProp="label"
                                options={warehouses.map((w: any) => ({ value: w.id, label: w.name }))}
                            />
                        </Form.Item>
                        <Form.Item
                            name="takeStatus"
                            label="盘点轮次"
                            rules={[{ required: true, message: '请选择盘点轮次' }]}
                            style={{ flex: 1 }}
                            initialValue={0}
                        >
                            <Select options={ROUND_OPTIONS} />
                        </Form.Item>
                    </div>
                    <Form.Item name="description" label="备注">
                        <TextArea rows={2} placeholder="例如：双人初盘、组长复盘" />
                    </Form.Item>
                </Form>

                <Divider>清点明细</Divider>
                <Space style={{ marginBottom: 16 }}>
                    <Button type="dashed" icon={<PlusOutlined />} onClick={handleAddDetail}>
                        添加商品
                    </Button>
                    <span style={{ color: '#8c8c8c' }}>
                        共 {details.length} 条。初盘 / 复盘 / 抽盘三个数量互不影响，可只填其中一轮。
                    </span>
                </Space>

                <Table
                    rowKey="key"
                    columns={detailColumns}
                    dataSource={details}
                    pagination={false}
                    scroll={{ x: 1000 }}
                />
            </Card>
        </div>
    );
};

export default PreTakeStockForm;

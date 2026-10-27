import React, { useEffect, useState } from 'react';
import {
    Card, Table, Space, Input, Select, Button, message, Tag, Modal, Form, InputNumber,
    Drawer, TreeSelect, Divider, Alert, Descriptions, Spin,
} from 'antd';
import {
    SearchOutlined, ReloadOutlined, PlusOutlined, DeleteOutlined, EyeOutlined,
    ExclamationCircleOutlined, ThunderboltOutlined, StopOutlined, SaveOutlined,
} from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import {
    getTakePlanPage, getTakePlanById, addTakePlan, saveTakeNums,
    generateTakeDiff, cancelTakePlan, deleteTakePlan,
} from '@/api/stock/takePlan';
import { getAllWarehouses } from '@/api/basedata/warehouse';
import { getMaterialList } from '@/api/material/material';
import { getCategoryTree } from '@/api/material/category';
import { getBrandList } from '@/api/basedata/brand';

/** 盘点类别 */
const TYPE_OPTIONS = [
    { value: 0, label: '全场' },
    { value: 1, label: '单品' },
    { value: 2, label: '分类' },
    { value: 3, label: '品牌' },
];

/** 任务状态（与后端 TakeStockPlanStatus 的 0/6/9/12 对齐） */
const STATUS_MAP: Record<number, { text: string; color: string }> = {
    0: { text: '任务生成', color: 'blue' },
    6: { text: '差异已生成', color: 'orange' },
    9: { text: '盘点完成', color: 'green' },
    12: { text: '已作废', color: 'default' },
};

/** 分类树 → TreeSelect 结构（后端返回的是 id / name / children） */
const toTreeData = (nodes: any[]): any[] =>
    (nodes || []).map((n: any) => ({
        value: n.id,
        title: n.name,
        children: n.children && n.children.length ? toTreeData(n.children) : undefined,
    }));

/** 状态兜底，避免未知状态导致渲染报错 */
const statusInfo = (code: number) => STATUS_MAP[code] || { text: '未知', color: 'default' };

const TakeStockPlanPage: React.FC = () => {
    const navigate = useNavigate();

    // ========== 下拉选项 ==========
    const [warehouses, setWarehouses] = useState<any[]>([]);
    const [materials, setMaterials] = useState<any[]>([]);
    const [categoryTree, setCategoryTree] = useState<any[]>([]);
    const [brands, setBrands] = useState<any[]>([]);

    // ========== 列表 ==========
    const [loading, setLoading] = useState(false);
    const [dataSource, setDataSource] = useState<any[]>([]);
    const [total, setTotal] = useState(0);
    // 分页与查询条件：唯一的取数依据，搜索/重置/翻页都只改它
    const [query, setQuery] = useState<any>({ current: 1, size: 10 });
    const [searchCode, setSearchCode] = useState('');
    const [searchScId, setSearchScId] = useState<number | undefined>(undefined);
    const [searchStatus, setSearchStatus] = useState<number | undefined>(undefined);
    const [searchType, setSearchType] = useState<number | undefined>(undefined);

    // ========== 新增 ==========
    const [formVisible, setFormVisible] = useState(false);
    const [form] = Form.useForm();
    const [takeType, setTakeType] = useState<number>(0);
    const [submitting, setSubmitting] = useState(false);

    // ========== 明细抽屉 ==========
    const [drawerOpen, setDrawerOpen] = useState(false);
    const [detailLoading, setDetailLoading] = useState(false);
    const [plan, setPlan] = useState<any>(null);
    const [rows, setRows] = useState<any[]>([]);
    const [saving, setSaving] = useState(false);

    const [modal, contextHolder] = Modal.useModal();

    const loadOptions = async () => {
        try {
            const [wh, mt, ct, bd] = await Promise.all([
                getAllWarehouses(), getMaterialList(), getCategoryTree(), getBrandList(),
            ]);
            setWarehouses((wh as any) || []);
            setMaterials((mt as any) || []);
            setCategoryTree(toTreeData((ct as any) || []));
            setBrands((bd as any) || []);
        } catch (error) {
            console.error('加载下拉选项失败', error);
        }
    };

    const loadData = async () => {
        setLoading(true);
        try {
            const res: any = await getTakePlanPage({
                ...query,
                code: searchCode || undefined,
                scId: searchScId,
                takeStatus: searchStatus,
                takeType: searchType,
            });
            setDataSource(res?.records || []);
            setTotal(res?.total || 0);
        } catch (error) {
            message.error('加载盘点任务失败');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadOptions();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    useEffect(() => {
        loadData();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [query]);

    const handleSearch = () => setQuery((q: any) => ({ ...q, current: 1 }));

    const handleReset = () => {
        setSearchCode('');
        setSearchScId(undefined);
        setSearchStatus(undefined);
        setSearchType(undefined);
        setQuery((q: any) => ({ ...q, current: 1 }));
    };

    // ==================== 新增任务 ====================

    const handleAdd = () => {
        form.resetFields();
        form.setFieldsValue({ takeType: 0 });
        setTakeType(0);
        setFormVisible(true);
    };

    const handleSubmit = async () => {
        try {
            const values = await form.validateFields();
            const bizIds: number[] | undefined = values.bizIds;
            const payload: any = {
                scId: values.scId,
                takeType: values.takeType,
                description: values.description,
            };
            if (values.takeType !== 0) {
                if (!bizIds || bizIds.length === 0) {
                    message.warning('请选择盘点范围');
                    return;
                }
                // 后端约定：逗号分隔的 ID 串
                payload.bizId = bizIds.join(',');
            }
            setSubmitting(true);
            const newId: any = await addTakePlan(payload);
            message.success('盘点任务已创建');
            setFormVisible(false);
            handleSearch();
            if (newId) {
                openDrawer(newId);
            }
        } catch (error: any) {
            if (error?.errorFields) {
                return;
            }
            console.error('创建盘点任务失败', error);
        } finally {
            setSubmitting(false);
        }
    };

    // ==================== 明细抽屉 ====================

    const openDrawer = async (id: number) => {
        setDrawerOpen(true);
        setDetailLoading(true);
        setPlan(null);
        setRows([]);
        try {
            const data: any = await getTakePlanById(id);
            setPlan(data);
            setRows((data?.details || []).map((d: any) => ({ ...d })));
        } catch (error) {
            message.error('加载任务明细失败');
        } finally {
            setDetailLoading(false);
        }
    };

    const refreshDrawer = async () => {
        if (plan?.id) {
            await openDrawer(plan.id);
        }
        loadData();
    };

    const handleRowChange = (id: number, field: string, value: any) => {
        setRows((list) =>
            list.map((r) => {
                if (r.id !== id) {
                    return r;
                }
                const updated = { ...r, [field]: value };
                const takeNum = Number(updated.takeNum ?? 0);
                const stockNum = Number(updated.stockNum ?? 0);
                updated.diffNum = Number.isFinite(takeNum - stockNum) ? takeNum - stockNum : null;
                return updated;
            }),
        );
    };

    const handleSaveTakeNums = async () => {
        const payload = rows
            .filter((r) => r.takeNum !== null && r.takeNum !== undefined)
            .map((r) => ({ id: r.id, takeNum: r.takeNum, description: r.description }));
        if (payload.length === 0) {
            message.warning('请至少填写一个商品的盘点数量');
            return;
        }
        setSaving(true);
        try {
            await saveTakeNums(plan.id, payload);
            message.success('盘点数量已保存');
            await refreshDrawer();
        } catch (error) {
            console.error(error);
        } finally {
            setSaving(false);
        }
    };

    const handleGenerateDiff = () => {
        modal.confirm({
            title: '生成差异',
            icon: <ExclamationCircleOutlined />,
            content:
                '将按「盘点数量 - 库存快照」生成一张库存盘点单，由盘点单的审核流程调整库存。' +
                '若盘点参数已开启「自动调整库存」，盘点单会同时被审核。确定继续吗？',
            okText: '确定生成',
            cancelText: '取消',
            onOk: async () => {
                try {
                    const checkId: any = await generateTakeDiff(plan.id);
                    if (checkId) {
                        message.success('差异已生成，盘点单ID：' + checkId);
                        setDrawerOpen(false);
                        loadData();
                        modal.confirm({
                            title: '已生成库存盘点单',
                            content: '是否立即前往盘点单页面查看并审核？',
                            okText: '前往查看',
                            cancelText: '稍后',
                            onOk: () => navigate('/business/stock/check'),
                        });
                    } else {
                        message.success('盘点数量与快照完全一致，任务已置为「盘点完成」');
                        await refreshDrawer();
                    }
                } catch (error) {
                    console.error(error);
                }
            },
        });
    };

    const handleCancelPlan = (record: any) => {
        modal.confirm({
            title: '作废盘点任务',
            icon: <ExclamationCircleOutlined />,
            content: `确定作废任务「${record.code}」吗？作废后明细不可再录入。`,
            okText: '确定作废',
            cancelText: '取消',
            onOk: async () => {
                try {
                    await cancelTakePlan(record.id);
                    message.success('已作废');
                    if (plan?.id === record.id) {
                        setDrawerOpen(false);
                    }
                    loadData();
                } catch (error) {
                    console.error(error);
                }
            },
        });
    };

    const handleDelete = (record: any) => {
        modal.confirm({
            title: '确认删除',
            icon: <ExclamationCircleOutlined />,
            content: `确定要删除盘点任务「${record.code}」及其全部明细吗？`,
            okText: '确定删除',
            cancelText: '取消',
            okButtonProps: { danger: true },
            onOk: async () => {
                try {
                    await deleteTakePlan(record.id);
                    message.success('删除成功');
                    if (plan?.id === record.id) {
                        setDrawerOpen(false);
                    }
                    loadData();
                } catch (error) {
                    console.error(error);
                }
            },
        });
    };

    // ==================== 表格 ====================

    // 只有「任务生成」状态可录入 / 可作废
    const editable = plan?.takeStatus === 0;

    const columns = [
        { title: '任务编号', dataIndex: 'code', key: 'code', width: 160 },
        { title: '仓库', dataIndex: 'scName', key: 'scName', width: 120, render: (v: any, r: any) => v || r.scId },
        {
            title: '盘点类别',
            dataIndex: 'takeType',
            key: 'takeType',
            width: 100,
            render: (v: number) => TYPE_OPTIONS.find((t) => t.value === v)?.label || '-',
        },
        {
            title: '盘点范围',
            dataIndex: 'bizId',
            key: 'bizId',
            render: (v: string, r: any) =>
                r.takeType === 0 ? '该仓库全部商品' : v ? `已选 ${String(v).split(',').length} 项` : '-',
        },
        {
            title: '录入进度',
            key: 'progress',
            width: 120,
            render: (_: any, r: any) => `${r.enteredCount ?? 0} / ${r.detailCount ?? 0}`,
        },
        {
            title: '状态',
            dataIndex: 'takeStatus',
            key: 'takeStatus',
            width: 120,
            render: (v: number) => {
                const info = statusInfo(v);
                return <Tag color={info.color}>{info.text}</Tag>;
            },
        },
        { title: '备注', dataIndex: 'description', key: 'description', ellipsis: true },
        { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 170 },
        {
            title: '操作',
            key: 'action',
            width: 240,
            fixed: 'right' as const,
            render: (_: any, record: any) => {
                const rowEditable = record.takeStatus === 0;
                return (
                    <Space size="small">
                        <Button type="link" size="small" icon={<EyeOutlined />} onClick={() => openDrawer(record.id)}>
                            {rowEditable ? '录入盘点' : '查看'}
                        </Button>
                        <Button
                            type="link"
                            size="small"
                            disabled={!rowEditable}
                            icon={<StopOutlined />}
                            onClick={() => handleCancelPlan(record)}
                        >
                            作废
                        </Button>
                        <Button type="link" size="small" danger icon={<DeleteOutlined />} onClick={() => handleDelete(record)}>
                            删除
                        </Button>
                    </Space>
                );
            },
        },
    ];

    const detailColumns = [
        { title: '商品编号', dataIndex: 'productCode', key: 'productCode', width: 130 },
        { title: '商品名称', dataIndex: 'productName', key: 'productName' },
        {
            title: '规格 / 属性',
            key: 'spec',
            width: 160,
            render: (_: any, r: any) => [r.specification, r.salePropertyText].filter(Boolean).join(' / ') || '-',
        },
        {
            title: '库存快照',
            dataIndex: 'stockNum',
            key: 'stockNum',
            width: 110,
            render: (v: any) => <span style={{ color: '#8c8c8c' }}>{v ?? '-'}</span>,
        },
        { title: '当前库存', dataIndex: 'currentStockNum', key: 'currentStockNum', width: 110 },
        {
            title: '盘点数量',
            dataIndex: 'takeNum',
            key: 'takeNum',
            width: 140,
            render: (value: any, r: any) => (
                <InputNumber
                    min={0}
                    style={{ width: '100%' }}
                    value={value}
                    disabled={!editable}
                    placeholder="待录入"
                    onChange={(v) => handleRowChange(r.id, 'takeNum', v)}
                />
            ),
        },
        {
            title: '差异',
            key: 'diffNum',
            width: 100,
            render: (_: any, r: any) => {
                if (r.takeNum === null || r.takeNum === undefined) {
                    return '-';
                }
                const diff = Number(r.takeNum ?? 0) - Number(r.stockNum ?? 0);
                if (diff === 0) {
                    return <Tag color="green">一致</Tag>;
                }
                return (
                    <span style={{ color: diff > 0 ? '#cf1322' : '#3f8600', fontWeight: 500 }}>
                        {diff > 0 ? `+${diff}` : diff}
                    </span>
                );
            },
        },
        {
            title: '备注',
            dataIndex: 'description',
            key: 'description',
            width: 180,
            render: (value: any, r: any) => (
                <Input
                    value={value}
                    disabled={!editable}
                    placeholder="备注"
                    onChange={(e) => handleRowChange(r.id, 'description', e.target.value)}
                />
            ),
        },
    ];

    const warehouseOptions = warehouses.map((w: any) => ({ value: w.id, label: w.name }));
    const diffCount = rows.filter(
        (r) => r.takeNum !== null && r.takeNum !== undefined && Number(r.takeNum) !== Number(r.stockNum ?? 0),
    ).length;

    const renderScopeSelect = () => {
        if (takeType === 1) {
            return (
                <Select
                    mode="multiple"
                    placeholder="请选择商品（支持名称搜索）"
                    showSearch
                    optionFilterProp="label"
                    options={materials.map((m: any) => ({
                        value: m.id,
                        label: `${m.name}（${m.code}）${m.salePropertyText ? ' ' + m.salePropertyText : ''}`,
                    }))}
                />
            );
        }
        if (takeType === 2) {
            return <TreeSelect treeData={categoryTree} treeCheckable showCheckedStrategy={TreeSelect.SHOW_ALL} placeholder="请选择分类" />;
        }
        if (takeType === 3) {
            return (
                <Select
                    mode="multiple"
                    placeholder="请选择品牌"
                    showSearch
                    optionFilterProp="label"
                    options={brands.map((b: any) => ({ value: b.id, label: b.name }))}
                />
            );
        }
        return null;
    };

    return (
        <div style={{ padding: '24px' }}>
            {contextHolder}
            <Card title="盘点任务">
                <Space style={{ marginBottom: 16 }} wrap>
                    <Input
                        placeholder="任务编号"
                        style={{ width: 180 }}
                        value={searchCode}
                        onChange={(e) => setSearchCode(e.target.value)}
                        onPressEnter={handleSearch}
                    />
                    <Select
                        placeholder="仓库"
                        style={{ width: 180 }}
                        value={searchScId}
                        onChange={setSearchScId}
                        allowClear
                        options={warehouseOptions}
                    />
                    <Select placeholder="盘点类别" style={{ width: 130 }} value={searchType} onChange={setSearchType} allowClear options={TYPE_OPTIONS} />
                    <Select
                        placeholder="状态"
                        style={{ width: 140 }}
                        value={searchStatus}
                        onChange={setSearchStatus}
                        allowClear
                        options={Object.entries(STATUS_MAP).map(([k, v]) => ({ value: Number(k), label: v.text }))}
                    />
                    <Button type="primary" icon={<SearchOutlined />} onClick={handleSearch}>
                        查询
                    </Button>
                    <Button icon={<ReloadOutlined />} onClick={handleReset}>
                        重置
                    </Button>
                    <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
                        新增任务
                    </Button>
                </Space>

                <Table
                    loading={loading}
                    columns={columns}
                    dataSource={dataSource}
                    rowKey="id"
                    scroll={{ x: 1300 }}
                    pagination={{
                        current: query.current,
                        pageSize: query.size,
                        total,
                        showSizeChanger: true,
                        showTotal: (t) => `共 ${t} 条`,
                        onChange: (page, size) => setQuery((q: any) => ({ ...q, current: page, size })),
                    }}
                />
            </Card>

            {/* 新增任务 */}
            <Modal
                title="新增盘点任务"
                open={formVisible}
                onOk={handleSubmit}
                onCancel={() => setFormVisible(false)}
                okText="创建"
                cancelText="取消"
                confirmLoading={submitting}
                width={620}
                destroyOnClose
            >
                <Alert
                    type="info"
                    showIcon
                    style={{ marginBottom: 16 }}
                    message="创建任务时会把「当前库存」写进明细作为盘点基准（快照），盘点期间的出入库不会影响比对结果。"
                />
                <Form form={form} layout="vertical">
                    <Form.Item name="scId" label="仓库" rules={[{ required: true, message: '请选择仓库' }]}>
                        <Select placeholder="请选择仓库" showSearch optionFilterProp="label" options={warehouseOptions} />
                    </Form.Item>
                    <Form.Item name="takeType" label="盘点类别" rules={[{ required: true, message: '请选择盘点类别' }]}>
                        <Select
                            options={TYPE_OPTIONS}
                            onChange={(v) => {
                                setTakeType(v);
                                form.setFieldsValue({ bizIds: undefined });
                            }}
                        />
                    </Form.Item>
                    {takeType !== 0 && (
                        <Form.Item name="bizIds" label="盘点范围">
                            {renderScopeSelect()}
                        </Form.Item>
                    )}
                    {takeType === 0 && (
                        <Alert
                            type="warning"
                            showIcon
                            style={{ marginBottom: 16 }}
                            message="「全场」会取该仓库库存表中现有的全部商品作为盘点范围，请确认商品数量可控。"
                        />
                    )}
                    <Form.Item name="description" label="备注">
                        <Input.TextArea rows={2} placeholder="请输入备注" />
                    </Form.Item>
                </Form>
            </Modal>

            {/* 明细录入 */}
            <Drawer
                title={plan ? `盘点任务 ${plan.code}` : '盘点任务明细'}
                open={drawerOpen}
                onClose={() => setDrawerOpen(false)}
                width={1080}
                destroyOnClose
            >
                <Spin spinning={detailLoading}>
                    {plan && (
                        <>
                            <Descriptions size="small" column={4} bordered style={{ marginBottom: 16 }}>
                                <Descriptions.Item label="仓库">{plan.scName || plan.scId}</Descriptions.Item>
                                <Descriptions.Item label="盘点类别">
                                    {TYPE_OPTIONS.find((t) => t.value === plan.takeType)?.label || '-'}
                                </Descriptions.Item>
                                <Descriptions.Item label="状态">
                                    <Tag color={statusInfo(plan.takeStatus).color}>{statusInfo(plan.takeStatus).text}</Tag>
                                </Descriptions.Item>
                                <Descriptions.Item label="录入进度">
                                    {plan.enteredCount ?? 0} / {plan.detailCount ?? 0}
                                </Descriptions.Item>
                                {plan.checkId && <Descriptions.Item label="关联盘点单" span={4}>#{plan.checkId}</Descriptions.Item>}
                            </Descriptions>

                            {!editable && (
                                <Alert
                                    type="warning"
                                    showIcon
                                    style={{ marginBottom: 16 }}
                                    message="该任务已不是「任务生成」状态，明细仅可查看，不能修改。"
                                />
                            )}

                            {editable && (
                                <Space style={{ marginBottom: 12 }} wrap>
                                    <Button type="primary" icon={<SaveOutlined />} loading={saving} onClick={handleSaveTakeNums}>
                                        保存盘点数量
                                    </Button>
                                    <Button type="primary" danger icon={<ThunderboltOutlined />} onClick={handleGenerateDiff}>
                                        生成差异
                                    </Button>
                                    <span style={{ color: '#8c8c8c' }}>
                                        共 {rows.length} 条，其中 {diffCount} 条与快照不一致
                                    </span>
                                </Space>
                            )}

                            <Divider style={{ margin: '8px 0 16px' }} />

                            <Table
                                rowKey="id"
                                size="small"
                                columns={detailColumns}
                                dataSource={rows}
                                scroll={{ x: 1100 }}
                                pagination={{ pageSize: 20, showSizeChanger: true, showTotal: (t) => `共 ${t} 条` }}
                            />
                        </>
                    )}
                </Spin>
            </Drawer>
        </div>
    );
};

export default TakeStockPlanPage;

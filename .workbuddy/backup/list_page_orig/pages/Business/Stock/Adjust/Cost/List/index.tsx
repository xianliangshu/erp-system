import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Card, Table, Space, Input, Select, Button, message, Tag, Modal, Form } from 'antd';
import {
    SearchOutlined, ReloadOutlined, PlusOutlined, EditOutlined, DeleteOutlined,
    ExclamationCircleOutlined, EyeOutlined, CheckCircleOutlined, CloseCircleOutlined,
} from '@ant-design/icons';
import {
    getCostAdjustPage, deleteCostAdjust, approveCostAdjust, refuseCostAdjust,
} from '@/api/stock/costAdjust';
import { getAllWarehouses } from '@/api/basedata/warehouse';

const statusTag = (status: number) => {
    if (status === 0) return <Tag color="orange">待审核</Tag>;
    if (status === 1) return <Tag color="green">审核通过</Tag>;
    return <Tag color="red">审核拒绝</Tag>;
};

const money = (v: any) => (v === null || v === undefined ? '-' : Number(v).toFixed(2));

/**
 * 库存成本调整单列表
 */
const CostAdjustList: React.FC = () => {
    const navigate = useNavigate();
    const [loading, setLoading] = useState(false);
    const [dataSource, setDataSource] = useState<any[]>([]);
    const [total, setTotal] = useState(0);
    const [current, setCurrent] = useState(1);
    const [pageSize, setPageSize] = useState(10);

    const [searchCode, setSearchCode] = useState('');
    const [searchScId, setSearchScId] = useState<number | undefined>(undefined);
    const [searchStatus, setSearchStatus] = useState<number | undefined>(undefined);

    const [warehouses, setWarehouses] = useState<any[]>([]);
    const [modal, contextHolder] = Modal.useModal();

    const [refuseVisible, setRefuseVisible] = useState(false);
    const [refuseId, setRefuseId] = useState<number | null>(null);
    const [refuseForm] = Form.useForm();

    const loadData = async () => {
        setLoading(true);
        try {
            const res: any = await getCostAdjustPage({
                current,
                size: pageSize,
                code: searchCode || undefined,
                scId: searchScId,
                status: searchStatus,
            });
            setDataSource(res?.records || []);
            setTotal(res?.total || 0);
        } catch (error) {
            message.error('加载数据失败');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadData();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [current, pageSize]);

    useEffect(() => {
        getAllWarehouses()
            .then((res: any) => setWarehouses(res || []))
            .catch(() => undefined);
    }, []);

    const handleSearch = () => {
        setCurrent(1);
        loadData();
    };

    const handleReset = () => {
        setSearchCode('');
        setSearchScId(undefined);
        setSearchStatus(undefined);
        setCurrent(1);
        setTimeout(loadData, 0);
    };

    const handleApprove = (record: any) => {
        modal.confirm({
            title: '审核通过',
            icon: <CheckCircleOutlined />,
            content: (
                <div>
                    <div>确定通过单据「{record.code}」吗？</div>
                    <div style={{ color: '#d4380d', marginTop: 8 }}>
                        通过后将按明细直接改写库存成本价与库存金额（库存数量保持不变），且不可撤销。
                    </div>
                </div>
            ),
            okText: '确定通过',
            cancelText: '取消',
            onOk: async () => {
                try {
                    await approveCostAdjust(record.id);
                    message.success('审核通过');
                    loadData();
                } catch (error) {
                    message.error('审核失败');
                }
            },
        });
    };

    const handleRefuse = (record: any) => {
        setRefuseId(record.id);
        refuseForm.resetFields();
        setRefuseVisible(true);
    };

    const handleRefuseSubmit = async () => {
        try {
            const values = await refuseForm.validateFields();
            await refuseCostAdjust(refuseId as number, values.refuseReason);
            message.success('已拒绝');
            setRefuseVisible(false);
            loadData();
        } catch (error: any) {
            if (error?.errorFields) {
                return;
            }
            console.error('拒绝失败', error);
        }
    };

    const handleDelete = (record: any) => {
        modal.confirm({
            title: '确认删除',
            icon: <ExclamationCircleOutlined />,
            content: `确定要删除单据「${record.code}」吗？`,
            okText: '确定',
            cancelText: '取消',
            onOk: async () => {
                try {
                    await deleteCostAdjust(record.id);
                    message.success('删除成功');
                    loadData();
                } catch (error) {
                    message.error('删除失败');
                }
            },
        });
    };

    const columns = [
        { title: '单据编号', dataIndex: 'code', key: 'code', width: 160 },
        { title: '仓库', dataIndex: 'scName', key: 'scName', width: 120 },
        { title: '调价品种数', dataIndex: 'productNum', key: 'productNum', width: 110 },
        {
            title: '调价差额',
            dataIndex: 'diffAmount',
            key: 'diffAmount',
            width: 120,
            render: (v: any) => (
                <span style={{ color: Number(v) > 0 ? '#cf1322' : Number(v) < 0 ? '#3f8600' : undefined }}>
                    {money(v)}
                </span>
            ),
        },
        { title: '状态', dataIndex: 'status', key: 'status', width: 100, render: (s: number) => statusTag(s) },
        { title: '制单人', dataIndex: 'createBy', key: 'createBy', width: 110 },
        { title: '制单时间', dataIndex: 'createTime', key: 'createTime', width: 170 },
        { title: '审核人', dataIndex: 'approveBy', key: 'approveBy', width: 110, render: (v: any) => v || '-' },
        { title: '审核时间', dataIndex: 'approveTime', key: 'approveTime', width: 170, render: (v: any) => v || '-' },
        {
            title: '操作',
            key: 'action',
            width: 260,
            fixed: 'right' as const,
            render: (_: any, record: any) => {
                const pending = record.status === 0;
                return (
                    <Space size={4} wrap>
                        <Button
                            type="link"
                            size="small"
                            icon={<EyeOutlined />}
                            onClick={() => navigate(`/stock/adjust/cost/edit/${record.id}?mode=view`)}
                        >
                            查看
                        </Button>
                        {pending && (
                            <>
                                <Button
                                    type="link"
                                    size="small"
                                    icon={<EditOutlined />}
                                    onClick={() => navigate(`/stock/adjust/cost/edit/${record.id}`)}
                                >
                                    编辑
                                </Button>
                                <Button type="link" size="small" icon={<CheckCircleOutlined />} onClick={() => handleApprove(record)}>
                                    通过
                                </Button>
                                <Button type="link" size="small" danger icon={<CloseCircleOutlined />} onClick={() => handleRefuse(record)}>
                                    拒绝
                                </Button>
                                <Button type="link" size="small" danger icon={<DeleteOutlined />} onClick={() => handleDelete(record)}>
                                    删除
                                </Button>
                            </>
                        )}
                    </Space>
                );
            },
        },
    ];

    return (
        <div style={{ padding: '24px' }}>
            {contextHolder}
            <Card title="库存成本调整">
                <Space style={{ marginBottom: 16 }} wrap>
                    <Input
                        placeholder="单据编号"
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
                        options={warehouses.map((w: any) => ({ value: w.id, label: w.name }))}
                    />
                    <Select
                        placeholder="状态"
                        style={{ width: 130 }}
                        value={searchStatus}
                        onChange={setSearchStatus}
                        allowClear
                    >
                        <Select.Option value={0}>待审核</Select.Option>
                        <Select.Option value={1}>审核通过</Select.Option>
                        <Select.Option value={2}>审核拒绝</Select.Option>
                    </Select>
                    <Button type="primary" icon={<SearchOutlined />} onClick={handleSearch}>
                        查询
                    </Button>
                    <Button icon={<ReloadOutlined />} onClick={handleReset}>
                        重置
                    </Button>
                    <Button type="primary" icon={<PlusOutlined />} onClick={() => navigate('/stock/adjust/cost/add')}>
                        新增
                    </Button>
                </Space>

                <Table
                    loading={loading}
                    columns={columns}
                    dataSource={dataSource}
                    rowKey="id"
                    scroll={{ x: 1600 }}
                    pagination={{
                        current,
                        pageSize,
                        total,
                        showSizeChanger: true,
                        showTotal: (t) => `共 ${t} 条`,
                        onChange: (page, size) => {
                            setCurrent(page);
                            setPageSize(size);
                        },
                    }}
                />
            </Card>

            <Modal
                title="审核拒绝"
                open={refuseVisible}
                onOk={handleRefuseSubmit}
                onCancel={() => setRefuseVisible(false)}
                okText="确定拒绝"
                cancelText="取消"
                okButtonProps={{ danger: true }}
            >
                <Form form={refuseForm} layout="vertical">
                    <Form.Item
                        name="refuseReason"
                        label="拒绝原因"
                        rules={[{ required: true, message: '请填写拒绝原因' }]}
                    >
                        <Input.TextArea rows={3} placeholder="请说明拒绝原因" />
                    </Form.Item>
                </Form>
            </Modal>
        </div>
    );
};

export default CostAdjustList;

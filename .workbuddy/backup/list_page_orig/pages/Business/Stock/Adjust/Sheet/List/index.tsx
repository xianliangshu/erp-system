import React, { useState, useEffect } from 'react';
import { Table, Card, Space, Input, Select, Button, message, Tag, Modal } from 'antd';
import { SearchOutlined, ReloadOutlined, PlusOutlined, EyeOutlined, EditOutlined, DeleteOutlined, CheckOutlined, CloseOutlined, ExclamationCircleOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { getAdjustSheetPage, deleteAdjustSheet, approveAdjustSheet, refuseAdjustSheet } from '@/api/stock/adjustSheet';
import { getAdjustReasonList } from '@/api/stock/adjustReason';
import { getWarehouseList } from '@/api/basedata/warehouse';

const statusMap: Record<number, { color: string; text: string }> = {
    0: { color: 'processing', text: '待审核' },
    1: { color: 'success', text: '审核通过' },
    2: { color: 'error', text: '审核拒绝' },
};

const bizTypeMap: Record<number, { color: string; text: string }> = {
    0: { color: 'green', text: '入库调整' },
    1: { color: 'orange', text: '出库调整' },
};

const AdjustSheetList: React.FC = () => {
    const navigate = useNavigate();
    const [loading, setLoading] = useState(false);
    const [dataSource, setDataSource] = useState<any[]>([]);
    const [total, setTotal] = useState(0);
    const [current, setCurrent] = useState(1);
    const [pageSize, setPageSize] = useState(10);

    // 搜索条件
    const [searchCode, setSearchCode] = useState('');
    const [searchScId, setSearchScId] = useState<number | undefined>(undefined);
    const [searchReasonId, setSearchReasonId] = useState<number | undefined>(undefined);
    const [searchBizType, setSearchBizType] = useState<number | undefined>(undefined);
    const [searchStatus, setSearchStatus] = useState<number | undefined>(undefined);

    // 下拉选项
    const [warehouses, setWarehouses] = useState<any[]>([]);
    const [reasons, setReasons] = useState<any[]>([]);

    const [modal, contextHolder] = Modal.useModal();

    const loadSelectOptions = async () => {
        try {
            const [whRes, reasonRes]: any[] = await Promise.all([
                getWarehouseList(),
                getAdjustReasonList(),
            ]);
            // 响应拦截器已解包 data.data
            setWarehouses(whRes || []);
            setReasons(reasonRes || []);
        } catch (error) {
            console.error('加载选项失败', error);
        }
    };

    const loadData = async () => {
        setLoading(true);
        try {
            const params = {
                current,
                size: pageSize,
                code: searchCode || undefined,
                scId: searchScId,
                reasonId: searchReasonId,
                bizType: searchBizType,
                status: searchStatus,
            };
            const res: any = await getAdjustSheetPage(params);
            // 响应拦截器已解包 data.data
            setDataSource(res?.records || []);
            setTotal(res?.total || 0);
        } catch (error) {
            message.error('加载数据失败');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadSelectOptions();
    }, []);

    useEffect(() => {
        loadData();
    }, [current, pageSize]);

    const handleSearch = () => {
        setCurrent(1);
        loadData();
    };

    const handleReset = () => {
        setSearchCode('');
        setSearchScId(undefined);
        setSearchReasonId(undefined);
        setSearchBizType(undefined);
        setSearchStatus(undefined);
        setCurrent(1);
        loadData();
    };

    const handleAdd = () => {
        navigate('/stock/adjust/sheet/add');
    };

    const handleView = (id: number) => {
        navigate(`/stock/adjust/sheet/detail/${id}`);
    };

    const handleEdit = (id: number) => {
        navigate(`/stock/adjust/sheet/edit/${id}`);
    };

    const handleDelete = (record: any) => {
        modal.confirm({
            title: '确认删除',
            icon: <ExclamationCircleOutlined />,
            content: `确定要删除调整单 "${record.code}" 吗？`,
            okText: '确定',
            cancelText: '取消',
            onOk: async () => {
                try {
                    await deleteAdjustSheet(record.id);
                    message.success('删除成功');
                    loadData();
                } catch (error) {
                    message.error('删除失败');
                }
            },
        });
    };

    const handleApprove = async (record: any) => {
        try {
            await approveAdjustSheet(record.id);
            message.success('审核通过');
            loadData();
        } catch (error) {
            message.error('审核失败');
        }
    };

    const handleRefuse = (record: any) => {
        Modal.confirm({
            title: '审核拒绝',
            content: (
                <Input.TextArea id="refuseReasonInput" rows={3} placeholder="请输入拒绝原因" />
            ),
            okText: '确定',
            cancelText: '取消',
            onOk: async () => {
                const refuseReason = (document.getElementById('refuseReasonInput') as HTMLTextAreaElement)?.value || '';
                try {
                    await refuseAdjustSheet(record.id, refuseReason);
                    message.success('审核拒绝成功');
                    loadData();
                } catch (error) {
                    message.error('审核失败');
                }
            },
        });
    };

    const getWarehouseName = (scId: number) => {
        return warehouses.find(w => w.id === scId)?.name || '-';
    };

    const getReasonName = (reasonId: number) => {
        return reasons.find(r => r.id === reasonId)?.name || '-';
    };

    const columns = [
        { title: '调整单编号', dataIndex: 'code', key: 'code', width: 180 },
        {
            title: '仓库',
            dataIndex: 'scId',
            key: 'scId',
            render: (scId: number) => getWarehouseName(scId),
        },
        {
            title: '调整原因',
            dataIndex: 'reasonId',
            key: 'reasonId',
            render: (reasonId: number) => getReasonName(reasonId),
        },
        {
            title: '业务类型',
            dataIndex: 'bizType',
            key: 'bizType',
            width: 100,
            render: (bizType: number) => {
                const info = bizTypeMap[bizType] || { color: 'default', text: '-' };
                return <Tag color={info.color}>{info.text}</Tag>;
            },
        },
        {
            title: '状态',
            dataIndex: 'status',
            key: 'status',
            width: 100,
            render: (status: number) => {
                const info = statusMap[status] || { color: 'default', text: '-' };
                return <Tag color={info.color}>{info.text}</Tag>;
            },
        },
        { title: '创建人', dataIndex: 'createBy', key: 'createBy', width: 100 },
        { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
        {
            title: '操作',
            key: 'action',
            width: 250,
            render: (_: any, record: any) => (
                <Space>
                    <Button type="link" size="small" icon={<EyeOutlined />} onClick={() => handleView(record.id)}>
                        查看
                    </Button>
                    {record.status === 0 && (
                        <>
                            <Button type="link" size="small" icon={<EditOutlined />} onClick={() => handleEdit(record.id)}>
                                编辑
                            </Button>
                            <Button type="link" size="small" style={{ color: '#52c41a' }} icon={<CheckOutlined />} onClick={() => handleApprove(record)}>
                                通过
                            </Button>
                            <Button type="link" size="small" danger icon={<CloseOutlined />} onClick={() => handleRefuse(record)}>
                                拒绝
                            </Button>
                            <Button type="link" size="small" danger icon={<DeleteOutlined />} onClick={() => handleDelete(record)}>
                                删除
                            </Button>
                        </>
                    )}
                </Space>
            ),
        },
    ];

    return (
        <div style={{ padding: '24px' }}>
            {contextHolder}
            <Card title="库存调整">
                <Space style={{ marginBottom: 16 }} wrap>
                    <Input
                        placeholder="调整单编号"
                        style={{ width: 180 }}
                        value={searchCode}
                        onChange={e => setSearchCode(e.target.value)}
                        onPressEnter={handleSearch}
                    />
                    <Select
                        placeholder="仓库"
                        style={{ width: 150 }}
                        value={searchScId}
                        onChange={setSearchScId}
                        allowClear
                    >
                        {warehouses.map(w => (
                            <Select.Option key={w.id} value={w.id}>{w.name}</Select.Option>
                        ))}
                    </Select>
                    <Select
                        placeholder="调整原因"
                        style={{ width: 150 }}
                        value={searchReasonId}
                        onChange={setSearchReasonId}
                        allowClear
                    >
                        {reasons.map(r => (
                            <Select.Option key={r.id} value={r.id}>{r.name}</Select.Option>
                        ))}
                    </Select>
                    <Select
                        placeholder="业务类型"
                        style={{ width: 120 }}
                        value={searchBizType}
                        onChange={setSearchBizType}
                        allowClear
                    >
                        <Select.Option value={0}>入库调整</Select.Option>
                        <Select.Option value={1}>出库调整</Select.Option>
                    </Select>
                    <Select
                        placeholder="状态"
                        style={{ width: 120 }}
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
                    <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
                        新增
                    </Button>
                </Space>

                <Table
                    loading={loading}
                    columns={columns}
                    dataSource={dataSource}
                    rowKey="id"
                    pagination={{
                        current,
                        pageSize,
                        total,
                        showSizeChanger: true,
                        showTotal: (total) => `共 ${total} 条`,
                        onChange: (page, size) => {
                            setCurrent(page);
                            setPageSize(size);
                        },
                    }}
                />
            </Card>
        </div>
    );
};

export default AdjustSheetList;

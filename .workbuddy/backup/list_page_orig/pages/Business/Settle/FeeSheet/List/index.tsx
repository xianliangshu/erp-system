import React, { useState, useEffect } from 'react';
import { Table, Card, Space, Input, Select, Button, message, Tag, Modal } from 'antd';
import { SearchOutlined, ReloadOutlined, PlusOutlined, EyeOutlined, EditOutlined, DeleteOutlined, CheckOutlined, CloseOutlined, ExclamationCircleOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { getFeeSheetPage, deleteFeeSheet, approveFeeSheet, refuseFeeSheet } from '@/api/business/settle';
import { getSupplierList } from '@/api/basedata/supplier';

const statusMap: Record<number, { color: string; text: string }> = {
    0: { color: 'processing', text: '待审核' },
    1: { color: 'success', text: '已审核' },
    2: { color: 'error', text: '已拒绝' },
};

const sheetTypeMap: Record<number, { color: string; text: string }> = {
    1: { color: 'blue', text: '付款' },
    2: { color: 'orange', text: '扣款' },
};

const FeeSheetList: React.FC = () => {
    const navigate = useNavigate();
    const [loading, setLoading] = useState(false);
    const [dataSource, setDataSource] = useState<any[]>([]);
    const [total, setTotal] = useState(0);
    const [current, setCurrent] = useState(1);
    const [pageSize, setPageSize] = useState(10);

    const [searchCode, setSearchCode] = useState('');
    const [searchSupplierId, setSearchSupplierId] = useState<number | undefined>(undefined);
    const [searchSheetType, setSearchSheetType] = useState<number | undefined>(undefined);
    const [searchStatus, setSearchStatus] = useState<number | undefined>(undefined);

    const [suppliers, setSuppliers] = useState<any[]>([]);

    const loadOptions = async () => {
        try {
            const res: any = await getSupplierList();
            setSuppliers(res || []);
        } catch (e) { console.error(e); }
    };

    const loadData = async () => {
        setLoading(true);
        try {
            const res: any = await getFeeSheetPage({
                current,
                size: pageSize,
                code: searchCode,
                supplierId: searchSupplierId,
                sheetType: searchSheetType,
                status: searchStatus,
            });
            setDataSource(res?.records || []);
            setTotal(res?.total || 0);
        } catch (e) { message.error('加载数据失败'); }
        finally { setLoading(false); }
    };

    useEffect(() => { loadOptions(); }, []);
    useEffect(() => { loadData(); }, [current, pageSize]);

    const handleSearch = () => { setCurrent(1); loadData(); };
    const handleReset = () => { setSearchCode(''); setSearchSupplierId(undefined); setSearchSheetType(undefined); setSearchStatus(undefined); setCurrent(1); loadData(); };

    const columns = [
        { title: '单据编号', dataIndex: 'code', key: 'code', width: 180 },
        { title: '供应商', dataIndex: 'supplierId', key: 'supplierId', render: (id: number) => suppliers.find(s => s.id === id)?.name || id },
        { title: '单据类型', dataIndex: 'sheetType', key: 'sheetType', width: 100, render: (val: number) => <Tag color={sheetTypeMap[val]?.color}>{sheetTypeMap[val]?.text}</Tag> },
        { title: '总金额', dataIndex: 'totalAmount', key: 'totalAmount', width: 120, render: (val: number) => `￥${val?.toFixed(2) || '0.00'}` },
        { title: '状态', dataIndex: 'status', key: 'status', width: 100, render: (val: number) => <Tag color={statusMap[val]?.color}>{statusMap[val]?.text}</Tag> },
        { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
        {
            title: '操作', key: 'action', width: 250, render: (_: any, record: any) => (
                <Space>
                    <Button type="link" size="small" icon={<EyeOutlined />} onClick={() => navigate(`/business/settle/fee/detail/${record.id}`)}>查看</Button>
                    {record.status === 0 && (
                        <>
                            <Button type="link" size="small" icon={<EditOutlined />} onClick={() => navigate(`/business/settle/fee/edit/${record.id}`)}>编辑</Button>
                            <Button type="link" size="small" style={{ color: '#52c41a' }} icon={<CheckOutlined />} onClick={() => handleApprove(record.id)}>通过</Button>
                            <Button type="link" size="small" danger icon={<CloseOutlined />} onClick={() => handleRefuse(record.id)}>拒绝</Button>
                            <Button type="link" size="small" danger icon={<DeleteOutlined />} onClick={() => handleDelete(record)}>删除</Button>
                        </>
                    )}
                </Space>
            )
        },
    ];

    const handleApprove = async (id: number) => {
        try { await approveFeeSheet(id); message.success('审核通过'); loadData(); } catch (e) { message.error('操作失败'); }
    };

    const handleRefuse = (id: number) => {
        Modal.confirm({
            title: '审核拒绝',
            content: <Input.TextArea id="refuseReason" rows={3} placeholder="请输入拒绝原因" />,
            onOk: async () => {
                const reason = (document.getElementById('refuseReason') as HTMLTextAreaElement).value;
                try { await refuseFeeSheet(id, reason); message.success('已拒绝'); loadData(); } catch (e) { message.error('操作失败'); }
            }
        });
    };

    const handleDelete = (record: any) => {
        Modal.confirm({
            title: '确认删除',
            icon: <ExclamationCircleOutlined />,
            content: `确定要删除单据 "${record.code}" 吗？`,
            onOk: async () => {
                try { await deleteFeeSheet(record.id); message.success('删除成功'); loadData(); } catch (e) { message.error('删除失败'); }
            }
        });
    };

    return (
        <div style={{ padding: '24px' }}>
            <Card title="供应商费用单">
                <Space style={{ marginBottom: 16 }} wrap>
                    <Input placeholder="单据编号" style={{ width: 180 }} value={searchCode} onChange={e => setSearchCode(e.target.value)} onPressEnter={handleSearch} />
                    <Select placeholder="供应商" style={{ width: 180 }} value={searchSupplierId} onChange={setSearchSupplierId} allowClear showSearch optionFilterProp="children">
                        {suppliers.map(s => <Select.Option key={s.id} value={s.id}>{s.name}</Select.Option>)}
                    </Select>
                    <Select placeholder="单据类型" style={{ width: 120 }} value={searchSheetType} onChange={setSearchSheetType} allowClear>
                        <Select.Option value={1}>付款</Select.Option>
                        <Select.Option value={2}>扣款</Select.Option>
                    </Select>
                    <Select placeholder="状态" style={{ width: 120 }} value={searchStatus} onChange={setSearchStatus} allowClear>
                        <Select.Option value={0}>待审核</Select.Option>
                        <Select.Option value={1}>已审核</Select.Option>
                        <Select.Option value={2}>已拒绝</Select.Option>
                    </Select>
                    <Button type="primary" icon={<SearchOutlined />} onClick={handleSearch}>查询</Button>
                    <Button icon={<ReloadOutlined />} onClick={handleReset}>重置</Button>
                    <Button type="primary" icon={<PlusOutlined />} onClick={() => navigate('/business/settle/fee/add')}>新增</Button>
                </Space>
                <Table loading={loading} columns={columns} dataSource={dataSource} rowKey="id" pagination={{ current, pageSize, total, showSizeChanger: true, onChange: (page, size) => { setCurrent(page); setPageSize(size); } }} />
            </Card>
        </div>
    );
};

export default FeeSheetList;

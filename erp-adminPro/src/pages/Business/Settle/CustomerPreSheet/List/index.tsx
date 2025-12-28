import React, { useState, useEffect } from 'react';
import { Table, Card, Space, Input, Select, Button, message, Tag, Modal } from 'antd';
import { SearchOutlined, ReloadOutlined, PlusOutlined, EyeOutlined, EditOutlined, DeleteOutlined, CheckOutlined, CloseOutlined, ExclamationCircleOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { getCustomerPreSheetPage, deleteCustomerPreSheet, approveCustomerPreSheet, refuseCustomerPreSheet } from '@/api/business/settle';
import { getCustomerList } from '@/api/basedata/customer';

const statusMap: Record<number, { color: string; text: string }> = { 0: { color: 'processing', text: '待审核' }, 1: { color: 'success', text: '已审核' }, 2: { color: 'error', text: '已拒绝' } };

const CustomerPreSheetList: React.FC = () => {
    const navigate = useNavigate();
    const [loading, setLoading] = useState(false);
    const [dataSource, setDataSource] = useState<any[]>([]);
    const [total, setTotal] = useState(0);
    const [current, setCurrent] = useState(1);
    const [pageSize, setPageSize] = useState(10);
    const [searchCode, setSearchCode] = useState('');
    const [searchCustomerId, setSearchCustomerId] = useState<number | undefined>(undefined);
    const [searchStatus, setSearchStatus] = useState<number | undefined>(undefined);
    const [customers, setCustomers] = useState<any[]>([]);

    const loadOptions = async () => { try { const res: any = await getCustomerList(); setCustomers(res || []); } catch (e) { console.error(e); } };
    const loadData = async () => {
        setLoading(true);
        try {
            const res: any = await getCustomerPreSheetPage({ current, size: pageSize, code: searchCode, customerId: searchCustomerId, status: searchStatus });
            setDataSource(res?.records || []); setTotal(res?.total || 0);
        } catch (e) { message.error('加载数据失败'); }
        finally { setLoading(false); }
    };

    useEffect(() => { loadOptions(); }, []);
    useEffect(() => { loadData(); }, [current, pageSize]);

    const handleSearch = () => { setCurrent(1); loadData(); };
    const handleReset = () => { setSearchCode(''); setSearchCustomerId(undefined); setSearchStatus(undefined); setCurrent(1); loadData(); };

    const columns = [
        { title: '单据编号', dataIndex: 'code', key: 'code', width: 180 },
        { title: '客户', dataIndex: 'customerId', key: 'customerId', render: (id: number) => customers.find(c => c.id === id)?.name || id },
        { title: '金额', dataIndex: 'totalAmount', key: 'totalAmount', width: 120, render: (val: number) => `￥${val?.toFixed(2) || '0.00'}` },
        { title: '状态', dataIndex: 'status', key: 'status', width: 100, render: (val: number) => <Tag color={statusMap[val]?.color}>{statusMap[val]?.text}</Tag> },
        { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
        {
            title: '操作', key: 'action', width: 250, render: (_: any, record: any) => (
                <Space>
                    <Button type="link" size="small" icon={<EyeOutlined />} onClick={() => navigate(`/business/settle/customer/pre/detail/${record.id}`)}>查看</Button>
                    {record.status === 0 && (
                        <>
                            <Button type="link" size="small" icon={<EditOutlined />} onClick={() => navigate(`/business/settle/customer/pre/edit/${record.id}`)}>编辑</Button>
                            <Button type="link" size="small" style={{ color: '#52c41a' }} icon={<CheckOutlined />} onClick={() => handleApprove(record.id)}>通过</Button>
                            <Button type="link" size="small" danger icon={<CloseOutlined />} onClick={() => handleRefuse(record.id)}>拒绝</Button>
                            <Button type="link" size="small" danger icon={<DeleteOutlined />} onClick={() => handleDelete(record)}>删除</Button>
                        </>
                    )}
                </Space>
            )
        },
    ];

    const handleApprove = async (id: number) => { try { await approveCustomerPreSheet(id); message.success('审核通过'); loadData(); } catch (e) { message.error('操作失败'); } };
    const handleRefuse = (id: number) => {
        Modal.confirm({
            title: '审核拒绝',
            content: <Input.TextArea id="refuseReason" rows={3} placeholder="请输入拒绝原因" />,
            onOk: async () => { const reason = (document.getElementById('refuseReason') as HTMLTextAreaElement).value; try { await refuseCustomerPreSheet(id, reason); message.success('已拒绝'); loadData(); } catch (e) { message.error('操作失败'); } }
        });
    };
    const handleDelete = (record: any) => {
        Modal.confirm({
            title: '确认删除', icon: <ExclamationCircleOutlined />, content: `确定要删除单据 "${record.code}" 吗？`,
            onOk: async () => { try { await deleteCustomerPreSheet(record.id); message.success('删除成功'); loadData(); } catch (e) { message.error('删除失败'); } }
        });
    };

    return (
        <div style={{ padding: '24px' }}>
            <Card title="客户预收款单">
                <Space style={{ marginBottom: 16 }} wrap>
                    <Input placeholder="单据编号" style={{ width: 180 }} value={searchCode} onChange={e => setSearchCode(e.target.value)} onPressEnter={handleSearch} />
                    <Select placeholder="客户" style={{ width: 180 }} value={searchCustomerId} onChange={setSearchCustomerId} allowClear showSearch optionFilterProp="children">
                        {customers.map(c => <Select.Option key={c.id} value={c.id}>{c.name}</Select.Option>)}
                    </Select>
                    <Select placeholder="状态" style={{ width: 120 }} value={searchStatus} onChange={setSearchStatus} allowClear>
                        <Select.Option value={0}>待审核</Select.Option>
                        <Select.Option value={1}>已审核</Select.Option>
                        <Select.Option value={2}>已拒绝</Select.Option>
                    </Select>
                    <Button type="primary" icon={<SearchOutlined />} onClick={handleSearch}>查询</Button>
                    <Button icon={<ReloadOutlined />} onClick={handleReset}>重置</Button>
                    <Button type="primary" icon={<PlusOutlined />} onClick={() => navigate('/business/settle/customer/pre/add')}>新增</Button>
                </Space>
                <Table loading={loading} columns={columns} dataSource={dataSource} rowKey="id" pagination={{ current, pageSize, total, showSizeChanger: true, onChange: (page, size) => { setCurrent(page); setPageSize(size); } }} />
            </Card>
        </div>
    );
};

export default CustomerPreSheetList;

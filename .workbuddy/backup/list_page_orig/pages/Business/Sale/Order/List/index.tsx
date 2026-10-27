import React, { useState, useEffect } from 'react';
import { Table, Card, Space, Select, Button, message, Tag, Popconfirm, Modal, Input } from 'antd';
import { PlusOutlined, SearchOutlined, ReloadOutlined, CheckOutlined, CloseOutlined, DeleteOutlined, EyeOutlined, EditOutlined, StopOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { getSaleOrderPage, approveSaleOrder, rejectSaleOrder, cancelSaleOrder, deleteSaleOrder } from '@/api/business/saleOrder';
import { getCustomerList } from '@/api/basedata/customer';

const { Option } = Select;

// 订单状态映射
const statusMap: Record<number, { text: string; color: string }> = {
    0: { text: '待审核', color: 'orange' },
    1: { text: '已审核', color: 'green' },
    2: { text: '已拒绝', color: 'red' },
    3: { text: '已完成', color: 'blue' },
    4: { text: '已取消', color: 'default' },
};

const SaleOrderList: React.FC = () => {
    const navigate = useNavigate();
    const [loading, setLoading] = useState(false);
    const [dataSource, setDataSource] = useState<any[]>([]);
    const [total, setTotal] = useState(0);
    const [current, setCurrent] = useState(1);
    const [pageSize, setPageSize] = useState(10);

    const [searchCustomerId, setSearchCustomerId] = useState<number | undefined>(undefined);
    const [searchStatus, setSearchStatus] = useState<number | undefined>(undefined);
    const [customers, setCustomers] = useState<any[]>([]);

    const [rejectModalVisible, setRejectModalVisible] = useState(false);
    const [rejectingId, setRejectingId] = useState<number | null>(null);
    const [rejectReason, setRejectReason] = useState('');

    const loadCustomers = async () => {
        try {
            const data: any = await getCustomerList();
            setCustomers(Array.isArray(data) ? data : []);
        } catch (error) {
            console.error('加载客户失败', error);
        }
    };

    const loadData = async () => {
        setLoading(true);
        try {
            const params = { current, size: pageSize, customerId: searchCustomerId, status: searchStatus };
            const data = await getSaleOrderPage(params);
            setDataSource(data.records);
            setTotal(data.total);
        } catch (error) {
            message.error('加载订单数据失败');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => { loadCustomers(); }, []);
    useEffect(() => { loadData(); }, [current, pageSize]);

    const handleSearch = () => { setCurrent(1); loadData(); };
    const handleReset = () => { setSearchCustomerId(undefined); setSearchStatus(undefined); setCurrent(1); loadData(); };

    const handleApprove = async (id: number) => {
        try { await approveSaleOrder(id); message.success('审核通过'); loadData(); }
        catch (error) { message.error('操作失败'); }
    };

    const handleReject = async () => {
        if (!rejectingId || !rejectReason) { message.warning('请填写拒绝原因'); return; }
        try { await rejectSaleOrder(rejectingId, rejectReason); message.success('已拒绝'); setRejectModalVisible(false); setRejectReason(''); loadData(); }
        catch (error) { message.error('操作失败'); }
    };

    const handleCancel = async (id: number) => {
        try { await cancelSaleOrder(id); message.success('已取消'); loadData(); }
        catch (error) { message.error('操作失败'); }
    };

    const handleDelete = async (id: number) => {
        try { await deleteSaleOrder(id); message.success('删除成功'); loadData(); }
        catch (error) { message.error('删除失败'); }
    };

    const columns = [
        { title: '订单编号', dataIndex: 'code', key: 'code' },
        { title: '客户', dataIndex: 'customerId', key: 'customerId', render: (id: number) => customers.find(c => c.id === id)?.name || id },
        { title: '销售数量', dataIndex: 'totalNum', key: 'totalNum' },
        { title: '销售金额', dataIndex: 'totalAmount', key: 'totalAmount', render: (amount: number) => `￥${amount?.toFixed(2) || '0.00'}` },
        { title: '预计发货日期', dataIndex: 'expectDeliveryDate', key: 'expectDeliveryDate' },
        {
            title: '状态', dataIndex: 'status', key: 'status', render: (status: number) => {
                const info = statusMap[status] || { text: '未知', color: 'default' };
                return <Tag color={info.color}>{info.text}</Tag>;
            }
        },
        { title: '创建时间', dataIndex: 'createTime', key: 'createTime' },
        {
            title: '操作', key: 'action', render: (_: any, record: any) => (
                <Space size="small">
                    <Button type="link" size="small" icon={<EyeOutlined />} onClick={() => navigate(`/business/sale/order/detail/${record.id}`)}>查看</Button>
                    {record.status === 0 && (
                        <>
                            <Button type="link" size="small" icon={<EditOutlined />} onClick={() => navigate(`/business/sale/order/edit/${record.id}`)}>编辑</Button>
                            <Popconfirm title="确定审核通过?" onConfirm={() => handleApprove(record.id)}>
                                <Button type="link" size="small" icon={<CheckOutlined />}>通过</Button>
                            </Popconfirm>
                            <Button type="link" size="small" icon={<CloseOutlined />} onClick={() => { setRejectingId(record.id); setRejectModalVisible(true); }}>拒绝</Button>
                        </>
                    )}
                    {(record.status === 0 || record.status === 1) && (
                        <Popconfirm title="确定取消订单?" onConfirm={() => handleCancel(record.id)}>
                            <Button type="link" size="small" icon={<StopOutlined />}>取消</Button>
                        </Popconfirm>
                    )}
                    <Popconfirm title="确定删除?" onConfirm={() => handleDelete(record.id)}>
                        <Button type="link" size="small" danger icon={<DeleteOutlined />}>删除</Button>
                    </Popconfirm>
                </Space>
            )
        },
    ];

    return (
        <div style={{ padding: '24px' }}>
            <Card title="销售订单管理">
                <Space style={{ marginBottom: 16 }} wrap>
                    <Select placeholder="选择客户" style={{ width: 200 }} value={searchCustomerId} onChange={setSearchCustomerId} allowClear>
                        {customers.map((c: any) => <Option key={c.id} value={c.id}>{c.name}</Option>)}
                    </Select>
                    <Select placeholder="订单状态" style={{ width: 120 }} value={searchStatus} onChange={setSearchStatus} allowClear>
                        {Object.entries(statusMap).map(([key, val]) => <Option key={key} value={Number(key)}>{val.text}</Option>)}
                    </Select>
                    <Button type="primary" icon={<SearchOutlined />} onClick={handleSearch}>查询</Button>
                    <Button icon={<ReloadOutlined />} onClick={handleReset}>重置</Button>
                    <Button type="primary" icon={<PlusOutlined />} onClick={() => navigate('/business/sale/order/add')}>新增订单</Button>
                </Space>
                <Table loading={loading} columns={columns} dataSource={dataSource} rowKey="id"
                    pagination={{
                        current, pageSize, total, showSizeChanger: true, showTotal: (t) => `共 ${t} 条`,
                        onChange: (page, size) => { setCurrent(page); setPageSize(size); }
                    }}
                />
            </Card>
            <Modal title="拒绝原因" open={rejectModalVisible} onOk={handleReject} onCancel={() => setRejectModalVisible(false)}>
                <Input.TextArea value={rejectReason} onChange={(e) => setRejectReason(e.target.value)} placeholder="请输入拒绝原因" rows={3} />
            </Modal>
        </div>
    );
};

export default SaleOrderList;

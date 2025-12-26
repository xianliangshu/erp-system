import React, { useState, useEffect } from 'react';
import { Table, Card, Space, Select, Button, message, Tag, Popconfirm } from 'antd';
import { PlusOutlined, SearchOutlined, ReloadOutlined, CheckOutlined, DeleteOutlined, EyeOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { getSaleDeliveryPage, confirmSaleDelivery, deleteSaleDelivery } from '@/api/business/saleDelivery';
import { getCustomerList } from '@/api/basedata/customer';

const { Option } = Select;

const statusMap: Record<number, { text: string; color: string }> = {
    0: { text: '待确认', color: 'orange' }, 1: { text: '已确认', color: 'green' },
};

const SaleDeliveryList: React.FC = () => {
    const navigate = useNavigate();
    const [loading, setLoading] = useState(false);
    const [dataSource, setDataSource] = useState<any[]>([]);
    const [total, setTotal] = useState(0);
    const [current, setCurrent] = useState(1);
    const [pageSize, setPageSize] = useState(10);
    const [searchCustomerId, setSearchCustomerId] = useState<number | undefined>(undefined);
    const [searchStatus, setSearchStatus] = useState<number | undefined>(undefined);
    const [customers, setCustomers] = useState<any[]>([]);

    const loadCustomers = async () => {
        try { const data: any = await getCustomerList(); setCustomers(Array.isArray(data) ? data : []); }
        catch (error) { console.error('加载客户失败', error); }
    };

    const loadData = async () => {
        setLoading(true);
        try {
            const data = await getSaleDeliveryPage({ current, size: pageSize, customerId: searchCustomerId, status: searchStatus });
            setDataSource(data.records); setTotal(data.total);
        } catch (error) { message.error('加载出库数据失败'); }
        finally { setLoading(false); }
    };

    useEffect(() => { loadCustomers(); }, []);
    useEffect(() => { loadData(); }, [current, pageSize]);

    const handleSearch = () => { setCurrent(1); loadData(); };
    const handleReset = () => { setSearchCustomerId(undefined); setSearchStatus(undefined); setCurrent(1); loadData(); };
    const handleConfirm = async (id: number) => {
        try { await confirmSaleDelivery(id); message.success('确认出库成功'); loadData(); }
        catch (error) { message.error('操作失败'); }
    };
    const handleDelete = async (id: number) => {
        try { await deleteSaleDelivery(id); message.success('删除成功'); loadData(); }
        catch (error) { message.error('删除失败'); }
    };

    const columns = [
        { title: '出库单编号', dataIndex: 'code', key: 'code' },
        { title: '销售订单编号', dataIndex: 'orderCode', key: 'orderCode' },
        { title: '客户', dataIndex: 'customerId', key: 'customerId', render: (id: number) => customers.find(c => c.id === id)?.name || id },
        { title: '出库数量', dataIndex: 'totalNum', key: 'totalNum' },
        { title: '出库金额', dataIndex: 'totalAmount', key: 'totalAmount', render: (amount: number) => `￥${amount?.toFixed(2) || '0.00'}` },
        { title: '状态', dataIndex: 'status', key: 'status', render: (status: number) => { const info = statusMap[status] || { text: '未知', color: 'default' }; return <Tag color={info.color}>{info.text}</Tag>; } },
        { title: '创建时间', dataIndex: 'createTime', key: 'createTime' },
        {
            title: '操作', key: 'action', render: (_: any, record: any) => (
                <Space size="small">
                    <Button type="link" size="small" icon={<EyeOutlined />} onClick={() => navigate(`/business/sale/delivery/detail/${record.id}`)}>查看</Button>
                    {record.status === 0 && (
                        <Popconfirm title="确定确认出库? 确认后将扣减库存" onConfirm={() => handleConfirm(record.id)}>
                            <Button type="link" size="small" icon={<CheckOutlined />}>确认</Button>
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
            <Card title="销售出库管理">
                <Space style={{ marginBottom: 16 }} wrap>
                    <Select placeholder="选择客户" style={{ width: 200 }} value={searchCustomerId} onChange={setSearchCustomerId} allowClear>
                        {customers.map((c: any) => <Option key={c.id} value={c.id}>{c.name}</Option>)}
                    </Select>
                    <Select placeholder="出库状态" style={{ width: 120 }} value={searchStatus} onChange={setSearchStatus} allowClear>
                        {Object.entries(statusMap).map(([key, val]) => <Option key={key} value={Number(key)}>{val.text}</Option>)}
                    </Select>
                    <Button type="primary" icon={<SearchOutlined />} onClick={handleSearch}>查询</Button>
                    <Button icon={<ReloadOutlined />} onClick={handleReset}>重置</Button>
                    <Button type="primary" icon={<PlusOutlined />} onClick={() => navigate('/business/sale/delivery/add')}>新增出库</Button>
                </Space>
                <Table loading={loading} columns={columns} dataSource={dataSource} rowKey="id"
                    pagination={{ current, pageSize, total, showSizeChanger: true, showTotal: (t) => `共 ${t} 条`, onChange: (page, size) => { setCurrent(page); setPageSize(size); } }}
                />
            </Card>
        </div>
    );
};

export default SaleDeliveryList;

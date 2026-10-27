import React, { useState, useEffect } from 'react';
import { Table, Card, Space, Select, Button, message, Tag, Popconfirm } from 'antd';
import { PlusOutlined, SearchOutlined, ReloadOutlined, CheckOutlined, DeleteOutlined, EyeOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { getStockTransferPage, confirmStockTransfer, deleteStockTransfer } from '@/api/business/stockTransfer';
import { getWarehouseList } from '@/api/basedata/warehouse';

const { Option } = Select;
const statusMap: Record<number, { text: string; color: string }> = {
    0: { text: '待确认', color: 'orange' },
    1: { text: '已确认', color: 'green' }
};

const StockTransferList: React.FC = () => {
    const navigate = useNavigate();
    const [loading, setLoading] = useState(false);
    const [dataSource, setDataSource] = useState<any[]>([]);
    const [total, setTotal] = useState(0);
    const [current, setCurrent] = useState(1);
    const [pageSize, setPageSize] = useState(10);
    const [searchOutScId, setSearchOutScId] = useState<number | undefined>(undefined);
    const [searchInScId, setSearchInScId] = useState<number | undefined>(undefined);
    const [searchStatus, setSearchStatus] = useState<number | undefined>(undefined);
    const [warehouses, setWarehouses] = useState<any[]>([]);

    const loadWarehouses = async () => {
        try {
            const data: any = await getWarehouseList();
            setWarehouses(Array.isArray(data) ? data : []);
        } catch (e) {
            console.error(e);
        }
    };

    const loadData = async () => {
        setLoading(true);
        try {
            const data = await getStockTransferPage({
                current,
                size: pageSize,
                outScId: searchOutScId,
                inScId: searchInScId,
                status: searchStatus
            });
            setDataSource(data.records);
            setTotal(data.total);
        } catch (error) {
            message.error('加载数据失败');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => { loadWarehouses(); }, []);
    useEffect(() => { loadData(); }, [current, pageSize]);

    const handleSearch = () => { setCurrent(1); loadData(); };
    const handleReset = () => {
        setSearchOutScId(undefined);
        setSearchInScId(undefined);
        setSearchStatus(undefined);
        setCurrent(1);
        loadData();
    };

    const handleConfirm = async (id: number) => {
        try {
            await confirmStockTransfer(id);
            message.success('确认成功，库存已调拨');
            loadData();
        } catch (e) {
            message.error('操作失败');
        }
    };

    const handleDelete = async (id: number) => {
        try {
            await deleteStockTransfer(id);
            message.success('删除成功');
            loadData();
        } catch (e) {
            message.error('删除失败');
        }
    };

    const getWarehouseName = (id: number) => {
        return warehouses.find(w => w.id === id)?.name || id;
    };

    const columns = [
        { title: '调拨单编号', dataIndex: 'code', key: 'code' },
        { title: '调出仓库', dataIndex: 'outScId', key: 'outScId', render: (id: number) => getWarehouseName(id) },
        { title: '调入仓库', dataIndex: 'inScId', key: 'inScId', render: (id: number) => getWarehouseName(id) },
        { title: '调拨日期', dataIndex: 'transferDate', key: 'transferDate' },
        { title: '调拨数量', dataIndex: 'totalNum', key: 'totalNum' },
        {
            title: '状态', dataIndex: 'status', key: 'status',
            render: (status: number) => {
                const info = statusMap[status] || { text: '未知', color: 'default' };
                return <Tag color={info.color}>{info.text}</Tag>;
            }
        },
        { title: '创建时间', dataIndex: 'createTime', key: 'createTime' },
        {
            title: '操作', key: 'action',
            render: (_: any, record: any) => (
                <Space size="small">
                    <Button type="link" size="small" icon={<EyeOutlined />}
                        onClick={() => navigate(`/business/stock/transfer/detail/${record.id}`)}>查看</Button>
                    {record.status === 0 && (
                        <>
                            <Button type="link" size="small"
                                onClick={() => navigate(`/business/stock/transfer/edit/${record.id}`)}>编辑</Button>
                            <Popconfirm title="确定确认调拨? 确认后将执行库存变动" onConfirm={() => handleConfirm(record.id)}>
                                <Button type="link" size="small" icon={<CheckOutlined />}>确认</Button>
                            </Popconfirm>
                        </>
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
            <Card title="库存调拨管理">
                <Space style={{ marginBottom: 16 }} wrap>
                    <Select placeholder="调出仓库" style={{ width: 150 }} value={searchOutScId}
                        onChange={setSearchOutScId} allowClear>
                        {warehouses.map((w: any) => <Option key={w.id} value={w.id}>{w.name}</Option>)}
                    </Select>
                    <Select placeholder="调入仓库" style={{ width: 150 }} value={searchInScId}
                        onChange={setSearchInScId} allowClear>
                        {warehouses.map((w: any) => <Option key={w.id} value={w.id}>{w.name}</Option>)}
                    </Select>
                    <Select placeholder="状态" style={{ width: 100 }} value={searchStatus}
                        onChange={setSearchStatus} allowClear>
                        {Object.entries(statusMap).map(([key, val]) => (
                            <Option key={key} value={Number(key)}>{val.text}</Option>
                        ))}
                    </Select>
                    <Button type="primary" icon={<SearchOutlined />} onClick={handleSearch}>查询</Button>
                    <Button icon={<ReloadOutlined />} onClick={handleReset}>重置</Button>
                    <Button type="primary" icon={<PlusOutlined />}
                        onClick={() => navigate('/business/stock/transfer/add')}>新增调拨</Button>
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
                        showTotal: (t) => `共 ${t} 条`,
                        onChange: (page, size) => { setCurrent(page); setPageSize(size); }
                    }}
                />
            </Card>
        </div>
    );
};

export default StockTransferList;

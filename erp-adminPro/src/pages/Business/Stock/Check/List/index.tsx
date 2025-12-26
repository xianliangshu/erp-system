import React, { useState, useEffect } from 'react';
import { Table, Card, Space, Select, Button, message, Tag, Popconfirm } from 'antd';
import { PlusOutlined, SearchOutlined, ReloadOutlined, CheckOutlined, DeleteOutlined, EyeOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { getStockCheckPage, approveStockCheck, deleteStockCheck } from '@/api/business/stockCheck';
import { getWarehouseList } from '@/api/basedata/warehouse';

const { Option } = Select;
const statusMap: Record<number, { text: string; color: string }> = { 0: { text: '待审核', color: 'orange' }, 1: { text: '已审核', color: 'green' } };

const StockCheckList: React.FC = () => {
    const navigate = useNavigate();
    const [loading, setLoading] = useState(false);
    const [dataSource, setDataSource] = useState<any[]>([]);
    const [total, setTotal] = useState(0);
    const [current, setCurrent] = useState(1);
    const [pageSize, setPageSize] = useState(10);
    const [searchScId, setSearchScId] = useState<number | undefined>(undefined);
    const [searchStatus, setSearchStatus] = useState<number | undefined>(undefined);
    const [warehouses, setWarehouses] = useState<any[]>([]);

    const loadWarehouses = async () => { try { const data: any = await getWarehouseList(); setWarehouses(Array.isArray(data) ? data : []); } catch (e) { console.error(e); } };
    const loadData = async () => {
        setLoading(true);
        try { const data = await getStockCheckPage({ current, size: pageSize, scId: searchScId, status: searchStatus }); setDataSource(data.records); setTotal(data.total); }
        catch (error) { message.error('加载盘点数据失败'); } finally { setLoading(false); }
    };

    useEffect(() => { loadWarehouses(); }, []);
    useEffect(() => { loadData(); }, [current, pageSize]);

    const handleSearch = () => { setCurrent(1); loadData(); };
    const handleReset = () => { setSearchScId(undefined); setSearchStatus(undefined); setCurrent(1); loadData(); };
    const handleApprove = async (id: number) => { try { await approveStockCheck(id); message.success('审核成功'); loadData(); } catch (e) { message.error('操作失败'); } };
    const handleDelete = async (id: number) => { try { await deleteStockCheck(id); message.success('删除成功'); loadData(); } catch (e) { message.error('删除失败'); } };

    const columns = [
        { title: '盘点单编号', dataIndex: 'code', key: 'code' },
        { title: '仓库', dataIndex: 'scId', key: 'scId', render: (id: number) => warehouses.find(w => w.id === id)?.name || id },
        { title: '盘点日期', dataIndex: 'checkDate', key: 'checkDate' },
        { title: '盘盈数量', dataIndex: 'totalProfitNum', key: 'totalProfitNum', render: (num: number) => <span style={{ color: 'green' }}>+{num || 0}</span> },
        { title: '盘亏数量', dataIndex: 'totalLossNum', key: 'totalLossNum', render: (num: number) => <span style={{ color: 'red' }}>-{num || 0}</span> },
        { title: '状态', dataIndex: 'status', key: 'status', render: (status: number) => { const info = statusMap[status] || { text: '未知', color: 'default' }; return <Tag color={info.color}>{info.text}</Tag>; } },
        { title: '创建时间', dataIndex: 'createTime', key: 'createTime' },
        {
            title: '操作', key: 'action', render: (_: any, record: any) => (
                <Space size="small">
                    <Button type="link" size="small" icon={<EyeOutlined />} onClick={() => navigate(`/business/stock/check/detail/${record.id}`)}>查看</Button>
                    {record.status === 0 && (<Popconfirm title="确定审核? 审核后将调整库存" onConfirm={() => handleApprove(record.id)}><Button type="link" size="small" icon={<CheckOutlined />}>审核</Button></Popconfirm>)}
                    <Popconfirm title="确定删除?" onConfirm={() => handleDelete(record.id)}><Button type="link" size="small" danger icon={<DeleteOutlined />}>删除</Button></Popconfirm>
                </Space>
            )
        },
    ];

    return (
        <div style={{ padding: '24px' }}>
            <Card title="库存盘点管理">
                <Space style={{ marginBottom: 16 }} wrap>
                    <Select placeholder="选择仓库" style={{ width: 200 }} value={searchScId} onChange={setSearchScId} allowClear>
                        {warehouses.map((w: any) => <Option key={w.id} value={w.id}>{w.name}</Option>)}
                    </Select>
                    <Select placeholder="盘点状态" style={{ width: 120 }} value={searchStatus} onChange={setSearchStatus} allowClear>
                        {Object.entries(statusMap).map(([key, val]) => <Option key={key} value={Number(key)}>{val.text}</Option>)}
                    </Select>
                    <Button type="primary" icon={<SearchOutlined />} onClick={handleSearch}>查询</Button>
                    <Button icon={<ReloadOutlined />} onClick={handleReset}>重置</Button>
                    <Button type="primary" icon={<PlusOutlined />} onClick={() => navigate('/business/stock/check/add')}>新增盘点</Button>
                </Space>
                <Table loading={loading} columns={columns} dataSource={dataSource} rowKey="id"
                    pagination={{ current, pageSize, total, showSizeChanger: true, showTotal: (t) => `共 ${t} 条`, onChange: (page, size) => { setCurrent(page); setPageSize(size); } }}
                />
            </Card>
        </div>
    );
};

export default StockCheckList;

import React, { useState, useEffect } from 'react';
import { Descriptions, Card, Table, Tag, Space, Button, message, Spin } from 'antd';
import { ArrowLeftOutlined } from '@ant-design/icons';
import { useNavigate, useParams } from 'react-router-dom';
import { getStockCheckById } from '@/api/business/stockCheck';
import { getWarehouseList } from '@/api/basedata/warehouse';
import { getMaterialPage } from '@/api/material/material';

const statusMap: Record<number, { text: string; color: string }> = { 0: { text: '待审核', color: 'orange' }, 1: { text: '已审核', color: 'green' } };

const StockCheckDetail: React.FC = () => {
    const navigate = useNavigate();
    const { id } = useParams<{ id: string }>();
    const [loading, setLoading] = useState(true);
    const [checkData, setCheckData] = useState<any>(null);
    const [details, setDetails] = useState<any[]>([]);
    const [warehouses, setWarehouses] = useState<any[]>([]);
    const [materials, setMaterials] = useState<any[]>([]);

    useEffect(() => { loadBasicData(); loadCheckData(); }, [id]);

    const loadBasicData = async () => {
        try {
            const [warehouseData, materialData] = await Promise.all([getWarehouseList(), getMaterialPage({ current: 1, size: 1000 })]);
            setWarehouses(Array.isArray(warehouseData) ? warehouseData : []);
            setMaterials((materialData as any)?.records || []);
        } catch (e) { console.error(e); }
    };

    const loadCheckData = async () => {
        setLoading(true);
        try { const data: any = await getStockCheckById(Number(id)); if (data) { setCheckData(data.stockCheck || data); setDetails(data.details || []); } }
        catch (e) { message.error('加载盘点单数据失败'); } finally { setLoading(false); }
    };

    const getWarehouseName = (warehouseId: number) => warehouses.find(w => w.id === warehouseId)?.name || warehouseId;
    const getMaterialName = (productId: number) => materials.find(m => m.id === productId)?.name || productId;

    const detailColumns = [
        { title: '序号', key: 'index', width: 60, render: (_: any, __: any, index: number) => index + 1 },
        { title: '商品名称', dataIndex: 'productId', key: 'productId', render: (productId: number) => getMaterialName(productId) },
        { title: '账面数量', dataIndex: 'stockNum', key: 'stockNum', width: 100 },
        { title: '实盘数量', dataIndex: 'actualNum', key: 'actualNum', width: 100 },
        { title: '差异', dataIndex: 'diffNum', key: 'diffNum', width: 100, render: (diff: number) => <span style={{ color: diff > 0 ? 'green' : diff < 0 ? 'red' : 'inherit' }}>{diff > 0 ? '+' : ''}{diff}</span> },
        { title: '成本单价', dataIndex: 'costPrice', key: 'costPrice', width: 100, render: (price: number) => `￥${(price || 0).toFixed(2)}` },
        { title: '差异金额', dataIndex: 'diffAmount', key: 'diffAmount', width: 120, render: (amount: number) => <span style={{ color: amount > 0 ? 'green' : amount < 0 ? 'red' : 'inherit' }}>￥{(amount || 0).toFixed(2)}</span> },
        { title: '备注', dataIndex: 'description', key: 'description' },
    ];

    if (loading) return <div style={{ padding: '24px', textAlign: 'center' }}><Spin size="large" tip="加载中..." /></div>;
    if (!checkData) return <div style={{ padding: '24px', textAlign: 'center' }}><p>盘点单数据不存在</p><Button onClick={() => navigate(-1)}>返回</Button></div>;

    const statusInfo = statusMap[checkData.status] || { text: '未知', color: 'default' };

    return (
        <div style={{ padding: '24px' }}>
            <Card title={<Space><Button icon={<ArrowLeftOutlined />} onClick={() => navigate(-1)}>返回</Button><span>库存盘点详情</span></Space>}
                extra={checkData.status === 0 && <Button type="primary" onClick={() => navigate(`/business/stock/check/edit/${id}`)}>编辑</Button>}>
                <Descriptions title="基本信息" bordered column={3}>
                    <Descriptions.Item label="盘点单编号">{checkData.code || '-'}</Descriptions.Item>
                    <Descriptions.Item label="仓库">{getWarehouseName(checkData.scId)}</Descriptions.Item>
                    <Descriptions.Item label="盘点日期">{checkData.checkDate || '-'}</Descriptions.Item>
                    <Descriptions.Item label="盘盈数量"><span style={{ color: 'green' }}>+{checkData.totalProfitNum || 0}</span></Descriptions.Item>
                    <Descriptions.Item label="盘亏数量"><span style={{ color: 'red' }}>-{checkData.totalLossNum || 0}</span></Descriptions.Item>
                    <Descriptions.Item label="状态"><Tag color={statusInfo.color}>{statusInfo.text}</Tag></Descriptions.Item>
                    <Descriptions.Item label="创建时间">{checkData.createTime || '-'}</Descriptions.Item>
                    <Descriptions.Item label="备注" span={2}>{checkData.description || '-'}</Descriptions.Item>
                </Descriptions>
                <div style={{ marginTop: 24 }}>
                    <h3>盘点明细</h3>
                    <Table columns={detailColumns} dataSource={details} rowKey={(record) => record.id || record.key} pagination={false} />
                </div>
            </Card>
        </div>
    );
};

export default StockCheckDetail;

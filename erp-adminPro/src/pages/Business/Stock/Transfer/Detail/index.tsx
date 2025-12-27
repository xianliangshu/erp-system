import React, { useState, useEffect } from 'react';
import { Card, Descriptions, Table, Tag, Button, message, Space } from 'antd';
import { ArrowLeftOutlined, CheckOutlined } from '@ant-design/icons';
import { useNavigate, useParams } from 'react-router-dom';
import { getStockTransferById, confirmStockTransfer } from '@/api/business/stockTransfer';
import { getWarehouseList } from '@/api/basedata/warehouse';
import { getMaterialPage } from '@/api/material/material';

const statusMap: Record<number, { text: string; color: string }> = {
    0: { text: '待确认', color: 'orange' },
    1: { text: '已确认', color: 'green' }
};

const StockTransferDetail: React.FC = () => {
    const navigate = useNavigate();
    const { id } = useParams<{ id: string }>();
    const [transfer, setTransfer] = useState<any>(null);
    const [details, setDetails] = useState<any[]>([]);
    const [warehouses, setWarehouses] = useState<any[]>([]);
    const [materials, setMaterials] = useState<any[]>([]);

    useEffect(() => {
        loadWarehouses();
        loadMaterials();
        loadData();
    }, [id]);

    const loadWarehouses = async () => {
        try {
            const data: any = await getWarehouseList();
            setWarehouses(Array.isArray(data) ? data : []);
        } catch (e) {
            console.error(e);
        }
    };

    const loadMaterials = async () => {
        try {
            const data: any = await getMaterialPage({ current: 1, size: 1000 });
            setMaterials(data.records || []);
        } catch (e) {
            console.error(e);
        }
    };

    const loadData = async () => {
        try {
            const data: any = await getStockTransferById(Number(id));
            setTransfer(data.transfer);
            setDetails(data.details || []);
        } catch (e) {
            message.error('加载数据失败');
        }
    };

    const handleConfirm = async () => {
        try {
            await confirmStockTransfer(Number(id));
            message.success('确认成功，库存已调拨');
            loadData();
        } catch (e) {
            message.error('操作失败');
        }
    };

    const getWarehouseName = (id: number) => {
        return warehouses.find(w => w.id === id)?.name || id;
    };

    const getMaterialName = (id: number) => {
        const m = materials.find(m => m.id === id);
        return m ? `${m.name} (${m.code})` : id;
    };

    const detailColumns = [
        { title: '商品', dataIndex: 'productId', render: (id: number) => getMaterialName(id) },
        { title: '调拨数量', dataIndex: 'transferNum' },
        { title: '成本单价', dataIndex: 'costPrice', render: (v: number) => `￥${(v || 0).toFixed(2)}` },
        { title: '金额', render: (_: any, record: any) => `￥${((record.transferNum || 0) * (record.costPrice || 0)).toFixed(2)}` },
        { title: '备注', dataIndex: 'description' },
    ];

    if (!transfer) return null;

    const statusInfo = statusMap[transfer.status] || { text: '未知', color: 'default' };

    return (
        <div style={{ padding: '24px' }}>
            <Card
                title="调拨单详情"
                extra={
                    <Space>
                        {transfer.status === 0 && (
                            <Button type="primary" icon={<CheckOutlined />} onClick={handleConfirm}>
                                确认调拨
                            </Button>
                        )}
                        <Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/business/stock/transfer')}>
                            返回
                        </Button>
                    </Space>
                }
            >
                <Descriptions bordered column={3} style={{ marginBottom: 24 }}>
                    <Descriptions.Item label="调拨单编号">{transfer.code}</Descriptions.Item>
                    <Descriptions.Item label="调出仓库">{getWarehouseName(transfer.outScId)}</Descriptions.Item>
                    <Descriptions.Item label="调入仓库">{getWarehouseName(transfer.inScId)}</Descriptions.Item>
                    <Descriptions.Item label="调拨日期">{transfer.transferDate}</Descriptions.Item>
                    <Descriptions.Item label="调拨数量">{transfer.totalNum}</Descriptions.Item>
                    <Descriptions.Item label="状态">
                        <Tag color={statusInfo.color}>{statusInfo.text}</Tag>
                    </Descriptions.Item>
                    <Descriptions.Item label="创建时间">{transfer.createTime}</Descriptions.Item>
                    <Descriptions.Item label="备注" span={2}>{transfer.description || '-'}</Descriptions.Item>
                </Descriptions>

                <Card title="调拨明细" type="inner">
                    <Table
                        columns={detailColumns}
                        dataSource={details}
                        rowKey="id"
                        pagination={false}
                        size="small"
                    />
                </Card>
            </Card>
        </div>
    );
};

export default StockTransferDetail;

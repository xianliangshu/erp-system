import React, { useState, useEffect } from 'react';
import { Descriptions, Card, Table, Tag, Space, Button, message, Spin } from 'antd';
import { ArrowLeftOutlined } from '@ant-design/icons';
import { useNavigate, useParams } from 'react-router-dom';
import { getPurchaseReceiptById } from '@/api/business/purchaseReceipt';
import { getSupplierList } from '@/api/basedata/supplier';
import { getWarehouseList } from '@/api/basedata/warehouse';
import { getMaterialPage } from '@/api/material/material';

// 收货单状态映射
const statusMap: Record<number, { text: string; color: string }> = {
    0: { text: '待确认', color: 'orange' },
    1: { text: '已确认', color: 'green' },
};

const PurchaseReceiptDetail: React.FC = () => {
    const navigate = useNavigate();
    const { id } = useParams<{ id: string }>();

    const [loading, setLoading] = useState(true);
    const [receiptData, setReceiptData] = useState<any>(null);
    const [details, setDetails] = useState<any[]>([]);
    const [suppliers, setSuppliers] = useState<any[]>([]);
    const [warehouses, setWarehouses] = useState<any[]>([]);
    const [materials, setMaterials] = useState<any[]>([]);

    useEffect(() => {
        loadBasicData();
        loadReceiptData();
    }, [id]);

    const loadBasicData = async () => {
        try {
            const [supplierData, warehouseData, materialData] = await Promise.all([
                getSupplierList(),
                getWarehouseList(),
                getMaterialPage({ current: 1, size: 1000 })
            ]);
            setSuppliers(Array.isArray(supplierData) ? supplierData : []);
            setWarehouses(Array.isArray(warehouseData) ? warehouseData : []);
            setMaterials((materialData as any)?.records || []);
        } catch (error) {
            console.error('加载基础数据失败', error);
        }
    };

    const loadReceiptData = async () => {
        setLoading(true);
        try {
            const data: any = await getPurchaseReceiptById(Number(id));
            if (data) {
                setReceiptData(data.receipt || data);
                setDetails(data.details || []);
            }
        } catch (error) {
            message.error('加载收货单数据失败');
        } finally {
            setLoading(false);
        }
    };

    const getSupplierName = (supplierId: number) => {
        const supplier = suppliers.find(s => s.id === supplierId);
        return supplier?.name || supplierId;
    };

    const getWarehouseName = (warehouseId: number) => {
        const warehouse = warehouses.find(w => w.id === warehouseId);
        return warehouse?.name || warehouseId;
    };

    const getMaterialName = (productId: number) => {
        const material = materials.find(m => m.id === productId);
        return material?.name || productId;
    };

    const totalNum = details.reduce((sum, d) => sum + (d.receiveNum || 0), 0);
    const totalAmount = details.reduce((sum, d) => sum + ((d.receiveNum || 0) * (d.taxPrice || 0)), 0);

    const detailColumns = [
        { title: '序号', key: 'index', width: 60, render: (_: any, __: any, index: number) => index + 1 },
        { title: '商品名称', dataIndex: 'productId', key: 'productId', render: (productId: number) => getMaterialName(productId) },
        { title: '订单数量', dataIndex: 'orderNum', key: 'orderNum', width: 100 },
        { title: '收货数量', dataIndex: 'receiveNum', key: 'receiveNum', width: 100 },
        { title: '含税单价', dataIndex: 'taxPrice', key: 'taxPrice', width: 120, render: (price: number) => `￥${(price || 0).toFixed(2)}` },
        { title: '金额', dataIndex: 'taxAmount', key: 'taxAmount', width: 120, render: (amount: number) => `￥${(amount || 0).toFixed(2)}` },
        { title: '备注', dataIndex: 'description', key: 'description' },
    ];

    if (loading) {
        return <div style={{ padding: '24px', textAlign: 'center' }}><Spin size="large" tip="加载中..." /></div>;
    }

    if (!receiptData) {
        return <div style={{ padding: '24px', textAlign: 'center' }}><p>收货单数据不存在</p><Button onClick={() => navigate(-1)}>返回</Button></div>;
    }

    const statusInfo = statusMap[receiptData.status] || { text: '未知', color: 'default' };

    return (
        <div style={{ padding: '24px' }}>
            <Card
                title={<Space><Button icon={<ArrowLeftOutlined />} onClick={() => navigate(-1)}>返回</Button><span>采购收货详情</span></Space>}
                extra={receiptData.status === 0 && <Button type="primary" onClick={() => navigate(`/business/purchase/receipt/edit/${id}`)}>编辑</Button>}
            >
                <Descriptions title="基本信息" bordered column={3}>
                    <Descriptions.Item label="收货单编号">{receiptData.code || '-'}</Descriptions.Item>
                    <Descriptions.Item label="采购订单编号">{receiptData.orderCode || '-'}</Descriptions.Item>
                    <Descriptions.Item label="供应商">{getSupplierName(receiptData.supplierId)}</Descriptions.Item>
                    <Descriptions.Item label="仓库">{getWarehouseName(receiptData.scId)}</Descriptions.Item>
                    <Descriptions.Item label="状态"><Tag color={statusInfo.color}>{statusInfo.text}</Tag></Descriptions.Item>
                    <Descriptions.Item label="创建时间">{receiptData.createTime || '-'}</Descriptions.Item>
                    <Descriptions.Item label="收货数量">{receiptData.totalNum || totalNum}</Descriptions.Item>
                    <Descriptions.Item label="收货金额">￥{(receiptData.totalAmount || totalAmount).toFixed(2)}</Descriptions.Item>
                    <Descriptions.Item label="备注">{receiptData.description || '-'}</Descriptions.Item>
                </Descriptions>

                <div style={{ marginTop: 24 }}>
                    <h3>收货明细</h3>
                    <Table columns={detailColumns} dataSource={details} rowKey={(record) => record.id || record.key} pagination={false}
                        summary={() => (
                            <Table.Summary>
                                <Table.Summary.Row>
                                    <Table.Summary.Cell index={0}>合计</Table.Summary.Cell>
                                    <Table.Summary.Cell index={1}>-</Table.Summary.Cell>
                                    <Table.Summary.Cell index={2}>-</Table.Summary.Cell>
                                    <Table.Summary.Cell index={3}>{totalNum}</Table.Summary.Cell>
                                    <Table.Summary.Cell index={4}>-</Table.Summary.Cell>
                                    <Table.Summary.Cell index={5}>￥{totalAmount.toFixed(2)}</Table.Summary.Cell>
                                    <Table.Summary.Cell index={6}>-</Table.Summary.Cell>
                                </Table.Summary.Row>
                            </Table.Summary>
                        )}
                    />
                </div>
            </Card>
        </div>
    );
};

export default PurchaseReceiptDetail;

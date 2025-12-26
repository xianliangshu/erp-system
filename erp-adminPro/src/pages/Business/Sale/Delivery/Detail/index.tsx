import React, { useState, useEffect } from 'react';
import { Descriptions, Card, Table, Tag, Space, Button, message, Spin } from 'antd';
import { ArrowLeftOutlined } from '@ant-design/icons';
import { useNavigate, useParams } from 'react-router-dom';
import { getSaleDeliveryById } from '@/api/business/saleDelivery';
import { getCustomerList } from '@/api/basedata/customer';
import { getWarehouseList } from '@/api/basedata/warehouse';
import { getMaterialPage } from '@/api/material/material';

const statusMap: Record<number, { text: string; color: string }> = { 0: { text: '待确认', color: 'orange' }, 1: { text: '已确认', color: 'green' } };

const SaleDeliveryDetail: React.FC = () => {
    const navigate = useNavigate();
    const { id } = useParams<{ id: string }>();
    const [loading, setLoading] = useState(true);
    const [deliveryData, setDeliveryData] = useState<any>(null);
    const [details, setDetails] = useState<any[]>([]);
    const [customers, setCustomers] = useState<any[]>([]);
    const [warehouses, setWarehouses] = useState<any[]>([]);
    const [materials, setMaterials] = useState<any[]>([]);

    useEffect(() => { loadBasicData(); loadDeliveryData(); }, [id]);

    const loadBasicData = async () => {
        try {
            const [customerData, warehouseData, materialData] = await Promise.all([getCustomerList(), getWarehouseList(), getMaterialPage({ current: 1, size: 1000 })]);
            setCustomers(Array.isArray(customerData) ? customerData : []);
            setWarehouses(Array.isArray(warehouseData) ? warehouseData : []);
            setMaterials((materialData as any)?.records || []);
        } catch (error) { console.error('加载基础数据失败', error); }
    };

    const loadDeliveryData = async () => {
        setLoading(true);
        try { const data: any = await getSaleDeliveryById(Number(id)); if (data) { setDeliveryData(data.delivery || data); setDetails(data.details || []); } }
        catch (error) { message.error('加载出库单数据失败'); }
        finally { setLoading(false); }
    };

    const getCustomerName = (customerId: number) => customers.find(c => c.id === customerId)?.name || customerId;
    const getWarehouseName = (warehouseId: number) => warehouses.find(w => w.id === warehouseId)?.name || warehouseId;
    const getMaterialName = (productId: number) => materials.find(m => m.id === productId)?.name || productId;

    const totalNum = details.reduce((sum, d) => sum + (d.deliveryNum || 0), 0);
    const totalAmount = details.reduce((sum, d) => sum + ((d.deliveryNum || 0) * (d.taxPrice || 0)), 0);

    const detailColumns = [
        { title: '序号', key: 'index', width: 60, render: (_: any, __: any, index: number) => index + 1 },
        { title: '商品名称', dataIndex: 'productId', key: 'productId', render: (productId: number) => getMaterialName(productId) },
        { title: '订单数量', dataIndex: 'orderNum', key: 'orderNum', width: 100 },
        { title: '出库数量', dataIndex: 'deliveryNum', key: 'deliveryNum', width: 100 },
        { title: '含税单价', dataIndex: 'taxPrice', key: 'taxPrice', width: 120, render: (price: number) => `￥${(price || 0).toFixed(2)}` },
        { title: '金额', dataIndex: 'taxAmount', key: 'taxAmount', width: 120, render: (amount: number) => `￥${(amount || 0).toFixed(2)}` },
        { title: '备注', dataIndex: 'description', key: 'description' },
    ];

    if (loading) return <div style={{ padding: '24px', textAlign: 'center' }}><Spin size="large" tip="加载中..." /></div>;
    if (!deliveryData) return <div style={{ padding: '24px', textAlign: 'center' }}><p>出库单数据不存在</p><Button onClick={() => navigate(-1)}>返回</Button></div>;

    const statusInfo = statusMap[deliveryData.status] || { text: '未知', color: 'default' };

    return (
        <div style={{ padding: '24px' }}>
            <Card title={<Space><Button icon={<ArrowLeftOutlined />} onClick={() => navigate(-1)}>返回</Button><span>销售出库详情</span></Space>}
                extra={deliveryData.status === 0 && <Button type="primary" onClick={() => navigate(`/business/sale/delivery/edit/${id}`)}>编辑</Button>}>
                <Descriptions title="基本信息" bordered column={3}>
                    <Descriptions.Item label="出库单编号">{deliveryData.code || '-'}</Descriptions.Item>
                    <Descriptions.Item label="销售订单编号">{deliveryData.orderCode || '-'}</Descriptions.Item>
                    <Descriptions.Item label="客户">{getCustomerName(deliveryData.customerId)}</Descriptions.Item>
                    <Descriptions.Item label="仓库">{getWarehouseName(deliveryData.scId)}</Descriptions.Item>
                    <Descriptions.Item label="状态"><Tag color={statusInfo.color}>{statusInfo.text}</Tag></Descriptions.Item>
                    <Descriptions.Item label="创建时间">{deliveryData.createTime || '-'}</Descriptions.Item>
                    <Descriptions.Item label="出库数量">{deliveryData.totalNum || totalNum}</Descriptions.Item>
                    <Descriptions.Item label="出库金额">￥{(deliveryData.totalAmount || totalAmount).toFixed(2)}</Descriptions.Item>
                    <Descriptions.Item label="备注">{deliveryData.description || '-'}</Descriptions.Item>
                </Descriptions>
                <div style={{ marginTop: 24 }}>
                    <h3>出库明细</h3>
                    <Table columns={detailColumns} dataSource={details} rowKey={(record) => record.id || record.key} pagination={false}
                        summary={() => (<Table.Summary><Table.Summary.Row>
                            <Table.Summary.Cell index={0}>合计</Table.Summary.Cell><Table.Summary.Cell index={1}>-</Table.Summary.Cell>
                            <Table.Summary.Cell index={2}>-</Table.Summary.Cell><Table.Summary.Cell index={3}>{totalNum}</Table.Summary.Cell>
                            <Table.Summary.Cell index={4}>-</Table.Summary.Cell><Table.Summary.Cell index={5}>￥{totalAmount.toFixed(2)}</Table.Summary.Cell>
                            <Table.Summary.Cell index={6}>-</Table.Summary.Cell>
                        </Table.Summary.Row></Table.Summary>)}
                    />
                </div>
            </Card>
        </div>
    );
};

export default SaleDeliveryDetail;

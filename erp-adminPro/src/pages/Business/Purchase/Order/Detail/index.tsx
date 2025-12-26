import React, { useState, useEffect } from 'react';
import { Descriptions, Card, Table, Tag, Space, Button, message, Spin } from 'antd';
import { ArrowLeftOutlined } from '@ant-design/icons';
import { useNavigate, useParams } from 'react-router-dom';
import { getPurchaseOrderById } from '@/api/business/purchaseOrder';
import { getSupplierList } from '@/api/basedata/supplier';
import { getWarehouseList } from '@/api/basedata/warehouse';
import { getMaterialPage } from '@/api/material/material';

// 订单状态映射
const statusMap: Record<number, { text: string; color: string }> = {
    0: { text: '待审核', color: 'orange' },
    1: { text: '已审核', color: 'green' },
    2: { text: '已拒绝', color: 'red' },
    3: { text: '已完成', color: 'blue' },
    4: { text: '已取消', color: 'default' },
};

const PurchaseOrderDetail: React.FC = () => {
    const navigate = useNavigate();
    const { id } = useParams<{ id: string }>();

    const [loading, setLoading] = useState(true);
    const [orderData, setOrderData] = useState<any>(null);
    const [details, setDetails] = useState<any[]>([]);
    const [suppliers, setSuppliers] = useState<any[]>([]);
    const [warehouses, setWarehouses] = useState<any[]>([]);
    const [materials, setMaterials] = useState<any[]>([]);

    useEffect(() => {
        loadBasicData();
        loadOrderData();
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

    const loadOrderData = async () => {
        setLoading(true);
        try {
            const data: any = await getPurchaseOrderById(Number(id));
            if (data) {
                setOrderData(data.order || data);
                setDetails(data.details || []);
            }
        } catch (error) {
            message.error('加载订单数据失败');
        } finally {
            setLoading(false);
        }
    };

    // 获取供应商名称
    const getSupplierName = (supplierId: number) => {
        const supplier = suppliers.find(s => s.id === supplierId);
        return supplier?.name || supplierId;
    };

    // 获取仓库名称
    const getWarehouseName = (warehouseId: number) => {
        const warehouse = warehouses.find(w => w.id === warehouseId);
        return warehouse?.name || warehouseId;
    };

    // 获取商品名称
    const getMaterialName = (productId: number) => {
        const material = materials.find(m => m.id === productId);
        return material?.name || productId;
    };

    // 计算合计
    const totalNum = details.reduce((sum, d) => sum + (d.orderNum || 0), 0);
    const totalAmount = details.reduce((sum, d) => sum + ((d.orderNum || 0) * (d.taxPrice || 0)), 0);

    const detailColumns = [
        {
            title: '序号',
            key: 'index',
            width: 60,
            render: (_: any, __: any, index: number) => index + 1,
        },
        {
            title: '商品名称',
            dataIndex: 'productId',
            key: 'productId',
            render: (productId: number) => getMaterialName(productId),
        },
        {
            title: '采购数量',
            dataIndex: 'orderNum',
            key: 'orderNum',
            width: 120,
        },
        {
            title: '含税单价',
            dataIndex: 'taxPrice',
            key: 'taxPrice',
            width: 120,
            render: (price: number) => `￥${(price || 0).toFixed(2)}`,
        },
        {
            title: '金额',
            key: 'taxAmount',
            width: 120,
            render: (_: any, record: any) => `￥${((record.orderNum || 0) * (record.taxPrice || 0)).toFixed(2)}`,
        },
        {
            title: '备注',
            dataIndex: 'description',
            key: 'description',
        },
    ];

    if (loading) {
        return (
            <div style={{ padding: '24px', textAlign: 'center' }}>
                <Spin size="large" tip="加载中..." />
            </div>
        );
    }

    if (!orderData) {
        return (
            <div style={{ padding: '24px', textAlign: 'center' }}>
                <p>订单数据不存在</p>
                <Button onClick={() => navigate(-1)}>返回</Button>
            </div>
        );
    }

    const statusInfo = statusMap[orderData.status] || { text: '未知', color: 'default' };

    return (
        <div style={{ padding: '24px' }}>
            <Card
                title={
                    <Space>
                        <Button icon={<ArrowLeftOutlined />} onClick={() => navigate(-1)}>返回</Button>
                        <span>采购订单详情</span>
                    </Space>
                }
                extra={
                    <Button type="primary" onClick={() => navigate(`/business/purchase/order/edit/${id}`)}>
                        编辑
                    </Button>
                }
            >
                {/* 基本信息 */}
                <Descriptions title="基本信息" bordered column={3}>
                    <Descriptions.Item label="订单编号">{orderData.code || '-'}</Descriptions.Item>
                    <Descriptions.Item label="供应商">{getSupplierName(orderData.supplierId)}</Descriptions.Item>
                    <Descriptions.Item label="仓库">{getWarehouseName(orderData.scId)}</Descriptions.Item>
                    <Descriptions.Item label="预计到货日期">{orderData.expectArriveDate || '-'}</Descriptions.Item>
                    <Descriptions.Item label="订单状态">
                        <Tag color={statusInfo.color}>{statusInfo.text}</Tag>
                    </Descriptions.Item>
                    <Descriptions.Item label="创建时间">{orderData.createTime || '-'}</Descriptions.Item>
                    <Descriptions.Item label="采购数量">{orderData.totalNum || totalNum}</Descriptions.Item>
                    <Descriptions.Item label="采购金额">￥{(orderData.totalAmount || totalAmount).toFixed(2)}</Descriptions.Item>
                    <Descriptions.Item label="备注">{orderData.description || '-'}</Descriptions.Item>
                </Descriptions>

                {/* 商品明细 */}
                <div style={{ marginTop: 24 }}>
                    <h3>商品明细</h3>
                    <Table
                        columns={detailColumns}
                        dataSource={details}
                        rowKey={(record) => record.id || record.key}
                        pagination={false}
                        summary={() => (
                            <Table.Summary>
                                <Table.Summary.Row>
                                    <Table.Summary.Cell index={0}>合计</Table.Summary.Cell>
                                    <Table.Summary.Cell index={1}>-</Table.Summary.Cell>
                                    <Table.Summary.Cell index={2}>{totalNum}</Table.Summary.Cell>
                                    <Table.Summary.Cell index={3}>-</Table.Summary.Cell>
                                    <Table.Summary.Cell index={4}>￥{totalAmount.toFixed(2)}</Table.Summary.Cell>
                                    <Table.Summary.Cell index={5}>-</Table.Summary.Cell>
                                </Table.Summary.Row>
                            </Table.Summary>
                        )}
                    />
                </div>

                {/* 审核信息（如果有） */}
                {(orderData.status === 1 || orderData.status === 2) && (
                    <div style={{ marginTop: 24 }}>
                        <Descriptions title="审核信息" bordered column={2}>
                            <Descriptions.Item label="审核人">{orderData.approveBy || '-'}</Descriptions.Item>
                            <Descriptions.Item label="审核时间">{orderData.approveTime || '-'}</Descriptions.Item>
                            {orderData.status === 2 && (
                                <Descriptions.Item label="拒绝原因" span={2}>{orderData.refuseReason || '-'}</Descriptions.Item>
                            )}
                        </Descriptions>
                    </div>
                )}
            </Card>
        </div>
    );
};

export default PurchaseOrderDetail;

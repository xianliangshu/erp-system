import React, { useState, useEffect } from 'react';
import { Descriptions, Card, Table, Tag, Space, Button, message, Spin } from 'antd';
import { ArrowLeftOutlined } from '@ant-design/icons';
import { useNavigate, useParams } from 'react-router-dom';
import { getPurchaseReturnById } from '@/api/business/purchaseReturn';
import { getSupplierList } from '@/api/basedata/supplier';
import { getWarehouseList } from '@/api/basedata/warehouse';
import { getMaterialPage } from '@/api/material/material';

// 退货单状态映射
const statusMap: Record<number, { text: string; color: string }> = {
    0: { text: '待确认', color: 'orange' },
    1: { text: '已确认', color: 'green' },
};

const PurchaseReturnDetail: React.FC = () => {
    const navigate = useNavigate();
    const { id } = useParams<{ id: string }>();

    const [loading, setLoading] = useState(true);
    const [returnData, setReturnData] = useState<any>(null);
    const [details, setDetails] = useState<any[]>([]);
    const [suppliers, setSuppliers] = useState<any[]>([]);
    const [warehouses, setWarehouses] = useState<any[]>([]);
    const [materials, setMaterials] = useState<any[]>([]);

    useEffect(() => {
        loadBasicData();
        loadReturnData();
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

    const loadReturnData = async () => {
        setLoading(true);
        try {
            const data: any = await getPurchaseReturnById(Number(id));
            if (data) {
                setReturnData(data.purchaseReturn || data);
                setDetails(data.details || []);
            }
        } catch (error) {
            message.error('加载退货单数据失败');
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
    const totalNum = details.reduce((sum, d) => sum + (d.returnNum || 0), 0);
    const totalAmount = details.reduce((sum, d) => sum + ((d.returnNum || 0) * (d.taxPrice || 0)), 0);

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
            title: '原收货数量',
            dataIndex: 'receiveNum',
            key: 'receiveNum',
            width: 120,
        },
        {
            title: '退货数量',
            dataIndex: 'returnNum',
            key: 'returnNum',
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
            dataIndex: 'taxAmount',
            key: 'taxAmount',
            width: 120,
            render: (amount: number) => `￥${(amount || 0).toFixed(2)}`,
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

    if (!returnData) {
        return (
            <div style={{ padding: '24px', textAlign: 'center' }}>
                <p>退货单数据不存在</p>
                <Button onClick={() => navigate(-1)}>返回</Button>
            </div>
        );
    }

    const statusInfo = statusMap[returnData.status] || { text: '未知', color: 'default' };

    return (
        <div style={{ padding: '24px' }}>
            <Card
                title={
                    <Space>
                        <Button icon={<ArrowLeftOutlined />} onClick={() => navigate(-1)}>返回</Button>
                        <span>采购退货详情</span>
                    </Space>
                }
                extra={
                    returnData.status === 0 && (
                        <Button type="primary" onClick={() => navigate(`/business/purchase/return/edit/${id}`)}>
                            编辑
                        </Button>
                    )
                }
            >
                {/* 基本信息 */}
                <Descriptions title="基本信息" bordered column={3}>
                    <Descriptions.Item label="退货单编号">{returnData.code || '-'}</Descriptions.Item>
                    <Descriptions.Item label="收货单编号">{returnData.receiptCode || '-'}</Descriptions.Item>
                    <Descriptions.Item label="供应商">{getSupplierName(returnData.supplierId)}</Descriptions.Item>
                    <Descriptions.Item label="仓库">{getWarehouseName(returnData.scId)}</Descriptions.Item>
                    <Descriptions.Item label="退货单状态">
                        <Tag color={statusInfo.color}>{statusInfo.text}</Tag>
                    </Descriptions.Item>
                    <Descriptions.Item label="创建时间">{returnData.createTime || '-'}</Descriptions.Item>
                    <Descriptions.Item label="退货数量">{returnData.totalNum || totalNum}</Descriptions.Item>
                    <Descriptions.Item label="退货金额">￥{(returnData.totalAmount || totalAmount).toFixed(2)}</Descriptions.Item>
                    <Descriptions.Item label="备注">{returnData.description || '-'}</Descriptions.Item>
                </Descriptions>

                {/* 退货明细 */}
                <div style={{ marginTop: 24 }}>
                    <h3>退货明细</h3>
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

export default PurchaseReturnDetail;

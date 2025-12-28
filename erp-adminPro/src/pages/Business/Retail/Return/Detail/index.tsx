import React, { useState, useEffect } from 'react';
import { Descriptions, Card, Table, Tag, Space, Button, message, Spin } from 'antd';
import { ArrowLeftOutlined } from '@ant-design/icons';
import { useNavigate, useParams } from 'react-router-dom';
import { getRetailReturnById } from '@/api/business/retail';
import { getWarehouseList } from '@/api/basedata/warehouse';
import { getCustomerList } from '@/api/basedata/customer';
import { getMaterialList } from '@/api/material/material';

const statusMap: Record<number, { text: string; color: string }> = { 0: { text: '待审核', color: 'orange' }, 1: { text: '已审核', color: 'green' }, 2: { text: '已拒绝', color: 'red' } };

const RetailReturnDetail: React.FC = () => {
    const navigate = useNavigate();
    const { id } = useParams<{ id: string }>();
    const [loading, setLoading] = useState(true);
    const [data, setData] = useState<any>(null);
    const [warehouses, setWarehouses] = useState<any[]>([]);
    const [customers, setCustomers] = useState<any[]>([]);
    const [materials, setMaterials] = useState<any[]>([]);

    const loadData = async () => {
        setLoading(true);
        try {
            const [whRes, custRes, matRes, detailRes]: any[] = await Promise.all([getWarehouseList(), getCustomerList(), getMaterialList(), getRetailReturnById(Number(id))]);
            setWarehouses(whRes || []);
            setCustomers(custRes || []);
            setMaterials(matRes || []);
            setData(detailRes);
        } catch (e) { message.error('加载详情失败'); }
        finally { setLoading(false); }
    };

    useEffect(() => { loadData(); }, [id]);

    const columns = [
        { title: '商品', dataIndex: 'productId', key: 'productId', render: (id: number) => materials.find(m => m.id === id)?.name || id },
        { title: '数量', dataIndex: 'returnNum', key: 'returnNum' },
        { title: '含税单价', dataIndex: 'taxPrice', key: 'taxPrice', render: (val: number) => `￥${val.toFixed(2)}` },
        { title: '金额', dataIndex: 'taxAmount', key: 'taxAmount', render: (val: number) => `￥${val.toFixed(2)}` },
        { title: '备注', dataIndex: 'description', key: 'description' },
    ];

    if (loading) return <div style={{ padding: '24px', textAlign: 'center' }}><Spin size="large" /></div>;
    if (!data) return <div style={{ padding: '24px', textAlign: 'center' }}>数据不存在</div>;

    const { sheet, details } = data;

    return (
        <div style={{ padding: '24px' }}>
            <Card title={<Space><Button icon={<ArrowLeftOutlined />} onClick={() => navigate(-1)}>返回</Button><span>零售退货单详情</span></Space>} extra={sheet.status === 0 && <Button type="primary" onClick={() => navigate(`/business/retail/return/edit/${id}`)}>编辑</Button>}>
                <Descriptions bordered column={3}>
                    <Descriptions.Item label="单据编号">{sheet.code}</Descriptions.Item>
                    <Descriptions.Item label="仓库">{warehouses.find(w => w.id === sheet.scId)?.name || sheet.scId}</Descriptions.Item>
                    <Descriptions.Item label="客户">{customers.find(c => c.id === sheet.customerId)?.name || '-'}</Descriptions.Item>
                    <Descriptions.Item label="总数量">{sheet.totalNum}</Descriptions.Item>
                    <Descriptions.Item label="总金额">￥{sheet.totalAmount.toFixed(2)}</Descriptions.Item>
                    <Descriptions.Item label="状态"><Tag color={statusMap[sheet.status]?.color}>{statusMap[sheet.status]?.text}</Tag></Descriptions.Item>
                    <Descriptions.Item label="创建人">{sheet.createBy}</Descriptions.Item>
                    <Descriptions.Item label="创建时间">{sheet.createTime}</Descriptions.Item>
                    <Descriptions.Item label="备注" span={2}>{sheet.description}</Descriptions.Item>
                </Descriptions>
                <h3 style={{ marginTop: 24 }}>明细信息</h3>
                <Table columns={columns} dataSource={details} rowKey="id" pagination={false} />
            </Card>
        </div>
    );
};

export default RetailReturnDetail;

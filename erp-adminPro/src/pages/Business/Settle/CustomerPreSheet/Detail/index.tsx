import React, { useState, useEffect } from 'react';
import { Card, Descriptions, Table, Tag, Button, Spin } from 'antd';
import { ArrowLeftOutlined } from '@ant-design/icons';
import { useNavigate, useParams } from 'react-router-dom';
import { getCustomerPreSheetById } from '@/api/business/settle';
import { getCustomerList } from '@/api/basedata/customer';
import { getSettleInOutItemList } from '@/api/business/settle';

const statusMap: Record<number, { color: string; text: string }> = { 0: { color: 'processing', text: '待审核' }, 1: { color: 'success', text: '已审核' }, 2: { color: 'error', text: '已拒绝' } };

const CustomerPreSheetDetail: React.FC = () => {
    const navigate = useNavigate();
    const { id } = useParams<{ id: string }>();
    const [loading, setLoading] = useState(false);
    const [sheet, setSheet] = useState<any>(null);
    const [details, setDetails] = useState<any[]>([]);
    const [customers, setCustomers] = useState<any[]>([]);
    const [items, setItems] = useState<any[]>([]);

    const loadOptions = async () => {
        try { const res: any = await getCustomerList(); setCustomers(res || []); } catch (e) { console.error(e); }
        try { const res: any = await getSettleInOutItemList(); setItems(res || []); } catch (e) { console.error(e); }
    };
    const loadData = async () => { if (!id) return; setLoading(true); try { const res: any = await getCustomerPreSheetById(Number(id)); setSheet(res?.sheet || null); setDetails(res?.details || []); } catch (e) { console.error(e); } finally { setLoading(false); } };

    useEffect(() => { loadOptions(); }, []);
    useEffect(() => { loadData(); }, [id]);

    const getCustomerName = (customerId: number) => customers.find(c => c.id === customerId)?.name || customerId;
    const getItemName = (itemId: number) => items.find(i => i.id === itemId)?.name || itemId;

    const detailColumns = [
        { title: '序号', key: 'index', width: 60, render: (_: any, __: any, index: number) => index + 1 },
        { title: '收支项目', dataIndex: 'itemId', render: (val: number) => getItemName(val) },
        { title: '金额', dataIndex: 'amount', render: (val: number) => `￥${val?.toFixed(2) || '0.00'}` },
    ];

    return (
        <div style={{ padding: '24px' }}>
            <Card title="客户预收款单详情" extra={<Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/business/settle/customer/pre')}>返回</Button>}>
                <Spin spinning={loading}>
                    {sheet && (
                        <Descriptions bordered column={2}>
                            <Descriptions.Item label="单据编号">{sheet.code}</Descriptions.Item>
                            <Descriptions.Item label="客户">{getCustomerName(sheet.customerId)}</Descriptions.Item>
                            <Descriptions.Item label="金额">￥{sheet.totalAmount?.toFixed(2) || '0.00'}</Descriptions.Item>
                            <Descriptions.Item label="状态"><Tag color={statusMap[sheet.status]?.color}>{statusMap[sheet.status]?.text}</Tag></Descriptions.Item>
                            {sheet.status === 2 && <Descriptions.Item label="拒绝原因" span={2}>{sheet.refuseReason}</Descriptions.Item>}
                            <Descriptions.Item label="备注" span={2}>{sheet.description || '-'}</Descriptions.Item>
                        </Descriptions>
                    )}
                    <Card title="预收明细" size="small" style={{ marginTop: 24 }}>
                        <Table columns={detailColumns} dataSource={details} rowKey="id" pagination={false} size="small" />
                    </Card>
                </Spin>
            </Card>
        </div>
    );
};

export default CustomerPreSheetDetail;

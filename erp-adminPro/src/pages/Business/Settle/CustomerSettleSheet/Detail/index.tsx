import React, { useState, useEffect } from 'react';
import { Card, Descriptions, Tag, Button, Spin } from 'antd';
import { ArrowLeftOutlined } from '@ant-design/icons';
import { useNavigate, useParams } from 'react-router-dom';
import { getCustomerSettleSheetById } from '@/api/business/settle';
import { getCustomerList } from '@/api/basedata/customer';

const statusMap: Record<number, { color: string; text: string }> = { 0: { color: 'processing', text: '待审核' }, 1: { color: 'success', text: '已审核' }, 2: { color: 'error', text: '已拒绝' } };

const CustomerSettleSheetDetail: React.FC = () => {
    const navigate = useNavigate();
    const { id } = useParams<{ id: string }>();
    const [loading, setLoading] = useState(false);
    const [sheet, setSheet] = useState<any>(null);
    const [customers, setCustomers] = useState<any[]>([]);

    const loadOptions = async () => { try { const res: any = await getCustomerList(); setCustomers(res || []); } catch (e) { console.error(e); } };
    const loadData = async () => { if (!id) return; setLoading(true); try { const res: any = await getCustomerSettleSheetById(Number(id)); setSheet(res?.sheet || null); } catch (e) { console.error(e); } finally { setLoading(false); } };

    useEffect(() => { loadOptions(); }, []);
    useEffect(() => { loadData(); }, [id]);

    const getCustomerName = (customerId: number) => customers.find(c => c.id === customerId)?.name || customerId;

    return (
        <div style={{ padding: '24px' }}>
            <Card title="客户结算单详情" extra={<Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/business/settle/customer/sheet')}>返回</Button>}>
                <Spin spinning={loading}>
                    {sheet && (
                        <Descriptions bordered column={2}>
                            <Descriptions.Item label="单据编号">{sheet.code}</Descriptions.Item>
                            <Descriptions.Item label="客户">{getCustomerName(sheet.customerId)}</Descriptions.Item>
                            <Descriptions.Item label="起始日期">{sheet.startDate || '-'}</Descriptions.Item>
                            <Descriptions.Item label="截止日期">{sheet.endDate || '-'}</Descriptions.Item>
                            <Descriptions.Item label="应付金额">￥{sheet.totalAmount?.toFixed(2) || '0.00'}</Descriptions.Item>
                            <Descriptions.Item label="优惠金额">￥{sheet.totalDiscountAmount?.toFixed(2) || '0.00'}</Descriptions.Item>
                            <Descriptions.Item label="状态"><Tag color={statusMap[sheet.status]?.color}>{statusMap[sheet.status]?.text}</Tag></Descriptions.Item>
                            {sheet.status === 2 && <Descriptions.Item label="拒绝原因">{sheet.refuseReason}</Descriptions.Item>}
                            <Descriptions.Item label="备注" span={2}>{sheet.description || '-'}</Descriptions.Item>
                        </Descriptions>
                    )}
                </Spin>
            </Card>
        </div>
    );
};

export default CustomerSettleSheetDetail;

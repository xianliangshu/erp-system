import React, { useState, useEffect } from 'react';
import { Card, Descriptions, Table, Tag, Button, Spin } from 'antd';
import { ArrowLeftOutlined } from '@ant-design/icons';
import { useNavigate, useParams } from 'react-router-dom';
import { getCheckSheetById } from '@/api/business/settle';
import { getSupplierList } from '@/api/basedata/supplier';

const statusMap: Record<number, { color: string; text: string }> = { 0: { color: 'processing', text: '待审核' }, 1: { color: 'success', text: '已审核' }, 2: { color: 'error', text: '已拒绝' } };
const bizTypeMap: Record<number, string> = { 1: '采购入库', 2: '采购退货', 3: '费用单', 4: '预付款' };

const CheckSheetDetail: React.FC = () => {
    const navigate = useNavigate();
    const { id } = useParams<{ id: string }>();
    const [loading, setLoading] = useState(false);
    const [sheet, setSheet] = useState<any>(null);
    const [details, setDetails] = useState<any[]>([]);
    const [suppliers, setSuppliers] = useState<any[]>([]);

    const loadOptions = async () => { try { const res: any = await getSupplierList(); setSuppliers(res || []); } catch (e) { console.error(e); } };
    const loadData = async () => { if (!id) return; setLoading(true); try { const res: any = await getCheckSheetById(Number(id)); setSheet(res?.sheet || null); setDetails(res?.details || []); } catch (e) { console.error(e); } finally { setLoading(false); } };

    useEffect(() => { loadOptions(); }, []);
    useEffect(() => { loadData(); }, [id]);

    const getSupplierName = (supplierId: number) => suppliers.find(s => s.id === supplierId)?.name || supplierId;

    const detailColumns = [
        { title: '序号', key: 'index', width: 60, render: (_: any, __: any, index: number) => index + 1 },
        { title: '业务类型', dataIndex: 'bizType', render: (val: number) => bizTypeMap[val] || val },
        { title: '单据编号', dataIndex: 'bizCode' },
        { title: '应付金额', dataIndex: 'payAmount', render: (val: number) => `￥${val?.toFixed(2) || '0.00'}` },
        { title: '备注', dataIndex: 'description' },
    ];

    return (
        <div style={{ padding: '24px' }}>
            <Card title="供应商对账单详情" extra={<Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/business/settle/check')}>返回</Button>}>
                <Spin spinning={loading}>
                    {sheet && (
                        <Descriptions bordered column={2}>
                            <Descriptions.Item label="单据编号">{sheet.code}</Descriptions.Item>
                            <Descriptions.Item label="供应商">{getSupplierName(sheet.supplierId)}</Descriptions.Item>
                            <Descriptions.Item label="对账期间">{sheet.startDate} ~ {sheet.endDate}</Descriptions.Item>
                            <Descriptions.Item label="应付金额">￥{sheet.totalAmount?.toFixed(2) || '0.00'}</Descriptions.Item>
                            <Descriptions.Item label="优惠金额">￥{sheet.totalDiscountAmount?.toFixed(2) || '0.00'}</Descriptions.Item>
                            <Descriptions.Item label="状态"><Tag color={statusMap[sheet.status]?.color}>{statusMap[sheet.status]?.text}</Tag></Descriptions.Item>
                            {sheet.status === 2 && <Descriptions.Item label="拒绝原因" span={2}>{sheet.refuseReason}</Descriptions.Item>}
                            <Descriptions.Item label="备注" span={2}>{sheet.description || '-'}</Descriptions.Item>
                        </Descriptions>
                    )}
                    <Card title="对账明细" size="small" style={{ marginTop: 24 }}>
                        <Table columns={detailColumns} dataSource={details} rowKey="id" pagination={false} size="small" />
                    </Card>
                </Spin>
            </Card>
        </div>
    );
};

export default CheckSheetDetail;

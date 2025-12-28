import React, { useState, useEffect } from 'react';
import { Card, Row, Col, Statistic, Table, DatePicker, Space, Button, Spin, message } from 'antd';
import { SearchOutlined, ReloadOutlined, ShoppingCartOutlined } from '@ant-design/icons';
import { getPurchaseStats } from '@/api/business/chart';
import dayjs from 'dayjs';

const { RangePicker } = DatePicker;

const PurchaseChart: React.FC = () => {
    const [loading, setLoading] = useState(false);
    const [data, setData] = useState<any>(null);
    const [dateRange, setDateRange] = useState<[dayjs.Dayjs, dayjs.Dayjs]>([
        dayjs().subtract(30, 'day'),
        dayjs()
    ]);

    const loadData = async () => {
        setLoading(true);
        try {
            const res: any = await getPurchaseStats({
                startDate: dateRange[0].format('YYYY-MM-DD'),
                endDate: dateRange[1].format('YYYY-MM-DD')
            });
            setData(res);
        } catch (e) {
            message.error('加载数据失败');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => { loadData(); }, []);

    const handleSearch = () => { loadData(); };
    const handleReset = () => {
        setDateRange([dayjs().subtract(30, 'day'), dayjs()]);
        loadData();
    };

    const dailyColumns = [
        { title: '日期', dataIndex: 'date', key: 'date', width: 120 },
        { title: '采购金额', dataIndex: 'amount', key: 'amount', render: (val: number) => `￥${val?.toFixed(2) || '0.00'}` },
        { title: '订单数量', dataIndex: 'count', key: 'count' }
    ];

    const supplierColumns = [
        { title: '排名', key: 'rank', width: 60, render: (_: any, __: any, index: number) => index + 1 },
        { title: '供应商', dataIndex: 'supplierName', key: 'supplierName' },
        { title: '采购金额', dataIndex: 'amount', key: 'amount', render: (val: number) => `￥${val?.toFixed(2) || '0.00'}` },
        { title: '订单数量', dataIndex: 'count', key: 'count' }
    ];

    return (
        <div style={{ padding: '24px' }}>
            <Card title="采购统计">
                <Space style={{ marginBottom: 16 }}>
                    <RangePicker
                        value={dateRange}
                        onChange={(dates) => dates && setDateRange(dates as [dayjs.Dayjs, dayjs.Dayjs])}
                    />
                    <Button type="primary" icon={<SearchOutlined />} onClick={handleSearch}>查询</Button>
                    <Button icon={<ReloadOutlined />} onClick={handleReset}>重置</Button>
                </Space>

                <Spin spinning={loading}>
                    <Row gutter={[16, 16]}>
                        <Col span={12}>
                            <Statistic
                                title="采购总金额"
                                value={data?.totalAmount || 0}
                                precision={2}
                                prefix={<ShoppingCartOutlined />}
                                suffix="元"
                                valueStyle={{ color: '#1890ff', fontSize: 28 }}
                            />
                        </Col>
                        <Col span={12}>
                            <Statistic
                                title="采购订单数"
                                value={data?.totalCount || 0}
                                suffix="笔"
                                valueStyle={{ color: '#1890ff', fontSize: 28 }}
                            />
                        </Col>
                    </Row>

                    <Row gutter={16} style={{ marginTop: 24 }}>
                        <Col span={12}>
                            <Card title="每日采购统计" size="small">
                                <Table
                                    columns={dailyColumns}
                                    dataSource={data?.dailyData || []}
                                    rowKey="date"
                                    size="small"
                                    pagination={{ pageSize: 10 }}
                                    scroll={{ y: 300 }}
                                />
                            </Card>
                        </Col>
                        <Col span={12}>
                            <Card title="供应商采购排名 (Top 10)" size="small">
                                <Table
                                    columns={supplierColumns}
                                    dataSource={data?.supplierRanking || []}
                                    rowKey="supplierId"
                                    size="small"
                                    pagination={false}
                                />
                            </Card>
                        </Col>
                    </Row>
                </Spin>
            </Card>
        </div>
    );
};

export default PurchaseChart;

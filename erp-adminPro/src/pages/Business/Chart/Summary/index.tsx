import React, { useState, useEffect } from 'react';
import { Card, Row, Col, Statistic, DatePicker, Space, Button, Spin, message } from 'antd';
import { SearchOutlined, ReloadOutlined, FundOutlined, ShoppingCartOutlined, RiseOutlined, DatabaseOutlined, DollarOutlined } from '@ant-design/icons';
import { getSummaryReport } from '@/api/business/chart';
import dayjs from 'dayjs';

const { RangePicker } = DatePicker;

const Summary: React.FC = () => {
    const [loading, setLoading] = useState(false);
    const [data, setData] = useState<any>(null);
    const [dateRange, setDateRange] = useState<[dayjs.Dayjs, dayjs.Dayjs]>([
        dayjs().subtract(12, 'month').startOf('month'),
        dayjs()
    ]);

    const loadData = async () => {
        setLoading(true);
        try {
            const res: any = await getSummaryReport({
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
        setDateRange([dayjs().subtract(12, 'month').startOf('month'), dayjs()]);
        loadData();
    };

    const profitRate = data?.salesAmount > 0 ? ((data?.profit / data?.salesAmount) * 100).toFixed(1) : 0;

    return (
        <div style={{ padding: '24px' }}>
            <Card title={<><FundOutlined /> 进销存汇总</>}>
                <Space style={{ marginBottom: 24 }}>
                    <RangePicker
                        value={dateRange}
                        onChange={(dates) => dates && setDateRange(dates as [dayjs.Dayjs, dayjs.Dayjs])}
                    />
                    <Button type="primary" icon={<SearchOutlined />} onClick={handleSearch}>查询</Button>
                    <Button icon={<ReloadOutlined />} onClick={handleReset}>重置</Button>
                </Space>

                <Spin spinning={loading}>
                    <Row gutter={[24, 24]}>
                        <Col span={6}>
                            <Card style={{ background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' }}>
                                <Statistic
                                    title={<span style={{ color: 'rgba(255,255,255,0.85)' }}>采购总额</span>}
                                    value={data?.purchaseAmount || 0}
                                    precision={2}
                                    valueStyle={{ color: '#fff', fontSize: 28 }}
                                    prefix={<ShoppingCartOutlined />}
                                    suffix="元"
                                />
                            </Card>
                        </Col>
                        <Col span={6}>
                            <Card style={{ background: 'linear-gradient(135deg, #11998e 0%, #38ef7d 100%)' }}>
                                <Statistic
                                    title={<span style={{ color: 'rgba(255,255,255,0.85)' }}>销售总额</span>}
                                    value={data?.salesAmount || 0}
                                    precision={2}
                                    valueStyle={{ color: '#fff', fontSize: 28 }}
                                    prefix={<RiseOutlined />}
                                    suffix="元"
                                />
                            </Card>
                        </Col>
                        <Col span={6}>
                            <Card style={{ background: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)' }}>
                                <Statistic
                                    title={<span style={{ color: 'rgba(255,255,255,0.85)' }}>利润总额</span>}
                                    value={data?.profit || 0}
                                    precision={2}
                                    valueStyle={{ color: '#fff', fontSize: 28 }}
                                    prefix={<DollarOutlined />}
                                    suffix="元"
                                />
                            </Card>
                        </Col>
                        <Col span={6}>
                            <Card style={{ background: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)' }}>
                                <Statistic
                                    title={<span style={{ color: 'rgba(255,255,255,0.85)' }}>库存价值</span>}
                                    value={data?.stockValue || 0}
                                    precision={2}
                                    valueStyle={{ color: '#fff', fontSize: 28 }}
                                    prefix={<DatabaseOutlined />}
                                    suffix="元"
                                />
                            </Card>
                        </Col>
                    </Row>

                    <Row gutter={24} style={{ marginTop: 24 }}>
                        <Col span={8}>
                            <Card title="利润率分析" size="small">
                                <Statistic
                                    title="综合利润率"
                                    value={profitRate}
                                    suffix="%"
                                    valueStyle={{ fontSize: 36, color: Number(profitRate) >= 0 ? '#52c41a' : '#f5222d' }}
                                />
                            </Card>
                        </Col>
                        <Col span={8}>
                            <Card title="进销比例" size="small">
                                <Statistic
                                    title="销售/采购比"
                                    value={data?.purchaseAmount > 0 ? (data?.salesAmount / data?.purchaseAmount).toFixed(2) : 0}
                                    valueStyle={{ fontSize: 36, color: '#1890ff' }}
                                />
                            </Card>
                        </Col>
                        <Col span={8}>
                            <Card title="库存周转" size="small">
                                <Statistic
                                    title="库存占比"
                                    value={data?.salesAmount > 0 ? ((data?.stockValue / data?.salesAmount) * 100).toFixed(1) : 0}
                                    suffix="%"
                                    valueStyle={{ fontSize: 36, color: '#722ed1' }}
                                />
                            </Card>
                        </Col>
                    </Row>
                </Spin>
            </Card>
        </div>
    );
};

export default Summary;

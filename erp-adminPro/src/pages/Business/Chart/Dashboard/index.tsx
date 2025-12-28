import React, { useState, useEffect } from 'react';
import { Card, Row, Col, Statistic, Spin, message } from 'antd';
import { ShoppingCartOutlined, RiseOutlined, DatabaseOutlined, AlertOutlined, FileTextOutlined, DollarOutlined } from '@ant-design/icons';
import { getDashboard } from '@/api/business/chart';

const Dashboard: React.FC = () => {
    const [loading, setLoading] = useState(false);
    const [data, setData] = useState<any>(null);

    const loadData = async () => {
        setLoading(true);
        try {
            const res: any = await getDashboard();
            setData(res);
        } catch (e) {
            message.error('加载数据失败');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => { loadData(); }, []);

    return (
        <div style={{ padding: '24px' }}>
            <Spin spinning={loading}>
                <Row gutter={[16, 16]}>
                    {/* 今日统计 */}
                    <Col span={24}>
                        <Card title="📊 今日统计" size="small">
                            <Row gutter={16}>
                                <Col span={6}>
                                    <Statistic
                                        title="今日采购金额"
                                        value={data?.today?.purchaseAmount || 0}
                                        precision={2}
                                        prefix={<ShoppingCartOutlined style={{ color: '#1890ff' }} />}
                                        suffix="元"
                                    />
                                </Col>
                                <Col span={6}>
                                    <Statistic
                                        title="今日采购订单"
                                        value={data?.today?.purchaseCount || 0}
                                        prefix={<FileTextOutlined style={{ color: '#1890ff' }} />}
                                        suffix="笔"
                                    />
                                </Col>
                                <Col span={6}>
                                    <Statistic
                                        title="今日销售金额"
                                        value={data?.today?.salesAmount || 0}
                                        precision={2}
                                        prefix={<RiseOutlined style={{ color: '#52c41a' }} />}
                                        suffix="元"
                                    />
                                </Col>
                                <Col span={6}>
                                    <Statistic
                                        title="今日销售订单"
                                        value={data?.today?.salesCount || 0}
                                        prefix={<FileTextOutlined style={{ color: '#52c41a' }} />}
                                        suffix="笔"
                                    />
                                </Col>
                            </Row>
                        </Card>
                    </Col>

                    {/* 本月统计 */}
                    <Col span={24}>
                        <Card title="📈 本月统计" size="small">
                            <Row gutter={16}>
                                <Col span={4}>
                                    <Statistic
                                        title="本月采购金额"
                                        value={data?.month?.purchaseAmount || 0}
                                        precision={2}
                                        valueStyle={{ color: '#1890ff' }}
                                        suffix="元"
                                    />
                                </Col>
                                <Col span={4}>
                                    <Statistic
                                        title="本月采购订单"
                                        value={data?.month?.purchaseCount || 0}
                                        valueStyle={{ color: '#1890ff' }}
                                        suffix="笔"
                                    />
                                </Col>
                                <Col span={4}>
                                    <Statistic
                                        title="本月销售金额"
                                        value={data?.month?.salesAmount || 0}
                                        precision={2}
                                        valueStyle={{ color: '#52c41a' }}
                                        suffix="元"
                                    />
                                </Col>
                                <Col span={4}>
                                    <Statistic
                                        title="本月销售订单"
                                        value={data?.month?.salesCount || 0}
                                        valueStyle={{ color: '#52c41a' }}
                                        suffix="笔"
                                    />
                                </Col>
                                <Col span={4}>
                                    <Statistic
                                        title="本月利润"
                                        value={data?.month?.profit || 0}
                                        precision={2}
                                        valueStyle={{ color: data?.month?.profit >= 0 ? '#52c41a' : '#f5222d' }}
                                        prefix={<DollarOutlined />}
                                        suffix="元"
                                    />
                                </Col>
                                <Col span={4}>
                                    <Statistic
                                        title="利润率"
                                        value={data?.month?.salesAmount > 0 ? ((data?.month?.profit / data?.month?.salesAmount) * 100).toFixed(1) : 0}
                                        valueStyle={{ color: '#722ed1' }}
                                        suffix="%"
                                    />
                                </Col>
                            </Row>
                        </Card>
                    </Col>

                    {/* 预警信息 */}
                    <Col span={12}>
                        <Card title="⚠️ 库存预警" size="small">
                            <Statistic
                                title="低库存商品数量"
                                value={data?.lowStockCount || 0}
                                valueStyle={{ color: data?.lowStockCount > 0 ? '#f5222d' : '#52c41a' }}
                                prefix={<AlertOutlined />}
                                suffix="个"
                            />
                        </Card>
                    </Col>
                    <Col span={12}>
                        <Card title="📋 待处理订单" size="small">
                            <Statistic
                                title="待审核订单数量"
                                value={data?.pendingOrders || 0}
                                valueStyle={{ color: data?.pendingOrders > 0 ? '#faad14' : '#52c41a' }}
                                prefix={<FileTextOutlined />}
                                suffix="笔"
                            />
                        </Card>
                    </Col>
                </Row>
            </Spin>
        </div>
    );
};

export default Dashboard;

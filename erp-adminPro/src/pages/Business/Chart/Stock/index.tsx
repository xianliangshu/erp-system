import React, { useState, useEffect } from 'react';
import { Card, Row, Col, Statistic, Table, Select, Space, Button, Spin, message, Tag } from 'antd';
import { ReloadOutlined, DatabaseOutlined, AlertOutlined } from '@ant-design/icons';
import { getStockReport } from '@/api/business/chart';
import { getWarehouseList } from '@/api/basedata/warehouse';

const StockReport: React.FC = () => {
    const [loading, setLoading] = useState(false);
    const [data, setData] = useState<any>(null);
    const [warehouseId, setWarehouseId] = useState<number | undefined>(undefined);
    const [warehouses, setWarehouses] = useState<any[]>([]);

    const loadWarehouses = async () => {
        try {
            const res: any = await getWarehouseList();
            setWarehouses(res || []);
        } catch (e) {
            console.error(e);
        }
    };

    const loadData = async () => {
        setLoading(true);
        try {
            const res: any = await getStockReport({ warehouseId });
            setData(res);
        } catch (e) {
            message.error('加载数据失败');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => { loadWarehouses(); loadData(); }, []);

    const handleSearch = () => { loadData(); };
    const handleReset = () => { setWarehouseId(undefined); loadData(); };

    const warehouseColumns = [
        { title: '仓库', dataIndex: 'warehouseName', key: 'warehouseName' },
        { title: '库存价值', dataIndex: 'value', key: 'value', render: (val: number) => `￥${val?.toFixed(2) || '0.00'}` },
        { title: '商品种类', dataIndex: 'itemCount', key: 'itemCount' }
    ];

    const lowStockColumns = [
        { title: '物料编码', dataIndex: 'materialCode', key: 'materialCode' },
        { title: '物料名称', dataIndex: 'materialName', key: 'materialName' },
        { title: '当前库存', dataIndex: 'currentStock', key: 'currentStock', render: (val: number) => <Tag color={val < 5 ? 'red' : 'orange'}>{val}</Tag> },
        { title: '最低库存', dataIndex: 'minStock', key: 'minStock' }
    ];

    return (
        <div style={{ padding: '24px' }}>
            <Card title="库存报表">
                <Space style={{ marginBottom: 16 }}>
                    <Select
                        placeholder="选择仓库"
                        style={{ width: 200 }}
                        value={warehouseId}
                        onChange={setWarehouseId}
                        allowClear
                    >
                        {warehouses.map(w => <Select.Option key={w.id} value={w.id}>{w.name}</Select.Option>)}
                    </Select>
                    <Button type="primary" onClick={handleSearch}>查询</Button>
                    <Button icon={<ReloadOutlined />} onClick={handleReset}>重置</Button>
                </Space>

                <Spin spinning={loading}>
                    <Row gutter={[16, 16]}>
                        <Col span={8}>
                            <Statistic
                                title="库存总价值"
                                value={data?.totalValue || 0}
                                precision={2}
                                prefix={<DatabaseOutlined />}
                                suffix="元"
                                valueStyle={{ color: '#1890ff', fontSize: 28 }}
                            />
                        </Col>
                        <Col span={8}>
                            <Statistic
                                title="商品种类数"
                                value={data?.totalItems || 0}
                                suffix="种"
                                valueStyle={{ fontSize: 28 }}
                            />
                        </Col>
                        <Col span={8}>
                            <Statistic
                                title="低库存预警"
                                value={data?.lowStockCount || 0}
                                prefix={<AlertOutlined />}
                                suffix="项"
                                valueStyle={{ color: data?.lowStockCount > 0 ? '#f5222d' : '#52c41a', fontSize: 28 }}
                            />
                        </Col>
                    </Row>

                    <Row gutter={16} style={{ marginTop: 24 }}>
                        <Col span={12}>
                            <Card title="仓库库存分布" size="small">
                                <Table
                                    columns={warehouseColumns}
                                    dataSource={data?.warehouseStats || []}
                                    rowKey="warehouseId"
                                    size="small"
                                    pagination={false}
                                />
                            </Card>
                        </Col>
                        <Col span={12}>
                            <Card title="低库存商品" size="small" extra={<Tag color="red">{data?.lowStockCount || 0} 项</Tag>}>
                                <Table
                                    columns={lowStockColumns}
                                    dataSource={data?.lowStockItems || []}
                                    rowKey="materialId"
                                    size="small"
                                    pagination={{ pageSize: 10 }}
                                />
                            </Card>
                        </Col>
                    </Row>
                </Spin>
            </Card>
        </div>
    );
};

export default StockReport;

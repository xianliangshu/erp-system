import React, { useState, useEffect } from 'react';
import { Table, Card, Space, Input, Select, Button, message, Tag } from 'antd';
import { SearchOutlined, ReloadOutlined } from '@ant-design/icons';
import { getProductStockPage } from '@/api/business/stock';
import { getWarehouseList } from '@/api/basedata/warehouse';

const { Search } = Input;

const InventoryQuery: React.FC = () => {
    const [loading, setLoading] = useState(false);
    const [dataSource, setDataSource] = useState<any[]>([]);
    const [total, setTotal] = useState(0);
    const [current, setCurrent] = useState(1);
    const [pageSize, setPageSize] = useState(10);

    // 搜索条件
    const [searchScId, setSearchScId] = useState<number | undefined>(undefined);
    const [searchProductName, setSearchProductName] = useState('');
    const [warehouses, setWarehouses] = useState<any[]>([]);

    // 加载仓库列表
    const loadWarehouses = async () => {
        try {
            const data = await getWarehouseList();
            setWarehouses(data);
        } catch (error) {
            console.error('加载仓库失败', error);
        }
    };

    // 加载库存数据
    const loadData = async () => {
        setLoading(true);
        try {
            const params = {
                current,
                size: pageSize,
                scId: searchScId,
                productName: searchProductName || undefined,
            };
            const data = await getProductStockPage(params);
            setDataSource(data.records);
            setTotal(data.total);
        } catch (error) {
            message.error('加载库存数据失败');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadWarehouses();
    }, []);

    useEffect(() => {
        loadData();
    }, [current, pageSize]);

    const handleSearch = () => {
        setCurrent(1);
        loadData();
    };

    const handleReset = () => {
        setSearchScId(undefined);
        setSearchProductName('');
        setCurrent(1);
        loadData();
    };

    const columns = [
        {
            title: '仓库',
            dataIndex: 'scName',
            key: 'scName',
        },
        {
            title: '商品编号',
            dataIndex: 'productCode',
            key: 'productCode',
        },
        {
            title: '商品名称',
            dataIndex: 'productName',
            key: 'productName',
        },
        {
            title: '规格',
            dataIndex: 'productSpec',
            key: 'productSpec',
        },
        {
            title: '单位',
            dataIndex: 'unitName',
            key: 'unitName',
        },
        {
            title: '库存数量',
            dataIndex: 'stockNum',
            key: 'stockNum',
            render: (num: number) => (
                <span style={{ fontWeight: 'bold', color: num > 0 ? '#52c41a' : '#ff4d4f' }}>
                    {num}
                </span>
            ),
        },
        {
            title: '含税成本价',
            dataIndex: 'taxPrice',
            key: 'taxPrice',
            render: (price: number) => `￥${price.toFixed(2)}`,
        },
        {
            title: '库存金额',
            dataIndex: 'taxAmount',
            key: 'taxAmount',
            render: (amount: number) => `￥${amount.toFixed(2)}`,
        },
    ];

    return (
        <div style={{ padding: '24px' }}>
            <Card title="库存查询">
                <Space style={{ marginBottom: 16 }} wrap>
                    <Select
                        placeholder="选择仓库"
                        style={{ width: 200 }}
                        value={searchScId}
                        onChange={setSearchScId}
                        allowClear
                    >
                        {warehouses.map(w => (
                            <Select.Option key={w.id} value={w.id}>{w.name}</Select.Option>
                        ))}
                    </Select>
                    <Input
                        placeholder="商品名称/编号"
                        style={{ width: 200 }}
                        value={searchProductName}
                        onChange={e => setSearchProductName(e.target.value)}
                        onPressEnter={handleSearch}
                    />
                    <Button type="primary" icon={<SearchOutlined />} onClick={handleSearch}>
                        查询
                    </Button>
                    <Button icon={<ReloadOutlined />} onClick={handleReset}>
                        重置
                    </Button>
                </Space>

                <Table
                    loading={loading}
                    columns={columns}
                    dataSource={dataSource}
                    rowKey="id"
                    pagination={{
                        current,
                        pageSize,
                        total,
                        showSizeChanger: true,
                        showTotal: (total) => `共 ${total} 条`,
                        onChange: (page, size) => {
                            setCurrent(page);
                            setPageSize(size);
                        },
                    }}
                />
            </Card>
        </div>
    );
};

export default InventoryQuery;

import React, { useState, useEffect } from 'react';
import { Table, Card, Space, Input, Select, Button, message, DatePicker, Tag } from 'antd';
import { SearchOutlined, ReloadOutlined } from '@ant-design/icons';
import type { Dayjs } from 'dayjs';
import { getProductStockLogPage, getStockBizTypes, type StockLog, type BizTypeOption } from '@/api/business/stockLog';
import { getWarehouseList } from '@/api/basedata/warehouse';

const { RangePicker } = DatePicker;

const StockLogList: React.FC = () => {
    const [loading, setLoading] = useState(false);
    const [dataSource, setDataSource] = useState<StockLog[]>([]);
    const [total, setTotal] = useState(0);
    // 分页与查询条件：唯一的取数依据，搜索/重置/翻页都只改它
    const [query, setQuery] = useState<any>({ current: 1, size: 10 });

    // 搜索条件
    const [searchScId, setSearchScId] = useState<number | undefined>(undefined);
    const [searchProductName, setSearchProductName] = useState('');
    const [searchBizType, setSearchBizType] = useState<number | undefined>(undefined);
    const [dateRange, setDateRange] = useState<[Dayjs | null, Dayjs | null] | null>(null);

    const [warehouses, setWarehouses] = useState<any[]>([]);
    const [bizTypes, setBizTypes] = useState<BizTypeOption[]>([]);

    // 加载仓库列表
    const loadWarehouses = async () => {
        try {
            const data: any = await getWarehouseList();
            setWarehouses(Array.isArray(data) ? data : []);
        } catch (error) {
            console.error('加载仓库失败', error);
        }
    };

    // 加载业务类型字典
    const loadBizTypes = async () => {
        try {
            const data = await getStockBizTypes();
            setBizTypes(data || []);
        } catch (error) {
            console.error('加载业务类型失败', error);
        }
    };

    // 加载流水数据
    const loadData = async () => {
        setLoading(true);
        try {
            const params = {
                ...query,
                scId: searchScId,
                productName: searchProductName || undefined,
                bizType: searchBizType,
                startDate: dateRange?.[0] ? dateRange[0].format('YYYY-MM-DD') : undefined,
                endDate: dateRange?.[1] ? dateRange[1].format('YYYY-MM-DD') : undefined,
            };
            const data = await getProductStockLogPage(params);
            setDataSource(data.records || []);
            setTotal(data.total || 0);
        } catch (error) {
            message.error('加载库存流水失败');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadWarehouses();
        loadBizTypes();
    }, []);

    useEffect(() => {
        loadData();
    }, [query]);

    const handleSearch = () => {
        setQuery((q: any) => ({ ...q, current: 1 }));
    };

    const handleReset = () => {
        setSearchScId(undefined);
        setSearchProductName('');
        setSearchBizType(undefined);
        setDateRange(null);
        setQuery((q: any) => ({ ...q, current: 1 }));
    };

    const columns = [
        {
            title: '仓库',
            dataIndex: 'scName',
            key: 'scName',
            width: 120,
        },
        {
            title: '商品编号',
            dataIndex: 'productCode',
            key: 'productCode',
            width: 130,
        },
        {
            title: '商品名称',
            dataIndex: 'productName',
            key: 'productName',
            width: 160,
        },
        {
            title: '规格',
            dataIndex: 'productSpec',
            key: 'productSpec',
            width: 140,
            ellipsis: true,
        },
        {
            title: '单位',
            dataIndex: 'unitName',
            key: 'unitName',
            width: 80,
        },
        {
            title: '业务类型',
            dataIndex: 'bizTypeDesc',
            key: 'bizTypeDesc',
            width: 110,
            render: (desc: string, record: StockLog) => {
                const isIn = Number(record.stockNum) >= 0;
                return <Tag color={isIn ? 'green' : 'volcano'}>{desc || '-'}</Tag>;
            },
        },
        {
            title: '变动数量',
            dataIndex: 'stockNum',
            key: 'stockNum',
            width: 110,
            align: 'right' as const,
            render: (num: number) => {
                const value = Number(num);
                return (
                    <span style={{ fontWeight: 'bold', color: value >= 0 ? '#52c41a' : '#ff4d4f' }}>
                        {value >= 0 ? `+${value}` : value}
                    </span>
                );
            },
        },
        {
            title: '变动前库存',
            dataIndex: 'oriStockNum',
            key: 'oriStockNum',
            width: 110,
            align: 'right' as const,
        },
        {
            title: '变动后库存',
            dataIndex: 'curStockNum',
            key: 'curStockNum',
            width: 110,
            align: 'right' as const,
        },
        {
            title: '关联单据号',
            dataIndex: 'bizCode',
            key: 'bizCode',
            width: 170,
            render: (code: string) => code || '-',
        },
        {
            title: '操作人',
            dataIndex: 'createBy',
            key: 'createBy',
            width: 100,
            render: (v: string) => v || '-',
        },
        {
            title: '操作时间',
            dataIndex: 'createTime',
            key: 'createTime',
            width: 170,
            render: (v: string) => (v ? String(v).replace('T', ' ') : '-'),
        },
    ];

    return (
        <div style={{ padding: '24px' }}>
            <Card title="库存流水">
                <Space style={{ marginBottom: 16 }} wrap>
                    <Select
                        placeholder="选择仓库"
                        style={{ width: 180 }}
                        value={searchScId}
                        onChange={setSearchScId}
                        allowClear
                    >
                        {warehouses.map((w) => (
                            <Select.Option key={w.id} value={w.id}>
                                {w.name}
                            </Select.Option>
                        ))}
                    </Select>
                    <Select
                        placeholder="业务类型"
                        style={{ width: 160 }}
                        value={searchBizType}
                        onChange={setSearchBizType}
                        allowClear
                    >
                        {bizTypes.map((t) => (
                            <Select.Option key={t.value} value={t.value}>
                                {t.label}
                            </Select.Option>
                        ))}
                    </Select>
                    <Input
                        placeholder="商品名称/编号"
                        style={{ width: 200 }}
                        value={searchProductName}
                        onChange={(e) => setSearchProductName(e.target.value)}
                        onPressEnter={handleSearch}
                    />
                    <RangePicker
                        value={dateRange as any}
                        onChange={(range) => setDateRange(range as any)}
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
                    scroll={{ x: 1500 }}
                    pagination={{
                        current: query.current,
                        pageSize: query.size,
                        total,
                        showSizeChanger: true,
                        showTotal: (t) => `共 ${t} 条`,
                        onChange: (page, size) => setQuery((q: any) => ({ ...q, current: page, size })),
                    }}
                />
            </Card>
        </div>
    );
};

export default StockLogList;

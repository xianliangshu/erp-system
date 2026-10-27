import React, { useEffect, useState } from 'react';
import { App, Button, Card, DatePicker, Input, Select, Space, Table, Tag, Tooltip, Typography } from 'antd';
import { ClearOutlined, ReloadOutlined, SearchOutlined } from '@ant-design/icons';
import dayjs from 'dayjs';
import type { Dayjs } from 'dayjs';
import { getOperationLogPage, cleanOperationLog, BUSINESS_TYPE_OPTIONS } from '@/api/system/log';
import type { OperationLog } from '@/api/system/log';

const { Search } = Input;
const { RangePicker } = DatePicker;
const { Text } = Typography;

/** 业务类型 → Tag 颜色，让「新增/修改/删除」在列表里一眼可辨 */
const BUSINESS_TYPE_COLOR: Record<number, string> = {
    0: 'default',
    1: 'success',
    2: 'processing',
    3: 'error',
    4: 'default',
    5: 'warning',
    6: 'purple',
};

const businessTypeLabel = (value: number) =>
    BUSINESS_TYPE_OPTIONS.find((item) => item.value === value)?.label ?? '其它';

const formatTime = (value?: string) =>
    value ? dayjs(value).format('YYYY-MM-DD HH:mm:ss') : '-';

/**
 * 操作日志
 *
 * <p>数据由后端 LogAspect 自动写入（非 GET 请求 + 导出请求），本页只读。
 * 参数与结果列可能很长，统一用省略号 + Tooltip，避免表格被撑变形。</p>
 */
const OperationLogManagement: React.FC = () => {
    const { modal, message } = App.useApp();
    const [loading, setLoading] = useState(false);
    const [dataSource, setDataSource] = useState<OperationLog[]>([]);
    const [total, setTotal] = useState(0);
    // 分页与查询条件：唯一的取数依据，搜索/重置/翻页都只改它
    const [query, setQuery] = useState<any>({ current: 1, size: 10 });

    // 搜索条件
    const [searchUsername, setSearchUsername] = useState('');
    const [searchTitle, setSearchTitle] = useState('');
    const [searchStatus, setSearchStatus] = useState<number | undefined>(undefined);
    const [searchBusinessType, setSearchBusinessType] = useState<number | undefined>(undefined);
    const [dateRange, setDateRange] = useState<[Dayjs, Dayjs] | null>(null);

    const loadData = async () => {
        setLoading(true);
        try {
            const data = await getOperationLogPage({
                current: query.current,
                size: query.size,
                username: searchUsername || undefined,
                title: searchTitle || undefined,
                status: searchStatus,
                businessType: searchBusinessType,
                beginDate: dateRange?.[0]?.format('YYYY-MM-DD'),
                endDate: dateRange?.[1]?.format('YYYY-MM-DD'),
            });
            setDataSource(data.records || []);
            setTotal(data.total || 0);
        } catch (error) {
            message.error('加载数据失败');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadData();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [query]);

    const handleSearch = () => {
        setQuery((q: any) => ({ ...q, current: 1 }));
    };

    const handleReset = () => {
        setSearchUsername('');
        setSearchTitle('');
        setSearchStatus(undefined);
        setSearchBusinessType(undefined);
        setDateRange(null);
        setQuery((q: any) => ({ ...q, current: 1 }));
    };

    const handleClean = () => {
        modal.confirm({
            title: '确认清空',
            content: '清空后所有操作日志将被永久删除且无法恢复，确定继续吗？',
            okText: '确定清空',
            okButtonProps: { danger: true },
            cancelText: '取消',
            onOk: async () => {
                await cleanOperationLog();
                message.success('已清空操作日志');
                setQuery((q: any) => ({ ...q, current: 1 }));
            },
        });
    };

    const columns = [
        {
            title: '操作模块',
            dataIndex: 'title',
            key: 'title',
            width: 130,
        },
        {
            title: '业务类型',
            dataIndex: 'businessType',
            key: 'businessType',
            width: 100,
            render: (value: number) => (
                <Tag color={BUSINESS_TYPE_COLOR[value] ?? 'default'}>{businessTypeLabel(value)}</Tag>
            ),
        },
        {
            title: '操作人员',
            dataIndex: 'operName',
            key: 'operName',
            width: 110,
        },
        {
            title: '请求方式',
            dataIndex: 'requestMethod',
            key: 'requestMethod',
            width: 90,
            render: (value: string) => <Tag>{value}</Tag>,
        },
        {
            title: '请求地址',
            dataIndex: 'operUrl',
            key: 'operUrl',
            width: 220,
            ellipsis: { showTitle: false },
            render: (value: string) => (
                <Tooltip title={value}>
                    <Text>{value}</Text>
                </Tooltip>
            ),
        },
        {
            title: '操作地址',
            dataIndex: 'operIp',
            key: 'operIp',
            width: 130,
        },
        {
            title: '操作状态',
            dataIndex: 'status',
            key: 'status',
            width: 100,
            render: (status: number, record: OperationLog) =>
                status === 0 ? (
                    <Tag color="success">正常</Tag>
                ) : (
                    <Tooltip title={record.errorMsg}>
                        <Tag color="error">异常</Tag>
                    </Tooltip>
                ),
        },
        {
            title: '耗时',
            dataIndex: 'costTime',
            key: 'costTime',
            width: 90,
            render: (value?: number) => (value === null || value === undefined ? '-' : `${value} ms`),
        },
        {
            title: '请求参数',
            dataIndex: 'operParam',
            key: 'operParam',
            width: 260,
            ellipsis: { showTitle: false },
            render: (value?: string) =>
                value ? (
                    <Tooltip title={<pre style={{ margin: 0, maxHeight: 320, overflow: 'auto' }}>{value}</pre>}>
                        <Text>{value}</Text>
                    </Tooltip>
                ) : (
                    '-'
                ),
        },
        {
            title: '操作时间',
            dataIndex: 'operTime',
            key: 'operTime',
            width: 180,
            render: (value: string) => formatTime(value),
        },
    ];

    return (
        <div style={{ padding: '24px' }}>
            <Card>
                <Space style={{ marginBottom: 16 }} wrap>
                    <Search
                        placeholder="操作人员"
                        value={searchUsername}
                        onChange={(e) => setSearchUsername(e.target.value)}
                        onSearch={handleSearch}
                        style={{ width: 160 }}
                    />
                    <Search
                        placeholder="操作模块"
                        value={searchTitle}
                        onChange={(e) => setSearchTitle(e.target.value)}
                        onSearch={handleSearch}
                        style={{ width: 160 }}
                    />
                    <Select
                        placeholder="业务类型"
                        value={searchBusinessType}
                        onChange={setSearchBusinessType}
                        allowClear
                        style={{ width: 140 }}
                    >
                        {BUSINESS_TYPE_OPTIONS.map((item) => (
                            <Select.Option key={item.value} value={item.value}>
                                {item.label}
                            </Select.Option>
                        ))}
                    </Select>
                    <Select
                        placeholder="操作状态"
                        value={searchStatus}
                        onChange={setSearchStatus}
                        allowClear
                        style={{ width: 130 }}
                    >
                        <Select.Option value={0}>正常</Select.Option>
                        <Select.Option value={1}>异常</Select.Option>
                    </Select>
                    <RangePicker
                        value={dateRange}
                        onChange={(range) => setDateRange(range as [Dayjs, Dayjs] | null)}
                    />
                    <Button type="primary" icon={<SearchOutlined />} onClick={handleSearch}>
                        搜索
                    </Button>
                    <Button icon={<ReloadOutlined />} onClick={handleReset}>
                        重置
                    </Button>
                    <Button danger icon={<ClearOutlined />} onClick={handleClean}>
                        清空日志
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
                        showQuickJumper: true,
                        showTotal: (t) => `共 ${t} 条`,
                        onChange: (page, size) => setQuery((q: any) => ({ ...q, current: page, size })),
                    }}
                />
            </Card>
        </div>
    );
};

export default OperationLogManagement;

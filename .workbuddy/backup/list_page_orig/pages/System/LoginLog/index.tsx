import React, { useEffect, useState } from 'react';
import { App, Button, Card, DatePicker, Input, Select, Space, Table, Tag } from 'antd';
import { ClearOutlined, ReloadOutlined, SearchOutlined } from '@ant-design/icons';
import dayjs from 'dayjs';
import type { Dayjs } from 'dayjs';
import { getLoginLogPage, cleanLoginLog } from '@/api/system/log';
import type { LoginLog } from '@/api/system/log';

const { Search } = Input;
const { RangePicker } = DatePicker;

/**
 * 时间格式化：后端返回 ISO 字符串，统一显示成 yyyy-MM-DD HH:mm:ss
 */
const formatTime = (value?: string) =>
    value ? dayjs(value).format('YYYY-MM-DD HH:mm:ss') : '-';

/**
 * 登录日志
 *
 * <p>只读页面：登录成功与失败都由 AuthServiceImpl 在认证流程里写入，
 * 这里仅提供查询与清空。</p>
 */
const LoginLogManagement: React.FC = () => {
    const { modal, message } = App.useApp();
    const [loading, setLoading] = useState(false);
    const [dataSource, setDataSource] = useState<LoginLog[]>([]);
    const [total, setTotal] = useState(0);
    const [current, setCurrent] = useState(1);
    const [pageSize, setPageSize] = useState(10);

    // 搜索条件
    const [searchUsername, setSearchUsername] = useState('');
    const [searchStatus, setSearchStatus] = useState<number | undefined>(undefined);
    const [dateRange, setDateRange] = useState<[Dayjs, Dayjs] | null>(null);

    const loadData = async (page = current, size = pageSize) => {
        setLoading(true);
        try {
            const data = await getLoginLogPage({
                current: page,
                size,
                username: searchUsername || undefined,
                status: searchStatus,
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
    }, [current, pageSize]);

    const handleSearch = () => {
        setCurrent(1);
        loadData(1, pageSize);
    };

    const handleReset = () => {
        setSearchUsername('');
        setSearchStatus(undefined);
        setDateRange(null);
        setCurrent(1);
        loadData(1, pageSize);
    };

    const handleClean = () => {
        modal.confirm({
            title: '确认清空',
            content: '清空后所有登录日志将被永久删除且无法恢复，确定继续吗？',
            okText: '确定清空',
            okButtonProps: { danger: true },
            cancelText: '取消',
            onOk: async () => {
                await cleanLoginLog();
                message.success('已清空登录日志');
                setCurrent(1);
                loadData(1, pageSize);
            },
        });
    };

    const columns = [
        {
            title: '用户账号',
            dataIndex: 'username',
            key: 'username',
            width: 130,
        },
        {
            title: '登录状态',
            dataIndex: 'status',
            key: 'status',
            width: 100,
            render: (status: number) =>
                status === 0 ? <Tag color="success">成功</Tag> : <Tag color="error">失败</Tag>,
        },
        {
            title: '提示信息',
            dataIndex: 'msg',
            key: 'msg',
            width: 160,
            ellipsis: true,
        },
        {
            title: '登录IP',
            dataIndex: 'loginIp',
            key: 'loginIp',
            width: 140,
        },
        {
            title: '浏览器',
            dataIndex: 'browser',
            key: 'browser',
            width: 100,
        },
        {
            title: '操作系统',
            dataIndex: 'os',
            key: 'os',
            width: 110,
        },
        {
            title: '登录时间',
            dataIndex: 'loginTime',
            key: 'loginTime',
            width: 180,
            render: (value: string) => formatTime(value),
        },
    ];

    return (
        <div style={{ padding: '24px' }}>
            <Card>
                <Space style={{ marginBottom: 16 }} wrap>
                    <Search
                        placeholder="用户账号"
                        value={searchUsername}
                        onChange={(e) => setSearchUsername(e.target.value)}
                        onSearch={handleSearch}
                        style={{ width: 180 }}
                    />
                    <Select
                        placeholder="登录状态"
                        value={searchStatus}
                        onChange={setSearchStatus}
                        allowClear
                        style={{ width: 140 }}
                    >
                        <Select.Option value={0}>成功</Select.Option>
                        <Select.Option value={1}>失败</Select.Option>
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
                    scroll={{ x: 920 }}
                    pagination={{
                        current,
                        pageSize,
                        total,
                        showSizeChanger: true,
                        showQuickJumper: true,
                        showTotal: (t) => `共 ${t} 条`,
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

export default LoginLogManagement;

import React, { useEffect, useState } from 'react';
import { Card, Table, Space, Input, Select, Button, message, Tag } from 'antd';
import { SearchOutlined, ReloadOutlined, PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { getPreTakePage, deletePreTake } from '@/api/stock/preTakeSheet';
import { getAllWarehouses } from '@/api/basedata/warehouse';

/** 盘点轮次（与后端 takeStatus 列语义一致：0=初盘 / 1=复盘 / 2=抽盘） */
const ROUND_OPTIONS = [
    { value: 0, label: '初盘' },
    { value: 1, label: '复盘' },
    { value: 2, label: '抽盘' },
];

const roundTag = (v: number) => {
    const info = ROUND_OPTIONS.find((r) => r.value === v);
    if (!info) {
        return <Tag>{v ?? '-'}</Tag>;
    }
    const color = v === 0 ? 'blue' : v === 1 ? 'orange' : 'purple';
    return <Tag color={color}>{info.label}</Tag>;
};

const PreTakeStockList: React.FC = () => {
    const navigate = useNavigate();
    const [loading, setLoading] = useState(false);
    const [dataSource, setDataSource] = useState<any[]>([]);
    const [total, setTotal] = useState(0);
    // 分页与查询条件：唯一的取数依据，搜索/重置/翻页都只改它
    const [query, setQuery] = useState<any>({ current: 1, size: 10 });
    const [searchCode, setSearchCode] = useState('');
    const [searchScId, setSearchScId] = useState<number | undefined>(undefined);
    const [searchRound, setSearchRound] = useState<number | undefined>(undefined);
    const [warehouses, setWarehouses] = useState<any[]>([]);

    const loadWarehouses = async () => {
        try {
            const data: any = await getAllWarehouses();
            setWarehouses(Array.isArray(data) ? data : []);
        } catch (error) {
            console.error('加载仓库失败', error);
        }
    };

    const loadData = async () => {
        setLoading(true);
        try {
            const res: any = await getPreTakePage({
                ...query,
                code: searchCode || undefined,
                scId: searchScId,
                takeStatus: searchRound,
            });
            setDataSource(res?.records || []);
            setTotal(res?.total || 0);
        } catch (error) {
            message.error('加载预先盘点单失败');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadWarehouses();
    }, []);

    useEffect(() => {
        loadData();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [query]);

    const handleSearch = () => setQuery((q: any) => ({ ...q, current: 1 }));

    const handleReset = () => {
        setSearchCode('');
        setSearchScId(undefined);
        setSearchRound(undefined);
        setQuery((q: any) => ({ ...q, current: 1 }));
    };

    const handleDelete = async (id: number) => {
        try {
            await deletePreTake(id);
            message.success('删除成功');
            loadData();
        } catch (error) {
            message.error('删除失败');
        }
    };

    const columns = [
        { title: '单据编号', dataIndex: 'code', key: 'code', width: 180 },
        { title: '仓库', dataIndex: 'scName', key: 'scName', width: 140, render: (v: any, r: any) => v || r.scId },
        { title: '盘点轮次', dataIndex: 'takeStatus', key: 'takeStatus', width: 110, render: (v: number) => roundTag(v) },
        { title: '备注', dataIndex: 'description', key: 'description', ellipsis: true },
        { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 170 },
        {
            title: '操作',
            key: 'action',
            width: 160,
            render: (_: any, record: any) => (
                <Space size="small">
                    <Button type="link" size="small" icon={<EditOutlined />} onClick={() => navigate(`/stock/take/pre/edit/${record.id}`)}>
                        编辑
                    </Button>
                    <Button type="link" size="small" danger icon={<DeleteOutlined />} onClick={() => handleDelete(record.id)}>
                        删除
                    </Button>
                </Space>
            ),
        },
    ];

    return (
        <div style={{ padding: '24px' }}>
            <Card title="预先盘点单">
                <Space style={{ marginBottom: 16 }} wrap>
                    <Input
                        placeholder="单据编号"
                        style={{ width: 180 }}
                        value={searchCode}
                        onChange={(e) => setSearchCode(e.target.value)}
                        onPressEnter={handleSearch}
                    />
                    <Select
                        placeholder="仓库"
                        style={{ width: 180 }}
                        value={searchScId}
                        onChange={setSearchScId}
                        allowClear
                        options={warehouses.map((w: any) => ({ value: w.id, label: w.name }))}
                    />
                    <Select placeholder="盘点轮次" style={{ width: 130 }} value={searchRound} onChange={setSearchRound} allowClear options={ROUND_OPTIONS} />
                    <Button type="primary" icon={<SearchOutlined />} onClick={handleSearch}>
                        查询
                    </Button>
                    <Button icon={<ReloadOutlined />} onClick={handleReset}>
                        重置
                    </Button>
                    <Button type="primary" icon={<PlusOutlined />} onClick={() => navigate('/stock/take/pre/add')}>
                        新增预先盘点单
                    </Button>
                </Space>

                <Table
                    loading={loading}
                    columns={columns}
                    dataSource={dataSource}
                    rowKey="id"
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

export default PreTakeStockList;

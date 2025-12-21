import React, { useState, useEffect } from 'react';
import { Table, Card, Space, Select, Button, message, Tag, Popconfirm } from 'antd';
import { PlusOutlined, SearchOutlined, ReloadOutlined, CheckOutlined, DeleteOutlined, EyeOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { getPurchaseReceiptPage, confirmPurchaseReceipt, deletePurchaseReceipt } from '@/api/business/purchaseReceipt';
import { getSupplierList } from '@/api/basedata/supplier';

const { Option } = Select;

// 收货单状态映射
const statusMap: Record<number, { text: string; color: string }> = {
    0: { text: '待确认', color: 'orange' },
    1: { text: '已确认', color: 'green' },
};

const PurchaseReceiptList: React.FC = () => {
    const navigate = useNavigate();
    const [loading, setLoading] = useState(false);
    const [dataSource, setDataSource] = useState<any[]>([]);
    const [total, setTotal] = useState(0);
    const [current, setCurrent] = useState(1);
    const [pageSize, setPageSize] = useState(10);

    // 搜索条件
    const [searchSupplierId, setSearchSupplierId] = useState<number | undefined>(undefined);
    const [searchStatus, setSearchStatus] = useState<number | undefined>(undefined);
    const [suppliers, setSuppliers] = useState<any[]>([]);

    // 加载供应商列表
    const loadSuppliers = async () => {
        try {
            const data: any = await getSupplierList();
            setSuppliers(Array.isArray(data) ? data : []);
        } catch (error) {
            console.error('加载供应商失败', error);
        }
    };

    // 加载收货数据
    const loadData = async () => {
        setLoading(true);
        try {
            const params = {
                current,
                size: pageSize,
                supplierId: searchSupplierId,
                status: searchStatus,
            };
            const data = await getPurchaseReceiptPage(params);
            setDataSource(data.records);
            setTotal(data.total);
        } catch (error) {
            message.error('加载收货数据失败');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadSuppliers();
    }, []);

    useEffect(() => {
        loadData();
    }, [current, pageSize]);

    const handleSearch = () => {
        setCurrent(1);
        loadData();
    };

    const handleReset = () => {
        setSearchSupplierId(undefined);
        setSearchStatus(undefined);
        setCurrent(1);
        loadData();
    };

    const handleConfirm = async (id: number) => {
        try {
            await confirmPurchaseReceipt(id);
            message.success('确认成功');
            loadData();
        } catch (error) {
            message.error('操作失败');
        }
    };

    const handleDelete = async (id: number) => {
        try {
            await deletePurchaseReceipt(id);
            message.success('删除成功');
            loadData();
        } catch (error) {
            message.error('删除失败');
        }
    };

    const columns = [
        {
            title: '收货单编号',
            dataIndex: 'code',
            key: 'code',
        },
        {
            title: '采购订单编号',
            dataIndex: 'orderCode',
            key: 'orderCode',
        },
        {
            title: '供应商',
            dataIndex: 'supplierId',
            key: 'supplierId',
            render: (id: number) => {
                const supplier = suppliers.find(s => s.id === id);
                return supplier?.name || id;
            },
        },
        {
            title: '收货数量',
            dataIndex: 'totalNum',
            key: 'totalNum',
        },
        {
            title: '收货金额',
            dataIndex: 'totalAmount',
            key: 'totalAmount',
            render: (amount: number) => `￥${amount?.toFixed(2) || '0.00'}`,
        },
        {
            title: '状态',
            dataIndex: 'status',
            key: 'status',
            render: (status: number) => {
                const statusInfo = statusMap[status] || { text: '未知', color: 'default' };
                return <Tag color={statusInfo.color}>{statusInfo.text}</Tag>;
            },
        },
        {
            title: '创建时间',
            dataIndex: 'createTime',
            key: 'createTime',
        },
        {
            title: '操作',
            key: 'action',
            render: (_: any, record: any) => (
                <Space size="small">
                    <Button type="link" size="small" icon={<EyeOutlined />} onClick={() => navigate(`/business/purchase/receipt/detail/${record.id}`)}>
                        查看
                    </Button>
                    {record.status === 0 && (
                        <Popconfirm title="确定确认收货?" onConfirm={() => handleConfirm(record.id)}>
                            <Button type="link" size="small" icon={<CheckOutlined />}>
                                确认
                            </Button>
                        </Popconfirm>
                    )}
                    <Popconfirm title="确定删除此收货单?" onConfirm={() => handleDelete(record.id)}>
                        <Button type="link" size="small" danger icon={<DeleteOutlined />}>删除</Button>
                    </Popconfirm>
                </Space>
            ),
        },
    ];

    return (
        <div style={{ padding: '24px' }}>
            <Card title="采购收货管理">
                <Space style={{ marginBottom: 16 }} wrap>
                    <Select
                        placeholder="选择供应商"
                        style={{ width: 200 }}
                        value={searchSupplierId}
                        onChange={setSearchSupplierId}
                        allowClear
                    >
                        {suppliers.map((s: any) => (
                            <Option key={s.id} value={s.id}>{s.name}</Option>
                        ))}
                    </Select>
                    <Select
                        placeholder="收货状态"
                        style={{ width: 120 }}
                        value={searchStatus}
                        onChange={setSearchStatus}
                        allowClear
                    >
                        {Object.entries(statusMap).map(([key, val]) => (
                            <Option key={key} value={Number(key)}>{val.text}</Option>
                        ))}
                    </Select>
                    <Button type="primary" icon={<SearchOutlined />} onClick={handleSearch}>
                        查询
                    </Button>
                    <Button icon={<ReloadOutlined />} onClick={handleReset}>
                        重置
                    </Button>
                    <Button type="primary" icon={<PlusOutlined />} onClick={() => navigate('/business/purchase/receipt/add')}>
                        新增收货
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

export default PurchaseReceiptList;

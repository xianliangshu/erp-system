import React, { useState, useEffect } from 'react';
import { Table, Card, Space, Input, Select, Button, message, Tag, Modal, Popconfirm } from 'antd';
import { PlusOutlined, SearchOutlined, ReloadOutlined, CheckOutlined, CloseOutlined, DeleteOutlined, EyeOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { getPurchaseOrderPage, approvePurchaseOrder, rejectPurchaseOrder, cancelPurchaseOrder, deletePurchaseOrder } from '@/api/business/purchaseOrder';
import { getSupplierList } from '@/api/basedata/supplier';

const { Option } = Select;

// 订单状态映射
const statusMap: Record<number, { text: string; color: string }> = {
    0: { text: '待审核', color: 'orange' },
    1: { text: '已审核', color: 'green' },
    2: { text: '已拒绝', color: 'red' },
    3: { text: '已完成', color: 'blue' },
    4: { text: '已取消', color: 'default' },
};

const PurchaseOrderList: React.FC = () => {
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

    // 拒绝弹窗
    const [rejectModalVisible, setRejectModalVisible] = useState(false);
    const [rejectOrderId, setRejectOrderId] = useState<number | null>(null);
    const [rejectReason, setRejectReason] = useState('');

    // 加载供应商列表
    const loadSuppliers = async () => {
        try {
            const data: any = await getSupplierList();
            setSuppliers(Array.isArray(data) ? data : []);
        } catch (error) {
            console.error('加载供应商失败', error);
        }
    };

    // 加载订单数据
    const loadData = async () => {
        setLoading(true);
        try {
            const params = {
                current,
                size: pageSize,
                supplierId: searchSupplierId,
                status: searchStatus,
            };
            const data = await getPurchaseOrderPage(params);
            setDataSource(data.records);
            setTotal(data.total);
        } catch (error) {
            message.error('加载订单数据失败');
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

    const handleApprove = async (id: number) => {
        try {
            await approvePurchaseOrder(id);
            message.success('审核通过');
            loadData();
        } catch (error) {
            message.error('操作失败');
        }
    };

    const handleReject = async () => {
        if (!rejectOrderId || !rejectReason) {
            message.warning('请输入拒绝原因');
            return;
        }
        try {
            await rejectPurchaseOrder(rejectOrderId, rejectReason);
            message.success('已拒绝');
            setRejectModalVisible(false);
            setRejectReason('');
            loadData();
        } catch (error) {
            message.error('操作失败');
        }
    };

    const handleCancel = async (id: number) => {
        try {
            await cancelPurchaseOrder(id);
            message.success('已取消');
            loadData();
        } catch (error) {
            message.error('操作失败');
        }
    };

    const handleDelete = async (id: number) => {
        try {
            await deletePurchaseOrder(id);
            message.success('删除成功');
            loadData();
        } catch (error) {
            message.error('删除失败');
        }
    };

    const columns = [
        {
            title: '订单编号',
            dataIndex: 'code',
            key: 'code',
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
            title: '采购数量',
            dataIndex: 'totalNum',
            key: 'totalNum',
        },
        {
            title: '采购金额',
            dataIndex: 'totalAmount',
            key: 'totalAmount',
            render: (amount: number) => `￥${amount?.toFixed(2) || '0.00'}`,
        },
        {
            title: '预计到货日期',
            dataIndex: 'expectArriveDate',
            key: 'expectArriveDate',
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
                    <Button type="link" size="small" icon={<EyeOutlined />} onClick={() => navigate(`/business/purchase/order/detail/${record.id}`)}>
                        查看
                    </Button>
                    {record.status === 0 && (
                        <>
                            <Button type="link" size="small" icon={<CheckOutlined />} onClick={() => handleApprove(record.id)}>
                                通过
                            </Button>
                            <Button type="link" size="small" danger icon={<CloseOutlined />} onClick={() => { setRejectOrderId(record.id); setRejectModalVisible(true); }}>
                                拒绝
                            </Button>
                        </>
                    )}
                    {record.status !== 3 && record.status !== 4 && (
                        <Popconfirm title="确定取消此订单?" onConfirm={() => handleCancel(record.id)}>
                            <Button type="link" size="small">取消</Button>
                        </Popconfirm>
                    )}
                    <Popconfirm title="确定删除此订单?" onConfirm={() => handleDelete(record.id)}>
                        <Button type="link" size="small" danger icon={<DeleteOutlined />}>删除</Button>
                    </Popconfirm>
                </Space>
            ),
        },
    ];

    return (
        <div style={{ padding: '24px' }}>
            <Card title="采购订单管理">
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
                        placeholder="订单状态"
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
                    <Button type="primary" icon={<PlusOutlined />} onClick={() => navigate('/business/purchase/order/add')}>
                        新增订单
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

            {/* 拒绝弹窗 */}
            <Modal
                title="拒绝订单"
                open={rejectModalVisible}
                onOk={handleReject}
                onCancel={() => setRejectModalVisible(false)}
            >
                <Input.TextArea
                    placeholder="请输入拒绝原因"
                    value={rejectReason}
                    onChange={(e) => setRejectReason(e.target.value)}
                    rows={4}
                />
            </Modal>
        </div>
    );
};

export default PurchaseOrderList;

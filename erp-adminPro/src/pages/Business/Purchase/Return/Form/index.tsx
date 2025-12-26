import React, { useState, useEffect } from 'react';
import { Form, Select, InputNumber, Button, Card, Table, Space, message, Divider, Input } from 'antd';
import { DeleteOutlined, ArrowLeftOutlined } from '@ant-design/icons';
import { useNavigate, useParams } from 'react-router-dom';
import { createPurchaseReturn, updatePurchaseReturn, getPurchaseReturnById, getPendingReturnReceipts } from '@/api/business/purchaseReturn';
import { getPurchaseReceiptById } from '@/api/business/purchaseReceipt';
import { getSupplierList } from '@/api/basedata/supplier';
import { getMaterialPage } from '@/api/material/material';

const { Option } = Select;
const { TextArea } = Input;

const PurchaseReturnForm: React.FC = () => {
    const navigate = useNavigate();
    const { id } = useParams<{ id: string }>();
    const isEdit = !!id;

    const [form] = Form.useForm();
    const [loading, setLoading] = useState(false);
    const [suppliers, setSuppliers] = useState<any[]>([]);
    const [receipts, setReceipts] = useState<any[]>([]);
    const [materials, setMaterials] = useState<any[]>([]);
    const [details, setDetails] = useState<any[]>([]);
    const [selectedReceiptId, setSelectedReceiptId] = useState<number | undefined>(undefined);

    // 加载基础数据
    useEffect(() => {
        loadSuppliers();
        loadReceipts();
        loadMaterials();
        if (isEdit) {
            loadReturnData();
        }
    }, [id]);

    const loadSuppliers = async () => {
        try {
            const data: any = await getSupplierList();
            setSuppliers(Array.isArray(data) ? data : []);
        } catch (error) {
            console.error('加载供应商失败', error);
        }
    };

    const loadReceipts = async () => {
        try {
            const data: any = await getPendingReturnReceipts({ current: 1, size: 1000 });
            setReceipts(data?.records || []);
        } catch (error) {
            console.error('加载收货单失败', error);
        }
    };

    const loadMaterials = async () => {
        try {
            const data: any = await getMaterialPage({ current: 1, size: 1000 });
            setMaterials(data?.records || []);
        } catch (error) {
            console.error('加载物料失败', error);
        }
    };

    const loadReturnData = async () => {
        try {
            const data: any = await getPurchaseReturnById(Number(id));
            if (data?.purchaseReturn) {
                form.setFieldsValue({
                    receiptId: data.purchaseReturn.receiptId,
                    description: data.purchaseReturn.description,
                });
                setSelectedReceiptId(data.purchaseReturn.receiptId);
                setDetails(data.details || []);
            }
        } catch (error) {
            message.error('加载退货单数据失败');
        }
    };

    // 选择收货单后加载明细
    const handleReceiptChange = async (receiptId: number) => {
        setSelectedReceiptId(receiptId);
        try {
            const data: any = await getPurchaseReceiptById(receiptId);
            if (data?.details) {
                // 转换收货明细为退货明细
                const returnDetails = data.details.map((d: any, index: number) => ({
                    key: Date.now() + index,
                    receiptDetailId: d.id,
                    productId: d.productId,
                    receiveNum: d.receiveNum,
                    returnNum: 0,
                    taxPrice: d.taxPrice,
                    description: '',
                }));
                setDetails(returnDetails);
            }
            // 设置供应商
            if (data?.receipt) {
                form.setFieldsValue({
                    supplierId: data.receipt.supplierId,
                    scId: data.receipt.scId,
                });
            }
        } catch (error) {
            message.error('加载收货单明细失败');
        }
    };

    // 获取商品名称
    const getMaterialName = (productId: number) => {
        const material = materials.find(m => m.id === productId);
        return material?.name || productId;
    };

    // 更新明细行
    const handleDetailChange = (key: number, field: string, value: any) => {
        setDetails(details.map(d => {
            if (d.key === key || d.id === key) {
                const updated = { ...d, [field]: value };
                // 自动计算金额
                if (field === 'returnNum' || field === 'taxPrice') {
                    updated.taxAmount = (updated.returnNum || 0) * (updated.taxPrice || 0);
                }
                return updated;
            }
            return d;
        }));
    };

    // 删除明细行
    const handleDeleteDetail = (key: number) => {
        setDetails(details.filter(d => d.key !== key && d.id !== key));
    };

    // 提交表单
    const handleSubmit = async () => {
        try {
            const values = await form.validateFields();

            if (details.length === 0) {
                message.warning('请添加至少一个退货明细');
                return;
            }

            // 检查是否有退货数量
            const hasReturnNum = details.some(d => d.returnNum > 0);
            if (!hasReturnNum) {
                message.warning('请填写退货数量');
                return;
            }

            setLoading(true);

            const returnData = {
                id: isEdit ? Number(id) : undefined,
                receiptId: values.receiptId,
                scId: values.scId,
                supplierId: values.supplierId,
                description: values.description,
                details: details.filter(d => d.returnNum > 0).map(d => ({
                    receiptDetailId: d.receiptDetailId,
                    productId: d.productId,
                    receiveNum: d.receiveNum,
                    returnNum: d.returnNum,
                    taxPrice: d.taxPrice,
                    description: d.description,
                })),
            };

            if (isEdit) {
                await updatePurchaseReturn(returnData);
                message.success('更新成功');
            } else {
                await createPurchaseReturn(returnData);
                message.success('创建成功');
            }

            navigate('/business/purchase/return');
        } catch (error) {
            message.error('操作失败');
        } finally {
            setLoading(false);
        }
    };

    // 计算合计
    const totalNum = details.reduce((sum, d) => sum + (d.returnNum || 0), 0);
    const totalAmount = details.reduce((sum, d) => sum + ((d.returnNum || 0) * (d.taxPrice || 0)), 0);

    const detailColumns = [
        {
            title: '商品',
            dataIndex: 'productId',
            key: 'productId',
            width: 200,
            render: (productId: number) => getMaterialName(productId),
        },
        {
            title: '原收货数量',
            dataIndex: 'receiveNum',
            key: 'receiveNum',
            width: 120,
        },
        {
            title: '退货数量',
            dataIndex: 'returnNum',
            key: 'returnNum',
            width: 120,
            render: (value: number, record: any) => (
                <InputNumber
                    min={0}
                    max={record.receiveNum}
                    value={value}
                    onChange={(v) => handleDetailChange(record.key || record.id, 'returnNum', v)}
                />
            ),
        },
        {
            title: '含税单价',
            dataIndex: 'taxPrice',
            key: 'taxPrice',
            width: 120,
            render: (price: number) => `￥${(price || 0).toFixed(2)}`,
        },
        {
            title: '金额',
            key: 'taxAmount',
            width: 120,
            render: (_: any, record: any) => `￥${((record.returnNum || 0) * (record.taxPrice || 0)).toFixed(2)}`,
        },
        {
            title: '备注',
            dataIndex: 'description',
            key: 'description',
            render: (value: string, record: any) => (
                <Input
                    value={value}
                    onChange={(e) => handleDetailChange(record.key || record.id, 'description', e.target.value)}
                    placeholder="退货原因"
                />
            ),
        },
        {
            title: '操作',
            key: 'action',
            width: 80,
            render: (_: any, record: any) => (
                <Button
                    type="link"
                    danger
                    icon={<DeleteOutlined />}
                    onClick={() => handleDeleteDetail(record.key || record.id)}
                >
                    删除
                </Button>
            ),
        },
    ];

    return (
        <div style={{ padding: '24px' }}>
            <Card
                title={
                    <Space>
                        <Button icon={<ArrowLeftOutlined />} onClick={() => navigate(-1)}>返回</Button>
                        <span>{isEdit ? '编辑采购退货' : '新增采购退货'}</span>
                    </Space>
                }
            >
                <Form form={form} layout="vertical">
                    <div style={{ display: 'flex', gap: 24 }}>
                        <Form.Item
                            name="receiptId"
                            label="选择收货单"
                            rules={[{ required: true, message: '请选择收货单' }]}
                            style={{ flex: 1 }}
                        >
                            <Select
                                placeholder="选择已确认的收货单"
                                onChange={handleReceiptChange}
                                disabled={isEdit}
                                showSearch
                                filterOption={(input, option) =>
                                    (option?.children as unknown as string)?.toLowerCase()?.includes(input.toLowerCase())
                                }
                            >
                                {receipts.map((r: any) => (
                                    <Option key={r.id} value={r.id}>{r.code} - {suppliers.find(s => s.id === r.supplierId)?.name || ''}</Option>
                                ))}
                            </Select>
                        </Form.Item>
                        <Form.Item
                            name="supplierId"
                            label="供应商"
                            style={{ flex: 1 }}
                        >
                            <Select placeholder="供应商" disabled>
                                {suppliers.map((s: any) => (
                                    <Option key={s.id} value={s.id}>{s.name}</Option>
                                ))}
                            </Select>
                        </Form.Item>
                    </div>
                    <Form.Item name="description" label="备注">
                        <TextArea rows={2} placeholder="退货备注" />
                    </Form.Item>
                </Form>

                <Divider>退货明细</Divider>

                <Table
                    columns={detailColumns}
                    dataSource={details}
                    rowKey={(record) => record.key || record.id}
                    pagination={false}
                    summary={() => (
                        <Table.Summary>
                            <Table.Summary.Row>
                                <Table.Summary.Cell index={0}>合计</Table.Summary.Cell>
                                <Table.Summary.Cell index={1}>-</Table.Summary.Cell>
                                <Table.Summary.Cell index={2}>{totalNum}</Table.Summary.Cell>
                                <Table.Summary.Cell index={3}>-</Table.Summary.Cell>
                                <Table.Summary.Cell index={4}>￥{totalAmount.toFixed(2)}</Table.Summary.Cell>
                                <Table.Summary.Cell index={5}>-</Table.Summary.Cell>
                                <Table.Summary.Cell index={6}>-</Table.Summary.Cell>
                            </Table.Summary.Row>
                        </Table.Summary>
                    )}
                />

                <div style={{ marginTop: 24, textAlign: 'center' }}>
                    <Space>
                        <Button onClick={() => navigate(-1)}>取消</Button>
                        <Button type="primary" loading={loading} onClick={handleSubmit}>
                            {isEdit ? '保存修改' : '创建退货单'}
                        </Button>
                    </Space>
                </div>
            </Card>
        </div>
    );
};

export default PurchaseReturnForm;

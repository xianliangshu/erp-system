import React, { useEffect, useState } from 'react';
import { useNavigate, useParams, useSearchParams } from 'react-router-dom';
import {
    Card, Button, Table, InputNumber, Space, message, Select, Input, Descriptions, Tag, Divider,
} from 'antd';
import {
    PlusOutlined, DeleteOutlined, SaveOutlined, ArrowLeftOutlined,
} from '@ant-design/icons';
import {
    getCostAdjustById, addCostAdjust, updateCostAdjust, getCostAdjustStockInfo,
} from '@/api/stock/costAdjust';
import { getAllWarehouses } from '@/api/basedata/warehouse';
import { getMaterialList } from '@/api/material/material';

const calcDiff = (row: any) =>
    Number(((row.price ?? 0) - (row.oriPrice ?? 0)) * (row.stockNum ?? 0)).toFixed(2);

const money = (v: any) => (v === null || v === undefined ? '-' : Number(v).toFixed(2));

/**
 * 库存成本调整单 新增 / 编辑 / 查看
 */
const CostAdjustForm: React.FC = () => {
    const navigate = useNavigate();
    const { id } = useParams();
    const [searchParams] = useSearchParams();
    const isViewMode = searchParams.get('mode') === 'view';

    const [warehouses, setWarehouses] = useState<any[]>([]);
    const [materials, setMaterials] = useState<any[]>([]);
    const [scId, setScId] = useState<number | undefined>(undefined);
    const [description, setDescription] = useState('');
    const [details, setDetails] = useState<any[]>([]);
    const [status, setStatus] = useState<number>(0);
    const [code, setCode] = useState<string>('');
    const [approveInfo, setApproveInfo] = useState<any>({});
    const [saving, setSaving] = useState(false);

    const readonly = isViewMode || (!!id && status !== 0);

    useEffect(() => {
        Promise.all([getAllWarehouses(), getMaterialList()])
            .then(([wh, mt]) => {
                setWarehouses((wh as any) || []);
                setMaterials((mt as any) || []);
            })
            .catch(() => undefined);
    }, []);

    useEffect(() => {
        if (id) {
            loadDetail(Number(id));
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [id]);

    const loadDetail = async (sheetId: number) => {
        try {
            const res: any = await getCostAdjustById(sheetId);
            if (!res) {
                message.error('单据不存在');
                return;
            }
            setScId(res.scId);
            setDescription(res.description || '');
            setStatus(res.status);
            setCode(res.code);
            setApproveInfo({
                approveBy: res.approveBy,
                approveTime: res.approveTime,
                refuseReason: res.refuseReason,
            });
            setDetails((res.details || []).map((d: any) => ({ ...d })));
        } catch (error) {
            message.error('加载单据失败');
        }
    };

    const handleAddRow = () => {
        setDetails((prev) => [...prev, { _key: `new_${Date.now()}`, productId: undefined, price: undefined }]);
    };

    const handleRemoveRow = (index: number) => {
        setDetails((prev) => prev.filter((_, i) => i !== index));
    };

    const handleProductChange = async (index: number, productId: number) => {
        if (!scId) {
            message.warning('请先选择仓库');
            return;
        }
        try {
            const info: any = await getCostAdjustStockInfo(scId, productId);
            setDetails((prev) => {
                const arr = [...prev];
                const row = {
                    ...arr[index],
                    productId,
                    stockNum: info?.stockNum ?? 0,
                    oriPrice: info?.costPrice ?? 0,
                    purchasePrice: info?.purchasePrice,
                    productCode: info?.productCode,
                    productName: info?.productName,
                    specification: info?.specification,
                    salePropertyText: info?.salePropertyText,
                };
                row.diffAmount = calcDiff(row);
                arr[index] = row;
                return arr;
            });
        } catch (error) {
            message.error('获取该商品库存信息失败');
        }
    };

    const handlePriceChange = (index: number, price: number | null) => {
        setDetails((prev) => {
            const arr = [...prev];
            const row = { ...arr[index], price: price ?? undefined };
            row.diffAmount = calcDiff(row);
            arr[index] = row;
            return arr;
        });
    };

    const handleSubmit = async () => {
        if (!scId) {
            message.warning('请选择仓库');
            return;
        }
        if (details.length === 0) {
            message.warning('请至少添加一条调价明细');
            return;
        }
        for (let i = 0; i < details.length; i++) {
            const row = details[i];
            if (!row.productId) {
                message.warning(`第 ${i + 1} 行未选择商品`);
                return;
            }
            if (row.price === undefined || row.price === null) {
                message.warning(`第 ${i + 1} 行未填写调整后成本价`);
                return;
            }
        }

        const payload = {
            id: id ? Number(id) : undefined,
            scId,
            description,
            details: details.map((d) => ({
                productId: d.productId,
                price: d.price,
                description: d.description,
            })),
        };

        setSaving(true);
        try {
            if (id) {
                await updateCostAdjust(payload);
                message.success('修改成功');
            } else {
                await addCostAdjust(payload);
                message.success('新增成功');
            }
            navigate('/stock/adjust/cost');
        } catch (error) {
            console.error('保存失败', error);
        } finally {
            setSaving(false);
        }
    };

    const selectedProductIds = details.map((d) => d.productId).filter(Boolean);

    const columns = [
        {
            title: '商品',
            key: 'productId',
            width: 300,
            render: (_: any, record: any, index: number) =>
                readonly ? (
                    <span>
                        {record.productName || '-'}
                        {record.productCode ? `（${record.productCode}）` : ''}
                    </span>
                ) : (
                    <Select
                        style={{ width: '100%' }}
                        placeholder="请选择商品"
                        showSearch
                        optionFilterProp="label"
                        value={record.productId}
                        onChange={(val) => handleProductChange(index, val)}
                        options={materials
                            .filter((m: any) => !selectedProductIds.includes(m.id) || m.id === record.productId)
                            .map((m: any) => ({
                                value: m.id,
                                label: `${m.name}（${m.code}）${m.salePropertyText ? ' ' + m.salePropertyText : ''}`,
                            }))}
                    />
                ),
        },
        {
            title: '当前库存',
            dataIndex: 'stockNum',
            key: 'stockNum',
            width: 110,
            render: (v: any) => money(v),
        },
        {
            title: '当前成本价',
            dataIndex: 'oriPrice',
            key: 'oriPrice',
            width: 120,
            render: (v: any) => money(v),
        },
        {
            title: '调整后成本价',
            key: 'price',
            width: 160,
            render: (_: any, record: any, index: number) =>
                readonly ? (
                    money(record.price)
                ) : (
                    <InputNumber
                        style={{ width: '100%' }}
                        min={0}
                        precision={4}
                        placeholder="请输入新成本价"
                        value={record.price}
                        onChange={(val) => handlePriceChange(index, val)}
                    />
                ),
        },
        {
            title: '调价差额',
            dataIndex: 'diffAmount',
            key: 'diffAmount',
            width: 130,
            render: (v: any) => (
                <span style={{ color: Number(v) > 0 ? '#cf1322' : Number(v) < 0 ? '#3f8600' : undefined }}>
                    {money(v)}
                </span>
            ),
        },
        {
            title: '备注',
            key: 'description',
            width: 180,
            render: (_: any, record: any, index: number) =>
                readonly ? (
                    record.description || '-'
                ) : (
                    <Input
                        value={record.description}
                        placeholder="备注"
                        onChange={(e) =>
                            setDetails((prev) => {
                                const arr = [...prev];
                                arr[index] = { ...arr[index], description: e.target.value };
                                return arr;
                            })
                        }
                    />
                ),
        },
        ...(readonly
            ? []
            : [
                  {
                      title: '操作',
                      key: 'action',
                      width: 80,
                      render: (_: any, __: any, index: number) => (
                          <Button
                              type="link"
                              size="small"
                              danger
                              icon={<DeleteOutlined />}
                              onClick={() => handleRemoveRow(index)}
                          />
                      ),
                  },
              ]),
    ];

    const totalDiff = details.reduce((sum, d) => sum + Number(d.diffAmount || 0), 0);

    return (
        <div style={{ padding: '24px' }}>
            <Card
                title={id ? (readonly ? '查看成本调整单' : '编辑成本调整单') : '新增成本调整单'}
                extra={
                    <Space>
                        <Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/stock/adjust/cost')}>
                            返回
                        </Button>
                        {!readonly && (
                            <Button type="primary" icon={<SaveOutlined />} loading={saving} onClick={handleSubmit}>
                                保存
                            </Button>
                        )}
                    </Space>
                }
            >
                <Space size="large" style={{ marginBottom: 16 }} wrap>
                    {id && (
                        <span>
                            单据编号：<strong>{code}</strong>
                        </span>
                    )}
                    <span>
                        仓库：
                        {readonly ? (
                            <strong>{warehouses.find((w: any) => w.id === scId)?.name || scId}</strong>
                        ) : (
                            <Select
                                style={{ width: 220 }}
                                placeholder="请选择仓库"
                                showSearch
                                optionFilterProp="label"
                                value={scId}
                                onChange={setScId}
                                options={warehouses.map((w: any) => ({ value: w.id, label: w.name }))}
                            />
                        )}
                    </span>
                    <span>
                        状态：
                        {status === 0 && <Tag color="orange">待审核</Tag>}
                        {status === 1 && <Tag color="green">审核通过</Tag>}
                        {status === 2 && <Tag color="red">审核拒绝</Tag>}
                    </span>
                </Space>

                <div style={{ marginBottom: 16 }}>
                    备注：
                    {readonly ? (
                        <span>{description || '-'}</span>
                    ) : (
                        <Input
                            style={{ maxWidth: 520 }}
                            placeholder="请输入备注"
                            value={description}
                            onChange={(e) => setDescription(e.target.value)}
                        />
                    )}
                </div>

                {!readonly && (
                    <Button
                        type="dashed"
                        icon={<PlusOutlined />}
                        onClick={handleAddRow}
                        style={{ marginBottom: 12 }}
                        disabled={!scId}
                    >
                        添加明细
                    </Button>
                )}

                <Table
                    columns={columns as any}
                    dataSource={details}
                    rowKey={(r: any) => r.id ?? r._key}
                    pagination={false}
                    size="small"
                    locale={{ emptyText: scId ? '暂无明细，请点击「添加明细」' : '请先选择仓库' }}
                />

                <div style={{ marginTop: 16, textAlign: 'right' }}>
                    调价差额合计：
                    <span
                        style={{
                            fontSize: 16,
                            fontWeight: 500,
                            marginLeft: 8,
                            color: totalDiff > 0 ? '#cf1322' : totalDiff < 0 ? '#3f8600' : undefined,
                        }}
                    >
                        {totalDiff.toFixed(2)}
                    </span>
                </div>

                {readonly && status !== 0 && (
                    <>
                        <Divider />
                        <Descriptions size="small" column={2} title="审核信息">
                            <Descriptions.Item label="审核人">{approveInfo.approveBy || '-'}</Descriptions.Item>
                            <Descriptions.Item label="审核时间">{approveInfo.approveTime || '-'}</Descriptions.Item>
                            {status === 2 && (
                                <Descriptions.Item label="拒绝原因" span={2}>
                                    {approveInfo.refuseReason || '-'}
                                </Descriptions.Item>
                            )}
                        </Descriptions>
                    </>
                )}
            </Card>
        </div>
    );
};

export default CostAdjustForm;

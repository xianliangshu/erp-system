import React, { useState, useEffect } from 'react';
import { Drawer, Table, Button, Space, Modal, message, Form, Input, Select, Tag, App } from 'antd';
import { PlusOutlined, MinusCircleOutlined, BarcodeOutlined } from '@ant-design/icons';
import { getSpuSkus } from '@/api/basedata/spu';
import {
    getMaterialCodes,
    saveMaterialCodes,
    materialCodeTypeText,
} from '@/api/material/materialCode';

interface Props {
    open: boolean;
    spuId?: number;
    spuName?: string;
    onClose: () => void;
}

/**
 * SPU 下的 SKU 列表（含一品多码维护）
 */
const SkuDrawer: React.FC<Props> = ({ open, spuId, spuName, onClose }) => {
    const { modal } = App.useApp();
    const [loading, setLoading] = useState(false);
    const [skus, setSkus] = useState<any[]>([]);

    const [codeModalOpen, setCodeModalOpen] = useState(false);
    const [codeTarget, setCodeTarget] = useState<any>(null);
    const [codeForm] = Form.useForm();

    const loadSkus = async () => {
        if (!spuId) return;
        setLoading(true);
        try {
            const data = await getSpuSkus(spuId);
            setSkus(data || []);
        } catch (error) {
            message.error('加载 SKU 失败');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        if (open && spuId) {
            loadSkus();
        }
    }, [open, spuId]);

    const openCodeModal = async (record: any) => {
        setCodeTarget(record);
        setCodeModalOpen(true);
        try {
            const codes = await getMaterialCodes(record.id);
            codeForm.setFieldsValue({
                codes: (codes || []).length
                    ? codes.map((c: any) => ({
                          code: c.code,
                          codeType: c.codeType,
                          remark: c.remark,
                      }))
                    : [{ code: record.code, codeType: 1, remark: 'SKU主编号' }],
            });
        } catch (error) {
            codeForm.setFieldsValue({
                codes: [{ code: record.code, codeType: 1, remark: 'SKU主编号' }],
            });
        }
    };

    const submitCodes = async () => {
        try {
            const values = await codeForm.validateFields();
            await saveMaterialCodes(codeTarget.id, values.codes || []);
            message.success('编码保存成功');
            setCodeModalOpen(false);
            loadSkus();
        } catch (error: any) {
            if (error?.errorFields) return;
            message.error(error.message || '保存失败');
        }
    };

    const columns = [
        { title: 'SKU编号', dataIndex: 'code', key: 'code', width: 130 },
        { title: 'SKU名称', dataIndex: 'name', key: 'name', ellipsis: true },
        {
            title: '销售属性',
            dataIndex: 'salePropertyText',
            key: 'salePropertyText',
            width: 220,
            render: (text: string) =>
                text ? <Tag color="geekblue">{text}</Tag> : <span style={{ color: '#999' }}>—</span>,
        },
        {
            title: '采购价',
            dataIndex: 'purchasePrice',
            key: 'purchasePrice',
            width: 100,
            render: (v: number) => `¥${Number(v ?? 0).toFixed(2)}`,
        },
        {
            title: '销售价',
            dataIndex: 'salePrice',
            key: 'salePrice',
            width: 100,
            render: (v: number) => `¥${Number(v ?? 0).toFixed(2)}`,
        },
        {
            title: '多码',
            dataIndex: 'isMultiCode',
            key: 'isMultiCode',
            width: 80,
            render: (v: number) => (v === 1 ? <Tag color="green">是</Tag> : <Tag>否</Tag>),
        },
        {
            title: '状态',
            dataIndex: 'status',
            key: 'status',
            width: 80,
            render: (status: number) => (
                <span style={{ color: status === 1 ? '#52c41a' : '#ff4d4f' }}>
                    {status === 1 ? '启用' : '禁用'}
                </span>
            ),
        },
        {
            title: '操作',
            key: 'action',
            width: 110,
            fixed: 'right' as const,
            render: (_: any, record: any) => (
                <Button type="link" size="small" icon={<BarcodeOutlined />} onClick={() => openCodeModal(record)}>
                    编码
                </Button>
            ),
        },
    ];

    return (
        <>
            <Drawer
                title={`SKU 列表${spuName ? ' - ' + spuName : ''}`}
                open={open}
                onClose={onClose}
                width={980}
                destroyOnClose
            >
                <Table
                    loading={loading}
                    columns={columns}
                    dataSource={skus}
                    rowKey="id"
                    size="small"
                    scroll={{ x: 900 }}
                    pagination={false}
                />
            </Drawer>

            <Modal
                title={`一品多码 - ${codeTarget?.name || ''}`}
                open={codeModalOpen}
                onOk={submitCodes}
                onCancel={() => setCodeModalOpen(false)}
                width={640}
                destroyOnClose
            >
                <Form form={codeForm} layout="vertical">
                    <Form.List name="codes">
                        {(fields, { add, remove }) => (
                            <>
                                <div style={{ marginBottom: 8, color: '#888' }}>
                                    一个 SKU 可挂多个编码，扫码时任意一个码都能定位到它。主编号不可删除。
                                </div>
                                {fields.map(({ key, name, ...rest }) => (
                                    <Space key={key} align="baseline" style={{ display: 'flex', marginBottom: 8 }}>
                                        <Form.Item
                                            {...rest}
                                            name={[name, 'code']}
                                            rules={[{ required: true, message: '请输入编码' }]}
                                            style={{ marginBottom: 0 }}
                                        >
                                            <Input placeholder="编码 / 条码" style={{ width: 240 }} />
                                        </Form.Item>
                                        <Form.Item {...rest} name={[name, 'codeType']} style={{ marginBottom: 0 }}>
                                            <Select style={{ width: 110 }}>
                                                <Select.Option value={1}>主编号</Select.Option>
                                                <Select.Option value={2}>条码</Select.Option>
                                                <Select.Option value={3}>其他</Select.Option>
                                            </Select>
                                        </Form.Item>
                                        <Form.Item {...rest} name={[name, 'remark']} style={{ marginBottom: 0 }}>
                                            <Input placeholder="备注" style={{ width: 140 }} />
                                        </Form.Item>
                                        <MinusCircleOutlined onClick={() => remove(name)} />
                                    </Space>
                                ))}
                                <Button
                                    type="dashed"
                                    onClick={() => add({ code: '', codeType: 2, remark: '' })}
                                    block
                                    icon={<PlusOutlined />}
                                >
                                    添加编码
                                </Button>
                            </>
                        )}
                    </Form.List>
                </Form>
            </Modal>
        </>
    );
};

export default SkuDrawer;

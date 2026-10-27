import React, { useEffect, useState } from 'react';
import { Card, Form, Switch, InputNumber, Button, message, Spin, Space, Typography, Divider } from 'antd';
import { SaveOutlined, ReloadOutlined } from '@ant-design/icons';
import { getTakeStockConfig, saveTakeStockConfig } from '@/api/stock/takeConfig';

const { Paragraph, Text } = Typography;

/**
 * 盘点参数设置（单行配置）
 */
const TakeStockConfigPage: React.FC = () => {
    const [form] = Form.useForm();
    const [loading, setLoading] = useState(false);
    const [saving, setSaving] = useState(false);

    const loadData = async () => {
        setLoading(true);
        try {
            const res: any = await getTakeStockConfig();
            form.setFieldsValue({
                ...res,
                showProduct: res?.showProduct === 1,
                showStock: res?.showStock === 1,
                autoChangeStock: res?.autoChangeStock === 1,
                allowChangeNum: res?.allowChangeNum === 1,
            });
        } catch (error) {
            message.error('加载盘点参数失败');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadData();
    }, []);

    const handleSave = async () => {
        try {
            const values = await form.validateFields();
            setSaving(true);
            await saveTakeStockConfig(values);
            message.success('保存成功');
            loadData();
        } catch (error) {
            console.error('保存失败', error);
        } finally {
            setSaving(false);
        }
    };

    return (
        <div style={{ padding: '24px' }}>
            <Card
                title="盘点参数设置"
                extra={
                    <Space>
                        <Button icon={<ReloadOutlined />} onClick={loadData}>
                            重置
                        </Button>
                        <Button type="primary" icon={<SaveOutlined />} loading={saving} onClick={handleSave}>
                            保存
                        </Button>
                    </Space>
                }
            >
                <Spin spinning={loading}>
                    <Paragraph type="secondary" style={{ marginBottom: 16 }}>
                        这些参数控制「盘点任务」与「盘点单」的行为。改动即时生效，对所有仓库统一适用。
                    </Paragraph>

                    <Form form={form} layout="horizontal" labelCol={{ span: 6 }} wrapperCol={{ span: 12 }}>
                        <Form.Item
                            name="showProduct"
                            label="带出任务商品"
                            valuePropName="checked"
                            getValueFromEvent={(checked) => (checked ? 1 : 0)}
                            extra="开启后，盘点单关联盘点任务时自动带出任务中的商品明细"
                        >
                            <Switch checkedChildren="开" unCheckedChildren="关" />
                        </Form.Item>

                        <Form.Item
                            name="showStock"
                            label="显示库存快照"
                            valuePropName="checked"
                            getValueFromEvent={(checked) => (checked ? 1 : 0)}
                            extra="开启后，显示盘点任务创建时记录的系统库存数量，便于对照"
                        >
                            <Switch checkedChildren="开" unCheckedChildren="关" />
                        </Form.Item>

                        <Divider style={{ margin: '8px 0 16px' }} />

                        <Form.Item
                            name="autoChangeStock"
                            label="自动调整库存"
                            valuePropName="checked"
                            getValueFromEvent={(checked) => (checked ? 1 : 0)}
                            extra={
                                <Text type="danger">
                                    建议保持关闭。开启后盘点差异生成时会直接改动库存，不再经过人工审核
                                </Text>
                            }
                        >
                            <Switch checkedChildren="开" unCheckedChildren="关" />
                        </Form.Item>

                        <Form.Item
                            name="allowChangeNum"
                            label="允许手工改数"
                            valuePropName="checked"
                            getValueFromEvent={(checked) => (checked ? 1 : 0)}
                            extra="关闭后，盘点数量以盘点任务录入的原始值为准，不可修改"
                        >
                            <Switch checkedChildren="开" unCheckedChildren="关" />
                        </Form.Item>

                        <Form.Item
                            name="cancelHours"
                            label="任务超时作废"
                            rules={[{ required: true, message: '请输入超时小时数' }]}
                            extra="盘点任务创建后超过该小时数仍未完成则自动作废；填 0 表示不自动作废"
                        >
                            <InputNumber min={0} max={720} addonAfter="小时" style={{ width: 200 }} />
                        </Form.Item>
                    </Form>
                </Spin>
            </Card>
        </div>
    );
};

export default TakeStockConfigPage;

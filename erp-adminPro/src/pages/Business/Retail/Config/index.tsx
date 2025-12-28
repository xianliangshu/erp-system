import React, { useState, useEffect } from 'react';
import { Card, Form, Switch, Button, message, Spin } from 'antd';
import { SaveOutlined } from '@ant-design/icons';
import { getRetailConfig, updateRetailConfig } from '@/api/business/retail';

const RetailConfig: React.FC = () => {
    const [form] = Form.useForm();
    const [loading, setLoading] = useState(false);
    const [submitting, setSubmitting] = useState(false);

    const loadConfig = async () => {
        setLoading(true);
        try {
            const res: any = await getRetailConfig();
            form.setFieldsValue(res);
        } catch (error) {
            message.error('加载配置失败');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadConfig();
    }, []);

    const onFinish = async (values: any) => {
        setSubmitting(true);
        try {
            await updateRetailConfig(values);
            message.success('保存成功');
        } catch (error) {
            message.error('保存失败');
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <div style={{ padding: '24px' }}>
            <Card title="零售配置" style={{ maxWidth: 600, margin: '0 auto' }}>
                <Spin spinning={loading}>
                    <Form
                        form={form}
                        layout="vertical"
                        onFinish={onFinish}
                        initialValues={{ outStockUnApprove: false, returnStockUnApprove: false }}
                    >
                        <Form.Item
                            name="outStockUnApprove"
                            label="零售出库单自动审核"
                            valuePropName="checked"
                            extra="开启后，零售出库单提交后将自动审核通过并扣减库存"
                        >
                            <Switch />
                        </Form.Item>
                        <Form.Item
                            name="returnStockUnApprove"
                            label="零售退货单自动审核"
                            valuePropName="checked"
                            extra="开启后，零售退货单提交后将自动审核通过并增加库存"
                        >
                            <Switch />
                        </Form.Item>
                        <Form.Item>
                            <Button type="primary" icon={<SaveOutlined />} loading={submitting} htmlType="submit" block>
                                保存配置
                            </Button>
                        </Form.Item>
                    </Form>
                </Spin>
            </Card>
        </div>
    );
};

export default RetailConfig;

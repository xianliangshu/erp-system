import React, { useEffect } from 'react'
import { Modal, Form, Input, Radio, message } from 'antd'
import { createWarehouse, updateWarehouse, type Warehouse } from '@/api/basedata/warehouse'

interface WarehouseFormProps {
    visible: boolean
    warehouse: Warehouse | null
    onCancel: () => void
    onSuccess: () => void
}

const WarehouseForm: React.FC<WarehouseFormProps> = ({ visible, warehouse, onCancel, onSuccess }) => {
    const [form] = Form.useForm()

    useEffect(() => {
        if (visible) {
            if (warehouse) {
                form.setFieldsValue(warehouse)
            } else {
                form.resetFields()
            }
        }
    }, [visible, warehouse, form])

    const handleSubmit = async () => {
        try {
            const values = await form.validateFields()
            if (warehouse) {
                await updateWarehouse({ ...warehouse, ...values })
                message.success('更新成功')
            } else {
                await createWarehouse(values)
                message.success('创建成功')
            }
            onSuccess()
        } catch (error) {
            console.error('表单验证失败:', error)
        }
    }

    return (
        <Modal
            title={warehouse ? '编辑仓库' : '新增仓库'}
            open={visible}
            onOk={handleSubmit}
            onCancel={onCancel}
            width={600}
            destroyOnClose
        >
            <Form
                form={form}
                layout="vertical"
                initialValues={{ status: 1, isDefault: 0 }}
            >
                <Form.Item
                    label="仓库编号"
                    name="code"
                    tooltip="留空则系统自动生成，格式：WH000001"
                >
                    <Input placeholder="留空系统自动生成" />
                </Form.Item>

                <Form.Item
                    label="仓库名称"
                    name="name"
                    rules={[{ required: true, message: '请输入仓库名称' }]}
                >
                    <Input placeholder="请输入仓库名称" />
                </Form.Item>

                <Form.Item
                    label="联系人"
                    name="contact"
                >
                    <Input placeholder="请输入联系人" />
                </Form.Item>

                <Form.Item
                    label="联系电话"
                    name="phone"
                >
                    <Input placeholder="请输入联系电话" />
                </Form.Item>

                <Form.Item
                    label="仓库地址"
                    name="address"
                >
                    <Input placeholder="请输入仓库地址" />
                </Form.Item>

                <Form.Item
                    label="是否默认仓库"
                    name="isDefault"
                    rules={[{ required: true, message: '请选择是否默认仓库' }]}
                >
                    <Radio.Group>
                        <Radio value={1}>是</Radio>
                        <Radio value={0}>否</Radio>
                    </Radio.Group>
                </Form.Item>

                <Form.Item
                    label="状态"
                    name="status"
                    rules={[{ required: true, message: '请选择状态' }]}
                >
                    <Radio.Group>
                        <Radio value={1}>启用</Radio>
                        <Radio value={0}>禁用</Radio>
                    </Radio.Group>
                </Form.Item>

                <Form.Item
                    label="备注"
                    name="remark"
                >
                    <Input.TextArea rows={4} placeholder="请输入备注" />
                </Form.Item>
            </Form>
        </Modal>
    )
}

export default WarehouseForm

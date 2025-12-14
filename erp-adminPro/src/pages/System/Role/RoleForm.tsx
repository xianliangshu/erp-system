import React, { useEffect } from 'react'
import { Modal, Form, Input, Radio, message } from 'antd'
import { createRole, updateRole, type Role } from '@/api/system/role'

interface RoleFormProps {
    visible: boolean
    role: Role | null
    onCancel: () => void
    onSuccess: () => void
}

const RoleForm: React.FC<RoleFormProps> = ({ visible, role, onCancel, onSuccess }) => {
    const [form] = Form.useForm()

    useEffect(() => {
        if (visible) {
            if (role) {
                form.setFieldsValue(role)
            } else {
                form.resetFields()
            }
        }
    }, [visible, role, form])

    const handleSubmit = async () => {
        try {
            const values = await form.validateFields()
            if (role) {
                await updateRole({ ...role, ...values })
                message.success('更新成功')
            } else {
                await createRole(values)
                message.success('创建成功')
            }
            onSuccess()
        } catch (error) {
            console.error('表单验证失败:', error)
        }
    }

    return (
        <Modal
            title={role ? '编辑角色' : '新增角色'}
            open={visible}
            onOk={handleSubmit}
            onCancel={onCancel}
            width={600}
            destroyOnClose
        >
            <Form
                form={form}
                layout="vertical"
                initialValues={{ status: 1 }}
            >
                <Form.Item
                    label="角色编号"
                    name="code"
                    tooltip="留空则系统自动生成，格式：R000001"
                >
                    <Input placeholder="留空系统自动生成" />
                </Form.Item>

                <Form.Item
                    label="角色名称"
                    name="name"
                    rules={[{ required: true, message: '请输入角色名称' }]}
                >
                    <Input placeholder="请输入角色名称" />
                </Form.Item>

                <Form.Item
                    label="权限标识"
                    name="permissionCode"
                >
                    <Input placeholder="请输入权限标识,如: admin, user" />
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

export default RoleForm

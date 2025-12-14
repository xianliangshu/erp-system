import React, { useEffect } from 'react'
import { Modal, Form, Input, Select, Radio, message } from 'antd'
import { createUser, updateUser, type User } from '@/api/system/user'

const { Option } = Select
const { TextArea } = Input

interface UserFormProps {
    visible: boolean
    user: User | null
    onCancel: () => void
    onSuccess: () => void
}

const UserForm: React.FC<UserFormProps> = ({ visible, user, onCancel, onSuccess }) => {
    const [form] = Form.useForm()
    const isEdit = !!user

    useEffect(() => {
        if (visible) {
            if (user) {
                form.setFieldsValue(user)
            } else {
                form.resetFields()
            }
        }
    }, [visible, user, form])

    const handleSubmit = async () => {
        try {
            const values = await form.validateFields()
            if (isEdit) {
                await updateUser({ ...user, ...values })
                message.success('更新成功')
            } else {
                await createUser(values)
                message.success('创建成功')
            }
            onSuccess()
        } catch (error) {
            message.error(isEdit ? '更新失败' : '创建失败')
        }
    }

    return (
        <Modal
            title={isEdit ? '编辑用户' : '新增用户'}
            open={visible}
            onOk={handleSubmit}
            onCancel={onCancel}
            width={600}
            destroyOnClose
        >
            <Form
                form={form}
                labelCol={{ span: 6 }}
                wrapperCol={{ span: 16 }}
                autoComplete="off"
            >
                <Form.Item
                    label="用户名"
                    name="username"
                    rules={[
                        { required: true, message: '请输入用户名' },
                        { min: 3, max: 20, message: '用户名长度为3-20个字符' },
                        { pattern: /^[a-zA-Z0-9_]+$/, message: '只能包含字母、数字和下划线' }
                    ]}
                >
                    <Input placeholder="请输入用户名" disabled={isEdit} />
                </Form.Item>

                {!isEdit && (
                    <Form.Item
                        label="密码"
                        name="password"
                        rules={[
                            { required: true, message: '请输入密码' },
                            { min: 6, message: '密码至少6位' }
                        ]}
                    >
                        <Input.Password placeholder="请输入密码" />
                    </Form.Item>
                )}

                <Form.Item
                    label="昵称"
                    name="nickname"
                    rules={[{ max: 50, message: '昵称最多50个字符' }]}
                >
                    <Input placeholder="请输入昵称" />
                </Form.Item>

                <Form.Item
                    label="真实姓名"
                    name="realName"
                    rules={[{ max: 50, message: '真实姓名最多50个字符' }]}
                >
                    <Input placeholder="请输入真实姓名" />
                </Form.Item>

                <Form.Item
                    label="手机号"
                    name="phone"
                    rules={[
                        { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号' }
                    ]}
                >
                    <Input placeholder="请输入手机号" />
                </Form.Item>

                <Form.Item
                    label="邮箱"
                    name="email"
                    rules={[
                        { type: 'email', message: '请输入正确的邮箱地址' }
                    ]}
                >
                    <Input placeholder="请输入邮箱" />
                </Form.Item>

                <Form.Item
                    label="性别"
                    name="gender"
                    initialValue={0}
                >
                    <Radio.Group>
                        <Radio value={0}>未知</Radio>
                        <Radio value={1}>男</Radio>
                        <Radio value={2}>女</Radio>
                    </Radio.Group>
                </Form.Item>

                <Form.Item
                    label="状态"
                    name="status"
                    initialValue={1}
                >
                    <Radio.Group>
                        <Radio value={1}>启用</Radio>
                        <Radio value={0}>禁用</Radio>
                    </Radio.Group>
                </Form.Item>

                <Form.Item
                    label="备注"
                    name="remark"
                    rules={[{ max: 500, message: '备注最多500个字符' }]}
                >
                    <TextArea rows={3} placeholder="请输入备注" />
                </Form.Item>
            </Form>
        </Modal>
    )
}

export default UserForm

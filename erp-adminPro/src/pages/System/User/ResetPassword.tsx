import React from 'react'
import { Modal, Form, Input, message } from 'antd'
import { resetPassword, type User } from '@/api/system/user'

interface ResetPasswordProps {
    visible: boolean
    user: User
    onCancel: () => void
    onSuccess: () => void
}

const ResetPassword: React.FC<ResetPasswordProps> = ({ visible, user, onCancel, onSuccess }) => {
    const [form] = Form.useForm()

    const handleSubmit = async () => {
        try {
            const values = await form.validateFields()
            await resetPassword(user.id, values.newPassword)
            message.success('密码重置成功')
            onSuccess()
        } catch (error) {
            message.error('密码重置失败')
        }
    }

    return (
        <Modal
            title="重置密码"
            open={visible}
            onOk={handleSubmit}
            onCancel={onCancel}
            destroyOnClose
        >
            <Form
                form={form}
                labelCol={{ span: 6 }}
                wrapperCol={{ span: 16 }}
            >
                <Form.Item label="用户名">
                    <span>{user.username}</span>
                </Form.Item>

                <Form.Item
                    label="新密码"
                    name="newPassword"
                    rules={[
                        { required: true, message: '请输入新密码' },
                        { min: 6, message: '密码至少6位' }
                    ]}
                >
                    <Input.Password placeholder="请输入新密码" />
                </Form.Item>

                <Form.Item
                    label="确认密码"
                    name="confirmPassword"
                    dependencies={['newPassword']}
                    rules={[
                        { required: true, message: '请确认密码' },
                        ({ getFieldValue }) => ({
                            validator(_, value) {
                                if (!value || getFieldValue('newPassword') === value) {
                                    return Promise.resolve()
                                }
                                return Promise.reject(new Error('两次输入的密码不一致'))
                            }
                        })
                    ]}
                >
                    <Input.Password placeholder="请再次输入密码" />
                </Form.Item>
            </Form>
        </Modal>
    )
}

export default ResetPassword

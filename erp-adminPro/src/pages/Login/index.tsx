import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Form, Input, Button, Card, message } from 'antd'
import { UserOutlined, LockOutlined } from '@ant-design/icons'
import { login } from '@/api/auth'
import { useUserStore } from '@/store/userStore'
import './index.css'

interface LoginForm {
    username: string
    password: string
}

const Login = () => {
    const [loading, setLoading] = useState(false)
    const navigate = useNavigate()
    const { setUser, setToken } = useUserStore()

    const onFinish = async (values: LoginForm) => {
        try {
            setLoading(true)
            const result = await login(values)

            // 保存token和用户信息
            setToken(result.token)
            setUser(result.user)

            message.success('登录成功!')
            navigate('/')
        } catch (error: any) {
            // 错误已经在 request.ts 的响应拦截器中显示了，这里不需要再次显示
            console.error('登录失败:', error)
        } finally {
            setLoading(false)
        }
    }


    return (
        <div className="login-container">
            <Card className="login-card" title="ERP管理系统">
                <Form
                    name="login"
                    onFinish={onFinish}
                    autoComplete="off"
                    size="large"
                >
                    <Form.Item
                        name="username"
                        rules={[{ required: true, message: '请输入用户名!' }]}
                    >
                        <Input
                            prefix={<UserOutlined />}
                            placeholder="用户名"
                        />
                    </Form.Item>

                    <Form.Item
                        name="password"
                        rules={[{ required: true, message: '请输入密码!' }]}
                    >
                        <Input.Password
                            prefix={<LockOutlined />}
                            placeholder="密码"
                        />
                    </Form.Item>

                    <Form.Item>
                        <Button
                            type="primary"
                            htmlType="submit"
                            loading={loading}
                            block
                        >
                            登录
                        </Button>
                    </Form.Item>
                </Form>
            </Card>
        </div>
    )
}

export default Login

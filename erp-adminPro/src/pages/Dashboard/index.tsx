import { Card, Row, Col, Statistic } from 'antd'
import { UserOutlined, TeamOutlined, ApartmentOutlined, MenuOutlined } from '@ant-design/icons'
import { useUserStore } from '@/store/userStore'

const Dashboard = () => {
    const { user } = useUserStore()

    return (
        <div>
            <Card title="欢迎使用ERP管理系统" style={{ marginBottom: 24 }}>
                <p style={{ fontSize: 16 }}>您好，{user?.nickname || user?.username}！</p>
                <p style={{ color: '#666' }}>欢迎回来，这是您的工作台。</p>
            </Card>

            <Row gutter={16}>
                <Col span={6}>
                    <Card>
                        <Statistic
                            title="用户总数"
                            value={0}
                            prefix={<UserOutlined />}
                            valueStyle={{ color: '#3f8600' }}
                        />
                    </Card>
                </Col>
                <Col span={6}>
                    <Card>
                        <Statistic
                            title="角色总数"
                            value={0}
                            prefix={<TeamOutlined />}
                            valueStyle={{ color: '#1890ff' }}
                        />
                    </Card>
                </Col>
                <Col span={6}>
                    <Card>
                        <Statistic
                            title="部门总数"
                            value={0}
                            prefix={<ApartmentOutlined />}
                            valueStyle={{ color: '#cf1322' }}
                        />
                    </Card>
                </Col>
                <Col span={6}>
                    <Card>
                        <Statistic
                            title="菜单总数"
                            value={0}
                            prefix={<MenuOutlined />}
                            valueStyle={{ color: '#722ed1' }}
                        />
                    </Card>
                </Col>
            </Row>
        </div>
    )
}

export default Dashboard

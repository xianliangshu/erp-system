import { useState, useEffect } from 'react'
import { Layout, Menu, Dropdown, Breadcrumb, Button, Spin } from 'antd'
import { Outlet, useNavigate, useLocation } from 'react-router-dom'
import {
    MenuFoldOutlined,
    MenuUnfoldOutlined,
    UserOutlined,
    LogoutOutlined,
    DashboardOutlined
} from '@ant-design/icons'
import { useUserStore } from '@/store/userStore'
import { useMenuStore } from '@/store/menuStore'
import { transformMenuToAntdFormat, filterVisibleMenus } from '@/utils/menuUtils'
import './MainLayout.css'

const { Header, Sider, Content } = Layout

const MainLayout = () => {
    const navigate = useNavigate()
    const location = useLocation()
    const [collapsed, setCollapsed] = useState(false)

    const { user, clearUser } = useUserStore()
    const { menus, loading, loadUserMenus, clearMenus } = useMenuStore()

    // 加载用户菜单
    useEffect(() => {
        if (user?.id && menus.length === 0) {
            loadUserMenus(user.id)
        }
    }, [user?.id, menus.length, loadUserMenus])

    // 处理退出登录
    const handleLogout = () => {
        clearUser()
        clearMenus()
        navigate('/login')
    }

    // 用户下拉菜单
    const userMenuItems = [
        {
            key: 'logout',
            icon: <LogoutOutlined />,
            label: '退出登录',
            onClick: handleLogout
        }
    ]

    // 过滤并转换菜单数据
    console.log('🔍 Raw menus from store:', menus)
    const visibleMenus = filterVisibleMenus(menus)
    console.log('🔍 Visible menus after filter:', visibleMenus)
    const menuItems = transformMenuToAntdFormat(visibleMenus)
    console.log('🔍 Transformed menuItems:', menuItems)

    // 临时测试: 移除所有图标,只保留 key 和 label
    const menuItemsWithoutIcons = menuItems.map((item: any) => ({
        key: item?.key,
        label: item?.label,
        children: item?.children
    }))

    // 添加仪表板菜单项
    const allMenuItems: any[] = [
        {
            key: '/dashboard',
            label: '仪表板'
            // 临时移除图标进行测试
        },
        ...menuItemsWithoutIcons
    ]

    // 打印最终菜单项数量用于调试
    console.log('🔍 Total menu items:', allMenuItems.length)
    console.log('🔍 Menu items detail:', allMenuItems)

    // 处理菜单点击
    const handleMenuClick = ({ key }: { key: string }) => {
        navigate(key)
    }

    // 获取当前选中的菜单key
    const selectedKeys = [location.pathname]

    return (
        <Layout className="main-layout">
            <Sider trigger={null} collapsible collapsed={collapsed} className="main-sider">
                <div className="logo">
                    <h2>{collapsed ? 'ERP' : 'ERP管理系统'}</h2>
                </div>
                <Spin spinning={loading}>
                    <Menu
                        theme="dark"
                        mode="inline"
                        selectedKeys={selectedKeys}
                        items={allMenuItems}
                        onClick={handleMenuClick}
                    />
                </Spin>
            </Sider>
            <Layout>
                <Header className="main-header">
                    <div className="header-left">
                        <Button
                            type="text"
                            icon={collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
                            onClick={() => setCollapsed(!collapsed)}
                            className="trigger-btn"
                        />
                        <Breadcrumb className="breadcrumb">
                            <Breadcrumb.Item>首页</Breadcrumb.Item>
                            {location.pathname !== '/dashboard' && (
                                <Breadcrumb.Item>{location.pathname}</Breadcrumb.Item>
                            )}
                        </Breadcrumb>
                    </div>
                    <div className="header-right">
                        <Dropdown menu={{ items: userMenuItems }} placement="bottomRight">
                            <div className="user-info">
                                <UserOutlined />
                                <span className="username">{user?.nickname || user?.username}</span>
                            </div>
                        </Dropdown>
                    </div>
                </Header>
                <Content className="main-content">
                    <div className="content-wrapper">
                        <Outlet />
                    </div>
                </Content>
            </Layout>
        </Layout>
    )
}

export default MainLayout

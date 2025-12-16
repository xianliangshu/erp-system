import { createBrowserRouter, Navigate } from 'react-router-dom'
import Login from '@/pages/Login'
import Dashboard from '@/pages/Dashboard'
import MainLayout from '@/components/Layout/MainLayout'
import UserManagement from '@/pages/System/User'
import RoleManagement from '@/pages/System/Role'
import DeptManagement from '@/pages/System/Dept'
import MenuManagement from '@/pages/System/Menu'
import WarehouseManagement from '@/pages/Basedata/Warehouse'
import MaterialCategoryManagement from '@/pages/Basedata/Material/Category'
import UnitManagement from '@/pages/Basedata/Material/Unit'
import MaterialManagement from '@/pages/Basedata/Material/Info'
import SupplierManagement from '@/pages/Basedata/Supplier'

// 路由守卫组件
const ProtectedRoute = ({ children }: { children: React.ReactNode }) => {
    const token = localStorage.getItem('token')
    if (!token) {
        return <Navigate to="/login" replace />
    }
    return <>{children}</>
}

// 登录页守卫
const LoginRoute = ({ children }: { children: React.ReactNode }) => {
    const token = localStorage.getItem('token')
    if (token) {
        return <Navigate to="/dashboard" replace />
    }
    return <>{children}</>
}

const router = createBrowserRouter([
    {
        path: '/login',
        element: <LoginRoute><Login /></LoginRoute>
    },
    {
        path: '/',
        element: <ProtectedRoute><MainLayout /></ProtectedRoute>,
        children: [
            {
                index: true,
                element: <Navigate to="/dashboard" replace />
            },
            {
                path: 'dashboard',
                element: <Dashboard />
            },
            {
                path: 'system/user',
                element: <UserManagement />
            },
            {
                path: 'system/role',
                element: <RoleManagement />
            },
            {
                path: 'system/dept',
                element: <DeptManagement />
            },
            {
                path: 'system/menu',
                element: <MenuManagement />
            },
            {
                path: 'basedata/warehouse',
                element: <WarehouseManagement />
            },
            {
                path: 'basedata/material/category',
                element: <MaterialCategoryManagement />
            },
            {
                path: 'basedata/material/unit',
                element: <UnitManagement />
            },
            {
                path: 'basedata/material/info',
                element: <MaterialManagement />
            },
            {
                path: 'basedata/supplier',
                element: <SupplierManagement />
            }
        ]
    },
    {
        path: '*',
        element: <Navigate to="/login" replace />
    }
])

export default router

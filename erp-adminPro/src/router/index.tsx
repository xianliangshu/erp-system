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
import CustomerManagement from '@/pages/Basedata/Customer'
import BrandManagement from '@/pages/Basedata/Brand'
import InventoryQuery from '@/pages/Business/Stock/Query'
import PurchaseOrderList from '@/pages/Business/Purchase/Order/List'
import PurchaseOrderForm from '@/pages/Business/Purchase/Order/Form'
import PurchaseOrderDetail from '@/pages/Business/Purchase/Order/Detail'
import PurchaseReceiptList from '@/pages/Business/Purchase/Receipt/List'
import PurchaseReceiptForm from '@/pages/Business/Purchase/Receipt/Form'
import PurchaseReceiptDetail from '@/pages/Business/Purchase/Receipt/Detail'
import PurchaseReturnList from '@/pages/Business/Purchase/Return/List'
import PurchaseReturnForm from '@/pages/Business/Purchase/Return/Form'
import PurchaseReturnDetail from '@/pages/Business/Purchase/Return/Detail'
import SaleOrderList from '@/pages/Business/Sale/Order/List'
import SaleOrderForm from '@/pages/Business/Sale/Order/Form'
import SaleOrderDetail from '@/pages/Business/Sale/Order/Detail'
import SaleDeliveryList from '@/pages/Business/Sale/Delivery/List'
import SaleDeliveryForm from '@/pages/Business/Sale/Delivery/Form'
import SaleDeliveryDetail from '@/pages/Business/Sale/Delivery/Detail'
import SaleReturnList from '@/pages/Business/Sale/Return/List'
import SaleReturnForm from '@/pages/Business/Sale/Return/Form'
import SaleReturnDetail from '@/pages/Business/Sale/Return/Detail'
import StockCheckList from '@/pages/Business/Stock/Check/List'
import StockCheckForm from '@/pages/Business/Stock/Check/Form'
import StockCheckDetail from '@/pages/Business/Stock/Check/Detail'
import StockTransferList from '@/pages/Business/Stock/Transfer/List'
import StockTransferForm from '@/pages/Business/Stock/Transfer/Form'
import StockTransferDetail from '@/pages/Business/Stock/Transfer/Detail'

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
            },
            {
                path: 'basedata/customer',
                element: <CustomerManagement />
            },
            {
                path: 'basedata/brand',
                element: <BrandManagement />
            },
            {
                path: 'business/stock/query',
                element: <InventoryQuery />
            },
            {
                path: 'business/purchase/order',
                element: <PurchaseOrderList />
            },
            {
                path: 'business/purchase/order/add',
                element: <PurchaseOrderForm />
            },
            {
                path: 'business/purchase/order/edit/:id',
                element: <PurchaseOrderForm />
            },
            {
                path: 'business/purchase/order/detail/:id',
                element: <PurchaseOrderDetail />
            },
            {
                path: 'business/purchase/receipt',
                element: <PurchaseReceiptList />
            },
            {
                path: 'business/purchase/receipt/add',
                element: <PurchaseReceiptForm />
            },
            {
                path: 'business/purchase/receipt/edit/:id',
                element: <PurchaseReceiptForm />
            },
            {
                path: 'business/purchase/receipt/detail/:id',
                element: <PurchaseReceiptDetail />
            },
            {
                path: 'business/purchase/return',
                element: <PurchaseReturnList />
            },
            {
                path: 'business/purchase/return/add',
                element: <PurchaseReturnForm />
            },
            {
                path: 'business/purchase/return/edit/:id',
                element: <PurchaseReturnForm />
            },
            {
                path: 'business/purchase/return/detail/:id',
                element: <PurchaseReturnDetail />
            },
            {
                path: 'business/sale/order',
                element: <SaleOrderList />
            },
            {
                path: 'business/sale/order/add',
                element: <SaleOrderForm />
            },
            {
                path: 'business/sale/order/edit/:id',
                element: <SaleOrderForm />
            },
            {
                path: 'business/sale/order/detail/:id',
                element: <SaleOrderDetail />
            },
            {
                path: 'business/sale/delivery',
                element: <SaleDeliveryList />
            },
            {
                path: 'business/sale/delivery/add',
                element: <SaleDeliveryForm />
            },
            {
                path: 'business/sale/delivery/edit/:id',
                element: <SaleDeliveryForm />
            },
            {
                path: 'business/sale/delivery/detail/:id',
                element: <SaleDeliveryDetail />
            },
            {
                path: 'business/sale/return',
                element: <SaleReturnList />
            },
            {
                path: 'business/sale/return/add',
                element: <SaleReturnForm />
            },
            {
                path: 'business/sale/return/edit/:id',
                element: <SaleReturnForm />
            },
            {
                path: 'business/sale/return/detail/:id',
                element: <SaleReturnDetail />
            },
            {
                path: 'business/stock/check',
                element: <StockCheckList />
            },
            {
                path: 'business/stock/check/add',
                element: <StockCheckForm />
            },
            {
                path: 'business/stock/check/edit/:id',
                element: <StockCheckForm />
            },
            {
                path: 'business/stock/check/detail/:id',
                element: <StockCheckDetail />
            },
            {
                path: 'business/stock/transfer',
                element: <StockTransferList />
            },
            {
                path: 'business/stock/transfer/add',
                element: <StockTransferForm />
            },
            {
                path: 'business/stock/transfer/edit/:id',
                element: <StockTransferForm />
            },
            {
                path: 'business/stock/transfer/detail/:id',
                element: <StockTransferDetail />
            }
        ]
    },
    {
        path: '*',
        element: <Navigate to="/login" replace />
    }
])

export default router

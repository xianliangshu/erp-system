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
import StockAdjustReasonList from '@/pages/Business/Stock/Adjust/Reason'
import StockAdjustSheetList from '@/pages/Business/Stock/Adjust/Sheet/List'
import StockAdjustSheetForm from '@/pages/Business/Stock/Adjust/Sheet/Form'
import RetailConfig from '@/pages/Business/Retail/Config'
import RetailOutList from '@/pages/Business/Retail/Outbound/List'
import RetailOutForm from '@/pages/Business/Retail/Outbound/Form'
import RetailOutDetail from '@/pages/Business/Retail/Outbound/Detail'
import RetailReturnList from '@/pages/Business/Retail/Return/List'
import RetailReturnForm from '@/pages/Business/Retail/Return/Form'
import RetailReturnDetail from '@/pages/Business/Retail/Return/Detail'
import IncomeExpenseItemList from '@/pages/Business/Settle/IncomeExpenseItem/List'
import FeeSheetList from '@/pages/Business/Settle/FeeSheet/List'
import FeeSheetForm from '@/pages/Business/Settle/FeeSheet/Form'
import FeeSheetDetail from '@/pages/Business/Settle/FeeSheet/Detail'
import PreSheetList from '@/pages/Business/Settle/PreSheet/List'
import PreSheetForm from '@/pages/Business/Settle/PreSheet/Form'
import PreSheetDetail from '@/pages/Business/Settle/PreSheet/Detail'
import CheckSheetList from '@/pages/Business/Settle/CheckSheet/List'
import CheckSheetForm from '@/pages/Business/Settle/CheckSheet/Form'
import CheckSheetDetail from '@/pages/Business/Settle/CheckSheet/Detail'
import SettleSheetList from '@/pages/Business/Settle/SettleSheet/List'
import SettleSheetForm from '@/pages/Business/Settle/SettleSheet/Form'
import SettleSheetDetail from '@/pages/Business/Settle/SettleSheet/Detail'
import CustomerFeeSheetList from '@/pages/Business/Settle/CustomerFeeSheet/List'
import CustomerFeeSheetForm from '@/pages/Business/Settle/CustomerFeeSheet/Form'
import CustomerFeeSheetDetail from '@/pages/Business/Settle/CustomerFeeSheet/Detail'
import CustomerPreSheetList from '@/pages/Business/Settle/CustomerPreSheet/List'
import CustomerPreSheetForm from '@/pages/Business/Settle/CustomerPreSheet/Form'
import CustomerPreSheetDetail from '@/pages/Business/Settle/CustomerPreSheet/Detail'
import CustomerCheckSheetList from '@/pages/Business/Settle/CustomerCheckSheet/List'
import CustomerCheckSheetForm from '@/pages/Business/Settle/CustomerCheckSheet/Form'
import CustomerCheckSheetDetail from '@/pages/Business/Settle/CustomerCheckSheet/Detail'
import CustomerSettleSheetList from '@/pages/Business/Settle/CustomerSettleSheet/List'
import CustomerSettleSheetForm from '@/pages/Business/Settle/CustomerSettleSheet/Form'
import CustomerSettleSheetDetail from '@/pages/Business/Settle/CustomerSettleSheet/Detail'
import ChartDashboard from '@/pages/Business/Chart/Dashboard'
import ChartPurchase from '@/pages/Business/Chart/Purchase'
import ChartSales from '@/pages/Business/Chart/Sales'
import ChartStock from '@/pages/Business/Chart/Stock'
import ChartSummary from '@/pages/Business/Chart/Summary'

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
            },
            {
                path: 'stock/adjust/reason',
                element: <StockAdjustReasonList />
            },
            {
                path: 'stock/adjust/sheet',
                element: <StockAdjustSheetList />
            },
            {
                path: 'stock/adjust/sheet/add',
                element: <StockAdjustSheetForm />
            },
            {
                path: 'stock/adjust/sheet/edit/:id',
                element: <StockAdjustSheetForm />
            },
            {
                path: 'stock/adjust/sheet/detail/:id',
                element: <StockAdjustSheetList />
            },
            {
                path: 'business/retail/config',
                element: <RetailConfig />
            },
            {
                path: 'business/retail/out',
                element: <RetailOutList />
            },
            {
                path: 'business/retail/out/add',
                element: <RetailOutForm />
            },
            {
                path: 'business/retail/out/edit/:id',
                element: <RetailOutForm />
            },
            {
                path: 'business/retail/out/detail/:id',
                element: <RetailOutDetail />
            },
            {
                path: 'business/retail/return',
                element: <RetailReturnList />
            },
            {
                path: 'business/retail/return/add',
                element: <RetailReturnForm />
            },
            {
                path: 'business/retail/return/edit/:id',
                element: <RetailReturnForm />
            },
            {
                path: 'business/retail/return/detail/:id',
                element: <RetailReturnDetail />
            },
            {
                path: 'business/settle/item',
                element: <IncomeExpenseItemList />
            },
            {
                path: 'business/settle/fee',
                element: <FeeSheetList />
            },
            {
                path: 'business/settle/fee/add',
                element: <FeeSheetForm />
            },
            {
                path: 'business/settle/fee/edit/:id',
                element: <FeeSheetForm />
            },
            {
                path: 'business/settle/fee/detail/:id',
                element: <FeeSheetDetail />
            },
            {
                path: 'business/settle/pre',
                element: <PreSheetList />
            },
            {
                path: 'business/settle/pre/add',
                element: <PreSheetForm />
            },
            {
                path: 'business/settle/pre/edit/:id',
                element: <PreSheetForm />
            },
            {
                path: 'business/settle/pre/detail/:id',
                element: <PreSheetDetail />
            },
            {
                path: 'business/settle/check',
                element: <CheckSheetList />
            },
            {
                path: 'business/settle/check/add',
                element: <CheckSheetForm />
            },
            {
                path: 'business/settle/check/edit/:id',
                element: <CheckSheetForm />
            },
            {
                path: 'business/settle/check/detail/:id',
                element: <CheckSheetDetail />
            },
            {
                path: 'business/settle/sheet',
                element: <SettleSheetList />
            },
            {
                path: 'business/settle/sheet/add',
                element: <SettleSheetForm />
            },
            {
                path: 'business/settle/sheet/edit/:id',
                element: <SettleSheetForm />
            },
            {
                path: 'business/settle/sheet/detail/:id',
                element: <SettleSheetDetail />
            },
            // Customer Fee Sheet
            {
                path: 'business/settle/customer/fee',
                element: <CustomerFeeSheetList />
            },
            {
                path: 'business/settle/customer/fee/add',
                element: <CustomerFeeSheetForm />
            },
            {
                path: 'business/settle/customer/fee/edit/:id',
                element: <CustomerFeeSheetForm />
            },
            {
                path: 'business/settle/customer/fee/detail/:id',
                element: <CustomerFeeSheetDetail />
            },
            // Customer Pre Sheet
            {
                path: 'business/settle/customer/pre',
                element: <CustomerPreSheetList />
            },
            {
                path: 'business/settle/customer/pre/add',
                element: <CustomerPreSheetForm />
            },
            {
                path: 'business/settle/customer/pre/edit/:id',
                element: <CustomerPreSheetForm />
            },
            {
                path: 'business/settle/customer/pre/detail/:id',
                element: <CustomerPreSheetDetail />
            },
            // Customer Check Sheet
            {
                path: 'business/settle/customer/check',
                element: <CustomerCheckSheetList />
            },
            {
                path: 'business/settle/customer/check/add',
                element: <CustomerCheckSheetForm />
            },
            {
                path: 'business/settle/customer/check/edit/:id',
                element: <CustomerCheckSheetForm />
            },
            {
                path: 'business/settle/customer/check/detail/:id',
                element: <CustomerCheckSheetDetail />
            },
            // Customer Settle Sheet
            {
                path: 'business/settle/customer/sheet',
                element: <CustomerSettleSheetList />
            },
            {
                path: 'business/settle/customer/sheet/add',
                element: <CustomerSettleSheetForm />
            },
            {
                path: 'business/settle/customer/sheet/edit/:id',
                element: <CustomerSettleSheetForm />
            },
            {
                path: 'business/settle/customer/sheet/detail/:id',
                element: <CustomerSettleSheetDetail />
            },
            // Chart routes
            {
                path: 'chart/dashboard',
                element: <ChartDashboard />
            },
            {
                path: 'chart/purchase',
                element: <ChartPurchase />
            },
            {
                path: 'chart/sales',
                element: <ChartSales />
            },
            {
                path: 'chart/stock',
                element: <ChartStock />
            },
            {
                path: 'chart/summary',
                element: <ChartSummary />
            }
        ]
    },
    {
        path: '*',
        element: <Navigate to="/login" replace />
    }
])

export default router

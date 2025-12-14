import { RouterProvider } from 'react-router-dom'
import { ConfigProvider, App as AntdApp } from 'antd'
import zhCN from 'antd/locale/zh_CN'
import router from './router'
import 'dayjs/locale/zh-cn'
import { useEffect } from 'react'
import { setMessageInstance } from './utils/messageHolder'

// 1. 在 AntdApp 内部再建一个“初始化器”
function MessageInit() {
    const { message } = AntdApp.useApp()
    useEffect(() => {
        setMessageInstance(message)
    }, [message])
    return null
}

function App() {
    return (
        <ConfigProvider locale={zhCN}>
            <AntdApp>
                <MessageInit />          {/* ← 拿到实例并保存 */}
                <RouterProvider router={router} />
            </AntdApp>
        </ConfigProvider>
    )
}

export default App
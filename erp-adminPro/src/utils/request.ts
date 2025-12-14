import axios from 'axios'
import type { AxiosInstance, AxiosResponse } from 'axios'
import { getMessageInstance } from './messageHolder'

// 创建axios实例
const request: AxiosInstance = axios.create({
    baseURL: '/api',
    timeout: 10000,
    headers: {
        'Content-Type': 'application/json'
    }
})

// 请求拦截器
request.interceptors.request.use(
    (config: any) => {
        // 从localStorage获取token
        const token = localStorage.getItem('token')
        if (token) {
            config.headers.Authorization = `Bearer ${token}`
        }
        return config
    },
    (error) => {
        return Promise.reject(error)
    }
)

// 响应拦截器
request.interceptors.response.use(
    (response: AxiosResponse) => {
        const { data } = response

        // 如果返回的状态码为200,说明接口请求成功
        if (data.code === 200) {
            return data.data
        }

        // 其他情况都当作错误处理
        console.log('业务错误 - code:', data.code, 'message:', data.message)
        const errorMsg = data.message || '请求失败'

        const message = getMessageInstance()

        // 直接使用 message 实例
        message?.error(errorMsg)



        return Promise.reject(new Error(errorMsg))
    },
    (error) => {
        const message = getMessageInstance()
        // 处理HTTP错误
        if (error.response) {
            const { status, data } = error.response

            // 优先使用后端返回的错误消息
            const errorMessage = data?.message || error.message
            switch (status) {
                case 401:
                    message?.error('未授权,请重新登录')
                    // 清除token并跳转到登录页
                    localStorage.removeItem('token')
                    window.location.href = '/login'
                    break
                case 403:
                    message?.error(errorMessage || '拒绝访问')
                    break
                case 404:
                    message?.error('请求地址不存在')
                    break
                case 500:
                    message?.error(errorMessage || '服务器错误')
                    break
                default:
                    message?.error(errorMessage || '请求失败')
            }
        } else {
            message?.error('网络错误,请检查网络连接')
        }
        return Promise.reject(error)
    }
)

export default request

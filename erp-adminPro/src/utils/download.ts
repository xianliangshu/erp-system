import axios from 'axios'
import { getMessageInstance } from './messageHolder'

/**
 * 文件下载（Excel 导出 / 导入模板）
 *
 * <p>刻意不复用 utils/request：导出接口直接把二进制流写进响应，
 * 没有 {code, data} 包装，走 request 的响应拦截器会被当成业务错误弹提示。</p>
 *
 * @param url          相对于 /api 的路径，如 /basedata/material/export
 * @param params       查询参数（导出时传列表页当前的筛选条件）
 * @param fallbackName 服务端未给出文件名时使用的默认名
 */
export const downloadFile = async (
    url: string,
    params?: Record<string, any>,
    fallbackName = 'export.xlsx'
): Promise<void> => {
    const message = getMessageInstance()
    try {
        const token = localStorage.getItem('token')
        const resp = await axios.get(`/api${url}`, {
            params,
            responseType: 'blob',
            headers: token ? { Authorization: `Bearer ${token}` } : {}
        })

        // 后端抛业务异常时返回的仍是 HTTP 200 + JSON，被 responseType:'blob' 包成了 Blob，
        // 需要读出来还原成提示，否则用户只会看到一个坏掉的 xlsx
        const blob = resp.data as Blob
        if (blob.type && blob.type.indexOf('application/json') !== -1) {
            const body = JSON.parse(await blob.text())
            message?.error(body.message || '导出失败')
            return
        }

        const fileName = parseFileName(resp.headers['content-disposition']) || fallbackName
        saveBlob(blob, fileName)
    } catch (error: any) {
        if (error?.response?.status === 401) {
            message?.error('未授权,请重新登录')
            localStorage.removeItem('token')
            window.location.href = '/login'
            return
        }
        message?.error('导出失败，请稍后重试')
    }
}

/**
 * 从 Content-Disposition 解析文件名
 *
 * 后端格式为 `attachment;filename*=utf-8''%E7%89%A9%E6%96%99.xlsx`，
 * 同时兼容普通的 `filename="xxx.xlsx"`。
 */
const parseFileName = (disposition?: string): string => {
    if (!disposition) return ''
    const utf8Match = /filename\*=utf-8''([^;]+)/i.exec(disposition)
    const raw = utf8Match?.[1] || /filename="?([^";]+)"?/i.exec(disposition)?.[1]
    if (!raw) return ''
    try {
        return decodeURIComponent(raw)
    } catch {
        return raw
    }
}

/** 通过临时 <a> 触发浏览器下载 */
const saveBlob = (blob: Blob, fileName: string) => {
    const objectUrl = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = objectUrl
    link.download = fileName
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(objectUrl)
}

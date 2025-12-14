// messageHolder.ts
import { type MessageInstance } from 'antd/es/message/interface'

let message: MessageInstance | null = null

export const setMessageInstance = (instance: MessageInstance) => {
    message = instance
}

export const getMessageInstance = () => {
    if (!message) {
        console.warn('message 实例尚未初始化，请先调用 setMessageInstance')
    }
    return message
}
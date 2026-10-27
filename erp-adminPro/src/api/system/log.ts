import request from '@/utils/request'

// ==================== 类型定义 ====================

/** 与后端 com.erp.system.entity.SysLoginLog 对应 */
export interface LoginLog {
    id: number
    username?: string
    loginIp?: string
    loginLocation?: string
    browser?: string
    os?: string
    /** 0-成功 1-失败 */
    status: number
    msg?: string
    loginTime?: string
}

/** 与后端 com.erp.system.entity.SysOperationLog 对应 */
export interface OperationLog {
    id: number
    /** 操作模块，如「物料信息」 */
    title?: string
    /** 0-其它 1-新增 2-修改 3-删除 4-查询 5-导出 6-导入 */
    businessType: number
    method?: string
    requestMethod?: string
    /** 0-其它 1-后台用户 2-手机端用户 */
    operatorType?: number
    operName?: string
    operUrl?: string
    operIp?: string
    operLocation?: string
    operParam?: string
    jsonResult?: string
    /** 0-正常 1-异常 */
    status: number
    errorMsg?: string
    operTime?: string
    /** 耗时（毫秒） */
    costTime?: number
}

export interface LogQueryParam {
    current?: number
    size?: number
    username?: string
    title?: string
    status?: number
    businessType?: number
    /** yyyy-MM-dd */
    beginDate?: string
    /** yyyy-MM-dd */
    endDate?: string
}

export interface LogPageData<T> {
    records: T[]
    total: number
}

// ==================== API 方法 ====================

/**
 * 业务类型字典，与后端 com.erp.system.enums.BusinessType 保持一致
 */
export const BUSINESS_TYPE_OPTIONS = [
    { value: 0, label: '其它' },
    { value: 1, label: '新增' },
    { value: 2, label: '修改' },
    { value: 3, label: '删除' },
    { value: 4, label: '查询' },
    { value: 5, label: '导出' },
    { value: 6, label: '导入' },
]

/**
 * 分页查询登录日志
 */
export const getLoginLogPage = (params: LogQueryParam): Promise<LogPageData<LoginLog>> => {
    return request.get('/system/login-log/page', { params }) as unknown as Promise<LogPageData<LoginLog>>
}

/**
 * 清空登录日志（TRUNCATE，不可恢复）
 */
export const cleanLoginLog = (): Promise<void> => {
    return request.delete('/system/login-log/clean') as unknown as Promise<void>
}

/**
 * 分页查询操作日志
 */
export const getOperationLogPage = (params: LogQueryParam): Promise<LogPageData<OperationLog>> => {
    return request.get('/system/operation-log/page', { params }) as unknown as Promise<LogPageData<OperationLog>>
}

/**
 * 清空操作日志（TRUNCATE，不可恢复）
 */
export const cleanOperationLog = (): Promise<void> => {
    return request.delete('/system/operation-log/clean') as unknown as Promise<void>
}

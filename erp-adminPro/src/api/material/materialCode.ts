import request from '@/utils/request';

/**
 * SKU 一品多码 API
 *
 * codeType: 1=主编号 2=条码 3=其他
 */

export const MATERIAL_CODE_TYPE = {
    MAIN: 1,
    BARCODE: 2,
    OTHER: 3,
} as const;

export const materialCodeTypeText = (type: number) => {
    if (type === 1) return '主编号';
    if (type === 2) return '条码';
    return '其他';
};

// 查询某 SKU 的全部编码
export const getMaterialCodes = (materialId: number) => {
    return request({
        url: '/basedata/material-code/list',
        method: 'get',
        params: { materialId }
    });
};

// 按编码查询（扫码）
export const getMaterialCodeByCode = (code: string, codeType?: number) => {
    return request({
        url: '/basedata/material-code/by-code',
        method: 'get',
        params: { code, codeType }
    });
};

// 整体覆盖某 SKU 的编码集合
export const saveMaterialCodes = (materialId: number, codes: any[]) => {
    return request({
        url: `/basedata/material-code/${materialId}`,
        method: 'post',
        data: codes
    });
};

// 删除单条编码
export const deleteMaterialCode = (id: number) => {
    return request({
        url: `/basedata/material-code/${id}`,
        method: 'delete'
    });
};

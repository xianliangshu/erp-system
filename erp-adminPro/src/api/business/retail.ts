import request from '@/utils/request';

// 零售配置
export function getRetailConfig() {
    return request({
        url: '/retail/config',
        method: 'get',
    });
}

export function updateRetailConfig(data: any) {
    return request({
        url: '/retail/config',
        method: 'put',
        data,
    });
}

// 零售出库单
export function getRetailOutPage(params: any) {
    return request({
        url: '/retail/out/page',
        method: 'get',
        params,
    });
}

export function getRetailOutById(id: number) {
    return request({
        url: `/retail/out/${id}`,
        method: 'get',
    });
}

export function addRetailOut(data: any) {
    return request({
        url: '/retail/out',
        method: 'post',
        data,
    });
}

export function updateRetailOut(data: any) {
    return request({
        url: '/retail/out',
        method: 'put',
        data,
    });
}

export function approveRetailOut(id: number) {
    return request({
        url: `/retail/out/approve/${id}`,
        method: 'post',
    });
}

export function refuseRetailOut(id: number, refuseReason: string) {
    return request({
        url: `/retail/out/refuse/${id}`,
        method: 'post',
        data: { refuseReason },
    });
}

export function deleteRetailOut(id: number) {
    return request({
        url: `/retail/out/${id}`,
        method: 'delete',
    });
}

// 零售退货单
export function getRetailReturnPage(params: any) {
    return request({
        url: '/retail/return/page',
        method: 'get',
        params,
    });
}

export function getRetailReturnById(id: number) {
    return request({
        url: `/retail/return/${id}`,
        method: 'get',
    });
}

export function addRetailReturn(data: any) {
    return request({
        url: '/retail/return',
        method: 'post',
        data,
    });
}

export function updateRetailReturn(data: any) {
    return request({
        url: '/retail/return',
        method: 'put',
        data,
    });
}

export function approveRetailReturn(id: number) {
    return request({
        url: `/retail/return/approve/${id}`,
        method: 'post',
    });
}

export function refuseRetailReturn(id: number, refuseReason: string) {
    return request({
        url: `/retail/return/refuse/${id}`,
        method: 'post',
        data: { refuseReason },
    });
}

export function deleteRetailReturn(id: number) {
    return request({
        url: `/retail/return/${id}`,
        method: 'delete',
    });
}

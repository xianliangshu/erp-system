import request from '@/utils/request';

// 收支项目
export function getSettleItemPage(params: any) {
    return request({
        url: '/business/settle/item/page',
        method: 'get',
        params,
    });
}

export function getSettleItemById(id: number) {
    return request({
        url: `/business/settle/item/${id}`,
        method: 'get',
    });
}

export function addSettleItem(data: any) {
    return request({
        url: '/business/settle/item',
        method: 'post',
        data,
    });
}

export function updateSettleItem(data: any) {
    return request({
        url: '/business/settle/item',
        method: 'put',
        data,
    });
}

export function deleteSettleItem(ids: string) {
    return request({
        url: `/business/settle/item/${ids}`,
        method: 'delete',
    });
}

// 获取收支项目列表（用于下拉选择）
export function getSettleInOutItemList() {
    return request({
        url: '/business/settle/item/list',
        method: 'get',
    });
}


export function listEnabledSettleItems(itemType?: number) {
    return request({
        url: '/business/settle/item/list-enabled',
        method: 'get',
        params: { itemType },
    });
}

// =============================================
// 供应商费用单
// =============================================

export function getFeeSheetPage(params: any) {
    return request({
        url: '/business/settle/fee/page',
        method: 'get',
        params,
    });
}

export function getFeeSheetById(id: number) {
    return request({
        url: `/business/settle/fee/${id}`,
        method: 'get',
    });
}

export function addFeeSheet(data: any) {
    return request({
        url: '/business/settle/fee',
        method: 'post',
        data,
    });
}

export function updateFeeSheet(data: any) {
    return request({
        url: '/business/settle/fee',
        method: 'put',
        data,
    });
}

export function approveFeeSheet(id: number) {
    return request({
        url: `/business/settle/fee/approve/${id}`,
        method: 'post',
    });
}

export function refuseFeeSheet(id: number, refuseReason: string) {
    return request({
        url: `/business/settle/fee/refuse/${id}`,
        method: 'post',
        data: { refuseReason },
    });
}

export function deleteFeeSheet(id: number) {
    return request({
        url: `/business/settle/fee/${id}`,
        method: 'delete',
    });
}

// =============================================
// 供应商预付款单
// =============================================

export function getPreSheetPage(params: any) {
    return request({
        url: '/business/settle/pre/page',
        method: 'get',
        params,
    });
}

export function getPreSheetById(id: number) {
    return request({
        url: `/business/settle/pre/${id}`,
        method: 'get',
    });
}

export function addPreSheet(data: any) {
    return request({
        url: '/business/settle/pre',
        method: 'post',
        data,
    });
}

export function updatePreSheet(data: any) {
    return request({
        url: '/business/settle/pre',
        method: 'put',
        data,
    });
}

export function approvePreSheet(id: number) {
    return request({
        url: `/business/settle/pre/approve/${id}`,
        method: 'post',
    });
}

export function refusePreSheet(id: number, refuseReason: string) {
    return request({
        url: `/business/settle/pre/refuse/${id}`,
        method: 'post',
        data: { refuseReason },
    });
}

export function deletePreSheet(id: number) {
    return request({
        url: `/business/settle/pre/${id}`,
        method: 'delete',
    });
}

// =============================================
// 供应商对账单
// =============================================

export function getCheckSheetPage(params: any) {
    return request({ url: '/business/settle/check/page', method: 'get', params });
}

export function getCheckSheetById(id: number) {
    return request({ url: `/business/settle/check/${id}`, method: 'get' });
}

export function addCheckSheet(data: any) {
    return request({ url: '/business/settle/check', method: 'post', data });
}

export function updateCheckSheet(data: any) {
    return request({ url: '/business/settle/check', method: 'put', data });
}

export function approveCheckSheet(id: number) {
    return request({ url: `/business/settle/check/approve/${id}`, method: 'post' });
}

export function refuseCheckSheet(id: number, refuseReason: string) {
    return request({ url: `/business/settle/check/refuse/${id}`, method: 'post', data: { refuseReason } });
}

export function deleteCheckSheet(id: number) {
    return request({ url: `/business/settle/check/${id}`, method: 'delete' });
}

// =============================================
// 供应商结算单
// =============================================

export function getSettleSheetPage(params: any) {
    return request({ url: '/business/settle/sheet/page', method: 'get', params });
}

export function getSettleSheetById(id: number) {
    return request({ url: `/business/settle/sheet/${id}`, method: 'get' });
}

export function addSettleSheet(data: any) {
    return request({ url: '/business/settle/sheet', method: 'post', data });
}

export function updateSettleSheet(data: any) {
    return request({ url: '/business/settle/sheet', method: 'put', data });
}

export function approveSettleSheet(id: number) {
    return request({ url: `/business/settle/sheet/approve/${id}`, method: 'post' });
}

export function refuseSettleSheet(id: number, refuseReason: string) {
    return request({ url: `/business/settle/sheet/refuse/${id}`, method: 'post', data: { refuseReason } });
}

export function deleteSettleSheet(id: number) {
    return request({ url: `/business/settle/sheet/${id}`, method: 'delete' });
}

// =============================================
// 客户费用单
// =============================================
export function getCustomerFeeSheetPage(params: any) {
    return request({ url: '/business/settle/customer/fee/page', method: 'get', params });
}
export function getCustomerFeeSheetById(id: number) {
    return request({ url: `/business/settle/customer/fee/${id}`, method: 'get' });
}
export function addCustomerFeeSheet(data: any) {
    return request({ url: '/business/settle/customer/fee', method: 'post', data });
}
export function updateCustomerFeeSheet(data: any) {
    return request({ url: '/business/settle/customer/fee', method: 'put', data });
}
export function approveCustomerFeeSheet(id: number) {
    return request({ url: `/business/settle/customer/fee/approve/${id}`, method: 'post' });
}
export function refuseCustomerFeeSheet(id: number, refuseReason: string) {
    return request({ url: `/business/settle/customer/fee/refuse/${id}`, method: 'post', data: { refuseReason } });
}
export function deleteCustomerFeeSheet(id: number) {
    return request({ url: `/business/settle/customer/fee/${id}`, method: 'delete' });
}

// =============================================
// 客户预收款单
// =============================================
export function getCustomerPreSheetPage(params: any) {
    return request({ url: '/business/settle/customer/pre/page', method: 'get', params });
}
export function getCustomerPreSheetById(id: number) {
    return request({ url: `/business/settle/customer/pre/${id}`, method: 'get' });
}
export function addCustomerPreSheet(data: any) {
    return request({ url: '/business/settle/customer/pre', method: 'post', data });
}
export function updateCustomerPreSheet(data: any) {
    return request({ url: '/business/settle/customer/pre', method: 'put', data });
}
export function approveCustomerPreSheet(id: number) {
    return request({ url: `/business/settle/customer/pre/approve/${id}`, method: 'post' });
}
export function refuseCustomerPreSheet(id: number, refuseReason: string) {
    return request({ url: `/business/settle/customer/pre/refuse/${id}`, method: 'post', data: { refuseReason } });
}
export function deleteCustomerPreSheet(id: number) {
    return request({ url: `/business/settle/customer/pre/${id}`, method: 'delete' });
}

// =============================================
// 客户对账单
// =============================================
export function getCustomerCheckSheetPage(params: any) {
    return request({ url: '/business/settle/customer/check/page', method: 'get', params });
}
export function getCustomerCheckSheetById(id: number) {
    return request({ url: `/business/settle/customer/check/${id}`, method: 'get' });
}
export function addCustomerCheckSheet(data: any) {
    return request({ url: '/business/settle/customer/check', method: 'post', data });
}
export function updateCustomerCheckSheet(data: any) {
    return request({ url: '/business/settle/customer/check', method: 'put', data });
}
export function approveCustomerCheckSheet(id: number) {
    return request({ url: `/business/settle/customer/check/approve/${id}`, method: 'post' });
}
export function refuseCustomerCheckSheet(id: number, refuseReason: string) {
    return request({ url: `/business/settle/customer/check/refuse/${id}`, method: 'post', data: { refuseReason } });
}
export function deleteCustomerCheckSheet(id: number) {
    return request({ url: `/business/settle/customer/check/${id}`, method: 'delete' });
}

// =============================================
// 客户结算单
// =============================================
export function getCustomerSettleSheetPage(params: any) {
    return request({ url: '/business/settle/customer/sheet/page', method: 'get', params });
}
export function getCustomerSettleSheetById(id: number) {
    return request({ url: `/business/settle/customer/sheet/${id}`, method: 'get' });
}
export function addCustomerSettleSheet(data: any) {
    return request({ url: '/business/settle/customer/sheet', method: 'post', data });
}
export function updateCustomerSettleSheet(data: any) {
    return request({ url: '/business/settle/customer/sheet', method: 'put', data });
}
export function approveCustomerSettleSheet(id: number) {
    return request({ url: `/business/settle/customer/sheet/approve/${id}`, method: 'post' });
}
export function refuseCustomerSettleSheet(id: number, refuseReason: string) {
    return request({ url: `/business/settle/customer/sheet/refuse/${id}`, method: 'post', data: { refuseReason } });
}
export function deleteCustomerSettleSheet(id: number) {
    return request({ url: `/business/settle/customer/sheet/${id}`, method: 'delete' });
}

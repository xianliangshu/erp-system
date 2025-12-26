package com.erp.business.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.business.dto.PurchaseReturnDTO;
import com.erp.business.entity.PurchaseReturn;

/**
 * 采购退货Service接口
 */
public interface IPurchaseReturnService extends IService<PurchaseReturn> {

    /**
     * 分页查询采购退货
     */
    Page<PurchaseReturn> getReturnPage(Long current, Long size, Long receiptId, Long supplierId, Integer status);

    /**
     * 创建退货单
     */
    Long createReturn(PurchaseReturnDTO dto);

    /**
     * 更新退货单
     */
    void updateReturn(PurchaseReturnDTO dto);

    /**
     * 确认退货
     */
    void confirmReturn(Long id);

    /**
     * 生成退货单编号
     */
    String generateReturnCode();

    /**
     * 获取可退货的收货单列表
     */
    Page<?> getPendingReturnReceipts(Long current, Long size, Long supplierId);
}

package com.erp.business.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.business.dto.PurchaseReceiptDTO;
import com.erp.business.entity.PurchaseReceipt;

/**
 * 采购收货服务
 */
public interface IPurchaseReceiptService extends IService<PurchaseReceipt> {

    /**
     * 分页查询采购收货
     */
    Page<PurchaseReceipt> getReceiptPage(Long current, Long size, Long orderId, Long supplierId, Integer status);

    /**
     * 创建采购收货
     * 
     * @return 收货单ID
     */
    Long createReceipt(PurchaseReceiptDTO dto);

    /**
     * 更新采购收货
     */
    void updateReceipt(PurchaseReceiptDTO dto);

    /**
     * 确认收货
     */
    void confirmReceipt(Long id);

    /**
     * 生成收货单编号
     */
    String generateReceiptCode();

    /**
     * 获取待收货的采购订单
     */
    Page<?> getPendingReceiveOrders(Long current, Long size, Long supplierId);
}

package com.erp.business.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.business.dto.PurchaseOrderDTO;
import com.erp.business.entity.PurchaseOrder;

/**
 * 采购订单服务
 */
public interface IPurchaseOrderService extends IService<PurchaseOrder> {

    /**
     * 分页查询采购订单
     */
    Page<PurchaseOrder> getOrderPage(Long current, Long size, Long supplierId, Integer status);

    /**
     * 创建采购订单
     * 
     * @return 订单ID
     */
    Long createOrder(PurchaseOrderDTO dto);

    /**
     * 更新采购订单
     */
    void updateOrder(PurchaseOrderDTO dto);

    /**
     * 审核通过
     */
    void approve(Long id);

    /**
     * 审核拒绝
     */
    void reject(Long id, String reason);

    /**
     * 取消订单
     */
    void cancel(Long id);

    /**
     * 生成订单编号
     */
    String generateOrderCode();
}

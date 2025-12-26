package com.erp.business.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.business.dto.SaleOrderDTO;
import com.erp.business.entity.SaleOrder;

/**
 * 销售订单Service接口
 */
public interface ISaleOrderService extends IService<SaleOrder> {

    /**
     * 分页查询销售订单
     */
    Page<SaleOrder> getOrderPage(Long current, Long size, Long customerId, Integer status);

    /**
     * 创建销售订单
     */
    Long createOrder(SaleOrderDTO dto);

    /**
     * 更新销售订单
     */
    void updateOrder(SaleOrderDTO dto);

    /**
     * 审核通过
     */
    void approveOrder(Long id);

    /**
     * 审核拒绝
     */
    void rejectOrder(Long id, String reason);

    /**
     * 取消订单
     */
    void cancelOrder(Long id);

    /**
     * 生成订单编号
     */
    String generateOrderCode();
}

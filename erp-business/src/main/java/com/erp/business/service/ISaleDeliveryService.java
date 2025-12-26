package com.erp.business.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.business.dto.SaleDeliveryDTO;
import com.erp.business.entity.SaleDelivery;
import com.erp.business.entity.SaleOrder;

/**
 * 销售出库Service接口
 */
public interface ISaleDeliveryService extends IService<SaleDelivery> {

    /**
     * 分页查询销售出库
     */
    Page<SaleDelivery> getDeliveryPage(Long current, Long size, Long orderId, Long customerId, Integer status);

    /**
     * 创建出库单
     */
    Long createDelivery(SaleDeliveryDTO dto);

    /**
     * 更新出库单
     */
    void updateDelivery(SaleDeliveryDTO dto);

    /**
     * 确认出库（扣减库存）
     */
    void confirmDelivery(Long id);

    /**
     * 获取待出库的销售订单列表
     */
    Page<SaleOrder> getPendingDeliveryOrders(Long current, Long size, Long customerId);
}

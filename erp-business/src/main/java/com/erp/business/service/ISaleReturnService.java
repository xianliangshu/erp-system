package com.erp.business.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.business.dto.SaleReturnDTO;
import com.erp.business.entity.SaleDelivery;
import com.erp.business.entity.SaleReturn;

public interface ISaleReturnService extends IService<SaleReturn> {

    Page<SaleReturn> getReturnPage(Long current, Long size, Long deliveryId, Long customerId, Integer status);

    Long createReturn(SaleReturnDTO dto);

    void updateReturn(SaleReturnDTO dto);

    void confirmReturn(Long id);

    Page<SaleDelivery> getPendingReturnDeliveries(Long current, Long size, Long customerId);
}

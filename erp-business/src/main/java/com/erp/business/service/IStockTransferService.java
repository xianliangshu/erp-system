package com.erp.business.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.business.dto.StockTransferDTO;
import com.erp.business.entity.StockTransfer;

public interface IStockTransferService extends IService<StockTransfer> {

    /**
     * 分页查询调拨单
     */
    Page<StockTransfer> getTransferPage(Long current, Long size, Long outScId, Long inScId, Integer status);

    /**
     * 创建调拨单
     */
    Long createTransfer(StockTransferDTO dto);

    /**
     * 更新调拨单
     */
    void updateTransfer(StockTransferDTO dto);

    /**
     * 确认调拨（执行库存变动）
     */
    void confirmTransfer(Long id);

    /**
     * 删除调拨单
     */
    void deleteTransfer(Long id);
}

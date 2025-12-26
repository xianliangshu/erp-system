package com.erp.business.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.business.dto.StockCheckDTO;
import com.erp.business.entity.StockCheck;

public interface IStockCheckService extends IService<StockCheck> {
    Page<StockCheck> getCheckPage(Long current, Long size, Long scId, Integer status);

    Long createCheck(StockCheckDTO dto);

    void updateCheck(StockCheckDTO dto);

    void approveCheck(Long id);
}

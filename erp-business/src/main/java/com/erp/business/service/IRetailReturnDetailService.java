package com.erp.business.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.business.entity.RetailReturnDetail;
import java.util.List;

/**
 * 零售退货单明细 Service
 */
public interface IRetailReturnDetailService extends IService<RetailReturnDetail> {

    /**
     * 根据主表ID查询明细
     */
    List<RetailReturnDetail> listByReturnId(Long returnId);

    /**
     * 根据主表ID删除明细
     */
    void deleteByReturnId(Long returnId);
}

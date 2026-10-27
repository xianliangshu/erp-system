package com.erp.business.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.business.entity.ProductStockLog;
import com.erp.business.vo.ProductStockLogVO;

/**
 * 库存流水服务
 */
public interface IProductStockLogService extends IService<ProductStockLog> {

    /**
     * 分页查询库存流水（含仓库、商品关联信息）
     *
     * @param current     页码
     * @param size        每页条数
     * @param scId        仓库ID（可空）
     * @param productName 商品名称/编号（可空，模糊匹配）
     * @param bizType     业务类型（可空）
     * @param startDate   开始日期 yyyy-MM-dd（可空，含当天 00:00:00）
     * @param endDate     结束日期 yyyy-MM-dd（可空，含当天 23:59:59）
     */
    Page<ProductStockLogVO> getLogPage(Long current, Long size, Long scId, String productName,
                                       Integer bizType, String startDate, String endDate);
}

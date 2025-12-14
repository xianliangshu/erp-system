package com.erp.basedata.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.basedata.dto.WarehousePageParam;
import com.erp.basedata.entity.BaseWarehouse;

import java.util.List;

/**
 * 仓库Service接口
 * 
 * @author ERP System
 * @since 2025-12-14
 */
public interface IBaseWarehouseService extends IService<BaseWarehouse> {

    /**
     * 分页查询仓库
     * 
     * @param param 查询参数
     * @return 分页结果
     */
    Page<BaseWarehouse> getWarehousePage(WarehousePageParam param);

    /**
     * 新增仓库
     * 
     * @param warehouse 仓库信息
     * @return 是否成功
     */
    boolean saveWarehouse(BaseWarehouse warehouse);

    /**
     * 更新仓库
     * 
     * @param warehouse 仓库信息
     * @return 是否成功
     */
    boolean updateWarehouse(BaseWarehouse warehouse);

    /**
     * 删除仓库
     * 
     * @param id 仓库ID
     * @return 是否成功
     */
    boolean deleteWarehouse(Long id);

    /**
     * 设置默认仓库
     * 
     * @param id 仓库ID
     * @return 是否成功
     */
    boolean setDefaultWarehouse(Long id);

    /**
     * 获取所有启用的仓库
     * 
     * @return 仓库列表
     */
    List<BaseWarehouse> getAllEnabledWarehouses();
}

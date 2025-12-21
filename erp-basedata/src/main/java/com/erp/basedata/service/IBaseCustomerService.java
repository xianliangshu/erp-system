package com.erp.basedata.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.basedata.dto.CustomerPageParam;
import com.erp.basedata.entity.BaseCustomer;

import java.util.List;

/**
 * 客户信息Service接口
 *
 * @author ERP System
 * @since 2025-12-21
 */
public interface IBaseCustomerService extends IService<BaseCustomer> {

    /**
     * 分页查询客户
     *
     * @param param 查询参数
     * @return 分页结果
     */
    Page<BaseCustomer> getCustomerPage(CustomerPageParam param);

    /**
     * 获取所有启用的客户
     *
     * @return 客户列表
     */
    List<BaseCustomer> getAllEnabledCustomers();

    /**
     * 根据ID获取客户
     *
     * @param id 客户ID
     * @return 客户信息
     */
    BaseCustomer getById(Long id);

    /**
     * 保存客户
     *
     * @param customer 客户信息
     * @return 是否成功
     */
    boolean saveCustomer(BaseCustomer customer);

    /**
     * 更新客户
     *
     * @param customer 客户信息
     * @return 是否成功
     */
    boolean updateCustomer(BaseCustomer customer);

    /**
     * 删除客户
     *
     * @param id 客户ID
     * @return 是否成功
     */
    boolean deleteCustomer(Long id);
}

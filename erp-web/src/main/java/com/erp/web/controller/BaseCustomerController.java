package com.erp.web.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.basedata.dto.CustomerPageParam;
import com.erp.basedata.entity.BaseCustomer;
import com.erp.basedata.service.IBaseCustomerService;
import com.erp.common.core.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 客户信息Controller
 *
 * @author ERP System
 * @since 2025-12-21
 */
@RestController
@RequestMapping("/basedata/customer")
@RequiredArgsConstructor
public class BaseCustomerController {

    private final IBaseCustomerService customerService;

    /**
     * 分页查询客户
     */
    @GetMapping("/page")
    public Result<Page<BaseCustomer>> page(CustomerPageParam param) {
        Page<BaseCustomer> page = customerService.getCustomerPage(param);
        return Result.success(page);
    }

    /**
     * 获取所有启用的客户
     */
    @GetMapping("/list")
    public Result<List<BaseCustomer>> list() {
        List<BaseCustomer> list = customerService.getAllEnabledCustomers();
        return Result.success(list);
    }

    /**
     * 根据ID获取客户
     */
    @GetMapping("/{id}")
    public Result<BaseCustomer> getById(@PathVariable Long id) {
        BaseCustomer customer = customerService.getById(id);
        return Result.success(customer);
    }

    /**
     * 新增客户
     */
    @PostMapping
    public Result<Void> save(@RequestBody BaseCustomer customer) {
        customerService.saveCustomer(customer);
        return Result.success();
    }

    /**
     * 更新客户
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody BaseCustomer customer) {
        customer.setId(id);
        customerService.updateCustomer(customer);
        return Result.success();
    }

    /**
     * 删除客户
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return Result.success();
    }
}

package com.erp.web.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.basedata.dto.CustomerPageParam;
import com.erp.basedata.entity.BaseCustomer;
import com.erp.basedata.service.IBaseCustomerService;
import com.erp.basedata.vo.CustomerExcelVO;
import com.erp.common.core.result.Result;
import com.erp.common.core.vo.ExcelImportResult;
import com.erp.common.util.ExcelUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
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
    @SaCheckPermission("basedata:customer:list")
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
    @SaCheckPermission("basedata:customer:list")
    @GetMapping("/{id}")
    public Result<BaseCustomer> getById(@PathVariable Long id) {
        BaseCustomer customer = customerService.getById(id);
        return Result.success(customer);
    }

    /**
     * 新增客户
     */
    @SaCheckPermission("basedata:customer:list")
    @PostMapping
    public Result<Void> save(@RequestBody BaseCustomer customer) {
        customerService.saveCustomer(customer);
        return Result.success();
    }

    /**
     * 更新客户
     */
    @SaCheckPermission("basedata:customer:list")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody BaseCustomer customer) {
        customer.setId(id);
        customerService.updateCustomer(customer);
        return Result.success();
    }

    /**
     * 删除客户
     */
    @SaCheckPermission("basedata:customer:list")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return Result.success();
    }

    /**
     * 导出客户 Excel（按当前查询条件，不分页）
     */
    @SaCheckPermission("basedata:customer:list")
    @GetMapping("/export")
    public void export(CustomerPageParam param, HttpServletResponse response) {
        List<CustomerExcelVO> list = customerService.listForExport(param);
        ExcelUtils.export(response, "客户列表", "客户", CustomerExcelVO.class, list);
    }

    /**
     * 下载客户导入模板（只有表头，无数据行）
     */
    @SaCheckPermission("basedata:customer:list")
    @GetMapping("/template")
    public void template(HttpServletResponse response) {
        ExcelUtils.export(response, "客户导入模板", "客户", CustomerExcelVO.class, List.of());
    }

    /**
     * 批量导入客户
     */
    @SaCheckPermission("basedata:customer:list")
    @PostMapping("/import")
    public Result<ExcelImportResult> importExcel(@RequestParam("file") MultipartFile file) throws IOException {
        try (InputStream in = file.getInputStream()) {
            return Result.success(customerService.importExcel(in));
        }
    }
}

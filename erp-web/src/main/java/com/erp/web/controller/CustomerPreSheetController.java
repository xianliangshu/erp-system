package com.erp.web.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.business.dto.CustomerPreSheetDTO;
import com.erp.business.dto.CustomerPreSheetQueryDTO;
import com.erp.business.entity.CustomerPreSheet;
import com.erp.business.service.ICustomerPreSheetService;
import com.erp.common.core.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@Tag(name = "客户预收款单")
@RestController
@RequestMapping("/business/settle/customer/pre")
@RequiredArgsConstructor
public class CustomerPreSheetController {
    private final ICustomerPreSheetService preSheetService;

    @Operation(summary = "分页查询")
    @SaCheckPermission("business:settle:customer:pre")
    @GetMapping("/page")
    public Result<Page<CustomerPreSheet>> page(@RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size, CustomerPreSheetQueryDTO queryDTO) {
        return Result.success(preSheetService.queryPage(current, size, queryDTO));
    }

    @Operation(summary = "获取详情")
    @SaCheckPermission("business:settle:customer:pre")
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getInfo(@PathVariable Long id) {
        return Result.success(preSheetService.getDetailById(id));
    }

    @Operation(summary = "新增")
    @SaCheckPermission("business:settle:customer:pre")
    @PostMapping
    public Result<Long> add(@RequestBody CustomerPreSheetDTO dto) {
        return Result.success(preSheetService.create(dto));
    }

    @Operation(summary = "修改")
    @SaCheckPermission("business:settle:customer:pre")
    @PutMapping
    public Result<Void> edit(@RequestBody CustomerPreSheetDTO dto) {
        preSheetService.update(dto);
        return Result.success();
    }

    @Operation(summary = "审核通过")
    @SaCheckPermission("business:settle:customer:pre")
    @PostMapping("/approve/{id}")
    public Result<Void> approve(@PathVariable Long id) {
        preSheetService.approve(id);
        return Result.success();
    }

    @Operation(summary = "审核拒绝")
    @SaCheckPermission("business:settle:customer:pre")
    @PostMapping("/refuse/{id}")
    public Result<Void> refuse(@PathVariable Long id, @RequestBody Map<String, String> data) {
        preSheetService.refuse(id, data.get("refuseReason"));
        return Result.success();
    }

    @Operation(summary = "删除")
    @SaCheckPermission("business:settle:customer:pre")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        preSheetService.deleteById(id);
        return Result.success();
    }
}

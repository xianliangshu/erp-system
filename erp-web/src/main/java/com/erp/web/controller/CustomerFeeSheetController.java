package com.erp.web.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.business.dto.CustomerFeeSheetDTO;
import com.erp.business.dto.CustomerFeeSheetQueryDTO;
import com.erp.business.entity.CustomerFeeSheet;
import com.erp.business.service.ICustomerFeeSheetService;
import com.erp.common.core.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@Tag(name = "客户费用单")
@RestController
@RequestMapping("/business/settle/customer/fee")
@RequiredArgsConstructor
public class CustomerFeeSheetController {
    private final ICustomerFeeSheetService feeSheetService;

    @Operation(summary = "分页查询")
    @GetMapping("/page")
    public Result<Page<CustomerFeeSheet>> page(@RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size, CustomerFeeSheetQueryDTO queryDTO) {
        return Result.success(feeSheetService.queryPage(current, size, queryDTO));
    }

    @Operation(summary = "获取详情")
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getInfo(@PathVariable Long id) {
        return Result.success(feeSheetService.getDetailById(id));
    }

    @Operation(summary = "新增")
    @PostMapping
    public Result<Long> add(@RequestBody CustomerFeeSheetDTO dto) {
        return Result.success(feeSheetService.create(dto));
    }

    @Operation(summary = "修改")
    @PutMapping
    public Result<Void> edit(@RequestBody CustomerFeeSheetDTO dto) {
        feeSheetService.update(dto);
        return Result.success();
    }

    @Operation(summary = "审核通过")
    @PostMapping("/approve/{id}")
    public Result<Void> approve(@PathVariable Long id) {
        feeSheetService.approve(id);
        return Result.success();
    }

    @Operation(summary = "审核拒绝")
    @PostMapping("/refuse/{id}")
    public Result<Void> refuse(@PathVariable Long id, @RequestBody Map<String, String> data) {
        feeSheetService.refuse(id, data.get("refuseReason"));
        return Result.success();
    }

    @Operation(summary = "删除")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        feeSheetService.deleteById(id);
        return Result.success();
    }
}

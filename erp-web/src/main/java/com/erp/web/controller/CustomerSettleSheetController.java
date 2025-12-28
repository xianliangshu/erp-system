package com.erp.web.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.business.dto.CustomerSettleSheetDTO;
import com.erp.business.dto.CustomerSettleSheetQueryDTO;
import com.erp.business.entity.CustomerSettleSheet;
import com.erp.business.service.ICustomerSettleSheetService;
import com.erp.common.core.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@Tag(name = "客户结算单")
@RestController
@RequestMapping("/business/settle/customer/sheet")
@RequiredArgsConstructor
public class CustomerSettleSheetController {
    private final ICustomerSettleSheetService settleSheetService;

    @Operation(summary = "分页查询")
    @GetMapping("/page")
    public Result<Page<CustomerSettleSheet>> page(@RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size, CustomerSettleSheetQueryDTO queryDTO) {
        return Result.success(settleSheetService.queryPage(current, size, queryDTO));
    }

    @Operation(summary = "获取详情")
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getInfo(@PathVariable Long id) {
        return Result.success(settleSheetService.getDetailById(id));
    }

    @Operation(summary = "新增")
    @PostMapping
    public Result<Long> add(@RequestBody CustomerSettleSheetDTO dto) {
        return Result.success(settleSheetService.create(dto));
    }

    @Operation(summary = "修改")
    @PutMapping
    public Result<Void> edit(@RequestBody CustomerSettleSheetDTO dto) {
        settleSheetService.update(dto);
        return Result.success();
    }

    @Operation(summary = "审核通过")
    @PostMapping("/approve/{id}")
    public Result<Void> approve(@PathVariable Long id) {
        settleSheetService.approve(id);
        return Result.success();
    }

    @Operation(summary = "审核拒绝")
    @PostMapping("/refuse/{id}")
    public Result<Void> refuse(@PathVariable Long id, @RequestBody Map<String, String> data) {
        settleSheetService.refuse(id, data.get("refuseReason"));
        return Result.success();
    }

    @Operation(summary = "删除")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        settleSheetService.deleteById(id);
        return Result.success();
    }
}

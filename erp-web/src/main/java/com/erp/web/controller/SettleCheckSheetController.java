package com.erp.web.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.business.dto.SettleCheckSheetDTO;
import com.erp.business.dto.SettleCheckSheetQueryDTO;
import com.erp.business.entity.SettleCheckSheet;
import com.erp.business.service.ISettleCheckSheetService;
import com.erp.common.core.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@Tag(name = "供应商对账单")
@RestController
@RequestMapping("/business/settle/check")
@RequiredArgsConstructor
public class SettleCheckSheetController {

    private final ISettleCheckSheetService checkSheetService;

    @Operation(summary = "分页查询")
    @GetMapping("/page")
    public Result<Page<SettleCheckSheet>> page(@RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            SettleCheckSheetQueryDTO queryDTO) {
        return Result.success(checkSheetService.queryPage(current, size, queryDTO));
    }

    @Operation(summary = "获取详情")
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getInfo(@PathVariable Long id) {
        return Result.success(checkSheetService.getDetailById(id));
    }

    @Operation(summary = "新增")
    @PostMapping
    public Result<Long> add(@RequestBody SettleCheckSheetDTO dto) {
        return Result.success(checkSheetService.create(dto));
    }

    @Operation(summary = "修改")
    @PutMapping
    public Result<Void> edit(@RequestBody SettleCheckSheetDTO dto) {
        checkSheetService.update(dto);
        return Result.success();
    }

    @Operation(summary = "审核通过")
    @PostMapping("/approve/{id}")
    public Result<Void> approve(@PathVariable Long id) {
        checkSheetService.approve(id);
        return Result.success();
    }

    @Operation(summary = "审核拒绝")
    @PostMapping("/refuse/{id}")
    public Result<Void> refuse(@PathVariable Long id, @RequestBody Map<String, String> data) {
        checkSheetService.refuse(id, data.get("refuseReason"));
        return Result.success();
    }

    @Operation(summary = "删除")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        checkSheetService.deleteById(id);
        return Result.success();
    }
}

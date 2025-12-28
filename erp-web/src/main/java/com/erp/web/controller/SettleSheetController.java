package com.erp.web.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.business.dto.SettleSheetDTO;
import com.erp.business.dto.SettleSheetQueryDTO;
import com.erp.business.entity.SettleSheet;
import com.erp.business.service.ISettleSheetService;
import com.erp.common.core.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@Tag(name = "供应商结算单")
@RestController
@RequestMapping("/business/settle/sheet")
@RequiredArgsConstructor
public class SettleSheetController {

    private final ISettleSheetService sheetService;

    @Operation(summary = "分页查询")
    @GetMapping("/page")
    public Result<Page<SettleSheet>> page(@RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            SettleSheetQueryDTO queryDTO) {
        return Result.success(sheetService.queryPage(current, size, queryDTO));
    }

    @Operation(summary = "获取详情")
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getInfo(@PathVariable Long id) {
        return Result.success(sheetService.getDetailById(id));
    }

    @Operation(summary = "新增")
    @PostMapping
    public Result<Long> add(@RequestBody SettleSheetDTO dto) {
        return Result.success(sheetService.create(dto));
    }

    @Operation(summary = "修改")
    @PutMapping
    public Result<Void> edit(@RequestBody SettleSheetDTO dto) {
        sheetService.update(dto);
        return Result.success();
    }

    @Operation(summary = "审核通过")
    @PostMapping("/approve/{id}")
    public Result<Void> approve(@PathVariable Long id) {
        sheetService.approve(id);
        return Result.success();
    }

    @Operation(summary = "审核拒绝")
    @PostMapping("/refuse/{id}")
    public Result<Void> refuse(@PathVariable Long id, @RequestBody Map<String, String> data) {
        sheetService.refuse(id, data.get("refuseReason"));
        return Result.success();
    }

    @Operation(summary = "删除")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        sheetService.deleteById(id);
        return Result.success();
    }
}

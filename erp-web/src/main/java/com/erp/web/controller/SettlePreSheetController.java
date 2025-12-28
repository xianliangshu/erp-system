package com.erp.web.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.business.dto.SettlePreSheetDTO;
import com.erp.business.dto.SettlePreSheetQueryDTO;
import com.erp.business.entity.SettlePreSheet;
import com.erp.business.service.ISettlePreSheetService;
import com.erp.common.core.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "供应商预付款单")
@RestController
@RequestMapping("/business/settle/pre")
@RequiredArgsConstructor
public class SettlePreSheetController {

    private final ISettlePreSheetService preSheetService;

    @Operation(summary = "分页查询")
    @GetMapping("/page")
    public Result<Page<SettlePreSheet>> page(@RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            SettlePreSheetQueryDTO queryDTO) {
        return Result.success(preSheetService.queryPage(current, size, queryDTO));
    }

    @Operation(summary = "获取详情")
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getInfo(@PathVariable Long id) {
        return Result.success(preSheetService.getDetailById(id));
    }

    @Operation(summary = "新增")
    @PostMapping
    public Result<Long> add(@RequestBody SettlePreSheetDTO dto) {
        return Result.success(preSheetService.create(dto));
    }

    @Operation(summary = "修改")
    @PutMapping
    public Result<Void> edit(@RequestBody SettlePreSheetDTO dto) {
        preSheetService.update(dto);
        return Result.success();
    }

    @Operation(summary = "审核通过")
    @PostMapping("/approve/{id}")
    public Result<Void> approve(@PathVariable Long id) {
        preSheetService.approve(id);
        return Result.success();
    }

    @Operation(summary = "审核拒绝")
    @PostMapping("/refuse/{id}")
    public Result<Void> refuse(@PathVariable Long id, @RequestBody Map<String, String> data) {
        preSheetService.refuse(id, data.get("refuseReason"));
        return Result.success();
    }

    @Operation(summary = "删除")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        preSheetService.deleteById(id);
        return Result.success();
    }
}

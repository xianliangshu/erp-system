package com.erp.web.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.business.dto.SettleFeeSheetDTO;
import com.erp.business.dto.SettleFeeSheetQueryDTO;
import com.erp.business.entity.SettleFeeSheet;
import com.erp.business.service.ISettleFeeSheetService;
import com.erp.common.core.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 供应商费用单 Controller
 */
@Tag(name = "供应商费用单")
@RestController
@RequestMapping("/business/settle/fee")
@RequiredArgsConstructor
public class SettleFeeSheetController {

    private final ISettleFeeSheetService feeSheetService;

    /**
     * 分页查询
     */
    @Operation(summary = "分页查询")
    @GetMapping("/page")
    public Result<Page<SettleFeeSheet>> page(@RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            SettleFeeSheetQueryDTO queryDTO) {
        Page<SettleFeeSheet> page = feeSheetService.queryPage(current, size, queryDTO);
        return Result.success(page);
    }

    /**
     * 获取详情
     */
    @Operation(summary = "获取详情")
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getInfo(@PathVariable Long id) {
        return Result.success(feeSheetService.getDetailById(id));
    }

    /**
     * 新增
     */
    @Operation(summary = "新增")
    @PostMapping
    public Result<Long> add(@RequestBody SettleFeeSheetDTO dto) {
        Long id = feeSheetService.create(dto);
        return Result.success(id);
    }

    /**
     * 修改
     */
    @Operation(summary = "修改")
    @PutMapping
    public Result<Void> edit(@RequestBody SettleFeeSheetDTO dto) {
        feeSheetService.update(dto);
        return Result.success();
    }

    /**
     * 审核通过
     */
    @Operation(summary = "审核通过")
    @PostMapping("/approve/{id}")
    public Result<Void> approve(@PathVariable Long id) {
        feeSheetService.approve(id);
        return Result.success();
    }

    /**
     * 审核拒绝
     */
    @Operation(summary = "审核拒绝")
    @PostMapping("/refuse/{id}")
    public Result<Void> refuse(@PathVariable Long id, @RequestBody Map<String, String> data) {
        feeSheetService.refuse(id, data.get("refuseReason"));
        return Result.success();
    }

    /**
     * 删除
     */
    @Operation(summary = "删除")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        feeSheetService.deleteById(id);
        return Result.success();
    }
}

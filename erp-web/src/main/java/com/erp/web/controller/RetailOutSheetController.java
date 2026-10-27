package com.erp.web.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.business.entity.RetailOutSheet;
import com.erp.business.service.IRetailOutSheetService;
import com.erp.common.core.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 零售出库单 Controller
 */
@Tag(name = "零售出库单")
@RestController
@RequestMapping("/retail/out")
public class RetailOutSheetController {

    @Autowired
    private IRetailOutSheetService retailOutSheetService;

    @Operation(summary = "分页查询")
    @SaCheckPermission("business:retail:out")
    @GetMapping("/page")
    public Result<IPage<RetailOutSheet>> page(@RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) Long scId,
            @RequestParam(required = false) Integer status) {
        Page<RetailOutSheet> page = new Page<>(current, size);
        Map<String, Object> params = new HashMap<>();
        params.put("code", code);
        params.put("scId", scId);
        params.put("status", status);
        IPage<RetailOutSheet> result = retailOutSheetService.getPage(page, params);
        return Result.success(result);
    }

    @Operation(summary = "获取详情")
    @SaCheckPermission("business:retail:out")
    @GetMapping("/{id}")
    public Result<Map<String, Object>> get(@PathVariable Long id) {
        return Result.success(retailOutSheetService.getDetailById(id));
    }

    @Operation(summary = "新增")
    @SaCheckPermission("business:retail:out")
    @PostMapping
    public Result<Map<String, Object>> add(@RequestBody Map<String, Object> data) {
        return Result.success(retailOutSheetService.add(data));
    }

    @Operation(summary = "修改")
    @SaCheckPermission("business:retail:out")
    @PutMapping
    public Result<Void> update(@RequestBody Map<String, Object> data) {
        retailOutSheetService.update(data);
        return Result.success();
    }

    @Operation(summary = "审核通过")
    @SaCheckPermission("business:retail:out")
    @PostMapping("/approve/{id}")
    public Result<Void> approve(@PathVariable Long id) {
        retailOutSheetService.approve(id);
        return Result.success();
    }

    @Operation(summary = "审核拒绝")
    @SaCheckPermission("business:retail:out")
    @PostMapping("/refuse/{id}")
    public Result<Void> refuse(@PathVariable Long id, @RequestBody Map<String, String> data) {
        retailOutSheetService.refuse(id, data.get("refuseReason"));
        return Result.success();
    }

    @Operation(summary = "删除")
    @SaCheckPermission("business:retail:out")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        retailOutSheetService.deleteById(id);
        return Result.success();
    }
}

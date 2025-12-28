package com.erp.web.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.business.entity.RetailReturn;
import com.erp.business.service.IRetailReturnService;
import com.erp.common.core.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 零售退货单 Controller
 */
@Tag(name = "零售退货单")
@RestController
@RequestMapping("/retail/return")
public class RetailReturnController {

    @Autowired
    private IRetailReturnService retailReturnService;

    @Operation(summary = "分页查询")
    @GetMapping("/page")
    public Result<IPage<RetailReturn>> page(@RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) Long scId,
            @RequestParam(required = false) Integer status) {
        Page<RetailReturn> page = new Page<>(current, size);
        Map<String, Object> params = new HashMap<>();
        params.put("code", code);
        params.put("scId", scId);
        params.put("status", status);
        IPage<RetailReturn> result = retailReturnService.getPage(page, params);
        return Result.success(result);
    }

    @Operation(summary = "获取详情")
    @GetMapping("/{id}")
    public Result<Map<String, Object>> get(@PathVariable Long id) {
        return Result.success(retailReturnService.getDetailById(id));
    }

    @Operation(summary = "新增")
    @PostMapping
    public Result<Void> add(@RequestBody Map<String, Object> data) {
        retailReturnService.add(data);
        return Result.success();
    }

    @Operation(summary = "修改")
    @PutMapping
    public Result<Void> update(@RequestBody Map<String, Object> data) {
        retailReturnService.update(data);
        return Result.success();
    }

    @Operation(summary = "审核通过")
    @PostMapping("/approve/{id}")
    public Result<Void> approve(@PathVariable Long id) {
        retailReturnService.approve(id);
        return Result.success();
    }

    @Operation(summary = "审核拒绝")
    @PostMapping("/refuse/{id}")
    public Result<Void> refuse(@PathVariable Long id, @RequestBody Map<String, String> data) {
        retailReturnService.refuse(id, data.get("refuseReason"));
        return Result.success();
    }

    @Operation(summary = "删除")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        retailReturnService.deleteById(id);
        return Result.success();
    }
}

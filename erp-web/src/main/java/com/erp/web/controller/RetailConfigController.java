package com.erp.web.controller;

import com.erp.business.entity.RetailConfig;
import com.erp.business.service.IRetailConfigService;
import com.erp.common.core.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 零售配置 Controller
 */
@Tag(name = "零售配置")
@RestController
@RequestMapping("/retail/config")
public class RetailConfigController {

    @Autowired
    private IRetailConfigService retailConfigService;

    @Operation(summary = "获取零售配置")
    @GetMapping
    public Result<RetailConfig> get() {
        return Result.success(retailConfigService.getRetailConfig());
    }

    @Operation(summary = "更新零售配置")
    @PutMapping
    public Result<Void> update(@RequestBody RetailConfig config) {
        retailConfigService.updateRetailConfig(config);
        return Result.success();
    }
}

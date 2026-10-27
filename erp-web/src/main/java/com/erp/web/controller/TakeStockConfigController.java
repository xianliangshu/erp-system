package com.erp.web.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.erp.business.entity.TakeStockConfig;
import com.erp.business.service.ITakeStockConfigService;
import com.erp.common.core.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 库存盘点参数 Controller
 *
 * @author ERP System
 */
@RestController
@RequestMapping("/stock/take/config")
@RequiredArgsConstructor
public class TakeStockConfigController {

    private final ITakeStockConfigService takeStockConfigService;

    /**
     * 获取盘点参数（单行配置）
     */
    @SaCheckPermission("stock:take:config:query")
    @GetMapping
    public Result<TakeStockConfig> getConfig() {
        return Result.success(takeStockConfigService.getConfig());
    }

    /**
     * 保存盘点参数
     */
    @SaCheckPermission("stock:take:config:modify")
    @PutMapping
    public Result<?> saveConfig(@RequestBody TakeStockConfig config) {
        return takeStockConfigService.saveConfig(config) ? Result.success() : Result.error("保存失败");
    }
}

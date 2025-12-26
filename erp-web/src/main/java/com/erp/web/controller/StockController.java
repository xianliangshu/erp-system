package com.erp.web.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.business.service.IProductStockService;
import com.erp.business.vo.ProductStockVO;
import com.erp.common.core.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 库存管理Controller
 */
@RestController
@RequestMapping("/business/stock")
@RequiredArgsConstructor
public class StockController {

    private final IProductStockService productStockService;

    /**
     * 分页查询库存（包含关联信息）
     */
    @GetMapping("/page")
    public Result<Page<ProductStockVO>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Long scId,
            @RequestParam(required = false) String productName) {
        return Result.success(productStockService.getStockPage(current, size, scId, productName));
    }
}

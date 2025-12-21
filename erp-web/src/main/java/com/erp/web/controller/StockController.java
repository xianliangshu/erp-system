package com.erp.web.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.business.entity.ProductStock;
import com.erp.business.service.IProductStockService;
import com.erp.common.core.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
     * 分页查询库存
     */
    @GetMapping("/page")
    public Result<Page<ProductStock>> page(Long current, Long size, Long scId, String productName) {
        // 这里暂时简单实现，后续可以增加更复杂的关联查询（如商品名称、仓库名称等）
        Page<ProductStock> page = new Page<>(current != null ? current : 1, size != null ? size : 10);
        // TODO: 实现关联查询逻辑，目前仅返回基础数据
        return Result.success(productStockService.page(page));
    }
}

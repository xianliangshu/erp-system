package com.erp.web.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.business.enums.ProductStockBizType;
import com.erp.business.service.IProductStockLogService;
import com.erp.business.vo.ProductStockLogVO;
import com.erp.common.core.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 库存流水Controller
 */
@Tag(name = "库存流水", description = "库存出入库变动记录查询接口")
@RestController
@RequestMapping("/business/stock/log")
@RequiredArgsConstructor
public class ProductStockLogController {

    private final IProductStockLogService productStockLogService;

    /**
     * 分页查询库存流水
     */
    @Operation(summary = "分页查询库存流水", description = "按仓库、商品、业务类型、时间区间分页查询库存变动记录")
    @SaCheckPermission("business:stock:log")
    @GetMapping("/page")
    public Result<Page<ProductStockLogVO>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Long scId,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) Integer bizType,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return Result.success(productStockLogService.getLogPage(current, size, scId, productName,
                bizType, startDate, endDate));
    }

    /**
     * 获取业务类型字典（供前端筛选下拉使用）
     */
    @Operation(summary = "获取库存业务类型字典", description = "返回出/入库业务类型枚举，供前端筛选下拉使用")
    @GetMapping("/biz-types")
    public Result<List<Map<String, Object>>> bizTypes() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (ProductStockBizType type : ProductStockBizType.values()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("value", type.getValue());
            item.put("label", type.getDesc());
            list.add(item);
        }
        return Result.success(list);
    }
}

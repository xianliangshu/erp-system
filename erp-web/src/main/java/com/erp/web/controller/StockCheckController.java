package com.erp.web.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.business.dto.StockCheckDTO;
import com.erp.business.entity.StockCheck;
import com.erp.business.entity.StockCheckDetail;
import com.erp.business.mapper.StockCheckDetailMapper;
import com.erp.business.service.IStockCheckService;
import com.erp.common.core.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/business/stock/check")
@RequiredArgsConstructor
public class StockCheckController {

    private final IStockCheckService stockCheckService;
    private final StockCheckDetailMapper stockCheckDetailMapper;

    @GetMapping("/page")
    public Result<Page<StockCheck>> page(@RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Long scId, @RequestParam(required = false) Integer status) {
        return Result.success(stockCheckService.getCheckPage(current, size, scId, status));
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> getById(@PathVariable Long id) {
        StockCheck check = stockCheckService.getById(id);
        if (check == null)
            return Result.success(null);
        LambdaQueryWrapper<StockCheckDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StockCheckDetail::getCheckId, id);
        List<StockCheckDetail> details = stockCheckDetailMapper.selectList(wrapper);
        Map<String, Object> result = new HashMap<>();
        result.put("stockCheck", check);
        result.put("details", details);
        return Result.success(result);
    }

    @PostMapping
    public Result<Long> create(@RequestBody StockCheckDTO dto) {
        return Result.success(stockCheckService.createCheck(dto));
    }

    @PutMapping
    public Result<Void> update(@RequestBody StockCheckDTO dto) {
        stockCheckService.updateCheck(dto);
        return Result.success();
    }

    @PostMapping("/{id}/approve")
    public Result<Void> approve(@PathVariable Long id) {
        stockCheckService.approveCheck(id);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        stockCheckService.removeById(id);
        return Result.success();
    }
}

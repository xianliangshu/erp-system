package com.erp.web.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.basedata.dto.UnitPageParam;
import com.erp.basedata.entity.BaseUnit;
import com.erp.basedata.service.IBaseUnitService;
import com.erp.common.core.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 计量单位Controller
 *
 * @author ERP System
 * @since 2025-12-14
 */
@RestController
@RequestMapping("/api/basedata/unit")
@RequiredArgsConstructor
public class BaseUnitController {

    private final IBaseUnitService unitService;

    /**
     * 分页查询单位
     */
    @GetMapping("/page")
    public Result<Page<BaseUnit>> getPage(UnitPageParam param) {
        Page<BaseUnit> page = unitService.getUnitPage(param);
        return Result.success(page);
    }

    /**
     * 获取所有启用的单位
     */
    @GetMapping("/list")
    public Result<List<BaseUnit>> getList() {
        List<BaseUnit> list = unitService.getAllEnabledUnits();
        return Result.success(list);
    }

    /**
     * 根据ID获取单位
     */
    @GetMapping("/{id}")
    public Result<BaseUnit> getById(@PathVariable Long id) {
        BaseUnit unit = unitService.getById(id);
        return Result.success(unit);
    }

    /**
     * 新增单位
     */
    @PostMapping
    public Result<Void> save(@RequestBody BaseUnit unit) {
        unitService.saveUnit(unit);
        return Result.success();
    }

    /**
     * 更新单位
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody BaseUnit unit) {
        unit.setId(id);
        unitService.updateUnit(unit);
        return Result.success();
    }

    /**
     * 删除单位
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        unitService.deleteUnit(id);
        return Result.success();
    }
}

package com.erp.web.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.erp.common.core.page.PageResult;
import com.erp.common.core.result.Result;
import com.erp.system.entity.SysDictData;
import com.erp.system.entity.SysDictType;
import com.erp.system.param.SysDictTypePageParam;
import com.erp.system.service.ISysDictDataService;
import com.erp.system.service.ISysDictTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 字典管理Controller
 * 
 * @author ERP System
 * @since 2025-12-12
 */
@Tag(name = "字典管理", description = "字典的增删改查接口")
@RestController
@RequestMapping("/system/dict")
@RequiredArgsConstructor
public class SysDictController {

    private final ISysDictTypeService dictTypeService;
    private final ISysDictDataService dictDataService;

    /**
     * 分页查询字典类型列表
     */
    @Operation(summary = "分页查询字典类型", description = "根据条件分页查询字典类型列表")
    @GetMapping("/type/page")
    public Result<PageResult<SysDictType>> typePage(SysDictTypePageParam param) {
        // 调用Service业务方法
        PageResult<SysDictType> pageResult = dictTypeService.pageQuery(param);
        return Result.success(pageResult);
    }

    /**
     * 根据字典类型查询字典数据列表
     */
    @Operation(summary = "根据字典类型查询字典数据列表", description = "根据字典类型查询字典数据列表")
    @GetMapping("/data/list/{dictType}")
    public Result<List<SysDictData>> dataList(@PathVariable String dictType) {
        LambdaQueryWrapper<SysDictData> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDictData::getDictType, dictType)
                .eq(SysDictData::getStatus, 1)
                .orderByAsc(SysDictData::getDictSort);

        List<SysDictData> list = dictDataService.list(wrapper);
        return Result.success(list);
    }

    /**
     * 新增字典类型
     */
    @Operation(summary = "新增字典类型", description = "新增字典类型")
    @PostMapping("/type")
    public Result<Void> saveType(@RequestBody SysDictType dictType) {
        dictTypeService.save(dictType);
        return Result.success();
    }

    /**
     * 新增字典数据
     */
    @Operation(summary = "新增字典数据", description = "新增字典数据")
    @PostMapping("/data")
    public Result<Void> saveData(@RequestBody SysDictData dictData) {
        dictDataService.save(dictData);
        return Result.success();
    }

    /**
     * 修改字典类型
     */
    @Operation(summary = "修改字典类型", description = "修改字典类型")
    @PutMapping("/type")
    public Result<Void> updateType(@RequestBody SysDictType dictType) {
        dictTypeService.updateById(dictType);
        return Result.success();
    }

    /**
     * 修改字典数据
     */
    @Operation(summary = "修改字典数据", description = "修改字典数据")
    @PutMapping("/data")
    public Result<Void> updateData(@RequestBody SysDictData dictData) {
        dictDataService.updateById(dictData);
        return Result.success();
    }

    /**
     * 删除字典类型
     */
    @Operation(summary = "删除字典类型", description = "删除字典类型")
    @DeleteMapping("/type/{id}")
    public Result<Void> deleteType(@PathVariable Long id) {
        dictTypeService.removeById(id);
        return Result.success();
    }

    /**
     * 删除字典数据
     */
    @Operation(summary = "删除字典数据", description = "删除字典数据")
    @DeleteMapping("/data/{id}")
    public Result<Void> deleteData(@PathVariable Long id) {
        dictDataService.removeById(id);
        return Result.success();
    }
}

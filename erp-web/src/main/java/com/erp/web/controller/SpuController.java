package com.erp.web.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.basedata.dto.SpuPageParam;
import com.erp.basedata.dto.SpuSaveDTO;
import com.erp.basedata.entity.BaseMaterial;
import com.erp.basedata.entity.BaseSpu;
import com.erp.basedata.service.IBaseSpuService;
import com.erp.common.core.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SPU 商品 Controller
 *
 * @author ERP System
 * @since 2026-10-03
 */
@RestController
@RequestMapping("/basedata/spu")
@RequiredArgsConstructor
public class SpuController {

    private final IBaseSpuService spuService;

    /**
     * 分页查询 SPU
     */
    @SaCheckPermission("basedata:spu:list")
    @GetMapping("/page")
    public Result<Page<BaseSpu>> page(SpuPageParam param) {
        return Result.success(spuService.getSpuPage(param));
    }

    /**
     * 获取所有启用的 SPU（跨模块下拉数据源，只要求登录）
     */
    @GetMapping("/list")
    public Result<List<BaseSpu>> list() {
        return Result.success(spuService.getAllEnabled());
    }

    /**
     * 根据ID获取 SPU 详情（含已绑定的属性组）
     */
    @SaCheckPermission("basedata:spu:list")
    @GetMapping("/{id}")
    public Result<BaseSpu> getById(@PathVariable Long id) {
        return Result.success(spuService.getSpuDetail(id));
    }

    /**
     * 查询某 SPU 下的 SKU 列表
     */
    @SaCheckPermission("basedata:spu:list")
    @GetMapping("/{id}/skus")
    public Result<List<BaseMaterial>> listSkus(@PathVariable Long id) {
        return Result.success(spuService.listSkus(id));
    }

    /**
     * 新增 SPU（按勾选的属性值生成 SKU 矩阵）
     */
    @SaCheckPermission("basedata:spu:list")
    @PostMapping
    public Result<Map<String, Object>> save(@RequestBody SpuSaveDTO dto) {
        spuService.saveSpu(dto);
        Map<String, Object> data = new HashMap<>(2);
        data.put("spuId", dto.getId());
        return Result.success(data);
    }

    /**
     * 更新 SPU（属性组合只增不删）
     */
    @SaCheckPermission("basedata:spu:list")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody SpuSaveDTO dto) {
        dto.setId(id);
        spuService.updateSpu(dto);
        return Result.success();
    }

    /**
     * 删除 SPU（存在 SKU 引用时拒绝）
     */
    @SaCheckPermission("basedata:spu:list")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        spuService.deleteSpu(id);
        return Result.success();
    }

    /**
     * 仅按属性值组合生成 SKU 矩阵（不新增 SPU，用于补生成）
     */
    @SaCheckPermission("basedata:spu:list")
    @PostMapping("/{id}/generate-skus")
    public Result<Map<String, Object>> generateSkus(@PathVariable Long id,
                                                    @RequestBody SpuSaveDTO dto) {
        int created = spuService.generateSkus(id, dto.getSelections());
        Map<String, Object> data = new HashMap<>(2);
        data.put("created", created);
        return Result.success(data);
    }
}

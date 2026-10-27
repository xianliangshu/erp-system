package com.erp.web.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.erp.basedata.entity.BaseMaterialCode;
import com.erp.basedata.service.IMaterialCodeService;
import com.erp.common.core.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * SKU 一品多码 Controller
 *
 * <p>复用物料信息菜单的权限串（本功能是物料编辑表单的附属能力），
 * 不新增权限串，避免"注解串必须已存在于 sys_menu.permission"的落地风险。</p>
 *
 * @author ERP System
 * @since 2026-10-03
 */
@RestController
@RequestMapping("/basedata/material-code")
@RequiredArgsConstructor
public class MaterialCodeController {

    private final IMaterialCodeService materialCodeService;

    /**
     * 查询某 SKU 的全部编码
     */
    @SaCheckPermission("basedata:material:info")
    @GetMapping("/list")
    public Result<List<BaseMaterialCode>> list(@RequestParam Long materialId) {
        return Result.success(materialCodeService.listByMaterial(materialId));
    }

    /**
     * 按编码查询（扫码入口，跨模块复用，只要求登录）
     */
    @GetMapping("/by-code")
    public Result<List<BaseMaterialCode>> byCode(@RequestParam String code,
                                                 @RequestParam(required = false) Integer codeType) {
        BaseMaterialCode hit = materialCodeService.getByCode(code, codeType);
        return Result.success(hit == null ? List.of() : List.of(hit));
    }

    /**
     * 整体覆盖某 SKU 的编码集合
     */
    @SaCheckPermission("basedata:material:info")
    @PostMapping("/{materialId}")
    public Result<Void> save(@PathVariable Long materialId, @RequestBody List<BaseMaterialCode> codes) {
        materialCodeService.saveCodes(materialId, codes);
        return Result.success();
    }

    /**
     * 删除单条编码（主编号不可删）
     */
    @SaCheckPermission("basedata:material:info")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        materialCodeService.deleteCode(id);
        return Result.success();
    }
}

package com.erp.web.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.erp.business.entity.PreTakeStockSheet;
import com.erp.business.service.IPreTakeStockService;
import com.erp.common.core.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 库存预先盘点单 Controller
 *
 * <p>预先盘点单是正式盘点之前的分次清点记录（初盘/复盘/抽盘），<b>不产生任何库存变动</b>。</p>
 *
 * @author ERP System
 */
@RestController
@RequestMapping("/stock/take/pre")
@RequiredArgsConstructor
public class PreTakeStockController {

    private final IPreTakeStockService preTakeStockService;

    /**
     * 分页查询
     */
    @SaCheckPermission("stock:take:pre:query")
    @GetMapping("/page")
    public Result<IPage<PreTakeStockSheet>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) Long scId,
            @RequestParam(required = false) Integer takeStatus) {
        return Result.success(preTakeStockService.pageList(current, size, scId, takeStatus, code));
    }

    /**
     * 查询详情（含明细）
     */
    @SaCheckPermission("stock:take:pre:query")
    @GetMapping("/{id}")
    public Result<PreTakeStockSheet> getById(@PathVariable Long id) {
        return Result.success(preTakeStockService.getDetail(id));
    }

    /**
     * 下拉选项（供库存盘点单「关联预先盘点单」使用）。
     *
     * <p>刻意<b>不加</b>权限注解：这是跨页面复用的下拉数据源。
     */
    @GetMapping("/options")
    public Result<List<PreTakeStockSheet>> options() {
        return Result.success(preTakeStockService.listOptions());
    }

    /**
     * 新增（主表 + 明细）
     */
    @SaCheckPermission("stock:take:pre:add")
    @PostMapping
    public Result<?> add(@RequestBody PreTakeStockSheet sheet) {
        return Result.success(preTakeStockService.create(sheet, sheet.getDetails()));
    }

    /**
     * 修改（主表 + 明细整体覆盖）
     */
    @SaCheckPermission("stock:take:pre:modify")
    @PutMapping
    public Result<?> modify(@RequestBody PreTakeStockSheet sheet) {
        boolean success = preTakeStockService.modify(sheet, sheet.getDetails());
        return success ? Result.success() : Result.error("修改失败");
    }

    /**
     * 删除
     */
    @SaCheckPermission("stock:take:pre:delete")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        boolean success = preTakeStockService.deleteById(id);
        return success ? Result.success() : Result.error("删除失败");
    }
}

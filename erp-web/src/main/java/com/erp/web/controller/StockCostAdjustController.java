package com.erp.web.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.erp.business.entity.StockCostAdjustSheet;
import com.erp.business.service.IStockCostAdjustService;
import com.erp.common.constant.SessionKeys;
import com.erp.common.core.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 库存成本调整单 Controller
 *
 * @author ERP System
 */
@RestController
@RequestMapping("/stock/adjust/cost")
@RequiredArgsConstructor
public class StockCostAdjustController {

    private final IStockCostAdjustService stockCostAdjustService;

    /**
     * 分页查询
     */
    @SaCheckPermission("stock:adjust:cost:query")
    @GetMapping("/page")
    public Result<IPage<StockCostAdjustSheet>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) Long scId,
            @RequestParam(required = false) Integer status) {
        return Result.success(stockCostAdjustService.pageList(current, size, code, scId, status));
    }

    /**
     * 查询某商品在某仓库的当前库存与成本价（制单时展示用）
     */
    @SaCheckPermission("stock:adjust:cost:query")
    @GetMapping("/stock-info")
    public Result<Map<String, Object>> stockInfo(@RequestParam Long scId, @RequestParam Long productId) {
        return Result.success(stockCostAdjustService.getStockInfo(scId, productId));
    }

    /**
     * 查询详情（含明细）
     */
    @SaCheckPermission("stock:adjust:cost:query")
    @GetMapping("/{id}")
    public Result<StockCostAdjustSheet> getById(@PathVariable Long id) {
        return Result.success(stockCostAdjustService.getDetail(id));
    }

    /**
     * 新增（主表 + 明细）
     */
    @SaCheckPermission("stock:adjust:cost:add")
    @PostMapping
    public Result<?> add(@RequestBody StockCostAdjustSheet sheet) {
        boolean success = stockCostAdjustService.add(sheet, sheet.getDetails());
        return success ? Result.success(sheet) : Result.error("新增失败");
    }

    /**
     * 修改（主表 + 明细，仅待审核可改）
     */
    @SaCheckPermission("stock:adjust:cost:modify")
    @PutMapping
    public Result<?> modify(@RequestBody StockCostAdjustSheet sheet) {
        boolean success = stockCostAdjustService.modify(sheet, sheet.getDetails());
        return success ? Result.success() : Result.error("修改失败");
    }

    /**
     * 审核通过：回写库存成本价与金额（数量不变）
     */
    @SaCheckPermission("stock:adjust:cost:approve")
    @PostMapping("/approve/{id}")
    public Result<?> approvePass(@PathVariable Long id) {
        boolean success = stockCostAdjustService.approvePass(id, currentUsername());
        return success ? Result.success() : Result.error("审核失败");
    }

    /**
     * 审核拒绝
     */
    @SaCheckPermission("stock:adjust:cost:approve")
    @PostMapping("/refuse/{id}")
    public Result<?> approveRefuse(@PathVariable Long id, @RequestBody Map<String, String> params) {
        boolean success = stockCostAdjustService.approveRefuse(id, params.get("refuseReason"), currentUsername());
        return success ? Result.success() : Result.error("审核失败");
    }

    /**
     * 删除
     */
    @SaCheckPermission("stock:adjust:cost:delete")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        boolean success = stockCostAdjustService.deleteById(id);
        return success ? Result.success() : Result.error("删除失败");
    }

    /**
     * 取当前操作人登录名（与 LogAspect 同一取法，避免逐次回查数据库）
     */
    private String currentUsername() {
        try {
            String username = StpUtil.getSession().getString(SessionKeys.USERNAME);
            if (StringUtils.hasText(username)) {
                return username;
            }
            Object loginId = StpUtil.getLoginIdDefaultNull();
            return loginId != null ? String.valueOf(loginId) : null;
        } catch (Exception e) {
            return null;
        }
    }
}

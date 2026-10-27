package com.erp.web.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.erp.business.dto.TakeStockNumsParam;
import com.erp.business.entity.TakeStockPlan;
import com.erp.business.entity.TakeStockPlanDetail;
import com.erp.business.service.ITakeStockPlanService;
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
 * 库存盘点任务 Controller
 *
 * <p>创建任务时按「全场/单品/分类/品牌」解析商品范围并写入<b>库存快照</b>；
 * 录入盘点数量后生成差异，差异最终落成一张库存盘点单（走盘点单已有的审核流程）。</p>
 *
 * @author ERP System
 */
@RestController
@RequestMapping("/stock/take/plan")
@RequiredArgsConstructor
public class TakeStockPlanController {

    private final ITakeStockPlanService takeStockPlanService;

    /**
     * 分页查询
     */
    @SaCheckPermission("stock:take:plan:query")
    @GetMapping("/page")
    public Result<IPage<TakeStockPlan>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) Long scId,
            @RequestParam(required = false) Integer takeStatus,
            @RequestParam(required = false) Integer takeType) {
        return Result.success(takeStockPlanService.pageList(current, size, scId, takeStatus, takeType, code));
    }

    /**
     * 查询详情（含明细、商品信息、实时库存与差异）
     */
    @SaCheckPermission("stock:take:plan:query")
    @GetMapping("/{id}")
    public Result<TakeStockPlan> getById(@PathVariable Long id) {
        return Result.success(takeStockPlanService.getDetail(id));
    }

    /**
     * 未完成的任务下拉（供库存盘点单「关联盘点任务」使用）。
     *
     * <p>刻意<b>不加</b>权限注解：这是跨页面复用的下拉数据源，
     * 加了会让只有盘点单权限的角色下拉为空（照采购单选仓库的教训）。</p>
     */
    @GetMapping("/options")
    public Result<List<TakeStockPlan>> options() {
        return Result.success(takeStockPlanService.listActiveOptions());
    }

    /**
     * 任务明细（供盘点单「关联任务后带出商品」使用），同样不加权限注解。
     */
    @GetMapping("/{id}/details")
    public Result<List<TakeStockPlanDetail>> details(@PathVariable Long id) {
        TakeStockPlan plan = takeStockPlanService.getDetail(id);
        return Result.success(plan != null ? plan.getDetails() : List.of());
    }

    /**
     * 创建盘点任务
     */
    @SaCheckPermission("stock:take:plan:add")
    @PostMapping
    public Result<?> add(@RequestBody TakeStockPlan plan) {
        return Result.success(takeStockPlanService.createPlan(plan));
    }

    /**
     * 录入盘点数量
     */
    @SaCheckPermission("stock:take:plan:modify")
    @PutMapping("/take-nums")
    public Result<?> saveTakeNums(@RequestBody TakeStockNumsParam param) {
        if (param.getPlanId() == null) {
            return Result.error("缺少盘点任务ID");
        }
        boolean success = takeStockPlanService.saveTakeNums(param.getPlanId(), param.getDetails());
        return success ? Result.success() : Result.error("保存失败");
    }

    /**
     * 生成差异 → 落成库存盘点单
     */
    @SaCheckPermission("stock:take:plan:approve")
    @PostMapping("/generate-diff/{id}")
    public Result<?> generateDiff(@PathVariable Long id) {
        return Result.success(takeStockPlanService.generateDiff(id));
    }

    /**
     * 作废
     */
    @SaCheckPermission("stock:take:plan:approve")
    @PostMapping("/cancel/{id}")
    public Result<?> cancel(@PathVariable Long id) {
        boolean success = takeStockPlanService.cancel(id);
        return success ? Result.success() : Result.error("作废失败");
    }

    /**
     * 删除
     */
    @SaCheckPermission("stock:take:plan:delete")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        boolean success = takeStockPlanService.removeById(id);
        return success ? Result.success() : Result.error("删除失败");
    }
}

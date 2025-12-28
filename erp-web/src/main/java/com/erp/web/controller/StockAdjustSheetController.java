package com.erp.web.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.business.entity.StockAdjustSheet;
import com.erp.business.entity.StockAdjustSheetDetail;
import com.erp.business.enums.StockAdjustBizType;
import com.erp.business.enums.StockAdjustSheetStatus;
import com.erp.business.service.IStockAdjustSheetDetailService;
import com.erp.business.service.IStockAdjustSheetService;
import com.erp.common.core.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 库存调整单 Controller
 */
@RestController
@RequestMapping("/stock/adjust/sheet")
public class StockAdjustSheetController {

    @Autowired
    private IStockAdjustSheetService stockAdjustSheetService;

    @Autowired
    private IStockAdjustSheetDetailService stockAdjustSheetDetailService;

    /**
     * 分页查询
     */
    @GetMapping("/page")
    public Result<IPage<StockAdjustSheet>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) Long scId,
            @RequestParam(required = false) Long reasonId,
            @RequestParam(required = false) Integer bizType,
            @RequestParam(required = false) Integer status) {
        Page<StockAdjustSheet> page = new Page<>(current, size);

        StockAdjustBizType bizTypeEnum = bizType != null
                ? (bizType == 0 ? StockAdjustBizType.IN : StockAdjustBizType.OUT)
                : null;
        StockAdjustSheetStatus statusEnum = null;
        if (status != null) {
            for (StockAdjustSheetStatus s : StockAdjustSheetStatus.values()) {
                if (s.getCode().equals(status)) {
                    statusEnum = s;
                    break;
                }
            }
        }

        IPage<StockAdjustSheet> result = stockAdjustSheetService.page(page, code, scId,
                reasonId, bizTypeEnum, statusEnum);
        return Result.success(result);
    }

    /**
     * 根据ID查询详情（包含明细）
     */
    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable Long id) {
        StockAdjustSheet sheet = stockAdjustSheetService.getById(id);
        if (sheet == null) {
            return Result.error("调整单不存在");
        }
        List<StockAdjustSheetDetail> details = stockAdjustSheetDetailService.listBySheetId(id);

        Map<String, Object> result = new HashMap<>();
        result.put("sheet", sheet);
        result.put("details", details);
        return Result.success(result);
    }

    /**
     * 新增
     */
    @PostMapping
    public Result<?> add(@RequestBody Map<String, Object> params) {
        StockAdjustSheet sheet = parseSheet(params);
        List<StockAdjustSheetDetail> details = parseDetails(params);

        boolean success = stockAdjustSheetService.add(sheet, details);
        return success ? Result.success(sheet) : Result.error("新增失败");
    }

    /**
     * 修改
     */
    @PutMapping
    public Result<?> modify(@RequestBody Map<String, Object> params) {
        StockAdjustSheet sheet = parseSheet(params);
        List<StockAdjustSheetDetail> details = parseDetails(params);

        boolean success = stockAdjustSheetService.modify(sheet, details);
        return success ? Result.success() : Result.error("修改失败");
    }

    /**
     * 删除
     */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        try {
            boolean success = stockAdjustSheetService.deleteById(id);
            return success ? Result.success() : Result.error("删除失败");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 审核通过
     */
    @PostMapping("/approve/{id}")
    public Result<?> approvePass(@PathVariable Long id) {
        try {
            boolean success = stockAdjustSheetService.approvePass(id);
            return success ? Result.success() : Result.error("审核失败");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 审核拒绝
     */
    @PostMapping("/refuse/{id}")
    public Result<?> approveRefuse(@PathVariable Long id, @RequestBody Map<String, String> params) {
        try {
            String refuseReason = params.get("refuseReason");
            boolean success = stockAdjustSheetService.approveRefuse(id, refuseReason);
            return success ? Result.success() : Result.error("审核失败");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 解析调整单主表数据
     */
    @SuppressWarnings("unchecked")
    private StockAdjustSheet parseSheet(Map<String, Object> params) {
        Map<String, Object> sheetMap = (Map<String, Object>) params.get("sheet");
        if (sheetMap == null) {
            sheetMap = params;
        }

        StockAdjustSheet sheet = new StockAdjustSheet();
        if (sheetMap.get("id") != null) {
            sheet.setId(Long.valueOf(sheetMap.get("id").toString()));
        }
        sheet.setCode((String) sheetMap.get("code"));
        if (sheetMap.get("scId") != null) {
            sheet.setScId(Long.valueOf(sheetMap.get("scId").toString()));
        }
        if (sheetMap.get("reasonId") != null) {
            sheet.setReasonId(Long.valueOf(sheetMap.get("reasonId").toString()));
        }
        if (sheetMap.get("bizType") != null) {
            int bizType = Integer.parseInt(sheetMap.get("bizType").toString());
            sheet.setBizType(bizType == 0 ? StockAdjustBizType.IN : StockAdjustBizType.OUT);
        }
        sheet.setDescription((String) sheetMap.get("description"));

        return sheet;
    }

    /**
     * 解析调整单明细数据
     */
    @SuppressWarnings("unchecked")
    private List<StockAdjustSheetDetail> parseDetails(Map<String, Object> params) {
        List<Map<String, Object>> detailList = (List<Map<String, Object>>) params.get("details");
        if (detailList == null) {
            return null;
        }

        return detailList.stream().map(item -> {
            StockAdjustSheetDetail detail = new StockAdjustSheetDetail();
            if (item.get("productId") != null) {
                detail.setProductId(Long.valueOf(item.get("productId").toString()));
            }
            if (item.get("stockNum") != null) {
                detail.setStockNum(new java.math.BigDecimal(item.get("stockNum").toString()));
            }
            detail.setDescription((String) item.get("description"));
            return detail;
        }).toList();
    }
}

package com.erp.web.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.business.dto.SettleInOutItemDTO;
import com.erp.business.dto.SettleInOutItemQueryDTO;
import com.erp.business.entity.SettleInOutItem;
import com.erp.business.service.ISettleInOutItemService;
import com.erp.common.core.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 收支项目 Controller
 */
@Tag(name = "收支项目")
@RestController
@RequestMapping("/business/settle/item")
@RequiredArgsConstructor
public class SettleInOutItemController {

    private final ISettleInOutItemService itemService;

    /**
     * 分页查询
     */
    @Operation(summary = "分页查询")
    @GetMapping("/page")
    public Result<Page<SettleInOutItem>> page(@RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            SettleInOutItemQueryDTO queryDTO) {
        Page<SettleInOutItem> page = itemService.queryPage(current, size, queryDTO);
        return Result.success(page);
    }

    /**
     * 获取详情
     */
    @Operation(summary = "获取详情")
    @GetMapping("/{id}")
    public Result<SettleInOutItem> getInfo(@PathVariable Long id) {
        return Result.success(itemService.getById(id));
    }

    /**
     * 新增
     */
    @Operation(summary = "新增")
    @PostMapping
    public Result<Void> add(@RequestBody SettleInOutItemDTO dto) {
        SettleInOutItem item = new SettleInOutItem();
        if (dto.getCode() == null || dto.getCode().isEmpty()) {
            item.setCode(itemService.generateCode());
        } else {
            item.setCode(dto.getCode());
        }
        item.setName(dto.getName());
        item.setItemType(dto.getItemType());
        item.setStatus(dto.getStatus());
        item.setDescription(dto.getDescription());
        itemService.save(item);
        return Result.success();
    }

    /**
     * 修改
     */
    @Operation(summary = "修改")
    @PutMapping
    public Result<Void> edit(@RequestBody SettleInOutItemDTO dto) {
        SettleInOutItem item = itemService.getById(dto.getId());
        if (item == null) {
            return Result.error("项目不存在");
        }
        item.setName(dto.getName());
        item.setItemType(dto.getItemType());
        item.setStatus(dto.getStatus());
        item.setDescription(dto.getDescription());
        itemService.updateById(item);
        return Result.success();
    }

    /**
     * 删除
     */
    @Operation(summary = "删除")
    @DeleteMapping("/{ids}")
    public Result<Void> remove(@PathVariable List<Long> ids) {
        itemService.removeByIds(ids);
        return Result.success();
    }

    /**
     * 获取所有启用状态的项目
     */
    @Operation(summary = "获取所有启用状态的项目")
    @GetMapping("/list-enabled")
    public Result<List<SettleInOutItem>> listEnabled(Integer itemType) {
        return Result.success(itemService.listEnabled(itemType));
    }

    /**
     * 获取所有项目列表
     */
    @Operation(summary = "获取所有项目列表")
    @GetMapping("/list")
    public Result<List<SettleInOutItem>> list() {
        return Result.success(itemService.list());
    }
}

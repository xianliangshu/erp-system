package com.erp.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.business.entity.StockAdjustSheet;
import com.erp.business.entity.StockAdjustSheetDetail;
import com.erp.business.enums.ProductStockBizType;
import com.erp.business.enums.StockAdjustBizType;
import com.erp.business.enums.StockAdjustSheetStatus;
import com.erp.business.mapper.StockAdjustSheetMapper;
import com.erp.business.service.IProductStockService;
import com.erp.business.service.IStockAdjustSheetDetailService;
import com.erp.business.service.IStockAdjustSheetService;
import com.erp.common.service.ICodeGenerateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 库存调整单 Service 实现
 */
@Service
public class StockAdjustSheetServiceImpl extends ServiceImpl<StockAdjustSheetMapper, StockAdjustSheet>
        implements IStockAdjustSheetService {
    @Autowired
    private ICodeGenerateService codeGenerateService;

    @Autowired
    private IStockAdjustSheetDetailService detailService;

    @Autowired
    private IProductStockService productStockService;

    @Override
    public IPage<StockAdjustSheet> page(IPage<StockAdjustSheet> page, String code, Long scId,
            Long reasonId, StockAdjustBizType bizType, StockAdjustSheetStatus status) {
        LambdaQueryWrapper<StockAdjustSheet> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(code), StockAdjustSheet::getCode, code)
                .eq(scId != null, StockAdjustSheet::getScId, scId)
                .eq(reasonId != null, StockAdjustSheet::getReasonId, reasonId)
                .eq(bizType != null, StockAdjustSheet::getBizType, bizType)
                .eq(status != null, StockAdjustSheet::getStatus, status)
                .orderByDesc(StockAdjustSheet::getCreateTime);
        return page(page, wrapper);
    }

    @Override
    public String generateCode() {
        return codeGenerateService.generate("STOCK_ADJUST");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean add(StockAdjustSheet sheet, List<StockAdjustSheetDetail> details) {
        // 生成编号
        if (!StringUtils.hasText(sheet.getCode())) {
            sheet.setCode(generateCode());
        }
        // 设置初始状态
        sheet.setStatus(StockAdjustSheetStatus.CREATED);

        // 保存主表
        boolean success = save(sheet);
        if (!success) {
            return false;
        }

        // 保存明细
        if (details != null && !details.isEmpty()) {
            int sort = 1;
            for (StockAdjustSheetDetail detail : details) {
                detail.setSheetId(sheet.getId());
                detail.setSort(sort++);
            }
            detailService.saveBatch(details);
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean modify(StockAdjustSheet sheet, List<StockAdjustSheetDetail> details) {
        StockAdjustSheet existing = getById(sheet.getId());
        if (existing == null) {
            throw new RuntimeException("调整单不存在");
        }
        if (existing.getStatus() != StockAdjustSheetStatus.CREATED) {
            throw new RuntimeException("只能修改待审核状态的调整单");
        }

        // 更新主表
        boolean success = updateById(sheet);
        if (!success) {
            return false;
        }

        // 删除旧明细，保存新明细
        detailService.deleteBySheetId(sheet.getId());
        if (details != null && !details.isEmpty()) {
            int sort = 1;
            for (StockAdjustSheetDetail detail : details) {
                detail.setId(null);
                detail.setSheetId(sheet.getId());
                detail.setSort(sort++);
            }
            detailService.saveBatch(details);
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean approvePass(Long id) {
        StockAdjustSheet sheet = getById(id);
        if (sheet == null) {
            throw new RuntimeException("调整单不存在");
        }
        if (sheet.getStatus() != StockAdjustSheetStatus.CREATED) {
            throw new RuntimeException("只能审核待审核状态的调整单");
        }

        // 更新状态
        sheet.setStatus(StockAdjustSheetStatus.APPROVE_PASS);
        sheet.setApproveTime(LocalDateTime.now());
        // TODO: 获取当前登录用户
        // sheet.setApproveBy(currentUser);

        boolean success = updateById(sheet);
        if (!success) {
            return false;
        }

        // 更新库存
        List<StockAdjustSheetDetail> details = detailService.listBySheetId(id);
        for (StockAdjustSheetDetail detail : details) {
            BigDecimal stockNum = detail.getStockNum();
            // 根据业务类型决定增减库存
            // 修复(2026-10-03)：原实现对两个方向都调 addStock(..., BigDecimal.ZERO)，
            // 而出库方向还写成 addStock(数量.negate())。addStock 的 updateCost=true 会
            // 把传入的 ZERO 写进成本价 ⇒ 调整单一审核，该商品成本价与库存金额直接归零。
            // 调整单只修正数量、不影响价格，故出库走 subStock、入库走 addStockKeepCost。
            if (sheet.getBizType() == StockAdjustBizType.OUT) {
                productStockService.subStock(
                        sheet.getScId(),
                        detail.getProductId(),
                        stockNum,
                        BigDecimal.ZERO, // 库存调整不影响价格
                        sheet.getId(),
                        sheet.getCode(),
                        ProductStockBizType.STOCK_ADJUST);
            } else {
                productStockService.addStockKeepCost(
                        sheet.getScId(),
                        detail.getProductId(),
                        stockNum,
                        BigDecimal.ZERO, // 库存调整不影响价格
                        sheet.getId(),
                        sheet.getCode(),
                        ProductStockBizType.STOCK_ADJUST);
            }
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean approveRefuse(Long id, String refuseReason) {
        StockAdjustSheet sheet = getById(id);
        if (sheet == null) {
            throw new RuntimeException("调整单不存在");
        }
        if (sheet.getStatus() != StockAdjustSheetStatus.CREATED) {
            throw new RuntimeException("只能审核待审核状态的调整单");
        }

        // 更新状态
        sheet.setStatus(StockAdjustSheetStatus.APPROVE_REFUSE);
        sheet.setApproveTime(LocalDateTime.now());
        sheet.setRefuseReason(refuseReason);

        return updateById(sheet);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteById(Long id) {
        StockAdjustSheet sheet = getById(id);
        if (sheet == null) {
            throw new RuntimeException("调整单不存在");
        }
        if (sheet.getStatus() == StockAdjustSheetStatus.APPROVE_PASS) {
            throw new RuntimeException("审核通过的调整单不能删除");
        }

        // 删除明细
        detailService.deleteBySheetId(id);

        // 删除主表（软删除）
        return removeById(id);
    }
}

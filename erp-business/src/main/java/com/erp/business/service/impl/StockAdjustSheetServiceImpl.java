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
        String prefix = "SA" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String maxCode = baseMapper.selectMaxCode();

        int seq = 1;
        if (maxCode != null && maxCode.startsWith(prefix)) {
            String seqStr = maxCode.substring(prefix.length());
            seq = Integer.parseInt(seqStr) + 1;
        }
        return prefix + String.format("%04d", seq);
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
            if (sheet.getBizType() == StockAdjustBizType.OUT) {
                stockNum = stockNum.negate(); // 出库调整为负数
            }

            // 更新库存
            productStockService.addStock(
                    sheet.getScId(),
                    detail.getProductId(),
                    stockNum,
                    BigDecimal.ZERO, // 库存调整不影响价格
                    sheet.getId(),
                    sheet.getCode(),
                    ProductStockBizType.STOCK_ADJUST);
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

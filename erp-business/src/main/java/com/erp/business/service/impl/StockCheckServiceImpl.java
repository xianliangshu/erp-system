package com.erp.business.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.business.dto.StockCheckDTO;
import com.erp.business.entity.StockCheck;
import com.erp.business.entity.StockCheckDetail;
import com.erp.business.enums.ProductStockBizType;
import com.erp.business.mapper.StockCheckDetailMapper;
import com.erp.business.mapper.StockCheckMapper;
import com.erp.business.service.IProductStockService;
import com.erp.business.service.IStockCheckService;
import com.erp.common.service.ICodeGenerateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockCheckServiceImpl extends ServiceImpl<StockCheckMapper, StockCheck> implements IStockCheckService {
    private final ICodeGenerateService codeGenerateService;

    private final StockCheckDetailMapper checkDetailMapper;
    private final IProductStockService productStockService;

    @Override
    public Page<StockCheck> getCheckPage(Long current, Long size, Long scId, Integer status) {
        Page<StockCheck> page = new Page<>(current != null ? current : 1, size != null ? size : 10);
        LambdaQueryWrapper<StockCheck> wrapper = new LambdaQueryWrapper<>();
        if (scId != null)
            wrapper.eq(StockCheck::getScId, scId);
        if (status != null)
            wrapper.eq(StockCheck::getStatus, status);
        wrapper.orderByDesc(StockCheck::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createCheck(StockCheckDTO dto) {
        StockCheck check = new StockCheck();
        check.setCode(generateCheckCode());
        check.setScId(dto.getScId());
        if (dto.getCheckDate() != null)
            check.setCheckDate(LocalDate.parse(dto.getCheckDate(), DateTimeFormatter.ISO_DATE));
        check.setDescription(dto.getDescription());
        check.setStatus(0);
        // 单据来源关联：盘点任务生成的差异单回填 planId；预先盘点单转来的回填 preSheetId。
        // 手工在盘点单页面新建时两者均为 null，不影响原有流程。
        check.setPlanId(dto.getPlanId());
        check.setPreSheetId(dto.getPreSheetId());

        BigDecimal totalProfit = BigDecimal.ZERO;
        BigDecimal totalLoss = BigDecimal.ZERO;

        if (dto.getDetails() != null) {
            for (StockCheckDTO.StockCheckDetailDTO d : dto.getDetails()) {
                BigDecimal diff = d.getActualNum().subtract(d.getStockNum());
                if (diff.compareTo(BigDecimal.ZERO) > 0)
                    totalProfit = totalProfit.add(diff);
                else if (diff.compareTo(BigDecimal.ZERO) < 0)
                    totalLoss = totalLoss.add(diff.abs());
            }
        }
        check.setTotalProfitNum(totalProfit);
        check.setTotalLossNum(totalLoss);
        this.save(check);

        if (dto.getDetails() != null) {
            for (StockCheckDTO.StockCheckDetailDTO detailDTO : dto.getDetails()) {
                StockCheckDetail detail = new StockCheckDetail();
                detail.setCheckId(check.getId());
                detail.setProductId(detailDTO.getProductId());
                detail.setStockNum(detailDTO.getStockNum());
                detail.setActualNum(detailDTO.getActualNum());
                detail.setDiffNum(detailDTO.getActualNum().subtract(detailDTO.getStockNum()));
                detail.setCostPrice(detailDTO.getCostPrice());
                detail.setDiffAmount(detail.getDiffNum().multiply(detailDTO.getCostPrice()));
                detail.setDescription(detailDTO.getDescription());
                checkDetailMapper.insert(detail);
            }
        }
        return check.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCheck(StockCheckDTO dto) {
        StockCheck check = this.getById(dto.getId());
        if (check == null)
            throw new RuntimeException("盘点单不存在");
        if (!Integer.valueOf(0).equals(check.getStatus()))
            throw new RuntimeException("只有待审核的盘点单才能修改");

        if (dto.getCheckDate() != null)
            check.setCheckDate(LocalDate.parse(dto.getCheckDate(), DateTimeFormatter.ISO_DATE));
        check.setDescription(dto.getDescription());
        // 注意：planId / preSheetId 是「单据来源」标识，仅在创建时写入，修改时不覆盖
        // （前端编辑表单不会回传这两个字段，覆盖会把来源清空）。

        LambdaQueryWrapper<StockCheckDetail> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(StockCheckDetail::getCheckId, check.getId());
        checkDetailMapper.delete(deleteWrapper);

        BigDecimal totalProfit = BigDecimal.ZERO;
        BigDecimal totalLoss = BigDecimal.ZERO;

        if (dto.getDetails() != null) {
            for (StockCheckDTO.StockCheckDetailDTO detailDTO : dto.getDetails()) {
                BigDecimal diff = detailDTO.getActualNum().subtract(detailDTO.getStockNum());
                if (diff.compareTo(BigDecimal.ZERO) > 0)
                    totalProfit = totalProfit.add(diff);
                else if (diff.compareTo(BigDecimal.ZERO) < 0)
                    totalLoss = totalLoss.add(diff.abs());

                StockCheckDetail detail = new StockCheckDetail();
                detail.setCheckId(check.getId());
                detail.setProductId(detailDTO.getProductId());
                detail.setStockNum(detailDTO.getStockNum());
                detail.setActualNum(detailDTO.getActualNum());
                detail.setDiffNum(diff);
                detail.setCostPrice(detailDTO.getCostPrice());
                detail.setDiffAmount(diff.multiply(detailDTO.getCostPrice()));
                detail.setDescription(detailDTO.getDescription());
                checkDetailMapper.insert(detail);
            }
        }
        check.setTotalProfitNum(totalProfit);
        check.setTotalLossNum(totalLoss);
        this.updateById(check);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveCheck(Long id) {
        StockCheck check = this.getById(id);
        if (check == null)
            throw new RuntimeException("盘点单不存在");
        if (!Integer.valueOf(0).equals(check.getStatus()))
            throw new RuntimeException("盘点单已审核");

        check.setStatus(1);
        this.updateById(check);

        LambdaQueryWrapper<StockCheckDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StockCheckDetail::getCheckId, id);
        List<StockCheckDetail> details = checkDetailMapper.selectList(wrapper);

        // 根据盘点差异调整库存
        for (StockCheckDetail detail : details) {
            if (detail.getDiffNum().compareTo(BigDecimal.ZERO) > 0) {
                // 盘盈 - 增加库存
                // 修复(2026-10-03)：盘盈只增加数量，成本价应保持库存原值不变。
                // 原实现用 addStock 传盘点单上的 costPrice，会把成本价覆盖成盘点单填的价格
                // （盘点单成本价来自前端输入，未必等于库存实际成本）⇒ 库存金额被改错。
                productStockService.addStockKeepCost(check.getScId(), detail.getProductId(), detail.getDiffNum(),
                        detail.getCostPrice(), check.getId(), check.getCode(), ProductStockBizType.STOCK_CHECK_PROFIT);
            } else if (detail.getDiffNum().compareTo(BigDecimal.ZERO) < 0) {
                // 盘亏 - 扣减库存（出库走 subStock，不覆盖成本价）
                productStockService.subStock(check.getScId(), detail.getProductId(), detail.getDiffNum().abs(),
                        detail.getCostPrice(), check.getId(), check.getCode(), ProductStockBizType.STOCK_CHECK_LOSS);
            }
        }
    }

    private String generateCheckCode() {
        return codeGenerateService.generate("STOCK_CHECK");
    }
}

package com.erp.business.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.business.dto.StockTransferDTO;
import com.erp.business.entity.StockTransfer;
import com.erp.business.entity.StockTransferDetail;
import com.erp.business.enums.ProductStockBizType;
import com.erp.business.mapper.StockTransferDetailMapper;
import com.erp.business.mapper.StockTransferMapper;
import com.erp.business.service.IProductStockService;
import com.erp.business.service.IStockTransferService;
import com.erp.common.service.ICodeGenerateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StockTransferServiceImpl extends ServiceImpl<StockTransferMapper, StockTransfer>
        implements IStockTransferService {
    private final ICodeGenerateService codeGenerateService;

    private final StockTransferDetailMapper transferDetailMapper;
    private final IProductStockService productStockService;

    @Override
    public Page<StockTransfer> getTransferPage(Long current, Long size, Long outScId, Long inScId, Integer status) {
        Page<StockTransfer> page = new Page<>(current != null ? current : 1, size != null ? size : 10);
        LambdaQueryWrapper<StockTransfer> wrapper = new LambdaQueryWrapper<>();
        if (outScId != null)
            wrapper.eq(StockTransfer::getOutScId, outScId);
        if (inScId != null)
            wrapper.eq(StockTransfer::getInScId, inScId);
        if (status != null)
            wrapper.eq(StockTransfer::getStatus, status);
        wrapper.orderByDesc(StockTransfer::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createTransfer(StockTransferDTO dto) {
        // 验证调出仓库和调入仓库不能相同
        if (dto.getOutScId().equals(dto.getInScId())) {
            throw new RuntimeException("调出仓库和调入仓库不能相同");
        }

        StockTransfer transfer = new StockTransfer();
        transfer.setCode(generateTransferCode());
        transfer.setOutScId(dto.getOutScId());
        transfer.setInScId(dto.getInScId());
        if (dto.getTransferDate() != null) {
            transfer.setTransferDate(LocalDate.parse(dto.getTransferDate(), DateTimeFormatter.ISO_DATE));
        }
        transfer.setDescription(dto.getDescription());
        transfer.setStatus(0);

        BigDecimal totalNum = BigDecimal.ZERO;
        if (dto.getDetails() != null) {
            for (StockTransferDTO.StockTransferDetailDTO d : dto.getDetails()) {
                totalNum = totalNum.add(d.getTransferNum());
            }
        }
        transfer.setTotalNum(totalNum);
        this.save(transfer);

        // 保存明细
        if (dto.getDetails() != null) {
            for (StockTransferDTO.StockTransferDetailDTO detailDTO : dto.getDetails()) {
                StockTransferDetail detail = new StockTransferDetail();
                detail.setTransferId(transfer.getId());
                detail.setProductId(detailDTO.getProductId());
                detail.setTransferNum(detailDTO.getTransferNum());
                detail.setCostPrice(detailDTO.getCostPrice());
                detail.setDescription(detailDTO.getDescription());
                transferDetailMapper.insert(detail);
            }
        }
        return transfer.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTransfer(StockTransferDTO dto) {
        StockTransfer transfer = this.getById(dto.getId());
        if (transfer == null)
            throw new RuntimeException("调拨单不存在");
        if (!Integer.valueOf(0).equals(transfer.getStatus()))
            throw new RuntimeException("只有待确认的调拨单才能修改");

        // 验证调出仓库和调入仓库不能相同
        if (dto.getOutScId().equals(dto.getInScId())) {
            throw new RuntimeException("调出仓库和调入仓库不能相同");
        }

        transfer.setOutScId(dto.getOutScId());
        transfer.setInScId(dto.getInScId());
        if (dto.getTransferDate() != null) {
            transfer.setTransferDate(LocalDate.parse(dto.getTransferDate(), DateTimeFormatter.ISO_DATE));
        }
        transfer.setDescription(dto.getDescription());

        // 删除旧明细
        LambdaQueryWrapper<StockTransferDetail> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(StockTransferDetail::getTransferId, transfer.getId());
        transferDetailMapper.delete(deleteWrapper);

        // 保存新明细
        BigDecimal totalNum = BigDecimal.ZERO;
        if (dto.getDetails() != null) {
            for (StockTransferDTO.StockTransferDetailDTO detailDTO : dto.getDetails()) {
                totalNum = totalNum.add(detailDTO.getTransferNum());

                StockTransferDetail detail = new StockTransferDetail();
                detail.setTransferId(transfer.getId());
                detail.setProductId(detailDTO.getProductId());
                detail.setTransferNum(detailDTO.getTransferNum());
                detail.setCostPrice(detailDTO.getCostPrice());
                detail.setDescription(detailDTO.getDescription());
                transferDetailMapper.insert(detail);
            }
        }
        transfer.setTotalNum(totalNum);
        this.updateById(transfer);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmTransfer(Long id) {
        StockTransfer transfer = this.getById(id);
        if (transfer == null)
            throw new RuntimeException("调拨单不存在");
        if (!Integer.valueOf(0).equals(transfer.getStatus()))
            throw new RuntimeException("调拨单已确认");

        // 获取调拨明细
        LambdaQueryWrapper<StockTransferDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StockTransferDetail::getTransferId, id);
        List<StockTransferDetail> details = transferDetailMapper.selectList(wrapper);

        // 修复(2026-10-03)：调出仓库出库前必须校验库存，否则可以把库存扣成负数。
        // 按商品把调拨量汇总后再校验：同一商品可能落在多条明细上，逐条校验会漏掉累计超出。
        Map<Long, BigDecimal> requireByProduct = new LinkedHashMap<>();
        for (StockTransferDetail detail : details) {
            requireByProduct.merge(detail.getProductId(), detail.getTransferNum(), BigDecimal::add);
        }
        for (Map.Entry<Long, BigDecimal> entry : requireByProduct.entrySet()) {
            productStockService.checkStockEnough(transfer.getOutScId(), entry.getKey(), entry.getValue());
        }

        // 执行库存变动
        for (StockTransferDetail detail : details) {
            // 调出仓库扣减库存（出库走 subStock，不能用 addStock 传负数）
            productStockService.subStock(
                    transfer.getOutScId(),
                    detail.getProductId(),
                    detail.getTransferNum(),
                    detail.getCostPrice(),
                    transfer.getId(),
                    transfer.getCode(),
                    ProductStockBizType.STOCK_TRANSFER_OUT);

            // 调入仓库增加库存（调拨入库传的是成本价，走 addStock 正确）
            productStockService.addStock(
                    transfer.getInScId(),
                    detail.getProductId(),
                    detail.getTransferNum(),
                    detail.getCostPrice(),
                    transfer.getId(),
                    transfer.getCode(),
                    ProductStockBizType.STOCK_TRANSFER_IN);
        }

        // 更新状态为已确认
        transfer.setStatus(1);
        this.updateById(transfer);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTransfer(Long id) {
        StockTransfer transfer = this.getById(id);
        if (transfer == null)
            throw new RuntimeException("调拨单不存在");
        if (!Integer.valueOf(0).equals(transfer.getStatus()))
            throw new RuntimeException("已确认的调拨单不能删除");

        // 删除明细
        LambdaQueryWrapper<StockTransferDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StockTransferDetail::getTransferId, id);
        transferDetailMapper.delete(wrapper);

        // 删除主表
        this.removeById(id);
    }

    private String generateTransferCode() {
        return codeGenerateService.generate("STOCK_TRANSFER");
    }
}

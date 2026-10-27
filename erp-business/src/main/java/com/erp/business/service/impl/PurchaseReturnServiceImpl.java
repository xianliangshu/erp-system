package com.erp.business.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.business.dto.PurchaseReturnDTO;
import com.erp.business.entity.PurchaseReceipt;
import com.erp.business.entity.PurchaseReturn;
import com.erp.business.entity.PurchaseReturnDetail;
import com.erp.business.enums.ProductStockBizType;
import com.erp.business.mapper.PurchaseReturnDetailMapper;
import com.erp.business.mapper.PurchaseReturnMapper;
import com.erp.business.service.IPurchaseReceiptService;
import com.erp.business.service.IPurchaseReturnService;
import com.erp.business.service.IProductStockService;
import com.erp.common.service.ICodeGenerateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 采购退货服务实现
 */
@Service
@RequiredArgsConstructor
public class PurchaseReturnServiceImpl extends ServiceImpl<PurchaseReturnMapper, PurchaseReturn>
        implements IPurchaseReturnService {
    private final ICodeGenerateService codeGenerateService;

    private final PurchaseReturnDetailMapper returnDetailMapper;
    private final IPurchaseReceiptService purchaseReceiptService;
    private final IProductStockService productStockService;

    @Override
    public Page<PurchaseReturn> getReturnPage(Long current, Long size, Long receiptId, Long supplierId,
            Integer status) {
        Page<PurchaseReturn> page = new Page<>(current != null ? current : 1, size != null ? size : 10);
        LambdaQueryWrapper<PurchaseReturn> wrapper = new LambdaQueryWrapper<>();

        if (receiptId != null) {
            wrapper.eq(PurchaseReturn::getReceiptId, receiptId);
        }
        if (supplierId != null) {
            wrapper.eq(PurchaseReturn::getSupplierId, supplierId);
        }
        if (status != null) {
            wrapper.eq(PurchaseReturn::getStatus, status);
        }
        wrapper.orderByDesc(PurchaseReturn::getCreateTime);

        return this.page(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createReturn(PurchaseReturnDTO dto) {
        // 1. 检查收货单是否存在且已确认
        PurchaseReceipt receipt = purchaseReceiptService.getById(dto.getReceiptId());
        if (receipt == null) {
            throw new RuntimeException("收货单不存在");
        }
        if (!Integer.valueOf(1).equals(receipt.getStatus())) {
            throw new RuntimeException("只有已确认的收货单才能退货");
        }

        // 2. 创建退货单主表
        PurchaseReturn purchaseReturn = new PurchaseReturn();
        purchaseReturn.setCode(generateReturnCode());
        purchaseReturn.setReceiptId(receipt.getId());
        purchaseReturn.setReceiptCode(receipt.getCode());
        purchaseReturn.setScId(dto.getScId() != null ? dto.getScId() : receipt.getScId());
        purchaseReturn.setSupplierId(dto.getSupplierId() != null ? dto.getSupplierId() : receipt.getSupplierId());
        purchaseReturn.setDescription(dto.getDescription());
        purchaseReturn.setStatus(0); // 待确认

        // 计算总金额和总数量
        BigDecimal totalNum = BigDecimal.ZERO;
        BigDecimal totalAmount = BigDecimal.ZERO;

        if (dto.getDetails() != null) {
            for (PurchaseReturnDTO.PurchaseReturnDetailDTO detail : dto.getDetails()) {
                totalNum = totalNum.add(detail.getReturnNum());
                BigDecimal amount = detail.getTaxPrice().multiply(detail.getReturnNum());
                totalAmount = totalAmount.add(amount);
            }
        }
        purchaseReturn.setTotalNum(totalNum);
        purchaseReturn.setTotalAmount(totalAmount);

        this.save(purchaseReturn);

        // 3. 创建退货明细
        if (dto.getDetails() != null) {
            for (PurchaseReturnDTO.PurchaseReturnDetailDTO detailDTO : dto.getDetails()) {
                PurchaseReturnDetail detail = new PurchaseReturnDetail();
                detail.setReturnId(purchaseReturn.getId());
                detail.setReceiptDetailId(detailDTO.getReceiptDetailId());
                detail.setProductId(detailDTO.getProductId());
                detail.setReceiveNum(detailDTO.getReceiveNum());
                detail.setReturnNum(detailDTO.getReturnNum());
                detail.setTaxPrice(detailDTO.getTaxPrice());
                detail.setTaxAmount(detailDTO.getTaxPrice().multiply(detailDTO.getReturnNum()));
                detail.setDescription(detailDTO.getDescription());
                returnDetailMapper.insert(detail);
            }
        }

        return purchaseReturn.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateReturn(PurchaseReturnDTO dto) {
        PurchaseReturn purchaseReturn = this.getById(dto.getId());
        if (purchaseReturn == null) {
            throw new RuntimeException("退货单不存在");
        }
        if (!Integer.valueOf(0).equals(purchaseReturn.getStatus())) {
            throw new RuntimeException("只有待确认的退货单才能修改");
        }

        // 更新主表信息
        purchaseReturn.setDescription(dto.getDescription());

        // 删除原有明细
        LambdaQueryWrapper<PurchaseReturnDetail> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(PurchaseReturnDetail::getReturnId, purchaseReturn.getId());
        returnDetailMapper.delete(deleteWrapper);

        // 重新计算并插入明细
        BigDecimal totalNum = BigDecimal.ZERO;
        BigDecimal totalAmount = BigDecimal.ZERO;

        if (dto.getDetails() != null) {
            for (PurchaseReturnDTO.PurchaseReturnDetailDTO detailDTO : dto.getDetails()) {
                totalNum = totalNum.add(detailDTO.getReturnNum());
                BigDecimal amount = detailDTO.getTaxPrice().multiply(detailDTO.getReturnNum());
                totalAmount = totalAmount.add(amount);

                PurchaseReturnDetail detail = new PurchaseReturnDetail();
                detail.setReturnId(purchaseReturn.getId());
                detail.setReceiptDetailId(detailDTO.getReceiptDetailId());
                detail.setProductId(detailDTO.getProductId());
                detail.setReceiveNum(detailDTO.getReceiveNum());
                detail.setReturnNum(detailDTO.getReturnNum());
                detail.setTaxPrice(detailDTO.getTaxPrice());
                detail.setTaxAmount(amount);
                detail.setDescription(detailDTO.getDescription());
                returnDetailMapper.insert(detail);
            }
        }

        purchaseReturn.setTotalNum(totalNum);
        purchaseReturn.setTotalAmount(totalAmount);
        this.updateById(purchaseReturn);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmReturn(Long id) {
        PurchaseReturn purchaseReturn = this.getById(id);
        if (purchaseReturn == null) {
            throw new RuntimeException("退货单不存在");
        }
        if (!Integer.valueOf(0).equals(purchaseReturn.getStatus())) {
            throw new RuntimeException("只有待确认的退货单才能确认");
        }

        // 1. 获取退货明细
        LambdaQueryWrapper<PurchaseReturnDetail> detailWrapper = new LambdaQueryWrapper<>();
        detailWrapper.eq(PurchaseReturnDetail::getReturnId, id);
        var details = returnDetailMapper.selectList(detailWrapper);

        // 2. 修复(2026-10-03)：退货出库前必须校验库存，否则可以把库存扣成负数。
        // 按商品把退货量汇总后再校验：同一商品可能落在多条明细上，逐条校验会漏掉累计超出。
        Map<Long, BigDecimal> requireByProduct = new LinkedHashMap<>();
        for (PurchaseReturnDetail detail : details) {
            requireByProduct.merge(detail.getProductId(), detail.getReturnNum(), BigDecimal::add);
        }
        for (Map.Entry<Long, BigDecimal> entry : requireByProduct.entrySet()) {
            productStockService.checkStockEnough(purchaseReturn.getScId(), entry.getKey(), entry.getValue());
        }

        // 3. 扣减库存（出库走 subStock，不能用 addStock 传负数：那会用售价覆盖成本价）
        for (PurchaseReturnDetail detail : details) {
            productStockService.subStock(
                    purchaseReturn.getScId(),
                    detail.getProductId(),
                    detail.getReturnNum(),
                    detail.getTaxPrice(),
                    purchaseReturn.getId(),
                    purchaseReturn.getCode(),
                    ProductStockBizType.PURCHASE_RETURN);
        }

        // 4. 更新退货单状态
        purchaseReturn.setStatus(1); // 已确认
        this.updateById(purchaseReturn);
    }

    @Override
    public String generateReturnCode() {
        return codeGenerateService.generate("PURCHASE_RETURN");
    }

    @Override
    public Page<?> getPendingReturnReceipts(Long current, Long size, Long supplierId) {
        // 查询已确认的收货单（可以退货）
        return purchaseReceiptService.getReceiptPage(current, size, null, supplierId, 1);
    }
}

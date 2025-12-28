package com.erp.business.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.business.dto.SaleReturnDTO;
import com.erp.business.entity.SaleDelivery;
import com.erp.business.entity.SaleReturn;
import com.erp.business.entity.SaleReturnDetail;
import com.erp.business.enums.ProductStockBizType;
import com.erp.business.mapper.SaleReturnDetailMapper;
import com.erp.business.mapper.SaleReturnMapper;
import com.erp.business.service.ISaleDeliveryService;
import com.erp.business.service.ISaleReturnService;
import com.erp.business.service.IProductStockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SaleReturnServiceImpl extends ServiceImpl<SaleReturnMapper, SaleReturn> implements ISaleReturnService {

    private final SaleReturnDetailMapper returnDetailMapper;
    private final ISaleDeliveryService saleDeliveryService;
    private final IProductStockService productStockService;

    @Override
    public Page<SaleReturn> getReturnPage(Long current, Long size, Long deliveryId, Long customerId, Integer status) {
        Page<SaleReturn> page = new Page<>(current != null ? current : 1, size != null ? size : 10);
        LambdaQueryWrapper<SaleReturn> wrapper = new LambdaQueryWrapper<>();
        if (deliveryId != null)
            wrapper.eq(SaleReturn::getDeliveryId, deliveryId);
        if (customerId != null)
            wrapper.eq(SaleReturn::getCustomerId, customerId);
        if (status != null)
            wrapper.eq(SaleReturn::getStatus, status);
        wrapper.orderByDesc(SaleReturn::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createReturn(SaleReturnDTO dto) {
        SaleDelivery delivery = saleDeliveryService.getById(dto.getDeliveryId());
        if (delivery == null)
            throw new RuntimeException("出库单不存在");
        if (delivery.getStatus() != 1)
            throw new RuntimeException("只有已确认的出库单才能退货");

        SaleReturn saleReturn = new SaleReturn();
        saleReturn.setCode(generateReturnCode());
        saleReturn.setDeliveryId(delivery.getId());
        saleReturn.setDeliveryCode(delivery.getCode());
        saleReturn.setScId(dto.getScId() != null ? dto.getScId() : delivery.getScId());
        saleReturn.setCustomerId(delivery.getCustomerId());
        saleReturn.setDescription(dto.getDescription());
        saleReturn.setStatus(0);

        BigDecimal totalNum = BigDecimal.ZERO;
        BigDecimal totalAmount = BigDecimal.ZERO;

        if (dto.getDetails() != null) {
            for (SaleReturnDTO.SaleReturnDetailDTO d : dto.getDetails()) {
                totalNum = totalNum.add(d.getReturnNum());
                totalAmount = totalAmount.add(d.getTaxPrice().multiply(d.getReturnNum()));
            }
        }
        saleReturn.setTotalNum(totalNum);
        saleReturn.setTotalAmount(totalAmount);
        this.save(saleReturn);

        if (dto.getDetails() != null) {
            for (SaleReturnDTO.SaleReturnDetailDTO detailDTO : dto.getDetails()) {
                SaleReturnDetail detail = new SaleReturnDetail();
                detail.setReturnId(saleReturn.getId());
                detail.setDeliveryDetailId(detailDTO.getDeliveryDetailId());
                detail.setProductId(detailDTO.getProductId());
                detail.setDeliveryNum(detailDTO.getDeliveryNum());
                detail.setReturnNum(detailDTO.getReturnNum());
                detail.setTaxPrice(detailDTO.getTaxPrice());
                detail.setTaxAmount(detailDTO.getTaxPrice().multiply(detailDTO.getReturnNum()));
                detail.setDescription(detailDTO.getDescription());
                returnDetailMapper.insert(detail);
            }
        }
        return saleReturn.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateReturn(SaleReturnDTO dto) {
        SaleReturn saleReturn = this.getById(dto.getId());
        if (saleReturn == null)
            throw new RuntimeException("退货单不存在");
        if (saleReturn.getStatus() != 0)
            throw new RuntimeException("只有待确认的退货单才能修改");

        saleReturn.setDescription(dto.getDescription());

        LambdaQueryWrapper<SaleReturnDetail> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(SaleReturnDetail::getReturnId, saleReturn.getId());
        returnDetailMapper.delete(deleteWrapper);

        BigDecimal totalNum = BigDecimal.ZERO;
        BigDecimal totalAmount = BigDecimal.ZERO;

        if (dto.getDetails() != null) {
            for (SaleReturnDTO.SaleReturnDetailDTO detailDTO : dto.getDetails()) {
                totalNum = totalNum.add(detailDTO.getReturnNum());
                BigDecimal amount = detailDTO.getTaxPrice().multiply(detailDTO.getReturnNum());
                totalAmount = totalAmount.add(amount);

                SaleReturnDetail detail = new SaleReturnDetail();
                detail.setReturnId(saleReturn.getId());
                detail.setDeliveryDetailId(detailDTO.getDeliveryDetailId());
                detail.setProductId(detailDTO.getProductId());
                detail.setDeliveryNum(detailDTO.getDeliveryNum());
                detail.setReturnNum(detailDTO.getReturnNum());
                detail.setTaxPrice(detailDTO.getTaxPrice());
                detail.setTaxAmount(amount);
                detail.setDescription(detailDTO.getDescription());
                returnDetailMapper.insert(detail);
            }
        }
        saleReturn.setTotalNum(totalNum);
        saleReturn.setTotalAmount(totalAmount);
        this.updateById(saleReturn);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmReturn(Long id) {
        SaleReturn saleReturn = this.getById(id);
        if (saleReturn == null)
            throw new RuntimeException("退货单不存在");
        if (saleReturn.getStatus() != 0)
            throw new RuntimeException("退货单已确认");

        saleReturn.setStatus(1);
        this.updateById(saleReturn);

        LambdaQueryWrapper<SaleReturnDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SaleReturnDetail::getReturnId, id);
        List<SaleReturnDetail> details = returnDetailMapper.selectList(wrapper);

        // 退货入库（增加库存）
        for (SaleReturnDetail detail : details) {
            productStockService.addStock(
                    saleReturn.getScId(),
                    detail.getProductId(),
                    detail.getReturnNum(),
                    detail.getTaxPrice(),
                    saleReturn.getId(),
                    saleReturn.getCode(),
                    ProductStockBizType.SALES_RETURN);
        }
    }

    @Override
    public Page<SaleDelivery> getPendingReturnDeliveries(Long current, Long size, Long customerId) {
        Page<SaleDelivery> page = new Page<>(current != null ? current : 1, size != null ? size : 10);
        LambdaQueryWrapper<SaleDelivery> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SaleDelivery::getStatus, 1); // 只能退已确认的出库单
        if (customerId != null)
            wrapper.eq(SaleDelivery::getCustomerId, customerId);
        wrapper.orderByDesc(SaleDelivery::getCreateTime);
        return saleDeliveryService.page(page, wrapper);
    }

    private String generateReturnCode() {
        String dateStr = DateUtil.format(new Date(), "yyyyMMdd");
        String prefix = "SR" + dateStr;

        // 使用自定义SQL查询最大编号（绕过软删除过滤）
        String maxCode = baseMapper.selectMaxCodeByPrefix(prefix);

        int seq = 1;
        if (maxCode != null) {
            String seqStr = maxCode.substring(prefix.length());
            seq = Integer.parseInt(seqStr) + 1;
        }
        return prefix + String.format("%04d", seq);
    }
}

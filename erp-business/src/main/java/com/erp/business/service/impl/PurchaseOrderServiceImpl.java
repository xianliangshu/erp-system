package com.erp.business.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.business.dto.PurchaseOrderDTO;
import com.erp.business.entity.PurchaseOrder;
import com.erp.business.entity.PurchaseOrderDetail;
import com.erp.business.enums.PurchaseOrderStatus;
import com.erp.business.mapper.PurchaseOrderDetailMapper;
import com.erp.business.mapper.PurchaseOrderMapper;
import com.erp.business.service.IPurchaseOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 采购订单服务实现
 */
@Service
@RequiredArgsConstructor
public class PurchaseOrderServiceImpl extends ServiceImpl<PurchaseOrderMapper, PurchaseOrder>
        implements IPurchaseOrderService {

    private final PurchaseOrderDetailMapper detailMapper;

    @Override
    public Page<PurchaseOrder> getOrderPage(Long current, Long size, Long supplierId, Integer status) {
        Page<PurchaseOrder> page = new Page<>(current != null ? current : 1, size != null ? size : 10);
        LambdaQueryWrapper<PurchaseOrder> wrapper = new LambdaQueryWrapper<>();

        if (supplierId != null) {
            wrapper.eq(PurchaseOrder::getSupplierId, supplierId);
        }
        if (status != null) {
            wrapper.eq(PurchaseOrder::getStatus, status);
        }
        wrapper.orderByDesc(PurchaseOrder::getCreateTime);

        return this.page(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOrder(PurchaseOrderDTO dto) {
        // 1. 创建订单主表
        PurchaseOrder order = new PurchaseOrder();
        order.setCode(generateOrderCode());
        order.setScId(dto.getScId());
        order.setSupplierId(dto.getSupplierId());
        order.setPurchaserId(dto.getPurchaserId());
        order.setExpectArriveDate(dto.getExpectArriveDate());
        order.setDescription(dto.getDescription());
        order.setStatus(PurchaseOrderStatus.PENDING);

        // 计算总金额和总数量
        BigDecimal totalNum = BigDecimal.ZERO;
        BigDecimal totalAmount = BigDecimal.ZERO;

        if (dto.getDetails() != null) {
            for (PurchaseOrderDTO.PurchaseOrderDetailDTO detail : dto.getDetails()) {
                totalNum = totalNum.add(detail.getOrderNum());
                BigDecimal amount = detail.getTaxPrice().multiply(detail.getOrderNum());
                totalAmount = totalAmount.add(amount);
            }
        }
        order.setTotalNum(totalNum);
        order.setTotalAmount(totalAmount);

        this.save(order);

        // 2. 创建订单明细
        if (dto.getDetails() != null) {
            int sort = 0;
            for (PurchaseOrderDTO.PurchaseOrderDetailDTO detailDTO : dto.getDetails()) {
                PurchaseOrderDetail detail = new PurchaseOrderDetail();
                detail.setOrderId(order.getId());
                detail.setProductId(detailDTO.getProductId());
                detail.setOrderNum(detailDTO.getOrderNum());
                detail.setTaxPrice(detailDTO.getTaxPrice());
                detail.setTaxAmount(detailDTO.getTaxPrice().multiply(detailDTO.getOrderNum()));
                detail.setReceivedNum(BigDecimal.ZERO);
                detail.setDescription(detailDTO.getDescription());
                detail.setSort(sort++);
                detailMapper.insert(detail);
            }
        }

        return order.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOrder(PurchaseOrderDTO dto) {
        PurchaseOrder order = this.getById(dto.getId());
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getStatus() != PurchaseOrderStatus.PENDING) {
            throw new RuntimeException("只有待审核的订单才能修改");
        }

        // 更新订单主表
        order.setScId(dto.getScId());
        order.setSupplierId(dto.getSupplierId());
        order.setPurchaserId(dto.getPurchaserId());
        order.setExpectArriveDate(dto.getExpectArriveDate());
        order.setDescription(dto.getDescription());

        // 删除原有明细
        LambdaQueryWrapper<PurchaseOrderDetail> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(PurchaseOrderDetail::getOrderId, order.getId());
        detailMapper.delete(deleteWrapper);

        // 重新计算并插入明细
        BigDecimal totalNum = BigDecimal.ZERO;
        BigDecimal totalAmount = BigDecimal.ZERO;

        if (dto.getDetails() != null) {
            int sort = 0;
            for (PurchaseOrderDTO.PurchaseOrderDetailDTO detailDTO : dto.getDetails()) {
                totalNum = totalNum.add(detailDTO.getOrderNum());
                BigDecimal amount = detailDTO.getTaxPrice().multiply(detailDTO.getOrderNum());
                totalAmount = totalAmount.add(amount);

                PurchaseOrderDetail detail = new PurchaseOrderDetail();
                detail.setOrderId(order.getId());
                detail.setProductId(detailDTO.getProductId());
                detail.setOrderNum(detailDTO.getOrderNum());
                detail.setTaxPrice(detailDTO.getTaxPrice());
                detail.setTaxAmount(amount);
                detail.setReceivedNum(BigDecimal.ZERO);
                detail.setDescription(detailDTO.getDescription());
                detail.setSort(sort++);
                detailMapper.insert(detail);
            }
        }

        order.setTotalNum(totalNum);
        order.setTotalAmount(totalAmount);
        this.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(Long id) {
        PurchaseOrder order = this.getById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getStatus() != PurchaseOrderStatus.PENDING) {
            throw new RuntimeException("只有待审核的订单才能审核");
        }

        order.setStatus(PurchaseOrderStatus.APPROVED);
        order.setApproveTime(LocalDateTime.now());
        // TODO: 从当前登录用户获取审核人
        order.setApproveBy("admin");
        this.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reject(Long id, String reason) {
        PurchaseOrder order = this.getById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getStatus() != PurchaseOrderStatus.PENDING) {
            throw new RuntimeException("只有待审核的订单才能拒绝");
        }

        order.setStatus(PurchaseOrderStatus.REJECTED);
        order.setApproveTime(LocalDateTime.now());
        order.setApproveBy("admin");
        order.setRefuseReason(reason);
        this.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long id) {
        PurchaseOrder order = this.getById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getStatus() == PurchaseOrderStatus.COMPLETED) {
            throw new RuntimeException("已完成的订单无法取消");
        }

        order.setStatus(PurchaseOrderStatus.CANCELLED);
        this.updateById(order);
    }

    @Override
    public String generateOrderCode() {
        // 格式: PO + 日期 + 4位序号, 例如: PO202312210001
        String dateStr = DateUtil.format(new Date(), "yyyyMMdd");
        String prefix = "PO" + dateStr;

        // 查询今天最大的订单号
        LambdaQueryWrapper<PurchaseOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.likeRight(PurchaseOrder::getCode, prefix)
                .orderByDesc(PurchaseOrder::getCode)
                .last("LIMIT 1");
        PurchaseOrder lastOrder = this.getOne(wrapper);

        int seq = 1;
        if (lastOrder != null) {
            String lastCode = lastOrder.getCode();
            String seqStr = lastCode.substring(prefix.length());
            seq = Integer.parseInt(seqStr) + 1;
        }

        return prefix + String.format("%04d", seq);
    }
}

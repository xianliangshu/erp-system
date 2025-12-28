package com.erp.business.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.business.dto.SaleOrderDTO;
import com.erp.business.entity.SaleOrder;
import com.erp.business.entity.SaleOrderDetail;
import com.erp.business.enums.SaleOrderStatus;
import com.erp.business.mapper.SaleOrderDetailMapper;
import com.erp.business.mapper.SaleOrderMapper;
import com.erp.business.service.ISaleOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * 销售订单服务实现
 */
@Service
@RequiredArgsConstructor
public class SaleOrderServiceImpl extends ServiceImpl<SaleOrderMapper, SaleOrder>
        implements ISaleOrderService {

    private final SaleOrderDetailMapper orderDetailMapper;

    @Override
    public Page<SaleOrder> getOrderPage(Long current, Long size, Long customerId, Integer status) {
        Page<SaleOrder> page = new Page<>(current != null ? current : 1, size != null ? size : 10);
        LambdaQueryWrapper<SaleOrder> wrapper = new LambdaQueryWrapper<>();

        if (customerId != null) {
            wrapper.eq(SaleOrder::getCustomerId, customerId);
        }
        if (status != null) {
            wrapper.eq(SaleOrder::getStatus, status);
        }
        wrapper.orderByDesc(SaleOrder::getCreateTime);

        return this.page(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOrder(SaleOrderDTO dto) {
        // 创建订单主表
        SaleOrder order = new SaleOrder();
        order.setCode(generateOrderCode());
        order.setScId(dto.getScId());
        order.setCustomerId(dto.getCustomerId());
        order.setSalerId(dto.getSalerId());
        if (dto.getExpectDeliveryDate() != null) {
            order.setExpectDeliveryDate(LocalDate.parse(dto.getExpectDeliveryDate(), DateTimeFormatter.ISO_DATE));
        }
        order.setDescription(dto.getDescription());
        order.setStatus(SaleOrderStatus.PENDING);

        // 计算总金额和总数量
        BigDecimal totalNum = BigDecimal.ZERO;
        BigDecimal totalAmount = BigDecimal.ZERO;

        if (dto.getDetails() != null) {
            for (SaleOrderDTO.SaleOrderDetailDTO detail : dto.getDetails()) {
                totalNum = totalNum.add(detail.getOrderNum());
                BigDecimal amount = detail.getTaxPrice().multiply(detail.getOrderNum());
                totalAmount = totalAmount.add(amount);
            }
        }
        order.setTotalNum(totalNum);
        order.setTotalAmount(totalAmount);

        this.save(order);

        // 创建订单明细
        if (dto.getDetails() != null) {
            for (SaleOrderDTO.SaleOrderDetailDTO detailDTO : dto.getDetails()) {
                SaleOrderDetail detail = new SaleOrderDetail();
                detail.setOrderId(order.getId());
                detail.setProductId(detailDTO.getProductId());
                detail.setOrderNum(detailDTO.getOrderNum());
                detail.setTaxPrice(detailDTO.getTaxPrice());
                detail.setTaxAmount(detailDTO.getTaxPrice().multiply(detailDTO.getOrderNum()));
                detail.setDeliveredNum(BigDecimal.ZERO);
                detail.setDescription(detailDTO.getDescription());
                orderDetailMapper.insert(detail);
            }
        }

        return order.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOrder(SaleOrderDTO dto) {
        SaleOrder order = this.getById(dto.getId());
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getStatus() != SaleOrderStatus.PENDING) {
            throw new RuntimeException("只有待审核的订单才能修改");
        }

        order.setScId(dto.getScId());
        order.setCustomerId(dto.getCustomerId());
        order.setSalerId(dto.getSalerId());
        if (dto.getExpectDeliveryDate() != null) {
            order.setExpectDeliveryDate(LocalDate.parse(dto.getExpectDeliveryDate(), DateTimeFormatter.ISO_DATE));
        }
        order.setDescription(dto.getDescription());

        // 删除原有明细
        LambdaQueryWrapper<SaleOrderDetail> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(SaleOrderDetail::getOrderId, order.getId());
        orderDetailMapper.delete(deleteWrapper);

        // 重新计算并插入明细
        BigDecimal totalNum = BigDecimal.ZERO;
        BigDecimal totalAmount = BigDecimal.ZERO;

        if (dto.getDetails() != null) {
            for (SaleOrderDTO.SaleOrderDetailDTO detailDTO : dto.getDetails()) {
                totalNum = totalNum.add(detailDTO.getOrderNum());
                BigDecimal amount = detailDTO.getTaxPrice().multiply(detailDTO.getOrderNum());
                totalAmount = totalAmount.add(amount);

                SaleOrderDetail detail = new SaleOrderDetail();
                detail.setOrderId(order.getId());
                detail.setProductId(detailDTO.getProductId());
                detail.setOrderNum(detailDTO.getOrderNum());
                detail.setTaxPrice(detailDTO.getTaxPrice());
                detail.setTaxAmount(amount);
                detail.setDeliveredNum(BigDecimal.ZERO);
                detail.setDescription(detailDTO.getDescription());
                orderDetailMapper.insert(detail);
            }
        }

        order.setTotalNum(totalNum);
        order.setTotalAmount(totalAmount);
        this.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveOrder(Long id) {
        SaleOrder order = this.getById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getStatus() != SaleOrderStatus.PENDING) {
            throw new RuntimeException("只有待审核的订单才能审核");
        }
        order.setStatus(SaleOrderStatus.APPROVED);
        order.setApproveBy("admin"); // TODO: 从上下文获取当前用户
        order.setApproveTime(LocalDateTime.now());
        this.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectOrder(Long id, String reason) {
        SaleOrder order = this.getById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getStatus() != SaleOrderStatus.PENDING) {
            throw new RuntimeException("只有待审核的订单才能拒绝");
        }
        order.setStatus(SaleOrderStatus.REJECTED);
        order.setRefuseReason(reason);
        order.setApproveBy("admin");
        order.setApproveTime(LocalDateTime.now());
        this.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long id) {
        SaleOrder order = this.getById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getStatus() == SaleOrderStatus.COMPLETED) {
            throw new RuntimeException("已完成的订单不能取消");
        }
        order.setStatus(SaleOrderStatus.CANCELLED);
        this.updateById(order);
    }

    @Override
    public String generateOrderCode() {
        // 格式: SO + 日期 + 4位序号, 例如: SO202312210001
        String dateStr = DateUtil.format(new Date(), "yyyyMMdd");
        String prefix = "SO" + dateStr;

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

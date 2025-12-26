package com.erp.business.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.business.dto.SaleDeliveryDTO;
import com.erp.business.entity.SaleDelivery;
import com.erp.business.entity.SaleDeliveryDetail;
import com.erp.business.entity.SaleOrder;
import com.erp.business.entity.SaleOrderDetail;
import com.erp.business.enums.ProductStockBizType;
import com.erp.business.enums.SaleOrderStatus;
import com.erp.business.mapper.SaleDeliveryDetailMapper;
import com.erp.business.mapper.SaleDeliveryMapper;
import com.erp.business.mapper.SaleOrderDetailMapper;
import com.erp.business.service.ISaleDeliveryService;
import com.erp.business.service.ISaleOrderService;
import com.erp.business.service.IProductStockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 销售出库服务实现
 */
@Service
@RequiredArgsConstructor
public class SaleDeliveryServiceImpl extends ServiceImpl<SaleDeliveryMapper, SaleDelivery>
        implements ISaleDeliveryService {

    private final SaleDeliveryDetailMapper deliveryDetailMapper;
    private final ISaleOrderService saleOrderService;
    private final SaleOrderDetailMapper orderDetailMapper;
    private final IProductStockService productStockService;

    @Override
    public Page<SaleDelivery> getDeliveryPage(Long current, Long size, Long orderId, Long customerId, Integer status) {
        Page<SaleDelivery> page = new Page<>(current != null ? current : 1, size != null ? size : 10);
        LambdaQueryWrapper<SaleDelivery> wrapper = new LambdaQueryWrapper<>();

        if (orderId != null)
            wrapper.eq(SaleDelivery::getOrderId, orderId);
        if (customerId != null)
            wrapper.eq(SaleDelivery::getCustomerId, customerId);
        if (status != null)
            wrapper.eq(SaleDelivery::getStatus, status);
        wrapper.orderByDesc(SaleDelivery::getCreateTime);

        return this.page(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createDelivery(SaleDeliveryDTO dto) {
        SaleOrder order = saleOrderService.getById(dto.getOrderId());
        if (order == null)
            throw new RuntimeException("销售订单不存在");
        if (order.getStatus() != SaleOrderStatus.APPROVED)
            throw new RuntimeException("只有已审核的订单才能出库");

        SaleDelivery delivery = new SaleDelivery();
        delivery.setCode(generateDeliveryCode());
        delivery.setOrderId(order.getId());
        delivery.setOrderCode(order.getCode());
        delivery.setScId(dto.getScId() != null ? dto.getScId() : order.getScId());
        delivery.setCustomerId(order.getCustomerId());
        delivery.setDescription(dto.getDescription());
        delivery.setStatus(0);

        BigDecimal totalNum = BigDecimal.ZERO;
        BigDecimal totalAmount = BigDecimal.ZERO;

        if (dto.getDetails() != null) {
            for (SaleDeliveryDTO.SaleDeliveryDetailDTO d : dto.getDetails()) {
                totalNum = totalNum.add(d.getDeliveryNum());
                totalAmount = totalAmount.add(d.getTaxPrice().multiply(d.getDeliveryNum()));
            }
        }
        delivery.setTotalNum(totalNum);
        delivery.setTotalAmount(totalAmount);

        this.save(delivery);

        if (dto.getDetails() != null) {
            for (SaleDeliveryDTO.SaleDeliveryDetailDTO detailDTO : dto.getDetails()) {
                SaleDeliveryDetail detail = new SaleDeliveryDetail();
                detail.setDeliveryId(delivery.getId());
                detail.setOrderDetailId(detailDTO.getOrderDetailId());
                detail.setProductId(detailDTO.getProductId());
                detail.setOrderNum(detailDTO.getOrderNum());
                detail.setDeliveryNum(detailDTO.getDeliveryNum());
                detail.setTaxPrice(detailDTO.getTaxPrice());
                detail.setTaxAmount(detailDTO.getTaxPrice().multiply(detailDTO.getDeliveryNum()));
                detail.setDescription(detailDTO.getDescription());
                deliveryDetailMapper.insert(detail);
            }
        }

        return delivery.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDelivery(SaleDeliveryDTO dto) {
        SaleDelivery delivery = this.getById(dto.getId());
        if (delivery == null)
            throw new RuntimeException("出库单不存在");
        if (delivery.getStatus() != 0)
            throw new RuntimeException("只有待确认的出库单才能修改");

        delivery.setDescription(dto.getDescription());

        LambdaQueryWrapper<SaleDeliveryDetail> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(SaleDeliveryDetail::getDeliveryId, delivery.getId());
        deliveryDetailMapper.delete(deleteWrapper);

        BigDecimal totalNum = BigDecimal.ZERO;
        BigDecimal totalAmount = BigDecimal.ZERO;

        if (dto.getDetails() != null) {
            for (SaleDeliveryDTO.SaleDeliveryDetailDTO detailDTO : dto.getDetails()) {
                totalNum = totalNum.add(detailDTO.getDeliveryNum());
                BigDecimal amount = detailDTO.getTaxPrice().multiply(detailDTO.getDeliveryNum());
                totalAmount = totalAmount.add(amount);

                SaleDeliveryDetail detail = new SaleDeliveryDetail();
                detail.setDeliveryId(delivery.getId());
                detail.setOrderDetailId(detailDTO.getOrderDetailId());
                detail.setProductId(detailDTO.getProductId());
                detail.setOrderNum(detailDTO.getOrderNum());
                detail.setDeliveryNum(detailDTO.getDeliveryNum());
                detail.setTaxPrice(detailDTO.getTaxPrice());
                detail.setTaxAmount(amount);
                detail.setDescription(detailDTO.getDescription());
                deliveryDetailMapper.insert(detail);
            }
        }

        delivery.setTotalNum(totalNum);
        delivery.setTotalAmount(totalAmount);
        this.updateById(delivery);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmDelivery(Long id) {
        SaleDelivery delivery = this.getById(id);
        if (delivery == null)
            throw new RuntimeException("出库单不存在");
        if (delivery.getStatus() != 0)
            throw new RuntimeException("出库单已确认");

        delivery.setStatus(1);
        this.updateById(delivery);

        LambdaQueryWrapper<SaleDeliveryDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SaleDeliveryDetail::getDeliveryId, id);
        List<SaleDeliveryDetail> details = deliveryDetailMapper.selectList(wrapper);

        // 扣减库存
        for (SaleDeliveryDetail detail : details) {
            productStockService.subStock(
                    delivery.getScId(),
                    detail.getProductId(),
                    detail.getDeliveryNum(),
                    detail.getTaxPrice(),
                    delivery.getId(),
                    delivery.getCode(),
                    ProductStockBizType.SALES_DELIVERY);

            // 更新订单明细已发货数量
            if (detail.getOrderDetailId() != null) {
                SaleOrderDetail orderDetail = orderDetailMapper.selectById(detail.getOrderDetailId());
                if (orderDetail != null) {
                    orderDetail.setDeliveredNum(orderDetail.getDeliveredNum().add(detail.getDeliveryNum()));
                    orderDetailMapper.updateById(orderDetail);
                }
            }
        }

        // 检查订单是否全部发货完成
        checkAndUpdateOrderStatus(delivery.getOrderId());
    }

    private void checkAndUpdateOrderStatus(Long orderId) {
        LambdaQueryWrapper<SaleOrderDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SaleOrderDetail::getOrderId, orderId);
        List<SaleOrderDetail> details = orderDetailMapper.selectList(wrapper);

        boolean allDelivered = details.stream()
                .allMatch(d -> d.getDeliveredNum() != null && d.getDeliveredNum().compareTo(d.getOrderNum()) >= 0);

        if (allDelivered) {
            SaleOrder order = saleOrderService.getById(orderId);
            if (order != null && order.getStatus() == SaleOrderStatus.APPROVED) {
                order.setStatus(SaleOrderStatus.COMPLETED);
                saleOrderService.updateById(order);
            }
        }
    }

    @Override
    public Page<SaleOrder> getPendingDeliveryOrders(Long current, Long size, Long customerId) {
        Page<SaleOrder> page = new Page<>(current != null ? current : 1, size != null ? size : 10);
        LambdaQueryWrapper<SaleOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SaleOrder::getStatus, SaleOrderStatus.APPROVED);
        if (customerId != null)
            wrapper.eq(SaleOrder::getCustomerId, customerId);
        wrapper.orderByDesc(SaleOrder::getCreateTime);
        return saleOrderService.page(page, wrapper);
    }

    private String generateDeliveryCode() {
        String dateStr = DateUtil.format(new Date(), "yyyyMMdd");
        String prefix = "SD" + dateStr;
        LambdaQueryWrapper<SaleDelivery> wrapper = new LambdaQueryWrapper<>();
        wrapper.likeRight(SaleDelivery::getCode, prefix).orderByDesc(SaleDelivery::getCode).last("LIMIT 1");
        SaleDelivery last = this.getOne(wrapper);
        int seq = 1;
        if (last != null) {
            String seqStr = last.getCode().substring(prefix.length());
            seq = Integer.parseInt(seqStr) + 1;
        }
        return prefix + String.format("%04d", seq);
    }
}

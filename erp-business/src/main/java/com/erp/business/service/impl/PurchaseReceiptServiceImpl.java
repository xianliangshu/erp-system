package com.erp.business.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.business.dto.PurchaseReceiptDTO;
import com.erp.business.entity.PurchaseOrder;
import com.erp.business.entity.PurchaseOrderDetail;
import com.erp.business.entity.PurchaseReceipt;
import com.erp.business.entity.PurchaseReceiptDetail;
import com.erp.business.enums.ProductStockBizType;
import com.erp.business.enums.PurchaseOrderStatus;
import com.erp.business.mapper.PurchaseOrderDetailMapper;
import com.erp.business.mapper.PurchaseReceiptDetailMapper;
import com.erp.business.mapper.PurchaseReceiptMapper;
import com.erp.business.service.IPurchaseOrderService;
import com.erp.business.service.IPurchaseReceiptService;
import com.erp.business.service.IProductStockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 采购收货服务实现
 */
@Service
@RequiredArgsConstructor
public class PurchaseReceiptServiceImpl extends ServiceImpl<PurchaseReceiptMapper, PurchaseReceipt>
        implements IPurchaseReceiptService {

    private final PurchaseReceiptDetailMapper receiptDetailMapper;
    private final PurchaseOrderDetailMapper orderDetailMapper;
    private final IPurchaseOrderService purchaseOrderService;
    private final IProductStockService productStockService;

    @Override
    public Page<PurchaseReceipt> getReceiptPage(Long current, Long size, Long orderId, Long supplierId,
            Integer status) {
        Page<PurchaseReceipt> page = new Page<>(current != null ? current : 1, size != null ? size : 10);
        LambdaQueryWrapper<PurchaseReceipt> wrapper = new LambdaQueryWrapper<>();

        if (orderId != null) {
            wrapper.eq(PurchaseReceipt::getOrderId, orderId);
        }
        if (supplierId != null) {
            wrapper.eq(PurchaseReceipt::getSupplierId, supplierId);
        }
        if (status != null) {
            wrapper.eq(PurchaseReceipt::getStatus, status);
        }
        wrapper.orderByDesc(PurchaseReceipt::getCreateTime);

        return this.page(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createReceipt(PurchaseReceiptDTO dto) {
        // 1. 检查采购订单是否存在且已审核通过
        PurchaseOrder order = purchaseOrderService.getById(dto.getOrderId());
        if (order == null) {
            throw new RuntimeException("采购订单不存在");
        }
        if (order.getStatus() != PurchaseOrderStatus.APPROVED) {
            throw new RuntimeException("只有已审核的订单才能收货");
        }

        // 2. 创建收货单主表
        PurchaseReceipt receipt = new PurchaseReceipt();
        receipt.setCode(generateReceiptCode());
        receipt.setOrderId(order.getId());
        receipt.setOrderCode(order.getCode());
        receipt.setScId(dto.getScId() != null ? dto.getScId() : order.getScId());
        receipt.setSupplierId(dto.getSupplierId() != null ? dto.getSupplierId() : order.getSupplierId());
        receipt.setDescription(dto.getDescription());
        receipt.setStatus(0); // 待确认

        // 计算总金额和总数量
        BigDecimal totalNum = BigDecimal.ZERO;
        BigDecimal totalAmount = BigDecimal.ZERO;

        if (dto.getDetails() != null) {
            for (PurchaseReceiptDTO.PurchaseReceiptDetailDTO detail : dto.getDetails()) {
                totalNum = totalNum.add(detail.getReceiveNum());
                BigDecimal amount = detail.getTaxPrice().multiply(detail.getReceiveNum());
                totalAmount = totalAmount.add(amount);
            }
        }
        receipt.setTotalNum(totalNum);
        receipt.setTotalAmount(totalAmount);

        this.save(receipt);

        // 3. 创建收货明细
        if (dto.getDetails() != null) {
            for (PurchaseReceiptDTO.PurchaseReceiptDetailDTO detailDTO : dto.getDetails()) {
                PurchaseReceiptDetail detail = new PurchaseReceiptDetail();
                detail.setReceiptId(receipt.getId());
                detail.setOrderDetailId(detailDTO.getOrderDetailId());
                detail.setProductId(detailDTO.getProductId());
                detail.setOrderNum(detailDTO.getOrderNum());
                detail.setReceiveNum(detailDTO.getReceiveNum());
                detail.setTaxPrice(detailDTO.getTaxPrice());
                detail.setTaxAmount(detailDTO.getTaxPrice().multiply(detailDTO.getReceiveNum()));
                detail.setDescription(detailDTO.getDescription());
                receiptDetailMapper.insert(detail);
            }
        }

        return receipt.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateReceipt(PurchaseReceiptDTO dto) {
        PurchaseReceipt receipt = this.getById(dto.getId());
        if (receipt == null) {
            throw new RuntimeException("收货单不存在");
        }
        if (receipt.getStatus() != 0) {
            throw new RuntimeException("只有待确认的收货单才能修改");
        }

        // 更新主表信息
        receipt.setDescription(dto.getDescription());

        // 删除原有明细
        LambdaQueryWrapper<PurchaseReceiptDetail> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(PurchaseReceiptDetail::getReceiptId, receipt.getId());
        receiptDetailMapper.delete(deleteWrapper);

        // 重新计算并插入明细
        BigDecimal totalNum = BigDecimal.ZERO;
        BigDecimal totalAmount = BigDecimal.ZERO;

        if (dto.getDetails() != null) {
            for (PurchaseReceiptDTO.PurchaseReceiptDetailDTO detailDTO : dto.getDetails()) {
                totalNum = totalNum.add(detailDTO.getReceiveNum());
                BigDecimal amount = detailDTO.getTaxPrice().multiply(detailDTO.getReceiveNum());
                totalAmount = totalAmount.add(amount);

                PurchaseReceiptDetail detail = new PurchaseReceiptDetail();
                detail.setReceiptId(receipt.getId());
                detail.setOrderDetailId(detailDTO.getOrderDetailId());
                detail.setProductId(detailDTO.getProductId());
                detail.setOrderNum(detailDTO.getOrderNum());
                detail.setReceiveNum(detailDTO.getReceiveNum());
                detail.setTaxPrice(detailDTO.getTaxPrice());
                detail.setTaxAmount(amount);
                detail.setDescription(detailDTO.getDescription());
                receiptDetailMapper.insert(detail);
            }
        }

        receipt.setTotalNum(totalNum);
        receipt.setTotalAmount(totalAmount);
        this.updateById(receipt);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmReceipt(Long id) {
        PurchaseReceipt receipt = this.getById(id);
        if (receipt == null) {
            throw new RuntimeException("收货单不存在");
        }
        if (receipt.getStatus() != 0) {
            throw new RuntimeException("只有待确认的收货单才能确认");
        }

        // 1. 获取收货明细
        LambdaQueryWrapper<PurchaseReceiptDetail> detailWrapper = new LambdaQueryWrapper<>();
        detailWrapper.eq(PurchaseReceiptDetail::getReceiptId, id);
        var details = receiptDetailMapper.selectList(detailWrapper);

        // 2. 更新库存和订单明细的已收货数量
        for (PurchaseReceiptDetail detail : details) {
            // 增加库存
            productStockService.addStock(
                    receipt.getScId(),
                    detail.getProductId(),
                    detail.getReceiveNum(),
                    detail.getTaxPrice(),
                    receipt.getId(),
                    receipt.getCode(),
                    ProductStockBizType.PURCHASE_RECEIPT);

            // 更新订单明细的已收货数量
            PurchaseOrderDetail orderDetail = orderDetailMapper.selectById(detail.getOrderDetailId());
            if (orderDetail != null) {
                BigDecimal receivedNum = orderDetail.getReceivedNum() != null ? orderDetail.getReceivedNum()
                        : BigDecimal.ZERO;
                orderDetail.setReceivedNum(receivedNum.add(detail.getReceiveNum()));
                orderDetailMapper.updateById(orderDetail);
            }
        }

        // 3. 检查订单是否全部收货完成，更新订单状态
        checkAndUpdateOrderStatus(receipt.getOrderId());

        // 4. 更新收货单状态
        receipt.setStatus(1); // 已确认
        this.updateById(receipt);
    }

    /**
     * 检查并更新订单状态
     */
    private void checkAndUpdateOrderStatus(Long orderId) {
        LambdaQueryWrapper<PurchaseOrderDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PurchaseOrderDetail::getOrderId, orderId);
        var orderDetails = orderDetailMapper.selectList(wrapper);

        boolean allReceived = true;
        for (PurchaseOrderDetail detail : orderDetails) {
            BigDecimal orderNum = detail.getOrderNum() != null ? detail.getOrderNum() : BigDecimal.ZERO;
            BigDecimal receivedNum = detail.getReceivedNum() != null ? detail.getReceivedNum() : BigDecimal.ZERO;
            if (receivedNum.compareTo(orderNum) < 0) {
                allReceived = false;
                break;
            }
        }

        if (allReceived) {
            PurchaseOrder order = purchaseOrderService.getById(orderId);
            order.setStatus(PurchaseOrderStatus.COMPLETED);
            purchaseOrderService.updateById(order);
        }
    }

    @Override
    public String generateReceiptCode() {
        // 格式: PR + 日期 + 4位序号, 例如: PR202312210001
        String dateStr = DateUtil.format(new Date(), "yyyyMMdd");
        String prefix = "PR" + dateStr;

        // 查询今天最大的收货单号
        LambdaQueryWrapper<PurchaseReceipt> wrapper = new LambdaQueryWrapper<>();
        wrapper.likeRight(PurchaseReceipt::getCode, prefix)
                .orderByDesc(PurchaseReceipt::getCode)
                .last("LIMIT 1");
        PurchaseReceipt lastReceipt = this.getOne(wrapper);

        int seq = 1;
        if (lastReceipt != null) {
            String lastCode = lastReceipt.getCode();
            String seqStr = lastCode.substring(prefix.length());
            seq = Integer.parseInt(seqStr) + 1;
        }

        return prefix + String.format("%04d", seq);
    }

    @Override
    public Page<?> getPendingReceiveOrders(Long current, Long size, Long supplierId) {
        // 查询已审核但未完成的采购订单
        return purchaseOrderService.getOrderPage(current, size, supplierId, PurchaseOrderStatus.APPROVED.getValue());
    }
}

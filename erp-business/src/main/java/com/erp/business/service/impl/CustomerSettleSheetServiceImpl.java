package com.erp.business.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.business.dto.CustomerSettleSheetDTO;
import com.erp.business.dto.CustomerSettleSheetQueryDTO;
import com.erp.business.entity.CustomerSettleSheet;
import com.erp.business.entity.CustomerSettleSheetDetail;
import com.erp.business.mapper.CustomerSettleSheetDetailMapper;
import com.erp.business.mapper.CustomerSettleSheetMapper;
import com.erp.business.service.ICustomerSettleSheetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CustomerSettleSheetServiceImpl extends ServiceImpl<CustomerSettleSheetMapper, CustomerSettleSheet>
        implements ICustomerSettleSheetService {
    private final CustomerSettleSheetDetailMapper detailMapper;

    @Override
    public Page<CustomerSettleSheet> queryPage(Long current, Long size, CustomerSettleSheetQueryDTO queryDTO) {
        Page<CustomerSettleSheet> page = new Page<>(current != null ? current : 1, size != null ? size : 10);
        LambdaQueryWrapper<CustomerSettleSheet> wrapper = new LambdaQueryWrapper<>();
        if (queryDTO != null) {
            if (StrUtil.isNotBlank(queryDTO.getCode()))
                wrapper.like(CustomerSettleSheet::getCode, queryDTO.getCode());
            if (queryDTO.getCustomerId() != null)
                wrapper.eq(CustomerSettleSheet::getCustomerId, queryDTO.getCustomerId());
            if (queryDTO.getStatus() != null)
                wrapper.eq(CustomerSettleSheet::getStatus, queryDTO.getStatus());
        }
        wrapper.orderByDesc(CustomerSettleSheet::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    public Map<String, Object> getDetailById(Long id) {
        CustomerSettleSheet sheet = this.getById(id);
        if (sheet == null)
            return null;
        LambdaQueryWrapper<CustomerSettleSheetDetail> detailWrapper = new LambdaQueryWrapper<>();
        detailWrapper.eq(CustomerSettleSheetDetail::getSheetId, id).orderByAsc(CustomerSettleSheetDetail::getOrderNo);
        List<CustomerSettleSheetDetail> details = detailMapper.selectList(detailWrapper);
        Map<String, Object> result = new HashMap<>();
        result.put("sheet", sheet);
        result.put("details", details);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(CustomerSettleSheetDTO dto) {
        CustomerSettleSheet sheet = new CustomerSettleSheet();
        sheet.setCode(generateCode());
        sheet.setCustomerId(dto.getCustomerId());
        sheet.setStartDate(dto.getStartDate());
        sheet.setEndDate(dto.getEndDate());
        sheet.setTotalDiscountAmount(
                dto.getTotalDiscountAmount() != null ? dto.getTotalDiscountAmount() : BigDecimal.ZERO);
        sheet.setDescription(dto.getDescription());
        sheet.setStatus(0);
        BigDecimal totalAmount = BigDecimal.ZERO;
        if (dto.getDetails() != null) {
            for (CustomerSettleSheetDTO.DetailDTO d : dto.getDetails())
                totalAmount = totalAmount.add(d.getPayAmount());
        }
        sheet.setTotalAmount(totalAmount);
        this.save(sheet);
        if (dto.getDetails() != null) {
            int orderNo = 0;
            for (CustomerSettleSheetDTO.DetailDTO d : dto.getDetails()) {
                CustomerSettleSheetDetail detail = new CustomerSettleSheetDetail();
                detail.setSheetId(sheet.getId());
                detail.setBizId(d.getBizId());
                detail.setBizCode(d.getBizCode());
                detail.setPayAmount(d.getPayAmount());
                detail.setDiscountAmount(d.getDiscountAmount() != null ? d.getDiscountAmount() : BigDecimal.ZERO);
                detail.setDescription(d.getDescription());
                detail.setOrderNo(orderNo++);
                detailMapper.insert(detail);
            }
        }
        return sheet.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(CustomerSettleSheetDTO dto) {
        CustomerSettleSheet sheet = this.getById(dto.getId());
        if (sheet == null)
            throw new RuntimeException("结算单不存在");
        if (sheet.getStatus() != 0)
            throw new RuntimeException("只有待审核的结算单才能修改");
        sheet.setCustomerId(dto.getCustomerId());
        sheet.setStartDate(dto.getStartDate());
        sheet.setEndDate(dto.getEndDate());
        sheet.setTotalDiscountAmount(
                dto.getTotalDiscountAmount() != null ? dto.getTotalDiscountAmount() : BigDecimal.ZERO);
        sheet.setDescription(dto.getDescription());
        LambdaQueryWrapper<CustomerSettleSheetDetail> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(CustomerSettleSheetDetail::getSheetId, sheet.getId());
        detailMapper.delete(deleteWrapper);
        BigDecimal totalAmount = BigDecimal.ZERO;
        if (dto.getDetails() != null) {
            int orderNo = 0;
            for (CustomerSettleSheetDTO.DetailDTO d : dto.getDetails()) {
                totalAmount = totalAmount.add(d.getPayAmount());
                CustomerSettleSheetDetail detail = new CustomerSettleSheetDetail();
                detail.setSheetId(sheet.getId());
                detail.setBizId(d.getBizId());
                detail.setBizCode(d.getBizCode());
                detail.setPayAmount(d.getPayAmount());
                detail.setDiscountAmount(d.getDiscountAmount() != null ? d.getDiscountAmount() : BigDecimal.ZERO);
                detail.setDescription(d.getDescription());
                detail.setOrderNo(orderNo++);
                detailMapper.insert(detail);
            }
        }
        sheet.setTotalAmount(totalAmount);
        this.updateById(sheet);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(Long id) {
        CustomerSettleSheet sheet = this.getById(id);
        if (sheet == null)
            throw new RuntimeException("结算单不存在");
        if (sheet.getStatus() != 0)
            throw new RuntimeException("只有待审核的结算单才能审核");
        sheet.setStatus(1);
        sheet.setApproveTime(LocalDateTime.now());
        sheet.setApproveBy("admin");
        this.updateById(sheet);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refuse(Long id, String reason) {
        CustomerSettleSheet sheet = this.getById(id);
        if (sheet == null)
            throw new RuntimeException("结算单不存在");
        if (sheet.getStatus() != 0)
            throw new RuntimeException("只有待审核的结算单才能拒绝");
        sheet.setStatus(2);
        sheet.setApproveTime(LocalDateTime.now());
        sheet.setApproveBy("admin");
        sheet.setRefuseReason(reason);
        this.updateById(sheet);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        CustomerSettleSheet sheet = this.getById(id);
        if (sheet == null)
            return;
        if (sheet.getStatus() != 0)
            throw new RuntimeException("只有待审核的结算单才能删除");
        LambdaQueryWrapper<CustomerSettleSheetDetail> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(CustomerSettleSheetDetail::getSheetId, id);
        detailMapper.delete(deleteWrapper);
        this.removeById(id);
    }

    @Override
    public String generateCode() {
        String dateStr = DateUtil.format(new Date(), "yyyyMMdd");
        String prefix = "KJS" + dateStr;
        String maxCode = baseMapper.selectMaxCodeByPrefix(prefix);
        int seq = 1;
        if (maxCode != null && maxCode.length() > prefix.length())
            seq = Integer.parseInt(maxCode.substring(prefix.length())) + 1;
        return prefix + String.format("%04d", seq);
    }
}

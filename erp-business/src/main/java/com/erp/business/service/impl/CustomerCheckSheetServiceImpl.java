package com.erp.business.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.business.dto.CustomerCheckSheetDTO;
import com.erp.business.dto.CustomerCheckSheetQueryDTO;
import com.erp.business.entity.CustomerCheckSheet;
import com.erp.business.entity.CustomerCheckSheetDetail;
import com.erp.business.mapper.CustomerCheckSheetDetailMapper;
import com.erp.business.mapper.CustomerCheckSheetMapper;
import com.erp.business.service.ICustomerCheckSheetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CustomerCheckSheetServiceImpl extends ServiceImpl<CustomerCheckSheetMapper, CustomerCheckSheet>
        implements ICustomerCheckSheetService {
    private final CustomerCheckSheetDetailMapper detailMapper;

    @Override
    public Page<CustomerCheckSheet> queryPage(Long current, Long size, CustomerCheckSheetQueryDTO queryDTO) {
        Page<CustomerCheckSheet> page = new Page<>(current != null ? current : 1, size != null ? size : 10);
        LambdaQueryWrapper<CustomerCheckSheet> wrapper = new LambdaQueryWrapper<>();
        if (queryDTO != null) {
            if (StrUtil.isNotBlank(queryDTO.getCode()))
                wrapper.like(CustomerCheckSheet::getCode, queryDTO.getCode());
            if (queryDTO.getCustomerId() != null)
                wrapper.eq(CustomerCheckSheet::getCustomerId, queryDTO.getCustomerId());
            if (queryDTO.getStatus() != null)
                wrapper.eq(CustomerCheckSheet::getStatus, queryDTO.getStatus());
        }
        wrapper.orderByDesc(CustomerCheckSheet::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    public Map<String, Object> getDetailById(Long id) {
        CustomerCheckSheet sheet = this.getById(id);
        if (sheet == null)
            return null;
        LambdaQueryWrapper<CustomerCheckSheetDetail> detailWrapper = new LambdaQueryWrapper<>();
        detailWrapper.eq(CustomerCheckSheetDetail::getSheetId, id).orderByAsc(CustomerCheckSheetDetail::getOrderNo);
        List<CustomerCheckSheetDetail> details = detailMapper.selectList(detailWrapper);
        Map<String, Object> result = new HashMap<>();
        result.put("sheet", sheet);
        result.put("details", details);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(CustomerCheckSheetDTO dto) {
        CustomerCheckSheet sheet = new CustomerCheckSheet();
        sheet.setCode(generateCode());
        sheet.setCustomerId(dto.getCustomerId());
        sheet.setStartDate(dto.getStartDate());
        sheet.setEndDate(dto.getEndDate());
        sheet.setTotalDiscountAmount(
                dto.getTotalDiscountAmount() != null ? dto.getTotalDiscountAmount() : BigDecimal.ZERO);
        sheet.setDescription(dto.getDescription());
        sheet.setStatus(0);
        sheet.setTotalPayedAmount(BigDecimal.ZERO);
        BigDecimal totalAmount = BigDecimal.ZERO;
        if (dto.getDetails() != null) {
            for (CustomerCheckSheetDTO.DetailDTO d : dto.getDetails())
                totalAmount = totalAmount.add(d.getPayAmount());
        }
        sheet.setTotalAmount(totalAmount);
        this.save(sheet);
        if (dto.getDetails() != null) {
            int orderNo = 0;
            for (CustomerCheckSheetDTO.DetailDTO d : dto.getDetails()) {
                CustomerCheckSheetDetail detail = new CustomerCheckSheetDetail();
                detail.setSheetId(sheet.getId());
                detail.setBizId(d.getBizId());
                detail.setBizType(d.getBizType());
                detail.setBizCode(d.getBizCode());
                detail.setPayAmount(d.getPayAmount());
                detail.setDescription(d.getDescription());
                detail.setOrderNo(orderNo++);
                detailMapper.insert(detail);
            }
        }
        return sheet.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(CustomerCheckSheetDTO dto) {
        CustomerCheckSheet sheet = this.getById(dto.getId());
        if (sheet == null)
            throw new RuntimeException("对账单不存在");
        if (sheet.getStatus() != 0)
            throw new RuntimeException("只有待审核的对账单才能修改");
        sheet.setCustomerId(dto.getCustomerId());
        sheet.setStartDate(dto.getStartDate());
        sheet.setEndDate(dto.getEndDate());
        sheet.setTotalDiscountAmount(
                dto.getTotalDiscountAmount() != null ? dto.getTotalDiscountAmount() : BigDecimal.ZERO);
        sheet.setDescription(dto.getDescription());
        LambdaQueryWrapper<CustomerCheckSheetDetail> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(CustomerCheckSheetDetail::getSheetId, sheet.getId());
        detailMapper.delete(deleteWrapper);
        BigDecimal totalAmount = BigDecimal.ZERO;
        if (dto.getDetails() != null) {
            int orderNo = 0;
            for (CustomerCheckSheetDTO.DetailDTO d : dto.getDetails()) {
                totalAmount = totalAmount.add(d.getPayAmount());
                CustomerCheckSheetDetail detail = new CustomerCheckSheetDetail();
                detail.setSheetId(sheet.getId());
                detail.setBizId(d.getBizId());
                detail.setBizType(d.getBizType());
                detail.setBizCode(d.getBizCode());
                detail.setPayAmount(d.getPayAmount());
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
        CustomerCheckSheet sheet = this.getById(id);
        if (sheet == null)
            throw new RuntimeException("对账单不存在");
        if (sheet.getStatus() != 0)
            throw new RuntimeException("只有待审核的对账单才能审核");
        sheet.setStatus(1);
        sheet.setApproveTime(LocalDateTime.now());
        sheet.setApproveBy("admin");
        this.updateById(sheet);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refuse(Long id, String reason) {
        CustomerCheckSheet sheet = this.getById(id);
        if (sheet == null)
            throw new RuntimeException("对账单不存在");
        if (sheet.getStatus() != 0)
            throw new RuntimeException("只有待审核的对账单才能拒绝");
        sheet.setStatus(2);
        sheet.setApproveTime(LocalDateTime.now());
        sheet.setApproveBy("admin");
        sheet.setRefuseReason(reason);
        this.updateById(sheet);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        CustomerCheckSheet sheet = this.getById(id);
        if (sheet == null)
            return;
        if (sheet.getStatus() != 0)
            throw new RuntimeException("只有待审核的对账单才能删除");
        LambdaQueryWrapper<CustomerCheckSheetDetail> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(CustomerCheckSheetDetail::getSheetId, id);
        detailMapper.delete(deleteWrapper);
        this.removeById(id);
    }

    @Override
    public String generateCode() {
        String dateStr = DateUtil.format(new Date(), "yyyyMMdd");
        String prefix = "KDZ" + dateStr;
        String maxCode = baseMapper.selectMaxCodeByPrefix(prefix);
        int seq = 1;
        if (maxCode != null && maxCode.length() > prefix.length())
            seq = Integer.parseInt(maxCode.substring(prefix.length())) + 1;
        return prefix + String.format("%04d", seq);
    }
}

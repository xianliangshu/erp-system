package com.erp.business.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.business.dto.SettleCheckSheetDTO;
import com.erp.business.dto.SettleCheckSheetQueryDTO;
import com.erp.business.entity.SettleCheckSheet;
import com.erp.business.entity.SettleCheckSheetDetail;
import com.erp.business.mapper.SettleCheckSheetDetailMapper;
import com.erp.business.mapper.SettleCheckSheetMapper;
import com.erp.business.service.ISettleCheckSheetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SettleCheckSheetServiceImpl extends ServiceImpl<SettleCheckSheetMapper, SettleCheckSheet>
        implements ISettleCheckSheetService {

    private final SettleCheckSheetDetailMapper detailMapper;

    @Override
    public Page<SettleCheckSheet> queryPage(Long current, Long size, SettleCheckSheetQueryDTO queryDTO) {
        Page<SettleCheckSheet> page = new Page<>(current != null ? current : 1, size != null ? size : 10);
        LambdaQueryWrapper<SettleCheckSheet> wrapper = new LambdaQueryWrapper<>();
        if (queryDTO != null) {
            if (StrUtil.isNotBlank(queryDTO.getCode()))
                wrapper.like(SettleCheckSheet::getCode, queryDTO.getCode());
            if (queryDTO.getSupplierId() != null)
                wrapper.eq(SettleCheckSheet::getSupplierId, queryDTO.getSupplierId());
            if (queryDTO.getStatus() != null)
                wrapper.eq(SettleCheckSheet::getStatus, queryDTO.getStatus());
            if (queryDTO.getStartDate() != null)
                wrapper.ge(SettleCheckSheet::getStartDate, queryDTO.getStartDate());
            if (queryDTO.getEndDate() != null)
                wrapper.le(SettleCheckSheet::getEndDate, queryDTO.getEndDate());
        }
        wrapper.orderByDesc(SettleCheckSheet::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    public Map<String, Object> getDetailById(Long id) {
        SettleCheckSheet sheet = this.getById(id);
        if (sheet == null)
            return null;
        LambdaQueryWrapper<SettleCheckSheetDetail> detailWrapper = new LambdaQueryWrapper<>();
        detailWrapper.eq(SettleCheckSheetDetail::getSheetId, id).orderByAsc(SettleCheckSheetDetail::getOrderNo);
        List<SettleCheckSheetDetail> details = detailMapper.selectList(detailWrapper);
        Map<String, Object> result = new HashMap<>();
        result.put("sheet", sheet);
        result.put("details", details);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(SettleCheckSheetDTO dto) {
        SettleCheckSheet sheet = new SettleCheckSheet();
        sheet.setCode(generateCode());
        sheet.setSupplierId(dto.getSupplierId());
        sheet.setStartDate(dto.getStartDate());
        sheet.setEndDate(dto.getEndDate());
        sheet.setTotalDiscountAmount(
                dto.getTotalDiscountAmount() != null ? dto.getTotalDiscountAmount() : BigDecimal.ZERO);
        sheet.setDescription(dto.getDescription());
        sheet.setStatus(0);
        sheet.setTotalPayedAmount(BigDecimal.ZERO);

        BigDecimal totalAmount = BigDecimal.ZERO;
        if (dto.getDetails() != null) {
            for (SettleCheckSheetDTO.DetailDTO d : dto.getDetails()) {
                totalAmount = totalAmount.add(d.getPayAmount());
            }
        }
        sheet.setTotalAmount(totalAmount);
        this.save(sheet);

        if (dto.getDetails() != null) {
            int orderNo = 0;
            for (SettleCheckSheetDTO.DetailDTO d : dto.getDetails()) {
                SettleCheckSheetDetail detail = new SettleCheckSheetDetail();
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
    public void update(SettleCheckSheetDTO dto) {
        SettleCheckSheet sheet = this.getById(dto.getId());
        if (sheet == null)
            throw new RuntimeException("对账单不存在");
        if (sheet.getStatus() != 0)
            throw new RuntimeException("只有待审核的对账单才能修改");

        sheet.setSupplierId(dto.getSupplierId());
        sheet.setStartDate(dto.getStartDate());
        sheet.setEndDate(dto.getEndDate());
        sheet.setTotalDiscountAmount(
                dto.getTotalDiscountAmount() != null ? dto.getTotalDiscountAmount() : BigDecimal.ZERO);
        sheet.setDescription(dto.getDescription());

        LambdaQueryWrapper<SettleCheckSheetDetail> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(SettleCheckSheetDetail::getSheetId, sheet.getId());
        detailMapper.delete(deleteWrapper);

        BigDecimal totalAmount = BigDecimal.ZERO;
        if (dto.getDetails() != null) {
            int orderNo = 0;
            for (SettleCheckSheetDTO.DetailDTO d : dto.getDetails()) {
                totalAmount = totalAmount.add(d.getPayAmount());
                SettleCheckSheetDetail detail = new SettleCheckSheetDetail();
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
        SettleCheckSheet sheet = this.getById(id);
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
        SettleCheckSheet sheet = this.getById(id);
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
        SettleCheckSheet sheet = this.getById(id);
        if (sheet == null)
            return;
        if (sheet.getStatus() != 0)
            throw new RuntimeException("只有待审核的对账单才能删除");
        LambdaQueryWrapper<SettleCheckSheetDetail> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(SettleCheckSheetDetail::getSheetId, id);
        detailMapper.delete(deleteWrapper);
        this.removeById(id);
    }

    @Override
    public String generateCode() {
        String dateStr = DateUtil.format(new Date(), "yyyyMMdd");
        String prefix = "DZ" + dateStr;
        String maxCode = baseMapper.selectMaxCodeByPrefix(prefix);
        int seq = 1;
        if (maxCode != null && maxCode.length() > prefix.length()) {
            String seqStr = maxCode.substring(prefix.length());
            seq = Integer.parseInt(seqStr) + 1;
        }
        return prefix + String.format("%04d", seq);
    }
}

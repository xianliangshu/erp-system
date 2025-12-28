package com.erp.business.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.business.dto.SettleSheetDTO;
import com.erp.business.dto.SettleSheetQueryDTO;
import com.erp.business.entity.SettleSheet;
import com.erp.business.entity.SettleSheetDetail;
import com.erp.business.mapper.SettleSheetDetailMapper;
import com.erp.business.mapper.SettleSheetMapper;
import com.erp.business.service.ISettleSheetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SettleSheetServiceImpl extends ServiceImpl<SettleSheetMapper, SettleSheet>
        implements ISettleSheetService {

    private final SettleSheetDetailMapper detailMapper;

    @Override
    public Page<SettleSheet> queryPage(Long current, Long size, SettleSheetQueryDTO queryDTO) {
        Page<SettleSheet> page = new Page<>(current != null ? current : 1, size != null ? size : 10);
        LambdaQueryWrapper<SettleSheet> wrapper = new LambdaQueryWrapper<>();
        if (queryDTO != null) {
            if (StrUtil.isNotBlank(queryDTO.getCode()))
                wrapper.like(SettleSheet::getCode, queryDTO.getCode());
            if (queryDTO.getSupplierId() != null)
                wrapper.eq(SettleSheet::getSupplierId, queryDTO.getSupplierId());
            if (queryDTO.getStatus() != null)
                wrapper.eq(SettleSheet::getStatus, queryDTO.getStatus());
        }
        wrapper.orderByDesc(SettleSheet::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    public Map<String, Object> getDetailById(Long id) {
        SettleSheet sheet = this.getById(id);
        if (sheet == null)
            return null;
        LambdaQueryWrapper<SettleSheetDetail> detailWrapper = new LambdaQueryWrapper<>();
        detailWrapper.eq(SettleSheetDetail::getSheetId, id).orderByAsc(SettleSheetDetail::getOrderNo);
        List<SettleSheetDetail> details = detailMapper.selectList(detailWrapper);
        Map<String, Object> result = new HashMap<>();
        result.put("sheet", sheet);
        result.put("details", details);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(SettleSheetDTO dto) {
        SettleSheet sheet = new SettleSheet();
        sheet.setCode(generateCode());
        sheet.setSupplierId(dto.getSupplierId());
        sheet.setStartDate(dto.getStartDate());
        sheet.setEndDate(dto.getEndDate());
        sheet.setTotalDiscountAmount(
                dto.getTotalDiscountAmount() != null ? dto.getTotalDiscountAmount() : BigDecimal.ZERO);
        sheet.setDescription(dto.getDescription());
        sheet.setStatus(0);

        BigDecimal totalAmount = BigDecimal.ZERO;
        if (dto.getDetails() != null) {
            for (SettleSheetDTO.DetailDTO d : dto.getDetails()) {
                totalAmount = totalAmount.add(d.getPayAmount());
            }
        }
        sheet.setTotalAmount(totalAmount);
        this.save(sheet);

        if (dto.getDetails() != null) {
            int orderNo = 0;
            for (SettleSheetDTO.DetailDTO d : dto.getDetails()) {
                SettleSheetDetail detail = new SettleSheetDetail();
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
    public void update(SettleSheetDTO dto) {
        SettleSheet sheet = this.getById(dto.getId());
        if (sheet == null)
            throw new RuntimeException("结算单不存在");
        if (sheet.getStatus() != 0)
            throw new RuntimeException("只有待审核的结算单才能修改");

        sheet.setSupplierId(dto.getSupplierId());
        sheet.setStartDate(dto.getStartDate());
        sheet.setEndDate(dto.getEndDate());
        sheet.setTotalDiscountAmount(
                dto.getTotalDiscountAmount() != null ? dto.getTotalDiscountAmount() : BigDecimal.ZERO);
        sheet.setDescription(dto.getDescription());

        LambdaQueryWrapper<SettleSheetDetail> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(SettleSheetDetail::getSheetId, sheet.getId());
        detailMapper.delete(deleteWrapper);

        BigDecimal totalAmount = BigDecimal.ZERO;
        if (dto.getDetails() != null) {
            int orderNo = 0;
            for (SettleSheetDTO.DetailDTO d : dto.getDetails()) {
                totalAmount = totalAmount.add(d.getPayAmount());
                SettleSheetDetail detail = new SettleSheetDetail();
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
        SettleSheet sheet = this.getById(id);
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
        SettleSheet sheet = this.getById(id);
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
        SettleSheet sheet = this.getById(id);
        if (sheet == null)
            return;
        if (sheet.getStatus() != 0)
            throw new RuntimeException("只有待审核的结算单才能删除");
        LambdaQueryWrapper<SettleSheetDetail> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(SettleSheetDetail::getSheetId, id);
        detailMapper.delete(deleteWrapper);
        this.removeById(id);
    }

    @Override
    public String generateCode() {
        String dateStr = DateUtil.format(new Date(), "yyyyMMdd");
        String prefix = "JS" + dateStr;
        String maxCode = baseMapper.selectMaxCodeByPrefix(prefix);
        int seq = 1;
        if (maxCode != null && maxCode.length() > prefix.length()) {
            String seqStr = maxCode.substring(prefix.length());
            seq = Integer.parseInt(seqStr) + 1;
        }
        return prefix + String.format("%04d", seq);
    }
}

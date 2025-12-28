package com.erp.business.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.business.dto.SettlePreSheetDTO;
import com.erp.business.dto.SettlePreSheetQueryDTO;
import com.erp.business.entity.SettlePreSheet;
import com.erp.business.entity.SettlePreSheetDetail;
import com.erp.business.mapper.SettlePreSheetDetailMapper;
import com.erp.business.mapper.SettlePreSheetMapper;
import com.erp.business.service.ISettlePreSheetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SettlePreSheetServiceImpl extends ServiceImpl<SettlePreSheetMapper, SettlePreSheet>
        implements ISettlePreSheetService {

    private final SettlePreSheetDetailMapper detailMapper;

    @Override
    public Page<SettlePreSheet> queryPage(Long current, Long size, SettlePreSheetQueryDTO queryDTO) {
        Page<SettlePreSheet> page = new Page<>(current != null ? current : 1, size != null ? size : 10);
        LambdaQueryWrapper<SettlePreSheet> wrapper = new LambdaQueryWrapper<>();

        if (queryDTO != null) {
            if (StrUtil.isNotBlank(queryDTO.getCode())) {
                wrapper.like(SettlePreSheet::getCode, queryDTO.getCode());
            }
            if (queryDTO.getSupplierId() != null) {
                wrapper.eq(SettlePreSheet::getSupplierId, queryDTO.getSupplierId());
            }
            if (queryDTO.getStatus() != null) {
                wrapper.eq(SettlePreSheet::getStatus, queryDTO.getStatus());
            }
        }
        wrapper.orderByDesc(SettlePreSheet::getCreateTime);

        return this.page(page, wrapper);
    }

    @Override
    public Map<String, Object> getDetailById(Long id) {
        SettlePreSheet sheet = this.getById(id);
        if (sheet == null)
            return null;

        LambdaQueryWrapper<SettlePreSheetDetail> detailWrapper = new LambdaQueryWrapper<>();
        detailWrapper.eq(SettlePreSheetDetail::getSheetId, id);
        detailWrapper.orderByAsc(SettlePreSheetDetail::getOrderNo);
        List<SettlePreSheetDetail> details = detailMapper.selectList(detailWrapper);

        Map<String, Object> result = new HashMap<>();
        result.put("sheet", sheet);
        result.put("details", details);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(SettlePreSheetDTO dto) {
        SettlePreSheet sheet = new SettlePreSheet();
        sheet.setCode(generateCode());
        sheet.setSupplierId(dto.getSupplierId());
        sheet.setDescription(dto.getDescription());
        sheet.setStatus(0);

        BigDecimal totalAmount = BigDecimal.ZERO;
        if (dto.getDetails() != null) {
            for (SettlePreSheetDTO.DetailDTO detail : dto.getDetails()) {
                totalAmount = totalAmount.add(detail.getAmount());
            }
        }
        sheet.setTotalAmount(totalAmount);

        this.save(sheet);

        if (dto.getDetails() != null) {
            int orderNo = 0;
            for (SettlePreSheetDTO.DetailDTO detailDTO : dto.getDetails()) {
                SettlePreSheetDetail detail = new SettlePreSheetDetail();
                detail.setSheetId(sheet.getId());
                detail.setItemId(detailDTO.getItemId());
                detail.setAmount(detailDTO.getAmount());
                detail.setOrderNo(orderNo++);
                detailMapper.insert(detail);
            }
        }

        return sheet.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(SettlePreSheetDTO dto) {
        SettlePreSheet sheet = this.getById(dto.getId());
        if (sheet == null)
            throw new RuntimeException("预付款单不存在");
        if (sheet.getStatus() != 0)
            throw new RuntimeException("只有待审核的预付款单才能修改");

        sheet.setSupplierId(dto.getSupplierId());
        sheet.setDescription(dto.getDescription());

        LambdaQueryWrapper<SettlePreSheetDetail> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(SettlePreSheetDetail::getSheetId, sheet.getId());
        detailMapper.delete(deleteWrapper);

        BigDecimal totalAmount = BigDecimal.ZERO;
        if (dto.getDetails() != null) {
            int orderNo = 0;
            for (SettlePreSheetDTO.DetailDTO detailDTO : dto.getDetails()) {
                totalAmount = totalAmount.add(detailDTO.getAmount());

                SettlePreSheetDetail detail = new SettlePreSheetDetail();
                detail.setSheetId(sheet.getId());
                detail.setItemId(detailDTO.getItemId());
                detail.setAmount(detailDTO.getAmount());
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
        SettlePreSheet sheet = this.getById(id);
        if (sheet == null)
            throw new RuntimeException("预付款单不存在");
        if (sheet.getStatus() != 0)
            throw new RuntimeException("只有待审核的预付款单才能审核");

        sheet.setStatus(1);
        sheet.setApproveTime(LocalDateTime.now());
        sheet.setApproveBy("admin");
        this.updateById(sheet);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refuse(Long id, String reason) {
        SettlePreSheet sheet = this.getById(id);
        if (sheet == null)
            throw new RuntimeException("预付款单不存在");
        if (sheet.getStatus() != 0)
            throw new RuntimeException("只有待审核的预付款单才能拒绝");

        sheet.setStatus(2);
        sheet.setApproveTime(LocalDateTime.now());
        sheet.setApproveBy("admin");
        sheet.setRefuseReason(reason);
        this.updateById(sheet);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        SettlePreSheet sheet = this.getById(id);
        if (sheet == null)
            return;
        if (sheet.getStatus() != 0)
            throw new RuntimeException("只有待审核的预付款单才能删除");

        LambdaQueryWrapper<SettlePreSheetDetail> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(SettlePreSheetDetail::getSheetId, id);
        detailMapper.delete(deleteWrapper);

        this.removeById(id);
    }

    @Override
    public String generateCode() {
        String dateStr = DateUtil.format(new Date(), "yyyyMMdd");
        String prefix = "YF" + dateStr;
        String maxCode = baseMapper.selectMaxCodeByPrefix(prefix);

        int seq = 1;
        if (maxCode != null && maxCode.length() > prefix.length()) {
            String seqStr = maxCode.substring(prefix.length());
            seq = Integer.parseInt(seqStr) + 1;
        }

        return prefix + String.format("%04d", seq);
    }
}

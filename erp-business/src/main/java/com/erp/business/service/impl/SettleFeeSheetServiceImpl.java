package com.erp.business.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.business.dto.SettleFeeSheetDTO;
import com.erp.business.dto.SettleFeeSheetQueryDTO;
import com.erp.business.entity.SettleFeeSheet;
import com.erp.business.entity.SettleFeeSheetDetail;
import com.erp.business.mapper.SettleFeeSheetDetailMapper;
import com.erp.business.mapper.SettleFeeSheetMapper;
import com.erp.business.service.ISettleFeeSheetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 供应商费用单服务实现
 */
@Service
@RequiredArgsConstructor
public class SettleFeeSheetServiceImpl extends ServiceImpl<SettleFeeSheetMapper, SettleFeeSheet>
        implements ISettleFeeSheetService {

    private final SettleFeeSheetDetailMapper detailMapper;

    @Override
    public Page<SettleFeeSheet> queryPage(Long current, Long size, SettleFeeSheetQueryDTO queryDTO) {
        Page<SettleFeeSheet> page = new Page<>(current != null ? current : 1, size != null ? size : 10);
        LambdaQueryWrapper<SettleFeeSheet> wrapper = new LambdaQueryWrapper<>();

        if (queryDTO != null) {
            if (StrUtil.isNotBlank(queryDTO.getCode())) {
                wrapper.like(SettleFeeSheet::getCode, queryDTO.getCode());
            }
            if (queryDTO.getSupplierId() != null) {
                wrapper.eq(SettleFeeSheet::getSupplierId, queryDTO.getSupplierId());
            }
            if (queryDTO.getSheetType() != null) {
                wrapper.eq(SettleFeeSheet::getSheetType, queryDTO.getSheetType());
            }
            if (queryDTO.getStatus() != null) {
                wrapper.eq(SettleFeeSheet::getStatus, queryDTO.getStatus());
            }
        }
        wrapper.orderByDesc(SettleFeeSheet::getCreateTime);

        return this.page(page, wrapper);
    }

    @Override
    public Map<String, Object> getDetailById(Long id) {
        SettleFeeSheet sheet = this.getById(id);
        if (sheet == null) {
            return null;
        }

        LambdaQueryWrapper<SettleFeeSheetDetail> detailWrapper = new LambdaQueryWrapper<>();
        detailWrapper.eq(SettleFeeSheetDetail::getSheetId, id);
        detailWrapper.orderByAsc(SettleFeeSheetDetail::getOrderNo);
        List<SettleFeeSheetDetail> details = detailMapper.selectList(detailWrapper);

        Map<String, Object> result = new HashMap<>();
        result.put("sheet", sheet);
        result.put("details", details);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(SettleFeeSheetDTO dto) {
        SettleFeeSheet sheet = new SettleFeeSheet();
        sheet.setCode(generateCode());
        sheet.setSupplierId(dto.getSupplierId());
        sheet.setSheetType(dto.getSheetType());
        sheet.setDescription(dto.getDescription());
        sheet.setStatus(0); // 待审核

        BigDecimal totalAmount = BigDecimal.ZERO;
        if (dto.getDetails() != null) {
            for (SettleFeeSheetDTO.DetailDTO detail : dto.getDetails()) {
                totalAmount = totalAmount.add(detail.getAmount());
            }
        }
        sheet.setTotalAmount(totalAmount);

        this.save(sheet);

        // 保存明细
        if (dto.getDetails() != null) {
            int orderNo = 0;
            for (SettleFeeSheetDTO.DetailDTO detailDTO : dto.getDetails()) {
                SettleFeeSheetDetail detail = new SettleFeeSheetDetail();
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
    public void update(SettleFeeSheetDTO dto) {
        SettleFeeSheet sheet = this.getById(dto.getId());
        if (sheet == null) {
            throw new RuntimeException("费用单不存在");
        }
        if (sheet.getStatus() != 0) {
            throw new RuntimeException("只有待审核的费用单才能修改");
        }

        sheet.setSupplierId(dto.getSupplierId());
        sheet.setSheetType(dto.getSheetType());
        sheet.setDescription(dto.getDescription());

        // 删除原有明细
        LambdaQueryWrapper<SettleFeeSheetDetail> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(SettleFeeSheetDetail::getSheetId, sheet.getId());
        detailMapper.delete(deleteWrapper);

        // 重新计算并插入明细
        BigDecimal totalAmount = BigDecimal.ZERO;
        if (dto.getDetails() != null) {
            int orderNo = 0;
            for (SettleFeeSheetDTO.DetailDTO detailDTO : dto.getDetails()) {
                totalAmount = totalAmount.add(detailDTO.getAmount());

                SettleFeeSheetDetail detail = new SettleFeeSheetDetail();
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
        SettleFeeSheet sheet = this.getById(id);
        if (sheet == null) {
            throw new RuntimeException("费用单不存在");
        }
        if (sheet.getStatus() != 0) {
            throw new RuntimeException("只有待审核的费用单才能审核");
        }

        sheet.setStatus(1);
        sheet.setApproveTime(LocalDateTime.now());
        sheet.setApproveBy("admin"); // TODO: 从登录用户获取
        this.updateById(sheet);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refuse(Long id, String reason) {
        SettleFeeSheet sheet = this.getById(id);
        if (sheet == null) {
            throw new RuntimeException("费用单不存在");
        }
        if (sheet.getStatus() != 0) {
            throw new RuntimeException("只有待审核的费用单才能拒绝");
        }

        sheet.setStatus(2);
        sheet.setApproveTime(LocalDateTime.now());
        sheet.setApproveBy("admin");
        sheet.setRefuseReason(reason);
        this.updateById(sheet);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        SettleFeeSheet sheet = this.getById(id);
        if (sheet == null) {
            return;
        }
        if (sheet.getStatus() != 0) {
            throw new RuntimeException("只有待审核的费用单才能删除");
        }

        // 删除明细
        LambdaQueryWrapper<SettleFeeSheetDetail> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(SettleFeeSheetDetail::getSheetId, id);
        detailMapper.delete(deleteWrapper);

        // 删除主表
        this.removeById(id);
    }

    @Override
    public String generateCode() {
        String dateStr = DateUtil.format(new Date(), "yyyyMMdd");
        String prefix = "FY" + dateStr;

        String maxCode = baseMapper.selectMaxCodeByPrefix(prefix);

        int seq = 1;
        if (maxCode != null && maxCode.length() > prefix.length()) {
            String seqStr = maxCode.substring(prefix.length());
            seq = Integer.parseInt(seqStr) + 1;
        }

        return prefix + String.format("%04d", seq);
    }
}

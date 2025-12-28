package com.erp.business.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.business.dto.CustomerFeeSheetDTO;
import com.erp.business.dto.CustomerFeeSheetQueryDTO;
import com.erp.business.entity.CustomerFeeSheet;
import com.erp.business.entity.CustomerFeeSheetDetail;
import com.erp.business.mapper.CustomerFeeSheetDetailMapper;
import com.erp.business.mapper.CustomerFeeSheetMapper;
import com.erp.business.service.ICustomerFeeSheetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CustomerFeeSheetServiceImpl extends ServiceImpl<CustomerFeeSheetMapper, CustomerFeeSheet>
        implements ICustomerFeeSheetService {
    private final CustomerFeeSheetDetailMapper detailMapper;

    @Override
    public Page<CustomerFeeSheet> queryPage(Long current, Long size, CustomerFeeSheetQueryDTO queryDTO) {
        Page<CustomerFeeSheet> page = new Page<>(current != null ? current : 1, size != null ? size : 10);
        LambdaQueryWrapper<CustomerFeeSheet> wrapper = new LambdaQueryWrapper<>();
        if (queryDTO != null) {
            if (StrUtil.isNotBlank(queryDTO.getCode()))
                wrapper.like(CustomerFeeSheet::getCode, queryDTO.getCode());
            if (queryDTO.getCustomerId() != null)
                wrapper.eq(CustomerFeeSheet::getCustomerId, queryDTO.getCustomerId());
            if (queryDTO.getSheetType() != null)
                wrapper.eq(CustomerFeeSheet::getSheetType, queryDTO.getSheetType());
            if (queryDTO.getStatus() != null)
                wrapper.eq(CustomerFeeSheet::getStatus, queryDTO.getStatus());
        }
        wrapper.orderByDesc(CustomerFeeSheet::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    public Map<String, Object> getDetailById(Long id) {
        CustomerFeeSheet sheet = this.getById(id);
        if (sheet == null)
            return null;
        LambdaQueryWrapper<CustomerFeeSheetDetail> detailWrapper = new LambdaQueryWrapper<>();
        detailWrapper.eq(CustomerFeeSheetDetail::getSheetId, id).orderByAsc(CustomerFeeSheetDetail::getOrderNo);
        List<CustomerFeeSheetDetail> details = detailMapper.selectList(detailWrapper);
        Map<String, Object> result = new HashMap<>();
        result.put("sheet", sheet);
        result.put("details", details);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(CustomerFeeSheetDTO dto) {
        CustomerFeeSheet sheet = new CustomerFeeSheet();
        sheet.setCode(generateCode(dto.getSheetType()));
        sheet.setCustomerId(dto.getCustomerId());
        sheet.setSheetType(dto.getSheetType());
        sheet.setDescription(dto.getDescription());
        sheet.setStatus(0);
        BigDecimal totalAmount = BigDecimal.ZERO;
        if (dto.getDetails() != null) {
            for (CustomerFeeSheetDTO.DetailDTO d : dto.getDetails())
                totalAmount = totalAmount.add(d.getAmount());
        }
        sheet.setTotalAmount(totalAmount);
        this.save(sheet);
        if (dto.getDetails() != null) {
            int orderNo = 0;
            for (CustomerFeeSheetDTO.DetailDTO d : dto.getDetails()) {
                CustomerFeeSheetDetail detail = new CustomerFeeSheetDetail();
                detail.setSheetId(sheet.getId());
                detail.setItemId(d.getItemId());
                detail.setAmount(d.getAmount());
                detail.setOrderNo(orderNo++);
                detailMapper.insert(detail);
            }
        }
        return sheet.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(CustomerFeeSheetDTO dto) {
        CustomerFeeSheet sheet = this.getById(dto.getId());
        if (sheet == null)
            throw new RuntimeException("单据不存在");
        if (sheet.getStatus() != 0)
            throw new RuntimeException("只有待审核的单据才能修改");
        sheet.setCustomerId(dto.getCustomerId());
        sheet.setDescription(dto.getDescription());
        LambdaQueryWrapper<CustomerFeeSheetDetail> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(CustomerFeeSheetDetail::getSheetId, sheet.getId());
        detailMapper.delete(deleteWrapper);
        BigDecimal totalAmount = BigDecimal.ZERO;
        if (dto.getDetails() != null) {
            int orderNo = 0;
            for (CustomerFeeSheetDTO.DetailDTO d : dto.getDetails()) {
                totalAmount = totalAmount.add(d.getAmount());
                CustomerFeeSheetDetail detail = new CustomerFeeSheetDetail();
                detail.setSheetId(sheet.getId());
                detail.setItemId(d.getItemId());
                detail.setAmount(d.getAmount());
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
        CustomerFeeSheet sheet = this.getById(id);
        if (sheet == null)
            throw new RuntimeException("单据不存在");
        if (sheet.getStatus() != 0)
            throw new RuntimeException("只有待审核的单据才能审核");
        sheet.setStatus(1);
        sheet.setApproveTime(LocalDateTime.now());
        sheet.setApproveBy("admin");
        this.updateById(sheet);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refuse(Long id, String reason) {
        CustomerFeeSheet sheet = this.getById(id);
        if (sheet == null)
            throw new RuntimeException("单据不存在");
        if (sheet.getStatus() != 0)
            throw new RuntimeException("只有待审核的单据才能拒绝");
        sheet.setStatus(2);
        sheet.setApproveTime(LocalDateTime.now());
        sheet.setApproveBy("admin");
        sheet.setRefuseReason(reason);
        this.updateById(sheet);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        CustomerFeeSheet sheet = this.getById(id);
        if (sheet == null)
            return;
        if (sheet.getStatus() != 0)
            throw new RuntimeException("只有待审核的单据才能删除");
        LambdaQueryWrapper<CustomerFeeSheetDetail> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(CustomerFeeSheetDetail::getSheetId, id);
        detailMapper.delete(deleteWrapper);
        this.removeById(id);
    }

    @Override
    public String generateCode(Integer sheetType) {
        String dateStr = DateUtil.format(new Date(), "yyyyMMdd");
        String prefix = (sheetType != null && sheetType == 1 ? "KSK" : "KKK") + dateStr;
        String maxCode = baseMapper.selectMaxCodeByPrefix(prefix);
        int seq = 1;
        if (maxCode != null && maxCode.length() > prefix.length())
            seq = Integer.parseInt(maxCode.substring(prefix.length())) + 1;
        return prefix + String.format("%04d", seq);
    }
}

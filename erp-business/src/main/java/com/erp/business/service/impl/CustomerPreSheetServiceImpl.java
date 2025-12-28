package com.erp.business.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.business.dto.CustomerPreSheetDTO;
import com.erp.business.dto.CustomerPreSheetQueryDTO;
import com.erp.business.entity.CustomerPreSheet;
import com.erp.business.entity.CustomerPreSheetDetail;
import com.erp.business.mapper.CustomerPreSheetDetailMapper;
import com.erp.business.mapper.CustomerPreSheetMapper;
import com.erp.business.service.ICustomerPreSheetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CustomerPreSheetServiceImpl extends ServiceImpl<CustomerPreSheetMapper, CustomerPreSheet>
        implements ICustomerPreSheetService {
    private final CustomerPreSheetDetailMapper detailMapper;

    @Override
    public Page<CustomerPreSheet> queryPage(Long current, Long size, CustomerPreSheetQueryDTO queryDTO) {
        Page<CustomerPreSheet> page = new Page<>(current != null ? current : 1, size != null ? size : 10);
        LambdaQueryWrapper<CustomerPreSheet> wrapper = new LambdaQueryWrapper<>();
        if (queryDTO != null) {
            if (StrUtil.isNotBlank(queryDTO.getCode()))
                wrapper.like(CustomerPreSheet::getCode, queryDTO.getCode());
            if (queryDTO.getCustomerId() != null)
                wrapper.eq(CustomerPreSheet::getCustomerId, queryDTO.getCustomerId());
            if (queryDTO.getStatus() != null)
                wrapper.eq(CustomerPreSheet::getStatus, queryDTO.getStatus());
        }
        wrapper.orderByDesc(CustomerPreSheet::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    public Map<String, Object> getDetailById(Long id) {
        CustomerPreSheet sheet = this.getById(id);
        if (sheet == null)
            return null;
        LambdaQueryWrapper<CustomerPreSheetDetail> detailWrapper = new LambdaQueryWrapper<>();
        detailWrapper.eq(CustomerPreSheetDetail::getSheetId, id).orderByAsc(CustomerPreSheetDetail::getOrderNo);
        List<CustomerPreSheetDetail> details = detailMapper.selectList(detailWrapper);
        Map<String, Object> result = new HashMap<>();
        result.put("sheet", sheet);
        result.put("details", details);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(CustomerPreSheetDTO dto) {
        CustomerPreSheet sheet = new CustomerPreSheet();
        sheet.setCode(generateCode());
        sheet.setCustomerId(dto.getCustomerId());
        sheet.setDescription(dto.getDescription());
        sheet.setStatus(0);
        BigDecimal totalAmount = BigDecimal.ZERO;
        if (dto.getDetails() != null) {
            for (CustomerPreSheetDTO.DetailDTO d : dto.getDetails())
                totalAmount = totalAmount.add(d.getAmount());
        }
        sheet.setTotalAmount(totalAmount);
        this.save(sheet);
        if (dto.getDetails() != null) {
            int orderNo = 0;
            for (CustomerPreSheetDTO.DetailDTO d : dto.getDetails()) {
                CustomerPreSheetDetail detail = new CustomerPreSheetDetail();
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
    public void update(CustomerPreSheetDTO dto) {
        CustomerPreSheet sheet = this.getById(dto.getId());
        if (sheet == null)
            throw new RuntimeException("单据不存在");
        if (sheet.getStatus() != 0)
            throw new RuntimeException("只有待审核的单据才能修改");
        sheet.setCustomerId(dto.getCustomerId());
        sheet.setDescription(dto.getDescription());
        LambdaQueryWrapper<CustomerPreSheetDetail> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(CustomerPreSheetDetail::getSheetId, sheet.getId());
        detailMapper.delete(deleteWrapper);
        BigDecimal totalAmount = BigDecimal.ZERO;
        if (dto.getDetails() != null) {
            int orderNo = 0;
            for (CustomerPreSheetDTO.DetailDTO d : dto.getDetails()) {
                totalAmount = totalAmount.add(d.getAmount());
                CustomerPreSheetDetail detail = new CustomerPreSheetDetail();
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
        CustomerPreSheet sheet = this.getById(id);
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
        CustomerPreSheet sheet = this.getById(id);
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
        CustomerPreSheet sheet = this.getById(id);
        if (sheet == null)
            return;
        if (sheet.getStatus() != 0)
            throw new RuntimeException("只有待审核的单据才能删除");
        LambdaQueryWrapper<CustomerPreSheetDetail> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(CustomerPreSheetDetail::getSheetId, id);
        detailMapper.delete(deleteWrapper);
        this.removeById(id);
    }

    @Override
    public String generateCode() {
        String dateStr = DateUtil.format(new Date(), "yyyyMMdd");
        String prefix = "KYS" + dateStr;
        String maxCode = baseMapper.selectMaxCodeByPrefix(prefix);
        int seq = 1;
        if (maxCode != null && maxCode.length() > prefix.length())
            seq = Integer.parseInt(maxCode.substring(prefix.length())) + 1;
        return prefix + String.format("%04d", seq);
    }
}

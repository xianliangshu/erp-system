package com.erp.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.business.entity.RetailReturn;
import com.erp.business.entity.RetailReturnDetail;
import com.erp.business.enums.ProductStockBizType;
import com.erp.business.mapper.RetailReturnMapper;
import com.erp.business.service.IProductStockService;
import com.erp.business.service.IRetailReturnDetailService;
import com.erp.business.service.IRetailReturnService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 零售退货单 Service 实现
 */
@Service
public class RetailReturnServiceImpl extends ServiceImpl<RetailReturnMapper, RetailReturn>
        implements IRetailReturnService {

    @Autowired
    private IRetailReturnDetailService detailService;

    @Autowired
    private IProductStockService productStockService;

    @Override
    public IPage<RetailReturn> getPage(Page<RetailReturn> page, Map<String, Object> params) {
        LambdaQueryWrapper<RetailReturn> wrapper = new LambdaQueryWrapper<>();
        String code = (String) params.get("code");
        Long scId = (Long) params.get("scId");
        Integer status = (Integer) params.get("status");

        wrapper.like(StringUtils.hasText(code), RetailReturn::getCode, code)
                .eq(scId != null, RetailReturn::getScId, scId)
                .eq(status != null, RetailReturn::getStatus, status)
                .orderByDesc(RetailReturn::getCreateTime);

        return this.page(page, wrapper);
    }

    @Override
    public Map<String, Object> getDetailById(Long id) {
        RetailReturn sheet = this.getById(id);
        List<RetailReturnDetail> details = detailService.listByReturnId(id);
        Map<String, Object> result = new HashMap<>();
        result.put("sheet", sheet);
        result.put("details", details);
        return result;
    }

    private String generateCode() {
        String prefix = "LT" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String maxCode = baseMapper.selectMaxCode();
        int seq = 1;
        if (maxCode != null && maxCode.startsWith(prefix)) {
            seq = Integer.parseInt(maxCode.substring(prefix.length())) + 1;
        }
        return prefix + String.format("%04d", seq);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @SuppressWarnings("unchecked")
    public void add(Map<String, Object> data) {
        RetailReturn sheet = new RetailReturn();
        sheet.setCode(generateCode());
        sheet.setScId(Long.valueOf(data.get("scId").toString()));
        sheet.setOutSheetId(data.get("outSheetId") != null ? Long.valueOf(data.get("outSheetId").toString()) : null);
        sheet.setCustomerId(data.get("customerId") != null ? Long.valueOf(data.get("customerId").toString()) : null);
        sheet.setDescription((String) data.get("description"));
        sheet.setStatus(0); // 待审核

        List<Map<String, Object>> detailList = (List<Map<String, Object>>) data.get("details");
        BigDecimal totalNum = BigDecimal.ZERO;
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (Map<String, Object> d : detailList) {
            BigDecimal num = new BigDecimal(d.get("returnNum").toString());
            BigDecimal price = new BigDecimal(d.get("taxPrice").toString());
            totalNum = totalNum.add(num);
            totalAmount = totalAmount.add(num.multiply(price));
        }
        sheet.setTotalNum(totalNum);
        sheet.setTotalAmount(totalAmount);

        this.save(sheet);

        List<RetailReturnDetail> details = detailList.stream().map(d -> {
            RetailReturnDetail detail = new RetailReturnDetail();
            detail.setReturnId(sheet.getId());
            detail.setProductId(Long.valueOf(d.get("productId").toString()));
            detail.setReturnNum(new BigDecimal(d.get("returnNum").toString()));
            detail.setTaxPrice(new BigDecimal(d.get("taxPrice").toString()));
            detail.setTaxAmount(detail.getReturnNum().multiply(detail.getTaxPrice()));
            detail.setDescription((String) d.get("description"));
            return detail;
        }).collect(Collectors.toList());

        detailService.saveBatch(details);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @SuppressWarnings("unchecked")
    public void update(Map<String, Object> data) {
        Long id = Long.valueOf(data.get("id").toString());
        RetailReturn sheet = this.getById(id);
        if (sheet.getStatus() != 0) {
            throw new RuntimeException("只能修改待审核状态的单据");
        }

        sheet.setScId(Long.valueOf(data.get("scId").toString()));
        sheet.setOutSheetId(data.get("outSheetId") != null ? Long.valueOf(data.get("outSheetId").toString()) : null);
        sheet.setCustomerId(data.get("customerId") != null ? Long.valueOf(data.get("customerId").toString()) : null);
        sheet.setDescription((String) data.get("description"));

        List<Map<String, Object>> detailList = (List<Map<String, Object>>) data.get("details");
        BigDecimal totalNum = BigDecimal.ZERO;
        BigDecimal totalAmount = BigDecimal.ZERO;

        detailService.deleteByReturnId(id);

        List<RetailReturnDetail> details = detailList.stream().map(d -> {
            BigDecimal num = new BigDecimal(d.get("returnNum").toString());
            BigDecimal price = new BigDecimal(d.get("taxPrice").toString());
            RetailReturnDetail detail = new RetailReturnDetail();
            detail.setReturnId(id);
            detail.setProductId(Long.valueOf(d.get("productId").toString()));
            detail.setReturnNum(num);
            detail.setTaxPrice(price);
            detail.setTaxAmount(num.multiply(price));
            detail.setDescription((String) d.get("description"));
            return detail;
        }).collect(Collectors.toList());

        for (RetailReturnDetail d : details) {
            totalNum = totalNum.add(d.getReturnNum());
            totalAmount = totalAmount.add(d.getTaxAmount());
        }
        sheet.setTotalNum(totalNum);
        sheet.setTotalAmount(totalAmount);

        this.updateById(sheet);
        detailService.saveBatch(details);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(Long id) {
        RetailReturn sheet = this.getById(id);
        if (sheet.getStatus() != 0) {
            throw new RuntimeException("只能审核待审核状态的单据");
        }

        sheet.setStatus(1); // 已审核
        this.updateById(sheet);

        List<RetailReturnDetail> details = detailService.listByReturnId(id);
        for (RetailReturnDetail detail : details) {
            productStockService.addStock(
                    sheet.getScId(),
                    detail.getProductId(),
                    detail.getReturnNum(), // 退货为正数（入库）
                    detail.getTaxPrice(),
                    sheet.getId(),
                    sheet.getCode(),
                    ProductStockBizType.RETAIL_RETURN);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refuse(Long id, String refuseReason) {
        RetailReturn sheet = this.getById(id);
        if (sheet.getStatus() != 0) {
            throw new RuntimeException("只能审核待审核状态的单据");
        }
        sheet.setStatus(2); // 已拒绝
        sheet.setDescription(sheet.getDescription() + " [拒绝原因: " + refuseReason + "]");
        this.updateById(sheet);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        RetailReturn sheet = this.getById(id);
        if (sheet.getStatus() == 1) {
            throw new RuntimeException("已审核的单据不能删除");
        }
        detailService.deleteByReturnId(id);
        this.removeById(id);
    }
}

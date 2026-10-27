package com.erp.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.business.entity.RetailConfig;
import com.erp.business.entity.RetailReturn;
import com.erp.business.entity.RetailReturnDetail;
import com.erp.business.enums.ProductStockBizType;
import com.erp.business.mapper.RetailReturnMapper;
import com.erp.business.service.IProductStockService;
import com.erp.business.service.IRetailConfigService;
import com.erp.business.service.IRetailReturnDetailService;
import com.erp.business.service.IRetailReturnService;
import com.erp.common.exception.BusinessException;
import com.erp.common.service.ICodeGenerateService;
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
    private ICodeGenerateService codeGenerateService;

    @Autowired
    private IRetailReturnDetailService detailService;

    @Autowired
    private IProductStockService productStockService;

    @Autowired
    private IRetailConfigService retailConfigService;

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
        return codeGenerateService.generate("RETAIL_RETURN");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @SuppressWarnings("unchecked")
    public Map<String, Object> add(Map<String, Object> data) {
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

        // 自动审核：由「零售配置 → 零售退货单自动审核」控制
        boolean autoApproved = isAutoApproveEnabled();
        if (autoApproved) {
            this.approve(sheet.getId());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", sheet.getId());
        result.put("code", sheet.getCode());
        result.put("status", autoApproved ? 1 : 0);
        result.put("autoApproved", autoApproved);
        return result;
    }

    /** 读取零售配置：是否开启退货单自动审核 */
    private boolean isAutoApproveEnabled() {
        RetailConfig config = retailConfigService.getRetailConfig();
        return config != null && Boolean.TRUE.equals(config.getReturnStockUnApprove());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @SuppressWarnings("unchecked")
    public void update(Map<String, Object> data) {
        Long id = Long.valueOf(data.get("id").toString());
        RetailReturn sheet = this.getById(id);
        if (sheet == null) {
            throw new BusinessException("零售退货单不存在：id=" + id);
        }
        if (!Integer.valueOf(0).equals(sheet.getStatus())) {
            throw new BusinessException("只能修改待审核状态的单据");
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
        if (sheet == null) {
            throw new BusinessException("零售退货单不存在：id=" + id);
        }
        if (!Integer.valueOf(0).equals(sheet.getStatus())) {
            throw new BusinessException("只能审核待审核状态的单据");
        }

        sheet.setStatus(1); // 已审核
        this.updateById(sheet);

        List<RetailReturnDetail> details = detailService.listByReturnId(id);
        if (details.isEmpty()) {
            throw new BusinessException("单据没有明细，不能审核");
        }
        for (RetailReturnDetail detail : details) {
            // 退货入库用 addStockKeepCost：数量加回去，但传入的是"售价"，
            // 不能用它覆盖库存成本价，否则退货也会把成本价抬高。
            productStockService.addStockKeepCost(
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
        if (sheet == null) {
            throw new BusinessException("零售退货单不存在：id=" + id);
        }
        if (!Integer.valueOf(0).equals(sheet.getStatus())) {
            throw new BusinessException("只能审核待审核状态的单据");
        }
        sheet.setStatus(2); // 已拒绝
        String origin = StringUtils.hasText(sheet.getDescription()) ? sheet.getDescription() + " " : "";
        sheet.setDescription(origin + "[拒绝原因: " + refuseReason + "]");
        this.updateById(sheet);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        RetailReturn sheet = this.getById(id);
        if (sheet == null) {
            throw new BusinessException("零售退货单不存在：id=" + id);
        }
        // 已审核（1）的单据已经动过库存，不允许删除，否则库存会与单据对不上
        if (Integer.valueOf(1).equals(sheet.getStatus())) {
            throw new BusinessException("已审核的单据不能删除");
        }
        detailService.deleteByReturnId(id);
        this.removeById(id);
    }
}

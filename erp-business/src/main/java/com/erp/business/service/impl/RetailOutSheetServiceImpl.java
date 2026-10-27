package com.erp.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.business.entity.RetailConfig;
import com.erp.business.entity.RetailOutSheet;
import com.erp.business.entity.RetailOutSheetDetail;
import com.erp.business.enums.ProductStockBizType;
import com.erp.business.mapper.RetailOutSheetMapper;
import com.erp.business.service.IProductStockService;
import com.erp.business.service.IRetailConfigService;
import com.erp.business.service.IRetailOutSheetDetailService;
import com.erp.business.service.IRetailOutSheetService;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 零售出库单 Service 实现
 */
@Service
public class RetailOutSheetServiceImpl extends ServiceImpl<RetailOutSheetMapper, RetailOutSheet>
        implements IRetailOutSheetService {
    @Autowired
    private ICodeGenerateService codeGenerateService;

    @Autowired
    private IRetailOutSheetDetailService detailService;

    @Autowired
    private IProductStockService productStockService;

    @Autowired
    private IRetailConfigService retailConfigService;

    @Override
    public IPage<RetailOutSheet> getPage(Page<RetailOutSheet> page, Map<String, Object> params) {
        LambdaQueryWrapper<RetailOutSheet> wrapper = new LambdaQueryWrapper<>();
        String code = (String) params.get("code");
        Long scId = (Long) params.get("scId");
        Integer status = (Integer) params.get("status");

        wrapper.like(StringUtils.hasText(code), RetailOutSheet::getCode, code)
                .eq(scId != null, RetailOutSheet::getScId, scId)
                .eq(status != null, RetailOutSheet::getStatus, status)
                .orderByDesc(RetailOutSheet::getCreateTime);

        return this.page(page, wrapper);
    }

    @Override
    public Map<String, Object> getDetailById(Long id) {
        RetailOutSheet sheet = this.getById(id);
        List<RetailOutSheetDetail> details = detailService.listBySheetId(id);
        Map<String, Object> result = new HashMap<>();
        result.put("sheet", sheet);
        result.put("details", details);
        return result;
    }

    private String generateCode() {
        return codeGenerateService.generate("RETAIL_OUT");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @SuppressWarnings("unchecked")
    public Map<String, Object> add(Map<String, Object> data) {
        RetailOutSheet sheet = new RetailOutSheet();
        sheet.setCode(generateCode());
        sheet.setScId(Long.valueOf(data.get("scId").toString()));
        sheet.setCustomerId(data.get("customerId") != null ? Long.valueOf(data.get("customerId").toString()) : null);
        sheet.setDescription((String) data.get("description"));
        sheet.setStatus(0); // 待审核

        List<Map<String, Object>> detailList = (List<Map<String, Object>>) data.get("details");
        BigDecimal totalNum = BigDecimal.ZERO;
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (Map<String, Object> d : detailList) {
            BigDecimal num = new BigDecimal(d.get("outNum").toString());
            BigDecimal price = new BigDecimal(d.get("taxPrice").toString());
            totalNum = totalNum.add(num);
            totalAmount = totalAmount.add(num.multiply(price));
        }
        sheet.setTotalNum(totalNum);
        sheet.setTotalAmount(totalAmount);

        this.save(sheet);

        List<RetailOutSheetDetail> details = detailList.stream().map(d -> {
            RetailOutSheetDetail detail = new RetailOutSheetDetail();
            detail.setSheetId(sheet.getId());
            detail.setProductId(Long.valueOf(d.get("productId").toString()));
            detail.setOutNum(new BigDecimal(d.get("outNum").toString()));
            detail.setTaxPrice(new BigDecimal(d.get("taxPrice").toString()));
            detail.setTaxAmount(detail.getOutNum().multiply(detail.getTaxPrice()));
            detail.setDescription((String) d.get("description"));
            return detail;
        }).collect(Collectors.toList());

        detailService.saveBatch(details);

        // 自动审核：由「零售配置 → 零售出库单自动审核」控制。
        // 审核内部会先校验库存，库存不足时抛 BusinessException，
        // 因为整个 add 在同一事务里，单据与明细会一并回滚，不会留下半截数据。
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

    /** 读取零售配置：是否开启出库单自动审核 */
    private boolean isAutoApproveEnabled() {
        RetailConfig config = retailConfigService.getRetailConfig();
        return config != null && Boolean.TRUE.equals(config.getOutStockUnApprove());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @SuppressWarnings("unchecked")
    public void update(Map<String, Object> data) {
        Long id = Long.valueOf(data.get("id").toString());
        RetailOutSheet sheet = this.getById(id);
        if (sheet == null) {
            throw new BusinessException("零售出库单不存在：id=" + id);
        }
        if (!Integer.valueOf(0).equals(sheet.getStatus())) {
            throw new BusinessException("只能修改待审核状态的单据");
        }

        sheet.setScId(Long.valueOf(data.get("scId").toString()));
        sheet.setCustomerId(data.get("customerId") != null ? Long.valueOf(data.get("customerId").toString()) : null);
        sheet.setDescription((String) data.get("description"));

        List<Map<String, Object>> detailList = (List<Map<String, Object>>) data.get("details");
        BigDecimal totalNum = BigDecimal.ZERO;
        BigDecimal totalAmount = BigDecimal.ZERO;

        detailService.deleteBySheetId(id);

        List<RetailOutSheetDetail> details = detailList.stream().map(d -> {
            BigDecimal num = new BigDecimal(d.get("outNum").toString());
            BigDecimal price = new BigDecimal(d.get("taxPrice").toString());
            RetailOutSheetDetail detail = new RetailOutSheetDetail();
            detail.setSheetId(id);
            detail.setProductId(Long.valueOf(d.get("productId").toString()));
            detail.setOutNum(num);
            detail.setTaxPrice(price);
            detail.setTaxAmount(num.multiply(price));
            detail.setDescription((String) d.get("description"));
            return detail;
        }).collect(Collectors.toList());

        for (RetailOutSheetDetail d : details) {
            totalNum = totalNum.add(d.getOutNum());
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
        RetailOutSheet sheet = this.getById(id);
        if (sheet == null) {
            throw new BusinessException("零售出库单不存在：id=" + id);
        }
        if (!Integer.valueOf(0).equals(sheet.getStatus())) {
            throw new BusinessException("只能审核待审核状态的单据");
        }

        List<RetailOutSheetDetail> details = detailService.listBySheetId(id);
        if (details.isEmpty()) {
            throw new BusinessException("单据没有明细，不能审核");
        }

        // 按商品把出库量汇总后再校验库存：同一次出库中同一商品可能落在多条明细上，
        // 逐条校验会漏掉累计超出的情况（例如库存 88，两条明细各要 60）。
        Map<Long, BigDecimal> requireByProduct = new LinkedHashMap<>();
        for (RetailOutSheetDetail detail : details) {
            requireByProduct.merge(detail.getProductId(), detail.getOutNum(), BigDecimal::add);
        }
        for (Map.Entry<Long, BigDecimal> entry : requireByProduct.entrySet()) {
            productStockService.checkStockEnough(sheet.getScId(), entry.getKey(), entry.getValue());
        }

        sheet.setStatus(1); // 已审核
        this.updateById(sheet);

        for (RetailOutSheetDetail detail : details) {
            // 出库必须走 subStock，而不是 addStock 传负数：
            // addStock 的 isAdd=true 会用传入价格覆盖库存成本价，
            // 而出库传的是售价，会把售价写进成本价、抬高库存金额。
            productStockService.subStock(
                    sheet.getScId(),
                    detail.getProductId(),
                    detail.getOutNum(),
                    detail.getTaxPrice(),
                    sheet.getId(),
                    sheet.getCode(),
                    ProductStockBizType.RETAIL_OUT);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refuse(Long id, String refuseReason) {
        RetailOutSheet sheet = this.getById(id);
        if (sheet == null) {
            throw new BusinessException("零售出库单不存在：id=" + id);
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
        RetailOutSheet sheet = this.getById(id);
        if (sheet == null) {
            throw new BusinessException("零售出库单不存在：id=" + id);
        }
        // 已审核（1）的单据已经动过库存，不允许删除，否则库存会与单据对不上
        if (Integer.valueOf(1).equals(sheet.getStatus())) {
            throw new BusinessException("已审核的单据不能删除");
        }
        detailService.deleteBySheetId(id);
        this.removeById(id);
    }
}

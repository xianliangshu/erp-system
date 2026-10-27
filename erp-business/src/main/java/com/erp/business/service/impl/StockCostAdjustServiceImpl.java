package com.erp.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.basedata.entity.BaseMaterial;
import com.erp.basedata.entity.BaseWarehouse;
import com.erp.basedata.mapper.BaseMaterialMapper;
import com.erp.basedata.mapper.BaseWarehouseMapper;
import com.erp.business.entity.ProductStock;
import com.erp.business.entity.ProductStockLog;
import com.erp.business.entity.StockCostAdjustSheet;
import com.erp.business.entity.StockCostAdjustSheetDetail;
import com.erp.business.enums.ProductStockBizType;
import com.erp.business.enums.StockCostAdjustStatus;
import com.erp.business.mapper.ProductStockMapper;
import com.erp.business.mapper.StockCostAdjustSheetDetailMapper;
import com.erp.business.mapper.StockCostAdjustSheetMapper;
import com.erp.business.service.IProductStockLogService;
import com.erp.business.service.IStockCostAdjustService;
import com.erp.common.exception.BusinessException;
import com.erp.common.service.ICodeGenerateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 库存成本调整单 Service 实现
 *
 * <p>核心约束（改这里之前请先读）：</p>
 * <ol>
 *   <li>成本价与库存数量的<b>快照一律在服务端读取</b>，不信任前端传值；</li>
 *   <li>审核通过时<b>只 UPDATE product_stock 的 tax_price / tax_amount</b>，
 *       绝不调用 addStock / subStock —— 数量没变，走库存入口会把 stock_num 再加一遍；</li>
 *   <li>审核前二次校验库存数量与制单快照是否一致，不一致直接拒绝，避免差额算错。</li>
 * </ol>
 *
 * @author ERP System
 */
@Service
@RequiredArgsConstructor
public class StockCostAdjustServiceImpl extends ServiceImpl<StockCostAdjustSheetMapper, StockCostAdjustSheet>
        implements IStockCostAdjustService {

    private final ICodeGenerateService codeGenerateService;
    private final StockCostAdjustSheetDetailMapper detailMapper;
    private final ProductStockMapper productStockMapper;
    private final IProductStockLogService productStockLogService;
    private final BaseWarehouseMapper warehouseMapper;
    private final BaseMaterialMapper materialMapper;

    @Override
    public IPage<StockCostAdjustSheet> pageList(Long current, Long size, String code, Long scId, Integer status) {
        Page<StockCostAdjustSheet> page = new Page<>(current != null ? current : 1, size != null ? size : 10);
        StockCostAdjustStatus statusEnum = parseStatus(status);

        LambdaQueryWrapper<StockCostAdjustSheet> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(code), StockCostAdjustSheet::getCode, code)
                .eq(scId != null, StockCostAdjustSheet::getScId, scId)
                .eq(statusEnum != null, StockCostAdjustSheet::getStatus, statusEnum)
                .orderByDesc(StockCostAdjustSheet::getId);
        this.page(page, wrapper);

        fillScName(page.getRecords());
        return page;
    }

    @Override
    public Map<String, Object> getStockInfo(Long scId, Long productId) {
        Map<String, Object> result = new HashMap<>();
        result.put("scId", scId);
        result.put("productId", productId);

        ProductStock stock = findStock(scId, productId);
        result.put("stockNum", stock != null && stock.getStockNum() != null ? stock.getStockNum() : BigDecimal.ZERO);
        result.put("costPrice", stock != null && stock.getTaxPrice() != null ? stock.getTaxPrice() : BigDecimal.ZERO);

        BaseMaterial material = materialMapper.selectById(productId);
        if (material != null) {
            result.put("productCode", material.getCode());
            result.put("productName", material.getName());
            result.put("specification", material.getSpecification());
            result.put("salePropertyText", material.getSalePropertyText());
            result.put("purchasePrice", material.getPurchasePrice());
        }
        return result;
    }

    @Override
    public StockCostAdjustSheet getDetail(Long id) {
        StockCostAdjustSheet sheet = getById(id);
        if (sheet == null) {
            return null;
        }
        fillScName(List.of(sheet));

        List<StockCostAdjustSheetDetail> details = listDetails(id);
        fillProductInfo(details);
        sheet.setDetails(details);
        return sheet;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean add(StockCostAdjustSheet sheet, List<StockCostAdjustSheetDetail> details) {
        validate(sheet, details);
        if (!StringUtils.hasText(sheet.getCode())) {
            sheet.setCode(codeGenerateService.generate("STOCK_COST_ADJUST"));
        }
        sheet.setId(null);
        sheet.setStatus(StockCostAdjustStatus.CREATED);
        sheet.setProductNum(details.size());
        sheet.setDiffAmount(sumDiff(details));
        if (!save(sheet)) {
            return false;
        }
        insertDetails(sheet.getId(), details);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean modify(StockCostAdjustSheet sheet, List<StockCostAdjustSheetDetail> details) {
        if (sheet.getId() == null) {
            throw new BusinessException("缺少单据ID");
        }
        StockCostAdjustSheet existing = getById(sheet.getId());
        if (existing == null) {
            throw new BusinessException("成本调整单不存在");
        }
        if (!StockCostAdjustStatus.CREATED.equals(existing.getStatus())) {
            throw new BusinessException("只能修改待审核状态的成本调整单");
        }
        validate(sheet, details);

        sheet.setStatus(StockCostAdjustStatus.CREATED);
        sheet.setProductNum(details.size());
        sheet.setDiffAmount(sumDiff(details));
        if (!updateById(sheet)) {
            return false;
        }

        detailMapper.delete(new LambdaQueryWrapper<StockCostAdjustSheetDetail>()
                .eq(StockCostAdjustSheetDetail::getSheetId, sheet.getId()));
        insertDetails(sheet.getId(), details);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean approvePass(Long id, String operator) {
        StockCostAdjustSheet sheet = getById(id);
        if (sheet == null) {
            throw new BusinessException("成本调整单不存在");
        }
        if (!StockCostAdjustStatus.CREATED.equals(sheet.getStatus())) {
            throw new BusinessException("只能审核待审核状态的单据");
        }

        List<StockCostAdjustSheetDetail> details = listDetails(id);
        if (details.isEmpty()) {
            throw new BusinessException("单据没有明细，无法审核");
        }

        BigDecimal totalDiff = BigDecimal.ZERO;

        for (StockCostAdjustSheetDetail detail : details) {
            ProductStock stock = findStock(sheet.getScId(), detail.getProductId());
            String label = productLabel(detail.getProductId());
            if (stock == null) {
                throw new BusinessException(String.format("商品【%s】在该仓库没有库存记录，无法调整成本价", label));
            }

            BigDecimal currentNum = stock.getStockNum() == null ? BigDecimal.ZERO : stock.getStockNum();
            if (detail.getStockNum() == null || currentNum.compareTo(detail.getStockNum()) != 0) {
                throw new BusinessException(String.format(
                        "商品【%s】库存已变动（制单时 %s，当前 %s），差额将不准确，请重新制单",
                        label, detail.getStockNum(), currentNum));
            }
            if (detail.getPrice() == null) {
                throw new BusinessException(String.format("商品【%s】未填写调整后成本价", label));
            }

            BigDecimal oldPrice = stock.getTaxPrice() == null ? BigDecimal.ZERO : stock.getTaxPrice();
            BigDecimal newPrice = detail.getPrice();

            // 只改价格与金额，数量保持不变
            stock.setTaxPrice(newPrice);
            stock.setTaxAmount(newPrice.multiply(currentNum));
            productStockMapper.updateById(stock);

            totalDiff = totalDiff.add(newPrice.subtract(oldPrice).multiply(currentNum));

            // 留痕：数量变动为 0，业务类型标记为成本调整，便于在流水里过滤
            ProductStockLog log = new ProductStockLog();
            log.setScId(sheet.getScId());
            log.setProductId(detail.getProductId());
            log.setOriStockNum(currentNum);
            log.setCurStockNum(currentNum);
            log.setStockNum(BigDecimal.ZERO);
            log.setBizId(sheet.getId());
            log.setBizCode(sheet.getCode());
            log.setBizType(ProductStockBizType.COST_ADJUST);
            productStockLogService.save(log);
        }

        sheet.setStatus(StockCostAdjustStatus.APPROVE_PASS);
        sheet.setApproveBy(operator);
        sheet.setApproveTime(LocalDateTime.now());
        sheet.setProductNum(details.size());
        sheet.setDiffAmount(totalDiff);
        return updateById(sheet);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean approveRefuse(Long id, String refuseReason, String operator) {
        StockCostAdjustSheet sheet = getById(id);
        if (sheet == null) {
            throw new BusinessException("成本调整单不存在");
        }
        if (!StockCostAdjustStatus.CREATED.equals(sheet.getStatus())) {
            throw new BusinessException("只能审核待审核状态的单据");
        }
        sheet.setStatus(StockCostAdjustStatus.APPROVE_REFUSE);
        sheet.setApproveBy(operator);
        sheet.setApproveTime(LocalDateTime.now());
        sheet.setRefuseReason(refuseReason);
        return updateById(sheet);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteById(Long id) {
        StockCostAdjustSheet sheet = getById(id);
        if (sheet == null) {
            throw new BusinessException("成本调整单不存在");
        }
        if (StockCostAdjustStatus.APPROVE_PASS.equals(sheet.getStatus())) {
            throw new BusinessException("审核通过的成本调整单不能删除");
        }
        detailMapper.delete(new LambdaQueryWrapper<StockCostAdjustSheetDetail>()
                .eq(StockCostAdjustSheetDetail::getSheetId, id));
        return removeById(id);
    }

    // ==================== 内部方法 ====================

    /**
     * 校验并补齐明细快照：库存数量与调整前成本价一律以服务端当前值为准
     */
    private void validate(StockCostAdjustSheet sheet, List<StockCostAdjustSheetDetail> details) {
        if (sheet.getScId() == null) {
            throw new BusinessException("请选择仓库");
        }
        if (details == null || details.isEmpty()) {
            throw new BusinessException("请至少添加一条调价明细");
        }

        Set<Long> productIds = new HashSet<>();
        int sort = 1;
        for (StockCostAdjustSheetDetail detail : details) {
            if (detail.getProductId() == null) {
                throw new BusinessException("明细中存在未选择商品的行");
            }
            if (!productIds.add(detail.getProductId())) {
                throw new BusinessException(String.format("商品【%s】在明细中重复，请合并后重试",
                        productLabel(detail.getProductId())));
            }
            if (detail.getPrice() == null) {
                throw new BusinessException(String.format("商品【%s】未填写调整后成本价",
                        productLabel(detail.getProductId())));
            }
            if (detail.getPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessException(String.format("商品【%s】调整后成本价不能为负数",
                        productLabel(detail.getProductId())));
            }

            ProductStock stock = findStock(sheet.getScId(), detail.getProductId());
            if (stock == null) {
                throw new BusinessException(String.format("商品【%s】在该仓库没有库存记录，无法调整成本价",
                        productLabel(detail.getProductId())));
            }

            BigDecimal stockNum = stock.getStockNum() == null ? BigDecimal.ZERO : stock.getStockNum();
            BigDecimal oriPrice = stock.getTaxPrice() == null ? BigDecimal.ZERO : stock.getTaxPrice();
            detail.setId(null);
            detail.setStockNum(stockNum);
            detail.setOriPrice(oriPrice);
            detail.setDiffAmount(detail.getPrice().subtract(oriPrice).multiply(stockNum));
            detail.setOrderNo(sort++);
        }
    }

    private void insertDetails(Long sheetId, List<StockCostAdjustSheetDetail> details) {
        int sort = 1;
        for (StockCostAdjustSheetDetail detail : details) {
            detail.setId(null);
            detail.setSheetId(sheetId);
            detail.setOrderNo(sort++);
            detailMapper.insert(detail);
        }
    }

    private BigDecimal sumDiff(List<StockCostAdjustSheetDetail> details) {
        BigDecimal total = BigDecimal.ZERO;
        for (StockCostAdjustSheetDetail detail : details) {
            if (detail.getDiffAmount() != null) {
                total = total.add(detail.getDiffAmount());
            }
        }
        return total;
    }

    private List<StockCostAdjustSheetDetail> listDetails(Long sheetId) {
        return detailMapper.selectList(new LambdaQueryWrapper<StockCostAdjustSheetDetail>()
                .eq(StockCostAdjustSheetDetail::getSheetId, sheetId)
                .orderByAsc(StockCostAdjustSheetDetail::getOrderNo)
                .orderByAsc(StockCostAdjustSheetDetail::getId));
    }

    /**
     * (仓库, 商品) 理论上唯一，但表上没加唯一键，这里取第一条避免 TooManyResultsException
     */
    private ProductStock findStock(Long scId, Long productId) {
        List<ProductStock> list = productStockMapper.selectList(new LambdaQueryWrapper<ProductStock>()
                .eq(ProductStock::getScId, scId)
                .eq(ProductStock::getProductId, productId)
                .orderByAsc(ProductStock::getId));
        return list.isEmpty() ? null : list.get(0);
    }

    private String productLabel(Long productId) {
        BaseMaterial material = materialMapper.selectById(productId);
        return material != null ? material.getName() + "(" + material.getCode() + ")" : "#" + productId;
    }

    private void fillScName(List<StockCostAdjustSheet> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        Set<Long> scIds = records.stream().map(StockCostAdjustSheet::getScId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> scMap = scIds.isEmpty() ? Map.of()
                : warehouseMapper.selectBatchIds(scIds).stream()
                        .collect(Collectors.toMap(BaseWarehouse::getId, BaseWarehouse::getName));
        for (StockCostAdjustSheet sheet : records) {
            sheet.setScName(scMap.get(sheet.getScId()));
        }
    }

    private void fillProductInfo(List<StockCostAdjustSheetDetail> details) {
        if (details == null || details.isEmpty()) {
            return;
        }
        Set<Long> productIds = details.stream().map(StockCostAdjustSheetDetail::getProductId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        if (productIds.isEmpty()) {
            return;
        }
        Map<Long, BaseMaterial> materialMap = materialMapper.selectBatchIds(productIds).stream()
                .collect(Collectors.toMap(BaseMaterial::getId, Function.identity()));
        for (StockCostAdjustSheetDetail detail : details) {
            BaseMaterial material = materialMap.get(detail.getProductId());
            if (material != null) {
                detail.setProductCode(material.getCode());
                detail.setProductName(material.getName());
                detail.setSpecification(material.getSpecification());
                detail.setSalePropertyText(material.getSalePropertyText());
            }
        }
    }

    private StockCostAdjustStatus parseStatus(Integer code) {
        if (code == null) {
            return null;
        }
        for (StockCostAdjustStatus status : StockCostAdjustStatus.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}

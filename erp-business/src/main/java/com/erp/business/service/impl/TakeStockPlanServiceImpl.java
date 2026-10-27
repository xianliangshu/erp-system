package com.erp.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.basedata.entity.BaseMaterial;
import com.erp.basedata.entity.BaseSpu;
import com.erp.basedata.entity.BaseWarehouse;
import com.erp.basedata.mapper.BaseMaterialMapper;
import com.erp.basedata.mapper.BaseSpuMapper;
import com.erp.basedata.mapper.BaseWarehouseMapper;
import com.erp.business.dto.StockCheckDTO;
import com.erp.business.entity.ProductStock;
import com.erp.business.entity.StockCheck;
import com.erp.business.entity.TakeStockConfig;
import com.erp.business.entity.TakeStockPlan;
import com.erp.business.entity.TakeStockPlanDetail;
import com.erp.business.enums.TakeStockPlanStatus;
import com.erp.business.enums.TakeStockPlanType;
import com.erp.business.mapper.ProductStockMapper;
import com.erp.business.mapper.StockCheckMapper;
import com.erp.business.mapper.TakeStockPlanDetailMapper;
import com.erp.business.mapper.TakeStockPlanMapper;
import com.erp.business.service.IStockCheckService;
import com.erp.business.service.ITakeStockConfigService;
import com.erp.business.service.ITakeStockPlanService;
import com.erp.common.exception.BusinessException;
import com.erp.common.service.ICodeGenerateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 库存盘点任务 Service 实现
 *
 * <p>设计要点：</p>
 * <ol>
 *   <li>创建任务时把当时的库存写进明细作为<b>快照</b>，盘点期间的出入库不会污染比对基准；</li>
 *   <li>本任务<b>自身不改库存</b>：差异汇成一张 {@code stock_check}（回填 {@code plan_id}），
 *       后续走盘点单已有的审核流程 —— 复用而不是另写一套调账逻辑；</li>
 *   <li>超时作废只作用于「任务生成」状态，已生成差异的任务不会被自动作废。</li>
 * </ol>
 *
 * @author ERP System
 */
@Service
@RequiredArgsConstructor
public class TakeStockPlanServiceImpl extends ServiceImpl<TakeStockPlanMapper, TakeStockPlan>
        implements ITakeStockPlanService {

    /** 单次任务的明细上限：防止「全场」误选把库刷爆（同 SKU 矩阵的 200 上限思路） */
    private static final int MAX_DETAIL_SIZE = 2000;

    private final TakeStockPlanDetailMapper detailMapper;
    private final StockCheckMapper stockCheckMapper;
    private final BaseWarehouseMapper warehouseMapper;
    private final BaseMaterialMapper materialMapper;
    private final BaseSpuMapper spuMapper;
    private final ProductStockMapper productStockMapper;
    private final ICodeGenerateService codeGenerateService;
    private final ITakeStockConfigService takeStockConfigService;
    private final IStockCheckService stockCheckService;

    @Override
    public IPage<TakeStockPlan> pageList(Long current, Long size, Long scId, Integer takeStatus,
                                         Integer takeType, String code) {
        Page<TakeStockPlan> page = new Page<>(current != null ? current : 1, size != null ? size : 10);
        LambdaQueryWrapper<TakeStockPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(scId != null, TakeStockPlan::getScId, scId)
                .like(StringUtils.hasText(code), TakeStockPlan::getCode, code)
                .orderByDesc(TakeStockPlan::getId);
        TakeStockPlanStatus statusEnum = toStatusEnum(takeStatus);
        if (statusEnum != null) {
            wrapper.eq(TakeStockPlan::getTakeStatus, statusEnum);
        }
        TakeStockPlanType typeEnum = toTypeEnum(takeType);
        if (typeEnum != null) {
            wrapper.eq(TakeStockPlan::getTakeType, typeEnum);
        }
        this.page(page, wrapper);

        List<TakeStockPlan> records = page.getRecords();
        if (records.isEmpty()) {
            return page;
        }
        fillScNames(records);

        List<Long> planIds = records.stream().map(TakeStockPlan::getId).collect(Collectors.toList());
        Map<Long, Long> totalMap = countByPlan(planIds, false);
        Map<Long, Long> enteredMap = countByPlan(planIds, true);
        for (TakeStockPlan plan : records) {
            plan.setDetailCount(totalMap.getOrDefault(plan.getId(), 0L));
            plan.setEnteredCount(enteredMap.getOrDefault(plan.getId(), 0L));
        }
        return page;
    }

    @Override
    public TakeStockPlan getDetail(Long id) {
        TakeStockPlan plan = getById(id);
        if (plan == null) {
            throw new BusinessException("盘点任务不存在");
        }
        fillScNames(List.of(plan));

        List<TakeStockPlanDetail> details = detailMapper.selectList(
                new LambdaQueryWrapper<TakeStockPlanDetail>()
                        .eq(TakeStockPlanDetail::getPlanId, id)
                        .orderByAsc(TakeStockPlanDetail::getOrderNo)
                        .orderByAsc(TakeStockPlanDetail::getId));

        List<Long> productIds = details.stream().map(TakeStockPlanDetail::getProductId)
                .filter(Objects::nonNull).collect(Collectors.toList());
        Map<Long, BigDecimal> currentStockMap = queryStock(plan.getScId(), productIds);
        Map<Long, BaseMaterial> materialMap = loadMaterials(productIds);

        for (TakeStockPlanDetail detail : details) {
            BaseMaterial material = materialMap.get(detail.getProductId());
            if (material != null) {
                detail.setProductCode(material.getCode());
                detail.setProductName(material.getName());
                detail.setSpecification(material.getSpecification());
                detail.setSalePropertyText(material.getSalePropertyText());
            }
            detail.setCurrentStockNum(currentStockMap.getOrDefault(detail.getProductId(), BigDecimal.ZERO));
            detail.setDiffNum(detail.getTakeNum() != null && detail.getStockNum() != null
                    ? detail.getTakeNum().subtract(detail.getStockNum()) : null);
        }
        plan.setDetails(details);
        plan.setDetailCount((long) details.size());
        plan.setEnteredCount(details.stream().filter(d -> d.getTakeNum() != null).count());

        StockCheck check = stockCheckMapper.selectOne(new LambdaQueryWrapper<StockCheck>()
                .eq(StockCheck::getPlanId, id)
                .orderByDesc(StockCheck::getId)
                .last("LIMIT 1"));
        plan.setCheckId(check != null ? check.getId() : null);
        return plan;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPlan(TakeStockPlan plan) {
        if (plan.getScId() == null) {
            throw new BusinessException("请选择仓库");
        }
        List<Long> productIds = resolveProductIds(plan);
        if (productIds.isEmpty()) {
            throw new BusinessException("盘点范围内没有可盘点的商品");
        }
        if (productIds.size() > MAX_DETAIL_SIZE) {
            throw new BusinessException("盘点范围过大（" + productIds.size() + " 个商品），请缩小范围后重试");
        }

        plan.setId(null);
        plan.setCode(codeGenerateService.generate("STOCK_TAKE_PLAN"));
        plan.setTakeStatus(TakeStockPlanStatus.CREATED);
        save(plan);

        // 关键：写入「创建任务时」的库存快照
        Map<Long, BigDecimal> stockMap = queryStock(plan.getScId(), productIds);

        int orderNo = 0;
        for (Long productId : productIds) {
            TakeStockPlanDetail detail = new TakeStockPlanDetail();
            detail.setPlanId(plan.getId());
            detail.setProductId(productId);
            detail.setStockNum(stockMap.getOrDefault(productId, BigDecimal.ZERO));
            detail.setTotalOutNum(BigDecimal.ZERO);
            detail.setTotalInNum(BigDecimal.ZERO);
            detail.setOrderNo(++orderNo);
            detailMapper.insert(detail);
        }
        return plan.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveTakeNums(Long planId, List<TakeStockPlanDetail> details) {
        if (planId == null) {
            throw new BusinessException("缺少盘点任务ID");
        }
        TakeStockPlan plan = getById(planId);
        if (plan == null) {
            throw new BusinessException("盘点任务不存在");
        }
        assertEditable(plan);
        if (details == null || details.isEmpty()) {
            throw new BusinessException("请填写盘点数量");
        }

        TakeStockConfig config = takeStockConfigService.getConfig();
        boolean allowChange = Integer.valueOf(1).equals(config.getAllowChangeNum());

        Map<Long, TakeStockPlanDetail> existing = detailMapper.selectList(
                        new LambdaQueryWrapper<TakeStockPlanDetail>().eq(TakeStockPlanDetail::getPlanId, planId))
                .stream().collect(Collectors.toMap(TakeStockPlanDetail::getId, Function.identity(), (a, b) -> a));

        for (TakeStockPlanDetail input : details) {
            TakeStockPlanDetail db = existing.get(input.getId());
            if (db == null) {
                throw new BusinessException("明细不属于该盘点任务");
            }
            BigDecimal num = input.getTakeNum() != null ? input.getTakeNum() : input.getOriTakeNum();
            if (num == null) {
                continue;
            }
            if (num.compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessException("盘点数量不能为负数");
            }
            TakeStockPlanDetail update = new TakeStockPlanDetail();
            update.setId(db.getId());
            // 盘点参数关闭「允许手工修改」时，数量一律以库存快照为准
            update.setTakeNum(allowChange ? num : db.getStockNum());
            update.setOriTakeNum(num);
            update.setDescription(input.getDescription());
            detailMapper.updateById(update);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long generateDiff(Long planId) {
        if (planId == null) {
            throw new BusinessException("缺少盘点任务ID");
        }
        TakeStockPlan plan = getById(planId);
        if (plan == null) {
            throw new BusinessException("盘点任务不存在");
        }
        assertEditable(plan);

        List<TakeStockPlanDetail> details = detailMapper.selectList(
                new LambdaQueryWrapper<TakeStockPlanDetail>().eq(TakeStockPlanDetail::getPlanId, planId));

        List<TakeStockPlanDetail> diffList = details.stream()
                .filter(d -> d.getTakeNum() != null && d.getStockNum() != null
                        && d.getTakeNum().compareTo(d.getStockNum()) != 0)
                .collect(Collectors.toList());

        // 全部一致 ⇒ 没有差异可生成，直接置为盘点完成
        if (diffList.isEmpty()) {
            plan.setTakeStatus(TakeStockPlanStatus.FINISHED);
            updateById(plan);
            return null;
        }

        List<Long> productIds = diffList.stream().map(TakeStockPlanDetail::getProductId)
                .distinct().collect(Collectors.toList());
        Map<Long, BigDecimal> costMap = queryCostPrice(plan.getScId(), productIds);
        Map<Long, BaseMaterial> materialMap = loadMaterials(productIds);

        StockCheckDTO dto = new StockCheckDTO();
        dto.setScId(plan.getScId());
        dto.setCheckDate(LocalDate.now().toString());
        dto.setDescription("由盘点任务 " + plan.getCode() + " 自动生成");
        dto.setPlanId(planId);
        dto.setDetails(diffList.stream().map(d -> {
            StockCheckDTO.StockCheckDetailDTO item = new StockCheckDTO.StockCheckDetailDTO();
            item.setProductId(d.getProductId());
            // 账面数用任务快照，而不是「现在」的库存 —— 快照才是这次盘点的比对基准
            item.setStockNum(d.getStockNum());
            item.setActualNum(d.getTakeNum());
            item.setCostPrice(resolveCostPrice(costMap, materialMap, d.getProductId()));
            item.setDescription(d.getDescription());
            return item;
        }).collect(Collectors.toList()));

        // 复用盘点单已有的创建逻辑（含盘盈/盘亏汇总与明细落库）
        Long checkId = stockCheckService.createCheck(dto);

        plan.setTakeStatus(TakeStockPlanStatus.DIFF_GENERATED);
        updateById(plan);

        TakeStockConfig config = takeStockConfigService.getConfig();
        if (Integer.valueOf(1).equals(config.getAutoChangeStock())) {
            stockCheckService.approveCheck(checkId);
        }
        return checkId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancel(Long planId) {
        TakeStockPlan plan = getById(planId);
        if (plan == null) {
            throw new BusinessException("盘点任务不存在");
        }
        assertEditable(plan);
        plan.setTakeStatus(TakeStockPlanStatus.CANCELLED);
        return updateById(plan);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cancelExpired() {
        TakeStockConfig config = takeStockConfigService.getConfig();
        Integer hours = config.getCancelHours();
        if (hours == null || hours <= 0) {
            return 0;
        }
        LocalDateTime deadline = LocalDateTime.now().minusHours(hours);
        List<TakeStockPlan> expired = list(new LambdaQueryWrapper<TakeStockPlan>()
                .eq(TakeStockPlan::getTakeStatus, TakeStockPlanStatus.CREATED)
                .lt(TakeStockPlan::getCreateTime, deadline));
        if (expired.isEmpty()) {
            return 0;
        }
        for (TakeStockPlan plan : expired) {
            plan.setTakeStatus(TakeStockPlanStatus.CANCELLED);
        }
        updateBatchById(expired);
        return expired.size();
    }

    @Override
    public List<TakeStockPlan> listActiveOptions() {
        return list(new LambdaQueryWrapper<TakeStockPlan>()
                .eq(TakeStockPlan::getTakeStatus, TakeStockPlanStatus.CREATED)
                .orderByDesc(TakeStockPlan::getId));
    }

    // ==================== 内部方法 ====================

    /**
     * 只有「任务生成」状态可编辑 / 可作废；已生成差异的要先处理那张盘点单。
     */
    private void assertEditable(TakeStockPlan plan) {
        if (!TakeStockPlanStatus.CREATED.equals(plan.getTakeStatus())) {
            throw new BusinessException("只有「任务生成」状态的任务才能执行该操作（当前：" +
                    (plan.getTakeStatus() != null ? plan.getTakeStatus().getDesc() : "未知") + "）");
        }
    }

    /**
     * 按盘点类别解析出商品 ID 列表。
     *
     * <ul>
     *   <li>全场 —— 该仓库库存表里现有的商品</li>
     *   <li>单品 —— bizId 里的商品 ID</li>
     *   <li>分类 —— 商品档案 category_id 命中</li>
     *   <li>品牌 —— 品牌 → SPU（{@code base_spu.brand_id}）→ SKU（{@code base_material.spu_id}）</li>
     * </ul>
     */
    private List<Long> resolveProductIds(TakeStockPlan plan) {
        TakeStockPlanType type = plan.getTakeType();
        if (type == null) {
            throw new BusinessException("请选择盘点类别");
        }
        switch (type) {
            case ALL: {
                return productStockMapper.selectList(new LambdaQueryWrapper<ProductStock>()
                                .eq(ProductStock::getScId, plan.getScId()))
                        .stream().map(ProductStock::getProductId)
                        .filter(Objects::nonNull).distinct().collect(Collectors.toList());
            }
            case PRODUCT: {
                List<Long> ids = parseIds(plan.getBizId());
                if (ids.isEmpty()) {
                    throw new BusinessException("请选择要盘点的商品");
                }
                return materialMapper.selectBatchIds(ids).stream()
                        .map(BaseMaterial::getId).distinct().collect(Collectors.toList());
            }
            case CATEGORY: {
                List<Long> ids = parseIds(plan.getBizId());
                if (ids.isEmpty()) {
                    throw new BusinessException("请选择要盘点的分类");
                }
                return materialMapper.selectList(new LambdaQueryWrapper<BaseMaterial>()
                                .in(BaseMaterial::getCategoryId, ids))
                        .stream().map(BaseMaterial::getId).distinct().collect(Collectors.toList());
            }
            case BRAND: {
                List<Long> ids = parseIds(plan.getBizId());
                if (ids.isEmpty()) {
                    throw new BusinessException("请选择要盘点的品牌");
                }
                List<Long> spuIds = spuMapper.selectList(new LambdaQueryWrapper<BaseSpu>()
                                .in(BaseSpu::getBrandId, ids))
                        .stream().map(BaseSpu::getId).distinct().collect(Collectors.toList());
                if (spuIds.isEmpty()) {
                    return List.of();
                }
                return materialMapper.selectList(new LambdaQueryWrapper<BaseMaterial>()
                                .in(BaseMaterial::getSpuId, spuIds))
                        .stream().map(BaseMaterial::getId).distinct().collect(Collectors.toList());
            }
            default:
                throw new BusinessException("不支持的盘点类别");
        }
    }

    private List<Long> parseIds(String bizId) {
        if (!StringUtils.hasText(bizId)) {
            return List.of();
        }
        List<Long> ids = new ArrayList<>();
        for (String part : bizId.split(",")) {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            try {
                ids.add(Long.valueOf(trimmed));
            } catch (NumberFormatException e) {
                throw new BusinessException("盘点范围参数格式不正确：" + trimmed);
            }
        }
        return ids.stream().distinct().collect(Collectors.toList());
    }

    private Map<Long, BigDecimal> queryStock(Long scId, Collection<Long> productIds) {
        if (scId == null || productIds == null || productIds.isEmpty()) {
            return Map.of();
        }
        return productStockMapper.selectList(new LambdaQueryWrapper<ProductStock>()
                        .eq(ProductStock::getScId, scId)
                        .in(ProductStock::getProductId, productIds))
                .stream().collect(Collectors.toMap(ProductStock::getProductId,
                        s -> s.getStockNum() != null ? s.getStockNum() : BigDecimal.ZERO, (a, b) -> a));
    }

    private Map<Long, BigDecimal> queryCostPrice(Long scId, Collection<Long> productIds) {
        if (scId == null || productIds == null || productIds.isEmpty()) {
            return Map.of();
        }
        return productStockMapper.selectList(new LambdaQueryWrapper<ProductStock>()
                        .eq(ProductStock::getScId, scId)
                        .in(ProductStock::getProductId, productIds))
                .stream().filter(s -> s.getTaxPrice() != null)
                .collect(Collectors.toMap(ProductStock::getProductId, ProductStock::getTaxPrice, (a, b) -> a));
    }

    /**
     * 差异金额按库存成本价算；库存里没有成本价时退回商品档案的采购价，避免差异金额恒为 0。
     */
    private BigDecimal resolveCostPrice(Map<Long, BigDecimal> costMap, Map<Long, BaseMaterial> materialMap, Long productId) {
        BigDecimal price = costMap.get(productId);
        if (price != null) {
            return price;
        }
        BaseMaterial material = materialMap.get(productId);
        return material != null && material.getPurchasePrice() != null
                ? material.getPurchasePrice() : BigDecimal.ZERO;
    }

    private Map<Long, BaseMaterial> loadMaterials(Collection<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Map.of();
        }
        return materialMapper.selectBatchIds(productIds.stream().distinct().collect(Collectors.toList()))
                .stream().collect(Collectors.toMap(BaseMaterial::getId, Function.identity(), (a, b) -> a));
    }

    private void fillScNames(List<TakeStockPlan> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        Set<Long> scIds = list.stream().map(TakeStockPlan::getScId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> scMap = scIds.isEmpty() ? Map.of()
                : warehouseMapper.selectBatchIds(scIds).stream()
                        .collect(Collectors.toMap(BaseWarehouse::getId, BaseWarehouse::getName, (a, b) -> a));
        list.forEach(plan -> plan.setScName(scMap.get(plan.getScId())));
    }

    /**
     * 用两条 group by 统计明细总数 / 已录入数，避免逐行 count 的 N+1。
     */
    private Map<Long, Long> countByPlan(List<Long> planIds, boolean onlyEntered) {
        if (planIds == null || planIds.isEmpty()) {
            return Map.of();
        }
        QueryWrapper<TakeStockPlanDetail> wrapper = new QueryWrapper<>();
        wrapper.select("plan_id", "COUNT(*) AS cnt")
                .in("plan_id", planIds)
                .groupBy("plan_id");
        if (onlyEntered) {
            wrapper.isNotNull("take_num");
        }
        Map<Long, Long> result = new HashMap<>();
        for (Map<String, Object> row : detailMapper.selectMaps(wrapper)) {
            Object planId = row.get("plan_id");
            Object cnt = row.get("cnt");
            if (planId != null && cnt != null) {
                result.put(Long.valueOf(String.valueOf(planId)), Long.valueOf(String.valueOf(cnt)));
            }
        }
        return result;
    }

    private TakeStockPlanStatus toStatusEnum(Integer code) {
        if (code == null) {
            return null;
        }
        return Arrays.stream(TakeStockPlanStatus.values())
                .filter(s -> Objects.equals(s.getCode(), code)).findFirst().orElse(null);
    }

    private TakeStockPlanType toTypeEnum(Integer code) {
        if (code == null) {
            return null;
        }
        return Arrays.stream(TakeStockPlanType.values())
                .filter(t -> Objects.equals(t.getCode(), code)).findFirst().orElse(null);
    }
}

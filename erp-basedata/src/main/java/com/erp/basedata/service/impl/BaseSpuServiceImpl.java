package com.erp.basedata.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.basedata.dto.SpuPageParam;
import com.erp.basedata.dto.SpuSaveDTO;
import com.erp.basedata.entity.BaseBrand;
import com.erp.basedata.entity.BaseMaterial;
import com.erp.basedata.entity.BaseMaterialCategory;
import com.erp.basedata.entity.BaseMaterialPropertyRel;
import com.erp.basedata.entity.BaseSaleProperty;
import com.erp.basedata.entity.BaseSalePropertyItem;
import com.erp.basedata.entity.BaseSpu;
import com.erp.basedata.entity.BaseSpuSaleProperty;
import com.erp.basedata.entity.BaseUnit;
import com.erp.basedata.mapper.BaseBrandMapper;
import com.erp.basedata.mapper.BaseMaterialCategoryMapper;
import com.erp.basedata.mapper.BaseMaterialMapper;
import com.erp.basedata.mapper.BaseMaterialPropertyRelMapper;
import com.erp.basedata.mapper.BaseSalePropertyItemMapper;
import com.erp.basedata.mapper.BaseSalePropertyMapper;
import com.erp.basedata.mapper.BaseSpuMapper;
import com.erp.basedata.mapper.BaseSpuSalePropertyMapper;
import com.erp.basedata.mapper.BaseUnitMapper;
import com.erp.basedata.service.IBaseSpuService;
import com.erp.common.exception.BusinessException;
import com.erp.common.service.ICodeGenerateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * SPU 商品 Service 实现类
 *
 * <p>负责 SPU 的增删改查，以及按销售属性生成 SKU 矩阵。</p>
 *
 * @author ERP System
 * @since 2026-10-03
 */
@Service
public class BaseSpuServiceImpl extends ServiceImpl<BaseSpuMapper, BaseSpu> implements IBaseSpuService {

    /**
     * SKU 矩阵组合数上限。属性值勾选是笛卡尔积，2 个属性组各 20 个值就是 400 个 SKU，
     * 必须设闸门避免误操作把库刷爆。
     */
    private static final long MAX_MATRIX_SIZE = 200L;

    @Autowired
    private ICodeGenerateService codeGenerateService;

    @Autowired
    private BaseMaterialMapper materialMapper;

    @Autowired
    private BaseSpuSalePropertyMapper spuSalePropertyMapper;

    @Autowired
    private BaseMaterialPropertyRelMapper materialPropertyRelMapper;

    @Autowired
    private BaseSalePropertyMapper salePropertyMapper;

    @Autowired
    private BaseSalePropertyItemMapper salePropertyItemMapper;

    @Autowired
    private BaseMaterialCategoryMapper categoryMapper;

    @Autowired
    private BaseBrandMapper brandMapper;

    @Autowired
    private BaseUnitMapper unitMapper;

    @Override
    public Page<BaseSpu> getSpuPage(SpuPageParam param) {
        Page<BaseSpu> page = new Page<>(param.getCurrent(), param.getSize());

        LambdaQueryWrapper<BaseSpu> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(param.getName()), BaseSpu::getName, param.getName())
                .like(StrUtil.isNotBlank(param.getCode()), BaseSpu::getCode, param.getCode())
                .eq(param.getCategoryId() != null, BaseSpu::getCategoryId, param.getCategoryId())
                .eq(param.getBrandId() != null, BaseSpu::getBrandId, param.getBrandId())
                .eq(param.getStatus() != null, BaseSpu::getStatus, param.getStatus())
                .orderByDesc(BaseSpu::getId);

        Page<BaseSpu> result = this.page(page, wrapper);
        fillExtras(result.getRecords());
        return result;
    }

    @Override
    public List<BaseSpu> getAllEnabled() {
        LambdaQueryWrapper<BaseSpu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BaseSpu::getStatus, 1).orderByAsc(BaseSpu::getId);
        List<BaseSpu> list = this.list(wrapper);
        // 下拉数据源只需要名称，不填充分类/品牌/SKU 数量，避免多余查询
        return list;
    }

    @Override
    public BaseSpu getSpuDetail(Long id) {
        BaseSpu spu = this.getById(id);
        if (spu == null) {
            throw new BusinessException("商品不存在");
        }
        fillExtras(Collections.singletonList(spu));

        List<BaseSpuSaleProperty> rels = spuSalePropertyMapper.selectList(
                new LambdaQueryWrapper<BaseSpuSaleProperty>().eq(BaseSpuSaleProperty::getSpuId, id));
        spu.setPropertyIds(rels.stream().map(BaseSpuSaleProperty::getPropertyId).collect(Collectors.toList()));
        return spu;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveSpu(SpuSaveDTO dto) {
        BaseSpu spu = new BaseSpu();
        copyProps(dto, spu);

        if (StrUtil.isBlank(spu.getCode())) {
            spu.setCode(codeGenerateService.generate("SPU"));
        }
        checkCodeUnique(spu.getCode(), null);
        if (spu.getStatus() == null) {
            spu.setStatus(1);
        }

        this.save(spu);
        // 回写生成的 ID，便于调用方（Controller）直接返回
        dto.setId(spu.getId());

        savePropertyBindings(spu.getId(), dto.getSelections());
        generateSkus(spu.getId(), dto.getSelections());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateSpu(SpuSaveDTO dto) {
        BaseSpu exist = this.getById(dto.getId());
        if (exist == null) {
            throw new BusinessException("商品不存在");
        }

        BaseSpu spu = new BaseSpu();
        spu.setId(dto.getId());
        copyProps(dto, spu);

        if (StrUtil.isBlank(spu.getCode())) {
            // 编号不允许清空，保持原值
            spu.setCode(null);
        } else {
            checkCodeUnique(spu.getCode(), dto.getId());
        }

        this.updateById(spu);

        savePropertyBindings(dto.getId(), dto.getSelections());
        // 只增不删：已存在的属性组合会被跳过，不重建 SKU，保护既有库存与单据引用
        generateSkus(dto.getId(), dto.getSelections());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteSpu(Long id) {
        BaseSpu spu = this.getById(id);
        if (spu == null) {
            throw new BusinessException("商品不存在");
        }

        Long skuCount = materialMapper.selectCount(
                new LambdaQueryWrapper<BaseMaterial>().eq(BaseMaterial::getSpuId, id));
        if (skuCount != null && skuCount > 0) {
            throw new BusinessException("该商品下还有 " + skuCount + " 个 SKU，请先处理后再删除");
        }

        spuSalePropertyMapper.delete(
                new LambdaQueryWrapper<BaseSpuSaleProperty>().eq(BaseSpuSaleProperty::getSpuId, id));
        return this.removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int generateSkus(Long spuId, List<SpuSaveDTO.PropertySelection> selections) {
        if (selections == null || selections.isEmpty()) {
            return 0;
        }

        BaseSpu spu = this.getById(spuId);
        if (spu == null) {
            throw new BusinessException("商品不存在");
        }

        // 1. 只保留真正勾选了属性值的属性组
        List<SpuSaveDTO.PropertySelection> effective = selections.stream()
                .filter(s -> s.getPropertyId() != null && s.getItemIds() != null && !s.getItemIds().isEmpty())
                .collect(Collectors.toList());
        if (effective.isEmpty()) {
            return 0;
        }

        // 2. 组合数闸门（笛卡尔积可能爆炸）
        long total = 1L;
        for (SpuSaveDTO.PropertySelection sel : effective) {
            total *= sel.getItemIds().size();
            if (total > MAX_MATRIX_SIZE) {
                throw new BusinessException("按当前勾选将生成超过 " + MAX_MATRIX_SIZE + " 个 SKU，请减少属性值数量");
            }
        }

        // 3. 已存在的属性组合签名 —— 幂等的关键：相同组合直接跳过，绝不重建
        Set<String> existing = existingSignatures(spuId);

        // 4. 属性值 / 属性组元数据（拼属性文本用）
        Set<Long> allItemIds = effective.stream()
                .flatMap(s -> s.getItemIds().stream())
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (allItemIds.isEmpty()) {
            return 0;
        }
        Map<Long, BaseSalePropertyItem> itemMap = salePropertyItemMapper.selectBatchIds(allItemIds).stream()
                .collect(Collectors.toMap(BaseSalePropertyItem::getId, i -> i, (a, b) -> a));

        Set<Long> propIds = itemMap.values().stream()
                .map(BaseSalePropertyItem::getPropertyId)
                .collect(Collectors.toSet());
        Map<Long, BaseSaleProperty> propMap = propIds.isEmpty() ? Collections.emptyMap()
                : salePropertyMapper.selectBatchIds(propIds).stream()
                        .collect(Collectors.toMap(BaseSaleProperty::getId, p -> p, (a, b) -> a));

        // 5. 笛卡尔积
        List<List<Long>> groups = effective.stream()
                .map(SpuSaveDTO.PropertySelection::getItemIds)
                .collect(Collectors.toList());
        List<List<Long>> combos = cartesian(groups);

        int created = 0;
        for (List<Long> combo : combos) {
            List<Long> valid = combo.stream().filter(itemMap::containsKey).collect(Collectors.toList());
            if (valid.size() != combo.size()) {
                // 勾选的属性值已被删除，跳过该组合
                continue;
            }
            if (!existing.add(signature(valid))) {
                continue;
            }

            String propertyText = buildPropertyText(valid, itemMap, propMap);

            BaseMaterial sku = new BaseMaterial();
            sku.setSpuId(spuId);
            sku.setCategoryId(spu.getCategoryId());
            sku.setUnitId(spu.getUnitId());
            sku.setCode(codeGenerateService.generate("BASE_MATERIAL"));
            sku.setName(buildSkuName(spu.getName(), propertyText));
            sku.setShortName(spu.getShortName());
            sku.setSpecification(spu.getSpecification());
            sku.setSalePropertyText(propertyText);
            sku.setMainImage(spu.getMainImage());
            sku.setPurchasePrice(BigDecimal.ZERO);
            sku.setSalePrice(BigDecimal.ZERO);
            sku.setRetailPrice(BigDecimal.ZERO);
            sku.setMinStock(BigDecimal.ZERO);
            sku.setMaxStock(BigDecimal.ZERO);
            sku.setIsMultiCode(0);
            sku.setStatus(spu.getStatus() == null ? 1 : spu.getStatus());
            materialMapper.insert(sku);

            for (Long itemId : valid) {
                BaseSalePropertyItem item = itemMap.get(itemId);
                BaseMaterialPropertyRel rel = new BaseMaterialPropertyRel();
                rel.setMaterialId(sku.getId());
                rel.setPropertyId(item.getPropertyId());
                rel.setPropertyItemId(itemId);
                materialPropertyRelMapper.insert(rel);
            }
            created++;
        }

        return created;
    }

    @Override
    public List<BaseMaterial> listSkus(Long spuId) {
        List<BaseMaterial> skus = materialMapper.selectList(new LambdaQueryWrapper<BaseMaterial>()
                .eq(BaseMaterial::getSpuId, spuId)
                .orderByAsc(BaseMaterial::getId));
        if (skus.isEmpty()) {
            return skus;
        }

        // 回填每个 SKU 的属性值ID，供前端表单回显勾选状态
        List<Long> materialIds = skus.stream().map(BaseMaterial::getId).collect(Collectors.toList());
        List<BaseMaterialPropertyRel> rels = materialPropertyRelMapper.selectList(
                new LambdaQueryWrapper<BaseMaterialPropertyRel>()
                        .in(BaseMaterialPropertyRel::getMaterialId, materialIds));
        Map<Long, List<Long>> byMaterial = rels.stream().collect(Collectors.groupingBy(
                BaseMaterialPropertyRel::getMaterialId,
                Collectors.mapping(BaseMaterialPropertyRel::getPropertyItemId, Collectors.toList())));
        for (BaseMaterial sku : skus) {
            sku.setPropertyItemIds(byMaterial.getOrDefault(sku.getId(), new ArrayList<>()));
        }
        return skus;
    }

    // ------------------------------------------------------------------
    // 私有方法
    // ------------------------------------------------------------------

    private void copyProps(SpuSaveDTO dto, BaseSpu spu) {
        spu.setCategoryId(dto.getCategoryId());
        spu.setBrandId(dto.getBrandId());
        spu.setUnitId(dto.getUnitId());
        spu.setCode(dto.getCode());
        spu.setName(dto.getName());
        spu.setShortName(dto.getShortName());
        spu.setSpecification(dto.getSpecification());
        spu.setMainImage(dto.getMainImage());
        spu.setStatus(dto.getStatus());
        spu.setRemark(dto.getRemark());
    }

    private void checkCodeUnique(String code, Long excludeId) {
        Long count = baseMapper.countByCodeIncludeDeleted(code, excludeId);
        if (count != null && count > 0) {
            throw new BusinessException("商品编号已存在");
        }
    }

    /**
     * 覆盖保存 SPU 与属性组的绑定关系（先清后插，物理删除）
     */
    private void savePropertyBindings(Long spuId, List<SpuSaveDTO.PropertySelection> selections) {
        spuSalePropertyMapper.delete(
                new LambdaQueryWrapper<BaseSpuSaleProperty>().eq(BaseSpuSaleProperty::getSpuId, spuId));
        if (selections == null || selections.isEmpty()) {
            return;
        }
        Set<Long> saved = new HashSet<>();
        for (SpuSaveDTO.PropertySelection sel : selections) {
            if (sel.getPropertyId() == null || !saved.add(sel.getPropertyId())) {
                continue;
            }
            BaseSpuSaleProperty rel = new BaseSpuSaleProperty();
            rel.setSpuId(spuId);
            rel.setPropertyId(sel.getPropertyId());
            spuSalePropertyMapper.insert(rel);
        }
    }

    /**
     * 收集该 SPU 下已有 SKU 的"属性组合签名"集合
     */
    private Set<String> existingSignatures(Long spuId) {
        List<BaseMaterial> skus = materialMapper.selectList(new LambdaQueryWrapper<BaseMaterial>()
                .select(BaseMaterial::getId)
                .eq(BaseMaterial::getSpuId, spuId));
        if (skus.isEmpty()) {
            return new HashSet<>();
        }
        List<Long> materialIds = skus.stream().map(BaseMaterial::getId).collect(Collectors.toList());
        List<BaseMaterialPropertyRel> rels = materialPropertyRelMapper.selectList(
                new LambdaQueryWrapper<BaseMaterialPropertyRel>()
                        .in(BaseMaterialPropertyRel::getMaterialId, materialIds));

        Map<Long, List<Long>> byMaterial = rels.stream().collect(Collectors.groupingBy(
                BaseMaterialPropertyRel::getMaterialId,
                Collectors.mapping(BaseMaterialPropertyRel::getPropertyItemId, Collectors.toList())));

        Set<String> signatures = new HashSet<>();
        for (List<Long> itemIds : byMaterial.values()) {
            signatures.add(signature(itemIds));
        }
        return signatures;
    }

    /**
     * 属性组合签名：把属性值ID排序后拼接。同一组属性值顺序不同视为同一 SKU。
     */
    private String signature(List<Long> itemIds) {
        return itemIds.stream()
                .filter(Objects::nonNull)
                .sorted()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }

    /**
     * 笛卡尔积
     */
    private List<List<Long>> cartesian(List<List<Long>> groups) {
        List<List<Long>> result = new ArrayList<>();
        result.add(new ArrayList<>());
        for (List<Long> group : groups) {
            if (group == null || group.isEmpty()) {
                continue;
            }
            List<List<Long>> next = new ArrayList<>(result.size() * group.size());
            for (List<Long> base : result) {
                for (Long value : group) {
                    List<Long> row = new ArrayList<>(base);
                    row.add(value);
                    next.add(row);
                }
            }
            result = next;
        }
        return result;
    }

    /**
     * 拼接销售属性文本，如「颜色:红 / 尺码:XL」，按属性组的 sort 排序保证稳定
     */
    private String buildPropertyText(List<Long> itemIds,
                                     Map<Long, BaseSalePropertyItem> itemMap,
                                     Map<Long, BaseSaleProperty> propMap) {
        List<BaseSalePropertyItem> items = itemIds.stream()
                .map(itemMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        items.sort(Comparator.comparingInt((BaseSalePropertyItem item) -> {
            BaseSaleProperty prop = propMap.get(item.getPropertyId());
            return prop == null || prop.getSort() == null ? 0 : prop.getSort();
        }).thenComparing(BaseSalePropertyItem::getId));

        return items.stream()
                .map(item -> {
                    BaseSaleProperty prop = propMap.get(item.getPropertyId());
                    String propName = prop == null ? "" : prop.getName();
                    return propName + ":" + item.getName();
                })
                .collect(Collectors.joining(" / "));
    }

    private String buildSkuName(String spuName, String propertyText) {
        if (StrUtil.isBlank(propertyText)) {
            return truncate(spuName, 200);
        }
        // 名称里去掉冒号，读起来更顺：「钢板Q235 颜色红 尺码XL」
        String suffix = propertyText.replace(":", "").replace(" / ", " ");
        return truncate(spuName + " " + suffix, 200);
    }

    private String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        return value.length() > maxLength ? value.substring(0, maxLength) : value;
    }

    /**
     * 填充分类/品牌/单位名称与 SKU 数量
     */
    private void fillExtras(List<BaseSpu> list) {
        if (list == null || list.isEmpty()) {
            return;
        }

        Set<Long> categoryIds = list.stream().map(BaseSpu::getCategoryId).filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<Long> brandIds = list.stream().map(BaseSpu::getBrandId).filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<Long> unitIds = list.stream().map(BaseSpu::getUnitId).filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<Long> spuIds = list.stream().map(BaseSpu::getId).filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Map<Long, String> categoryNames = categoryIds.isEmpty() ? Collections.emptyMap()
                : categoryMapper.selectBatchIds(categoryIds).stream().collect(
                        Collectors.toMap(BaseMaterialCategory::getId, BaseMaterialCategory::getName, (a, b) -> a));
        Map<Long, String> brandNames = brandIds.isEmpty() ? Collections.emptyMap()
                : brandMapper.selectBatchIds(brandIds).stream()
                        .collect(Collectors.toMap(BaseBrand::getId, BaseBrand::getName, (a, b) -> a));
        Map<Long, String> unitNames = unitIds.isEmpty() ? Collections.emptyMap()
                : unitMapper.selectBatchIds(unitIds).stream()
                        .collect(Collectors.toMap(BaseUnit::getId, BaseUnit::getName, (a, b) -> a));
        Map<Long, Integer> skuCounts = countSkus(spuIds);

        for (BaseSpu spu : list) {
            spu.setCategoryName(categoryNames.get(spu.getCategoryId()));
            spu.setBrandName(brandNames.get(spu.getBrandId()));
            spu.setUnitName(unitNames.get(spu.getUnitId()));
            spu.setSkuCount(skuCounts.getOrDefault(spu.getId(), 0));
        }
    }

    private Map<Long, Integer> countSkus(Collection<Long> spuIds) {
        if (spuIds == null || spuIds.isEmpty()) {
            return Collections.emptyMap();
        }
        QueryWrapper<BaseMaterial> wrapper = new QueryWrapper<>();
        wrapper.select("spu_id", "COUNT(*) AS cnt").in("spu_id", spuIds).groupBy("spu_id");
        List<Map<String, Object>> rows = materialMapper.selectMaps(wrapper);

        Map<Long, Integer> result = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Object spuId = row.get("spu_id");
            Object cnt = row.get("cnt");
            if (spuId instanceof Number && cnt instanceof Number) {
                result.put(((Number) spuId).longValue(), ((Number) cnt).intValue());
            }
        }
        return result;
    }
}

package com.erp.basedata.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.basedata.dto.SalePropertyPageParam;
import com.erp.basedata.entity.BaseMaterialPropertyRel;
import com.erp.basedata.entity.BaseSaleProperty;
import com.erp.basedata.entity.BaseSalePropertyItem;
import com.erp.basedata.entity.BaseSpuSaleProperty;
import com.erp.basedata.mapper.BaseMaterialPropertyRelMapper;
import com.erp.basedata.mapper.BaseSalePropertyItemMapper;
import com.erp.basedata.mapper.BaseSalePropertyMapper;
import com.erp.basedata.mapper.BaseSpuSalePropertyMapper;
import com.erp.basedata.service.ISalePropertyService;
import com.erp.common.exception.BusinessException;
import com.erp.common.service.ICodeGenerateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 商品销售属性组 Service 实现类
 *
 * <p><b>属性值采用差异更新而非整体重建</b>：按名称匹配已有记录并复用其 ID，
 * 避免编辑属性组时把属性值 ID 换掉、导致 SKU 的属性绑定关系失效。</p>
 *
 * @author ERP System
 * @since 2026-10-03
 */
@Service
public class SalePropertyServiceImpl extends ServiceImpl<BaseSalePropertyMapper, BaseSaleProperty>
        implements ISalePropertyService {

    @Autowired
    private ICodeGenerateService codeGenerateService;

    @Autowired
    private BaseSalePropertyItemMapper itemMapper;

    @Autowired
    private BaseMaterialPropertyRelMapper materialPropertyRelMapper;

    @Autowired
    private BaseSpuSalePropertyMapper spuSalePropertyMapper;

    @Override
    public Page<BaseSaleProperty> getPropertyPage(SalePropertyPageParam param) {
        Page<BaseSaleProperty> page = new Page<>(param.getCurrent(), param.getSize());

        LambdaQueryWrapper<BaseSaleProperty> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(param.getName()), BaseSaleProperty::getName, param.getName())
                .like(StrUtil.isNotBlank(param.getCode()), BaseSaleProperty::getCode, param.getCode())
                .eq(param.getStatus() != null, BaseSaleProperty::getStatus, param.getStatus())
                .orderByAsc(BaseSaleProperty::getSort)
                .orderByAsc(BaseSaleProperty::getId);

        Page<BaseSaleProperty> result = this.page(page, wrapper);
        fillItems(result.getRecords());
        return result;
    }

    @Override
    public List<BaseSaleProperty> listWithItems() {
        LambdaQueryWrapper<BaseSaleProperty> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BaseSaleProperty::getStatus, 1)
                .orderByAsc(BaseSaleProperty::getSort)
                .orderByAsc(BaseSaleProperty::getId);
        List<BaseSaleProperty> list = this.list(wrapper);
        fillItems(list);
        return list;
    }

    @Override
    public List<BaseSalePropertyItem> listItems(Long propertyId) {
        LambdaQueryWrapper<BaseSalePropertyItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BaseSalePropertyItem::getPropertyId, propertyId)
                .eq(BaseSalePropertyItem::getStatus, 1)
                .orderByAsc(BaseSalePropertyItem::getSort)
                .orderByAsc(BaseSalePropertyItem::getId);
        return itemMapper.selectList(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveProperty(BaseSaleProperty property) {
        if (StrUtil.isBlank(property.getCode())) {
            property.setCode(codeGenerateService.generate("SALE_PROPERTY"));
        }
        checkCodeUnique(property.getCode(), null);
        if (property.getStatus() == null) {
            property.setStatus(1);
        }
        if (property.getSort() == null) {
            property.setSort(0);
        }

        this.save(property);
        saveItems(property.getId(), property.getItems());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateProperty(BaseSaleProperty property) {
        BaseSaleProperty exist = this.getById(property.getId());
        if (exist == null) {
            throw new BusinessException("属性组不存在");
        }
        if (StrUtil.isBlank(property.getCode())) {
            property.setCode(null);
        } else {
            checkCodeUnique(property.getCode(), property.getId());
        }

        this.updateById(property);
        saveItems(property.getId(), property.getItems());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteProperty(Long id) {
        BaseSaleProperty property = this.getById(id);
        if (property == null) {
            throw new BusinessException("属性组不存在");
        }

        Long used = materialPropertyRelMapper.selectCount(
                new LambdaQueryWrapper<BaseMaterialPropertyRel>().eq(BaseMaterialPropertyRel::getPropertyId, id));
        if (used != null && used > 0) {
            throw new BusinessException("该属性组已被 " + used + " 个 SKU 使用，不能删除");
        }

        // 解除 SPU 绑定并清理属性值（物理删除）
        spuSalePropertyMapper.delete(
                new LambdaQueryWrapper<BaseSpuSaleProperty>().eq(BaseSpuSaleProperty::getPropertyId, id));
        itemMapper.delete(new LambdaQueryWrapper<BaseSalePropertyItem>()
                .eq(BaseSalePropertyItem::getPropertyId, id));
        return this.removeById(id);
    }

    // ------------------------------------------------------------------
    // 私有方法
    // ------------------------------------------------------------------

    private void checkCodeUnique(String code, Long excludeId) {
        Long count = baseMapper.countByCodeIncludeDeleted(code, excludeId);
        if (count != null && count > 0) {
            throw new BusinessException("属性组编码已存在");
        }
    }

    /**
     * 差异更新属性值：按名称复用已有记录，只增改必要项，未出现的旧值在未被 SKU 引用时删除。
     */
    private void saveItems(Long propertyId, List<BaseSalePropertyItem> inputs) {
        if (inputs == null) {
            return;
        }

        List<BaseSalePropertyItem> existing = itemMapper.selectList(
                new LambdaQueryWrapper<BaseSalePropertyItem>().eq(BaseSalePropertyItem::getPropertyId, propertyId));
        Map<String, BaseSalePropertyItem> existingByName = existing.stream()
                .collect(Collectors.toMap(BaseSalePropertyItem::getName, Function.identity(), (a, b) -> a));

        Set<Long> keptIds = new HashSet<>();
        Set<String> seenNames = new LinkedHashSet<>();
        int order = 0;

        for (BaseSalePropertyItem input : inputs) {
            if (input == null || StrUtil.isBlank(input.getName())) {
                continue;
            }
            String name = input.getName().trim();
            if (!seenNames.add(name)) {
                throw new BusinessException("属性值名称重复：" + name);
            }

            BaseSalePropertyItem old = existingByName.get(name);
            if (old != null) {
                // 复用原记录，保持 ID 不变（SKU 绑定关系因此不会断）
                old.setSort(input.getSort() == null ? order : input.getSort());
                old.setStatus(input.getStatus() == null ? 1 : input.getStatus());
                old.setRemark(input.getRemark());
                itemMapper.updateById(old);
                keptIds.add(old.getId());
            } else {
                BaseSalePropertyItem entity = new BaseSalePropertyItem();
                entity.setPropertyId(propertyId);
                entity.setName(name);
                entity.setCode(StrUtil.isBlank(input.getCode())
                        ? codeGenerateService.generate("SALE_PROPERTY_ITEM")
                        : input.getCode());
                entity.setSort(input.getSort() == null ? order : input.getSort());
                entity.setStatus(input.getStatus() == null ? 1 : input.getStatus());
                entity.setRemark(input.getRemark());
                itemMapper.insert(entity);
                keptIds.add(entity.getId());
            }
            order++;
        }

        // 入参中没有的旧属性值：未被 SKU 使用才允许删除
        for (BaseSalePropertyItem old : existing) {
            if (keptIds.contains(old.getId())) {
                continue;
            }
            Long used = materialPropertyRelMapper.selectCount(new LambdaQueryWrapper<BaseMaterialPropertyRel>()
                    .eq(BaseMaterialPropertyRel::getPropertyItemId, old.getId()));
            if (used != null && used > 0) {
                throw new BusinessException("属性值「" + old.getName() + "」已被 " + used + " 个 SKU 使用，不能删除");
            }
            itemMapper.deleteById(old.getId());
        }
    }

    /**
     * 批量填充属性值
     */
    private void fillItems(List<BaseSaleProperty> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        Set<Long> propertyIds = list.stream().map(BaseSaleProperty::getId).filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (propertyIds.isEmpty()) {
            list.forEach(p -> p.setItems(new ArrayList<>()));
            return;
        }

        List<BaseSalePropertyItem> items = itemMapper.selectList(new LambdaQueryWrapper<BaseSalePropertyItem>()
                .in(BaseSalePropertyItem::getPropertyId, propertyIds)
                .orderByAsc(BaseSalePropertyItem::getSort)
                .orderByAsc(BaseSalePropertyItem::getId));

        Map<Long, List<BaseSalePropertyItem>> grouped = items.stream()
                .collect(Collectors.groupingBy(BaseSalePropertyItem::getPropertyId));

        for (BaseSaleProperty property : list) {
            property.setItems(grouped.getOrDefault(property.getId(), Collections.emptyList()));
        }
    }
}

package com.erp.business.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.basedata.entity.BaseMaterial;
import com.erp.basedata.entity.BaseUnit;
import com.erp.basedata.entity.BaseWarehouse;
import com.erp.basedata.mapper.BaseMaterialMapper;
import com.erp.basedata.mapper.BaseUnitMapper;
import com.erp.basedata.mapper.BaseWarehouseMapper;
import com.erp.business.entity.ProductStockLog;
import com.erp.business.enums.ProductStockBizType;
import com.erp.business.mapper.ProductStockLogMapper;
import com.erp.business.service.IProductStockLogService;
import com.erp.business.vo.ProductStockLogVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 库存流水服务实现
 */
@Service
@RequiredArgsConstructor
public class ProductStockLogServiceImpl extends ServiceImpl<ProductStockLogMapper, ProductStockLog>
        implements IProductStockLogService {

    private final BaseWarehouseMapper warehouseMapper;
    private final BaseMaterialMapper materialMapper;
    private final BaseUnitMapper unitMapper;

    @Override
    public Page<ProductStockLogVO> getLogPage(Long current, Long size, Long scId, String productName,
                                              Integer bizType, String startDate, String endDate) {
        Page<ProductStockLog> page = new Page<>(current != null ? current : 1, size != null ? size : 10);

        LambdaQueryWrapper<ProductStockLog> wrapper = new LambdaQueryWrapper<>();

        // 仓库过滤
        if (scId != null) {
            wrapper.eq(ProductStockLog::getScId, scId);
        }

        // 商品名称/编号过滤：先按关键字查出商品ID，再用 IN 过滤（流水表只存 product_id）
        if (StrUtil.isNotBlank(productName)) {
            String keyword = productName.trim();
            LambdaQueryWrapper<BaseMaterial> materialWrapper = new LambdaQueryWrapper<>();
            materialWrapper.and(w -> w.like(BaseMaterial::getName, keyword)
                    .or().like(BaseMaterial::getCode, keyword));
            List<BaseMaterial> materials = materialMapper.selectList(materialWrapper);
            if (materials.isEmpty()) {
                // 没有匹配商品，直接返回空页，避免 IN () 语法问题
                Page<ProductStockLogVO> empty = new Page<>(page.getCurrent(), page.getSize(), 0);
                empty.setRecords(Collections.emptyList());
                return empty;
            }
            List<Long> productIds = materials.stream()
                    .map(BaseMaterial::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            wrapper.in(ProductStockLog::getProductId, productIds);
        }

        // 业务类型过滤
        ProductStockBizType bizTypeEnum = parseBizType(bizType);
        if (bizTypeEnum != null) {
            wrapper.eq(ProductStockLog::getBizType, bizTypeEnum);
        }

        // 时间区间过滤
        LocalDateTime startTime = parseStartTime(startDate);
        if (startTime != null) {
            wrapper.ge(ProductStockLog::getCreateTime, startTime);
        }
        LocalDateTime endTime = parseEndTime(endDate);
        if (endTime != null) {
            wrapper.le(ProductStockLog::getCreateTime, endTime);
        }

        wrapper.orderByDesc(ProductStockLog::getCreateTime).orderByDesc(ProductStockLog::getId);

        this.page(page, wrapper);

        // 批量装配关联信息，避免 N+1 查询
        return toVoPage(page);
    }

    /**
     * 组装VO分页结果（批量查询仓库 / 商品 / 单位）
     */
    private Page<ProductStockLogVO> toVoPage(Page<ProductStockLog> page) {
        Page<ProductStockLogVO> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        List<ProductStockLog> records = page.getRecords();
        if (records == null || records.isEmpty()) {
            result.setRecords(Collections.emptyList());
            return result;
        }

        Set<Long> scIds = records.stream().map(ProductStockLog::getScId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> productIds = records.stream().map(ProductStockLog::getProductId)
                .filter(Objects::nonNull).collect(Collectors.toSet());

        Map<Long, BaseWarehouse> warehouseMap = scIds.isEmpty() ? Collections.emptyMap()
                : warehouseMapper.selectBatchIds(scIds).stream()
                        .collect(Collectors.toMap(BaseWarehouse::getId, Function.identity(), (a, b) -> a));

        Map<Long, BaseMaterial> materialMap = productIds.isEmpty() ? Collections.emptyMap()
                : materialMapper.selectBatchIds(productIds).stream()
                        .collect(Collectors.toMap(BaseMaterial::getId, Function.identity(), (a, b) -> a));

        Set<Long> unitIds = materialMap.values().stream().map(BaseMaterial::getUnitId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, BaseUnit> unitMap = unitIds.isEmpty() ? Collections.emptyMap()
                : unitMapper.selectBatchIds(unitIds).stream()
                        .collect(Collectors.toMap(BaseUnit::getId, Function.identity(), (a, b) -> a));

        List<ProductStockLogVO> voList = new ArrayList<>(records.size());
        for (ProductStockLog log : records) {
            ProductStockLogVO vo = new ProductStockLogVO();
            vo.setId(log.getId());
            vo.setScId(log.getScId());
            vo.setProductId(log.getProductId());
            vo.setOriStockNum(log.getOriStockNum());
            vo.setStockNum(log.getStockNum());
            vo.setCurStockNum(log.getCurStockNum());
            vo.setBizId(log.getBizId());
            vo.setBizCode(log.getBizCode());
            vo.setCreateBy(log.getCreateBy());
            vo.setCreateTime(log.getCreateTime());

            ProductStockBizType type = log.getBizType();
            if (type != null) {
                vo.setBizType(type.getValue());
                vo.setBizTypeDesc(type.getDesc());
            }

            BaseWarehouse warehouse = warehouseMap.get(log.getScId());
            if (warehouse != null) {
                vo.setScName(warehouse.getName());
            }

            BaseMaterial material = materialMap.get(log.getProductId());
            if (material != null) {
                vo.setProductCode(material.getCode());
                vo.setProductName(material.getName());
                vo.setProductSpec(material.getSpecification());
                if (material.getUnitId() != null) {
                    BaseUnit unit = unitMap.get(material.getUnitId());
                    if (unit != null) {
                        vo.setUnitName(unit.getName());
                    }
                }
            }
            voList.add(vo);
        }
        result.setRecords(voList);
        return result;
    }

    /**
     * 按值解析业务类型枚举
     */
    private ProductStockBizType parseBizType(Integer bizType) {
        if (bizType == null) {
            return null;
        }
        for (ProductStockBizType type : ProductStockBizType.values()) {
            if (type.getValue().equals(bizType)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 解析开始时间（当天 00:00:00）
     */
    private LocalDateTime parseStartTime(String date) {
        LocalDate localDate = parseDate(date);
        return localDate == null ? null : localDate.atStartOfDay();
    }

    /**
     * 解析结束时间（当天 23:59:59）
     */
    private LocalDateTime parseEndTime(String date) {
        LocalDate localDate = parseDate(date);
        return localDate == null ? null : localDate.atTime(LocalTime.MAX).withNano(0);
    }

    /**
     * 容错解析日期，兼容 yyyy-MM-dd 与 yyyy-MM-dd HH:mm:ss
     */
    private LocalDate parseDate(String date) {
        if (StrUtil.isBlank(date)) {
            return null;
        }
        String value = date.trim();
        try {
            if (value.length() > 10) {
                value = value.substring(0, 10);
            }
            return LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (Exception e) {
            return null;
        }
    }
}

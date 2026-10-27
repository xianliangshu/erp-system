package com.erp.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.basedata.entity.BaseMaterial;
import com.erp.basedata.entity.BaseWarehouse;
import com.erp.basedata.mapper.BaseMaterialMapper;
import com.erp.basedata.mapper.BaseWarehouseMapper;
import com.erp.business.entity.PreTakeStockSheet;
import com.erp.business.entity.PreTakeStockSheetDetail;
import com.erp.business.mapper.PreTakeStockSheetDetailMapper;
import com.erp.business.mapper.PreTakeStockSheetMapper;
import com.erp.business.service.IPreTakeStockService;
import com.erp.common.exception.BusinessException;
import com.erp.common.service.ICodeGenerateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 库存预先盘点单 Service 实现
 *
 * <p>预先盘点单<b>不产生任何库存变动</b>，本质是「正式盘点前的分次清点记录」，
 * 因此这里没有库存读写，只有单据与明细的 CRUD。</p>
 *
 * @author ERP System
 */
@Service
@RequiredArgsConstructor
public class PreTakeStockServiceImpl extends ServiceImpl<PreTakeStockSheetMapper, PreTakeStockSheet>
        implements IPreTakeStockService {

    private final PreTakeStockSheetDetailMapper detailMapper;
    private final BaseWarehouseMapper warehouseMapper;
    private final BaseMaterialMapper materialMapper;
    private final ICodeGenerateService codeGenerateService;

    @Override
    public IPage<PreTakeStockSheet> pageList(Long current, Long size, Long scId, Integer takeStatus, String code) {
        Page<PreTakeStockSheet> page = new Page<>(current != null ? current : 1, size != null ? size : 10);
        LambdaQueryWrapper<PreTakeStockSheet> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(scId != null, PreTakeStockSheet::getScId, scId)
                .eq(takeStatus != null, PreTakeStockSheet::getTakeStatus, takeStatus)
                .like(StringUtils.hasText(code), PreTakeStockSheet::getCode, code)
                .orderByDesc(PreTakeStockSheet::getId);
        this.page(page, wrapper);
        fillScNames(page.getRecords());
        return page;
    }

    @Override
    public PreTakeStockSheet getDetail(Long id) {
        PreTakeStockSheet sheet = getById(id);
        if (sheet == null) {
            throw new BusinessException("预先盘点单不存在");
        }
        fillScNames(List.of(sheet));
        sheet.setDetails(loadDetails(id));
        return sheet;
    }

    @Override
    public List<PreTakeStockSheet> listOptions() {
        return list(new LambdaQueryWrapper<PreTakeStockSheet>()
                .orderByDesc(PreTakeStockSheet::getId)
                .last("LIMIT 200"));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(PreTakeStockSheet sheet, List<PreTakeStockSheetDetail> details) {
        validate(sheet, details);
        sheet.setId(null);
        sheet.setCode(codeGenerateService.generate("STOCK_TAKE_PRE"));
        save(sheet);
        saveDetails(sheet.getId(), details);
        return sheet.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean modify(PreTakeStockSheet sheet, List<PreTakeStockSheetDetail> details) {
        if (sheet.getId() == null) {
            throw new BusinessException("缺少预先盘点单ID");
        }
        PreTakeStockSheet exists = getById(sheet.getId());
        if (exists == null) {
            throw new BusinessException("预先盘点单不存在");
        }
        validate(sheet, details);

        // 明细整体覆盖：先物理清掉旧明细再重插（明细表无逻辑删除列，直接 delete）
        detailMapper.delete(new LambdaQueryWrapper<PreTakeStockSheetDetail>()
                .eq(PreTakeStockSheetDetail::getSheetId, sheet.getId()));

        PreTakeStockSheet update = new PreTakeStockSheet();
        update.setId(sheet.getId());
        update.setScId(sheet.getScId());
        update.setTakeStatus(sheet.getTakeStatus());
        update.setDescription(sheet.getDescription());
        boolean success = updateById(update);

        saveDetails(sheet.getId(), details);
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteById(Long id) {
        if (id == null) {
            throw new BusinessException("缺少预先盘点单ID");
        }
        detailMapper.delete(new LambdaQueryWrapper<PreTakeStockSheetDetail>()
                .eq(PreTakeStockSheetDetail::getSheetId, id));
        return removeById(id);
    }

    // ==================== 内部方法 ====================

    private void validate(PreTakeStockSheet sheet, List<PreTakeStockSheetDetail> details) {
        if (sheet.getScId() == null) {
            throw new BusinessException("请选择仓库");
        }
        if (sheet.getTakeStatus() == null) {
            throw new BusinessException("请选择盘点轮次");
        }
        if (sheet.getTakeStatus() < 0 || sheet.getTakeStatus() > 2) {
            throw new BusinessException("盘点轮次取值不合法");
        }
        if (details == null || details.isEmpty()) {
            throw new BusinessException("请至少添加一条商品明细");
        }
        Set<Long> seen = new HashSet<>();
        for (PreTakeStockSheetDetail detail : details) {
            if (detail.getProductId() == null) {
                throw new BusinessException("明细中存在未选择商品的行");
            }
            if (!seen.add(detail.getProductId())) {
                throw new BusinessException("同一商品不能重复出现");
            }
            assertNotNegative(detail.getFirstNum(), "初盘数量");
            assertNotNegative(detail.getSecondNum(), "复盘数量");
            assertNotNegative(detail.getRandNum(), "抽盘数量");
        }
        // 商品必须存在（软删除的查不到，等于顺带校验了有效性）
        List<Long> ids = details.stream().map(PreTakeStockSheetDetail::getProductId).collect(Collectors.toList());
        if (materialMapper.selectBatchIds(ids).size() != ids.size()) {
            throw new BusinessException("明细中存在不存在的商品");
        }
    }

    private void assertNotNegative(java.math.BigDecimal value, String label) {
        if (value != null && value.compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new BusinessException(label + "不能为负数");
        }
    }

    private void saveDetails(Long sheetId, List<PreTakeStockSheetDetail> details) {
        if (details == null || details.isEmpty()) {
            return;
        }
        int orderNo = 0;
        for (PreTakeStockSheetDetail detail : details) {
            detail.setId(null);
            detail.setSheetId(sheetId);
            detail.setOrderNo(++orderNo);
            detailMapper.insert(detail);
        }
    }

    private List<PreTakeStockSheetDetail> loadDetails(Long sheetId) {
        List<PreTakeStockSheetDetail> details = detailMapper.selectList(
                new LambdaQueryWrapper<PreTakeStockSheetDetail>()
                        .eq(PreTakeStockSheetDetail::getSheetId, sheetId)
                        .orderByAsc(PreTakeStockSheetDetail::getOrderNo)
                        .orderByAsc(PreTakeStockSheetDetail::getId));
        fillProductInfo(details);
        return details;
    }

    private void fillScNames(List<PreTakeStockSheet> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        Set<Long> scIds = list.stream().map(PreTakeStockSheet::getScId)
                .filter(java.util.Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> scMap = scIds.isEmpty() ? Map.of()
                : warehouseMapper.selectBatchIds(scIds).stream()
                        .collect(Collectors.toMap(BaseWarehouse::getId, BaseWarehouse::getName));
        list.forEach(sheet -> sheet.setScName(scMap.get(sheet.getScId())));
    }

    private void fillProductInfo(List<PreTakeStockSheetDetail> details) {
        if (details == null || details.isEmpty()) {
            return;
        }
        Set<Long> productIds = details.stream().map(PreTakeStockSheetDetail::getProductId)
                .filter(java.util.Objects::nonNull).collect(Collectors.toSet());
        Map<Long, BaseMaterial> materialMap = productIds.isEmpty() ? Map.of()
                : materialMapper.selectBatchIds(productIds).stream()
                        .collect(Collectors.toMap(BaseMaterial::getId, Function.identity(), (a, b) -> a));
        for (PreTakeStockSheetDetail detail : details) {
            BaseMaterial material = materialMap.get(detail.getProductId());
            if (material != null) {
                detail.setProductCode(material.getCode());
                detail.setProductName(material.getName());
                detail.setSpecification(material.getSpecification());
                detail.setSalePropertyText(material.getSalePropertyText());
            }
        }
    }
}

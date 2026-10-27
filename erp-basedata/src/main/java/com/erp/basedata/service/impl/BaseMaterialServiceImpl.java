package com.erp.basedata.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.basedata.dto.MaterialPageParam;
import com.erp.basedata.entity.BaseMaterial;
import com.erp.basedata.entity.BaseMaterialCategory;
import com.erp.basedata.entity.BaseSpu;
import com.erp.basedata.entity.BaseUnit;
import com.erp.basedata.mapper.BaseMaterialMapper;
import com.erp.basedata.mapper.BaseMaterialCategoryMapper;
import com.erp.basedata.mapper.BaseSpuMapper;
import com.erp.basedata.mapper.BaseUnitMapper;
import com.erp.basedata.service.IBaseMaterialService;
import com.erp.basedata.vo.MaterialExcelVO;
import com.erp.common.core.vo.ExcelImportResult;
import com.erp.common.exception.BusinessException;
import com.erp.common.service.ICodeGenerateService;
import com.erp.common.util.ExcelUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 物料信息Service实现类
 *
 * @author ERP System
 * @since 2025-12-14
 */
@Service
@RequiredArgsConstructor
public class BaseMaterialServiceImpl extends ServiceImpl<BaseMaterialMapper, BaseMaterial>
        implements IBaseMaterialService {
    private final ICodeGenerateService codeGenerateService;

    private final BaseMaterialCategoryMapper categoryMapper;
    private final BaseUnitMapper unitMapper;
    private final BaseSpuMapper spuMapper;

    @Override
    public Page<BaseMaterial> getMaterialPage(MaterialPageParam param) {
        Page<BaseMaterial> page = new Page<>(param.getCurrent(), param.getSize());

        LambdaQueryWrapper<BaseMaterial> wrapper = buildQueryWrapper(param)
                .orderByDesc(BaseMaterial::getCreateTime);

        Page<BaseMaterial> result = this.page(page, wrapper);
        fillNames(result.getRecords());
        return result;
    }

    @Override
    public BaseMaterial getById(Long id) {
        return super.getById(id);
    }

    @Override
    public List<BaseMaterial> getByCategory(Long categoryId) {
        LambdaQueryWrapper<BaseMaterial> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BaseMaterial::getCategoryId, categoryId)
                .eq(BaseMaterial::getStatus, 1)
                .orderByAsc(BaseMaterial::getName);
        return this.list(wrapper);
    }

    @Override
    public List<BaseMaterial> getAll() {
        LambdaQueryWrapper<BaseMaterial> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BaseMaterial::getStatus, 1)
                .orderByAsc(BaseMaterial::getCode);
        return this.list(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveMaterial(BaseMaterial material) {
        // 1. 生成物料编号(如果未提供)
        if (StrUtil.isBlank(material.getCode())) {
            material.setCode(generateMaterialCode());
        }

        // 2. 校验编号唯一性
        checkCodeUnique(material.getCode(), null);

        // 3. 校验必填字段
        if (material.getCategoryId() == null) {
            throw new BusinessException("分类不能为空");
        }
        if (material.getUnitId() == null) {
            throw new BusinessException("计量单位不能为空");
        }

        // 4. 设置默认值
        if (material.getStatus() == null) {
            material.setStatus(1);
        }

        // 5. 保存物料
        return super.save(material);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateMaterial(BaseMaterial material) {
        // 1. 校验物料是否存在
        BaseMaterial existMaterial = this.getById(material.getId());
        if (existMaterial == null) {
            throw new BusinessException("物料不存在");
        }

        // 2. 校验编号唯一性
        if (StrUtil.isNotBlank(material.getCode())) {
            checkCodeUnique(material.getCode(), material.getId());
        }

        // 3. 更新物料
        return super.updateById(material);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteMaterial(Long id) {
        // 1. 校验物料是否存在
        BaseMaterial material = this.getById(id);
        if (material == null) {
            throw new BusinessException("物料不存在");
        }

        // 2. TODO: 检查是否有库存
        // 3. TODO: 检查是否有采购/销售单据

        // 4. 删除物料(逻辑删除)
        return super.removeById(id);
    }

    @Override
    public List<MaterialExcelVO> listForExport(MaterialPageParam param) {
        List<BaseMaterial> list = this.list(buildQueryWrapper(param).orderByAsc(BaseMaterial::getCode));
        fillNames(list);
        return list.stream().map(this::toExcelVO).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExcelImportResult importExcel(InputStream in) {
        List<MaterialExcelVO> rows = ExcelUtils.read(in, MaterialExcelVO.class);
        // 过滤掉 Excel 末尾常见的空白行（三个关键列都为空即视为空行）
        rows.removeIf(row -> row == null || (trim(row.getName()).isEmpty()
                && trim(row.getCode()).isEmpty() && trim(row.getCategoryName()).isEmpty()));

        ExcelImportResult result = new ExcelImportResult();
        if (rows.isEmpty()) {
            throw new BusinessException("导入文件中没有数据行，请先下载导入模板再填写");
        }
        result.setTotal(rows.size());

        // 分类 / 计量单位：名称 → ID。一次性加载，避免逐行查库
        Map<String, Long> categoryMap = categoryMapper.selectList(null).stream()
                .collect(Collectors.toMap(c -> trim(c.getName()), BaseMaterialCategory::getId, (a, b) -> a));
        Map<String, Long> unitMap = unitMapper.selectList(null).stream()
                .collect(Collectors.toMap(u -> trim(u.getName()), BaseUnit::getId, (a, b) -> a));
        // 库内已存在的名称与编码（含未启用记录），用于pass-1 快速判重
        Set<String> existNames = baseMapper.selectList(null).stream()
                .map(m -> trim(m.getName())).collect(Collectors.toSet());
        Set<String> existCodes = baseMapper.selectList(null).stream()
                .map(m -> trim(m.getCode())).collect(Collectors.toSet());

        // ---------- pass 1: 只校验、不落库，收集全部错误 ----------
        List<BaseMaterial> pending = new ArrayList<>();
        Set<String> fileNames = new HashSet<>();
        Set<String> fileCodes = new HashSet<>();
        for (int i = 0; i < rows.size(); i++) {
            int rowNum = i + 2; // Excel 第 1 行是表头，数据从第 2 行开始
            try {
                pending.add(convertRow(rows.get(i), categoryMap, unitMap,
                        existNames, existCodes, fileNames, fileCodes));
            } catch (Exception e) {
                result.addError(rowNum, e.getMessage());
            }
        }

        // 只要有一行不合法就整体不落库，避免出现「导入了一半」的中间状态
        if (!result.isAllSuccess()) {
            return result;
        }

        // ---------- pass 2: 此时已确认零错误，统一写入（本方法同一事务内，保证原子性） ----------
        for (BaseMaterial material : pending) {
            saveMaterial(material);
        }
        result.setSuccess(pending.size());
        return result;
    }

    /**
     * 列表页 / 导出共用的筛选条件构造
     *
     * @param param 查询参数
     * @return 不带排序的 wrapper（调用方自行补 orderBy）
     */
    private LambdaQueryWrapper<BaseMaterial> buildQueryWrapper(MaterialPageParam param) {
        LambdaQueryWrapper<BaseMaterial> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(param.getCategoryId() != null, BaseMaterial::getCategoryId, param.getCategoryId())
                .eq(param.getSpuId() != null, BaseMaterial::getSpuId, param.getSpuId())
                .like(StrUtil.isNotBlank(param.getName()), BaseMaterial::getName, param.getName())
                .like(StrUtil.isNotBlank(param.getCode()), BaseMaterial::getCode, param.getCode())
                .eq(param.getStatus() != null, BaseMaterial::getStatus, param.getStatus());
        return wrapper;
    }

    /**
     * 批量回填分类名称与计量单位名称（分页查询与导出共用）
     *
     * @param records 物料记录，为 null 或空时直接返回
     */
    private void fillNames(List<BaseMaterial> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        Set<Long> categoryIds = records.stream()
                .map(BaseMaterial::getCategoryId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        Set<Long> unitIds = records.stream()
                .map(BaseMaterial::getUnitId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        Set<Long> spuIds = records.stream()
                .map(BaseMaterial::getSpuId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, String> categoryMap = categoryIds.isEmpty() ? Map.of()
                : categoryMapper.selectBatchIds(categoryIds).stream()
                        .collect(Collectors.toMap(BaseMaterialCategory::getId, BaseMaterialCategory::getName));
        Map<Long, String> unitMap = unitIds.isEmpty() ? Map.of()
                : unitMapper.selectBatchIds(unitIds).stream()
                        .collect(Collectors.toMap(BaseUnit::getId, BaseUnit::getName));
        Map<Long, String> spuMap = spuIds.isEmpty() ? Map.of()
                : spuMapper.selectBatchIds(spuIds).stream()
                        .collect(Collectors.toMap(BaseSpu::getId, BaseSpu::getName));

        for (BaseMaterial material : records) {
            if (material.getCategoryId() != null) {
                material.setCategoryName(categoryMap.get(material.getCategoryId()));
            }
            if (material.getUnitId() != null) {
                material.setUnitName(unitMap.get(material.getUnitId()));
            }
            if (material.getSpuId() != null) {
                material.setSpuName(spuMap.get(material.getSpuId()));
            }
        }
    }

    /**
     * 实体 → Excel 行模型
     *
     * @param material 物料实体（分类 / 单位名称需已回填）
     * @return Excel 行数据
     */
    private MaterialExcelVO toExcelVO(BaseMaterial material) {
        MaterialExcelVO vo = new MaterialExcelVO();
        vo.setCode(material.getCode());
        vo.setName(material.getName());
        vo.setShortName(material.getShortName());
        vo.setSpecification(material.getSpecification());
        vo.setCategoryName(material.getCategoryName());
        vo.setUnitName(material.getUnitName());
        vo.setPurchasePrice(material.getPurchasePrice());
        vo.setSalePrice(material.getSalePrice());
        vo.setRetailPrice(material.getRetailPrice());
        vo.setMinStock(material.getMinStock());
        vo.setMaxStock(material.getMaxStock());
        vo.setStatusText(ExcelUtils.statusText(material.getStatus()));
        vo.setRemark(material.getRemark());
        return vo;
    }

    /**
     * Excel 行 → 物料实体（含全部业务校验，校验失败抛 BusinessException）
     *
     * @param row           Excel 行
     * @param categoryMap   分类名称 → ID
     * @param unitMap       计量单位名称 → ID
     * @param existNames    库内已存在的物料名称
     * @param existCodes    库内已存在的物料编码
     * @param fileNames     本次文件内已出现的名称（判断文件内重复）
     * @param fileCodes     本次文件内已出现的编码（判断文件内重复）
     * @return 待保存的物料实体
     */
    private BaseMaterial convertRow(MaterialExcelVO row, Map<String, Long> categoryMap,
                                    Map<String, Long> unitMap, Set<String> existNames,
                                    Set<String> existCodes, Set<String> fileNames, Set<String> fileCodes) {
        String name = trim(row.getName());
        if (name.isEmpty()) {
            throw new BusinessException("物料名称不能为空");
        }
        if (fileNames.contains(name)) {
            throw new BusinessException("物料名称在本文件中重复: " + name);
        }
        if (existNames.contains(name)) {
            throw new BusinessException("物料名称已存在: " + name);
        }

        String categoryName = trim(row.getCategoryName());
        if (categoryName.isEmpty()) {
            throw new BusinessException("分类不能为空");
        }
        Long categoryId = categoryMap.get(categoryName);
        if (categoryId == null) {
            throw new BusinessException("分类不存在，请先在物料分类中维护: " + categoryName);
        }

        String unitName = trim(row.getUnitName());
        if (unitName.isEmpty()) {
            throw new BusinessException("计量单位不能为空");
        }
        Long unitId = unitMap.get(unitName);
        if (unitId == null) {
            throw new BusinessException("计量单位不存在，请先在计量单位中维护: " + unitName);
        }

        BaseMaterial material = new BaseMaterial();
        material.setName(name);
        material.setShortName(row.getShortName());
        material.setSpecification(row.getSpecification());
        material.setCategoryId(categoryId);
        material.setUnitId(unitId);
        material.setPurchasePrice(row.getPurchasePrice());
        material.setSalePrice(row.getSalePrice());
        material.setRetailPrice(row.getRetailPrice());
        material.setMinStock(row.getMinStock());
        material.setMaxStock(row.getMaxStock());
        material.setStatus(ExcelUtils.parseStatus(row.getStatusText(), 1));
        material.setRemark(row.getRemark());

        // 编码留空则由编号规则自动生成（saveMaterial 内部处理），填了则要求全局唯一
        String code = trim(row.getCode());
        if (!code.isEmpty()) {
            if (fileCodes.contains(code)) {
                throw new BusinessException("物料编码在本文件中重复: " + code);
            }
            if (existCodes.contains(code)) {
                throw new BusinessException("物料编码已存在: " + code);
            }
            material.setCode(code);
        }

        // 全部校验通过，占位以便后面的行能识别出文件内重复
        fileNames.add(name);
        fileCodes.add(code);
        return material;
    }

    /**
     * null 安全的去空格
     */
    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }

    /**
     * 生成物料编号
     * 格式: MAT + 6位数字 (例如: MAT000001)
     *
     * @return 物料编号
     */
    private String generateMaterialCode() {
        return codeGenerateService.generate("BASE_MATERIAL");
    }

    /**
     * 校验物料编号唯一性
     * 注意: 需要查询所有记录(包括已删除的),避免编号冲突
     *
     * @param code       物料编号
     * @param materialId 物料ID(更新时传入)
     */
    private void checkCodeUnique(String code, Long materialId) {
        // 使用baseMapper直接查询,绕过@TableLogic的自动过滤
        LambdaQueryWrapper<BaseMaterial> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BaseMaterial::getCode, code);
        if (materialId != null) {
            wrapper.ne(BaseMaterial::getId, materialId);
        }
        // 查询所有记录(包括已删除的)
        Long count = baseMapper.selectCount(wrapper);
        if (count != null && count > 0) {
            throw new BusinessException("物料编号已存在(包括已删除的记录)");
        }
    }
}

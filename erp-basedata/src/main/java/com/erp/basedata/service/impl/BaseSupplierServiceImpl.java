package com.erp.basedata.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.basedata.dto.SupplierPageParam;
import com.erp.basedata.entity.BaseSupplier;
import com.erp.basedata.mapper.BaseSupplierMapper;
import com.erp.basedata.service.IBaseSupplierService;
import com.erp.basedata.vo.SupplierExcelVO;
import com.erp.common.core.vo.ExcelImportResult;
import com.erp.common.exception.BusinessException;
import com.erp.common.service.ICodeGenerateService;
import com.erp.common.util.ExcelUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 供应商信息Service实现类
 *
 * @author ERP System
 * @since 2025-12-16
 */
@Service
public class BaseSupplierServiceImpl extends ServiceImpl<BaseSupplierMapper, BaseSupplier>
        implements IBaseSupplierService {
    @Autowired
    private ICodeGenerateService codeGenerateService;

    @Override
    public Page<BaseSupplier> getSupplierPage(SupplierPageParam param) {
        Page<BaseSupplier> page = new Page<>(param.getCurrent(), param.getSize());

        LambdaQueryWrapper<BaseSupplier> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(param.getName()), BaseSupplier::getName, param.getName())
                .like(StrUtil.isNotBlank(param.getCode()), BaseSupplier::getCode, param.getCode())
                .eq(param.getType() != null, BaseSupplier::getType, param.getType())
                .eq(param.getStatus() != null, BaseSupplier::getStatus, param.getStatus())
                .orderByAsc(BaseSupplier::getSort)
                .orderByAsc(BaseSupplier::getId);

        return this.page(page, wrapper);
    }

    @Override
    public List<BaseSupplier> getAllEnabledSuppliers() {
        LambdaQueryWrapper<BaseSupplier> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BaseSupplier::getStatus, 1)
                .orderByAsc(BaseSupplier::getSort)
                .orderByAsc(BaseSupplier::getId);
        return this.list(wrapper);
    }

    @Override
    public BaseSupplier getById(Long id) {
        return super.getById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveSupplier(BaseSupplier supplier) {
        // 1. 生成供应商编号(如果未提供)
        if (StrUtil.isBlank(supplier.getCode())) {
            supplier.setCode(generateSupplierCode());
        }

        // 2. 校验编号唯一性
        checkCodeUnique(supplier.getCode(), null);

        // 3. 校验名称唯一性
        checkNameUnique(supplier.getName(), null);

        // 4. 设置默认值
        if (supplier.getSort() == null) {
            supplier.setSort(0);
        }
        if (supplier.getStatus() == null) {
            supplier.setStatus(1);
        }
        if (supplier.getType() == null) {
            supplier.setType(1);
        }
        if (supplier.getCreditLevel() == null) {
            supplier.setCreditLevel(3);
        }
        if (supplier.getSettlementMethod() == null) {
            supplier.setSettlementMethod(1);
        }
        if (supplier.getPaymentDays() == null) {
            supplier.setPaymentDays(0);
        }

        // 5. 保存供应商
        return super.save(supplier);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateSupplier(BaseSupplier supplier) {
        // 1. 校验供应商是否存在
        BaseSupplier existSupplier = this.getById(supplier.getId());
        if (existSupplier == null) {
            throw new BusinessException("供应商不存在");
        }

        // 2. 校验编号唯一性
        if (StrUtil.isNotBlank(supplier.getCode())) {
            checkCodeUnique(supplier.getCode(), supplier.getId());
        }

        // 3. 校验名称唯一性
        if (StrUtil.isNotBlank(supplier.getName())) {
            checkNameUnique(supplier.getName(), supplier.getId());
        }

        // 4. 更新供应商
        return super.updateById(supplier);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteSupplier(Long id) {
        // 1. 校验供应商是否存在
        BaseSupplier supplier = this.getById(id);
        if (supplier == null) {
            throw new BusinessException("供应商不存在");
        }

        // 2. TODO: 检查是否有采购单使用该供应商

        // 3. 删除供应商(逻辑删除)
        return super.removeById(id);
    }

    @Override
    public List<SupplierExcelVO> listForExport(SupplierPageParam param) {
        LambdaQueryWrapper<BaseSupplier> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(param.getName()), BaseSupplier::getName, param.getName())
                .like(StrUtil.isNotBlank(param.getCode()), BaseSupplier::getCode, param.getCode())
                .eq(param.getType() != null, BaseSupplier::getType, param.getType())
                .eq(param.getStatus() != null, BaseSupplier::getStatus, param.getStatus())
                .orderByAsc(BaseSupplier::getSort)
                .orderByAsc(BaseSupplier::getId);

        return this.list(wrapper).stream().map(this::toExcelVO).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExcelImportResult importExcel(InputStream in) {
        List<SupplierExcelVO> rows = ExcelUtils.read(in, SupplierExcelVO.class);
        rows.removeIf(row -> row == null || (trim(row.getName()).isEmpty() && trim(row.getCode()).isEmpty()));

        ExcelImportResult result = new ExcelImportResult();
        if (rows.isEmpty()) {
            throw new BusinessException("导入文件中没有数据行，请先下载导入模板再填写");
        }
        result.setTotal(rows.size());

        // 库内已存在的名称与编码，用于文件内 / 库内判重
        Set<String> existNames = this.list().stream()
                .map(s -> trim(s.getName())).collect(Collectors.toSet());
        Set<String> existCodes = baseMapper.selectList(null).stream()
                .map(s -> trim(s.getCode())).collect(Collectors.toSet());

        // pass 1: 只校验、不落库
        List<BaseSupplier> pending = new ArrayList<>();
        Set<String> fileNames = new HashSet<>();
        Set<String> fileCodes = new HashSet<>();
        for (int i = 0; i < rows.size(); i++) {
            int rowNum = i + 2; // 第 1 行是表头
            try {
                pending.add(convertRow(rows.get(i), existNames, existCodes, fileNames, fileCodes));
            } catch (Exception e) {
                result.addError(rowNum, e.getMessage());
            }
        }
        if (!result.isAllSuccess()) {
            return result;
        }

        // pass 2: 零错误，统一写入
        for (BaseSupplier supplier : pending) {
            saveSupplier(supplier);
        }
        result.setSuccess(pending.size());
        return result;
    }

    /**
     * 实体 → Excel 行模型
     */
    private SupplierExcelVO toExcelVO(BaseSupplier supplier) {
        SupplierExcelVO vo = new SupplierExcelVO();
        vo.setCode(supplier.getCode());
        vo.setName(supplier.getName());
        vo.setShortName(supplier.getShortName());
        vo.setType(supplier.getType());
        vo.setCreditLevel(supplier.getCreditLevel());
        vo.setContactPerson(supplier.getContactPerson());
        vo.setContactPhone(supplier.getContactPhone());
        vo.setContactEmail(supplier.getContactEmail());
        vo.setAddress(supplier.getAddress());
        vo.setSettlementMethod(supplier.getSettlementMethod());
        vo.setPaymentDays(supplier.getPaymentDays());
        vo.setBankName(supplier.getBankName());
        vo.setBankAccount(supplier.getBankAccount());
        vo.setTaxNumber(supplier.getTaxNumber());
        vo.setStatusText(ExcelUtils.statusText(supplier.getStatus()));
        vo.setRemark(supplier.getRemark());
        return vo;
    }

    /**
     * Excel 行 → 供应商实体（校验失败抛 BusinessException）
     */
    private BaseSupplier convertRow(SupplierExcelVO row, Set<String> existNames, Set<String> existCodes,
                                    Set<String> fileNames, Set<String> fileCodes) {
        String name = trim(row.getName());
        if (name.isEmpty()) {
            throw new BusinessException("供应商名称不能为空");
        }
        if (fileNames.contains(name)) {
            throw new BusinessException("供应商名称在本文件中重复: " + name);
        }
        if (existNames.contains(name)) {
            throw new BusinessException("供应商名称已存在: " + name);
        }

        BaseSupplier supplier = new BaseSupplier();
        supplier.setName(name);
        supplier.setShortName(row.getShortName());
        supplier.setType(row.getType());
        supplier.setCreditLevel(row.getCreditLevel());
        supplier.setContactPerson(row.getContactPerson());
        supplier.setContactPhone(row.getContactPhone());
        supplier.setContactEmail(row.getContactEmail());
        supplier.setAddress(row.getAddress());
        supplier.setSettlementMethod(row.getSettlementMethod());
        supplier.setPaymentDays(row.getPaymentDays());
        supplier.setBankName(row.getBankName());
        supplier.setBankAccount(row.getBankAccount());
        supplier.setTaxNumber(row.getTaxNumber());
        supplier.setStatus(ExcelUtils.parseStatus(row.getStatusText(), 1));
        supplier.setRemark(row.getRemark());

        String code = trim(row.getCode());
        if (!code.isEmpty()) {
            if (fileCodes.contains(code)) {
                throw new BusinessException("供应商编号在本文件中重复: " + code);
            }
            if (existCodes.contains(code)) {
                throw new BusinessException("供应商编号已存在: " + code);
            }
            supplier.setCode(code);
        }

        fileNames.add(name);
        fileCodes.add(code);
        return supplier;
    }

    /**
     * null 安全的去空格
     */
    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }

    /**
     * 生成供应商编号
     * 格式: SUP + 6位数字 (例如: SUP000001)
     *
     * @return 供应商编号
     */
    private String generateSupplierCode() {
        return codeGenerateService.generate("BASE_SUPPLIER");
    }

    /**
     * 校验供应商编号唯一性
     * 注意: 需要查询所有记录(包括已删除的),避免编号冲突
     *
     * @param code       供应商编号
     * @param supplierId 供应商ID(更新时传入)
     */
    private void checkCodeUnique(String code, Long supplierId) {
        // 使用baseMapper直接查询,绕过@TableLogic的自动过滤
        LambdaQueryWrapper<BaseSupplier> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BaseSupplier::getCode, code);
        if (supplierId != null) {
            wrapper.ne(BaseSupplier::getId, supplierId);
        }
        // 查询所有记录(包括已删除的)
        Long count = baseMapper.selectCount(wrapper);
        if (count != null && count > 0) {
            throw new BusinessException("供应商编号已存在(包括已删除的记录)");
        }
    }

    /**
     * 校验供应商名称唯一性
     *
     * @param name       供应商名称
     * @param supplierId 供应商ID(更新时传入)
     */
    private void checkNameUnique(String name, Long supplierId) {
        LambdaQueryWrapper<BaseSupplier> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BaseSupplier::getName, name);
        if (supplierId != null) {
            wrapper.ne(BaseSupplier::getId, supplierId);
        }
        long count = this.count(wrapper);
        if (count > 0) {
            throw new BusinessException("供应商名称已存在");
        }
    }
}

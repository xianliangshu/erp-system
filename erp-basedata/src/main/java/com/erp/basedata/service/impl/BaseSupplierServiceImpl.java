package com.erp.basedata.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.basedata.dto.SupplierPageParam;
import com.erp.basedata.entity.BaseSupplier;
import com.erp.basedata.mapper.BaseSupplierMapper;
import com.erp.basedata.service.IBaseSupplierService;
import com.erp.common.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 供应商信息Service实现类
 *
 * @author ERP System
 * @since 2025-12-16
 */
@Service
public class BaseSupplierServiceImpl extends ServiceImpl<BaseSupplierMapper, BaseSupplier>
        implements IBaseSupplierService {

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

    /**
     * 生成供应商编号
     * 格式: SUP + 6位数字 (例如: SUP000001)
     *
     * @return 供应商编号
     */
    private String generateSupplierCode() {
        // 查询最大的供应商编号
        LambdaQueryWrapper<BaseSupplier> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(BaseSupplier::getCode).last("LIMIT 1");
        BaseSupplier lastSupplier = this.getOne(wrapper);

        if (lastSupplier == null || StrUtil.isBlank(lastSupplier.getCode())) {
            // 如果没有供应商,从SUP000001开始
            return "SUP000001";
        }

        // 提取数字部分并加1
        String lastCode = lastSupplier.getCode();
        String numberPart = lastCode.substring(3); // 去掉前缀SUP
        int nextNumber = Integer.parseInt(numberPart) + 1;

        // 格式化为6位数字
        return String.format("SUP%06d", nextNumber);
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

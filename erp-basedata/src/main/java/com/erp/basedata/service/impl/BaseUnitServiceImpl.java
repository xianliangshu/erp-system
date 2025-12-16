package com.erp.basedata.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.basedata.dto.UnitPageParam;
import com.erp.basedata.entity.BaseMaterial;
import com.erp.basedata.entity.BaseUnit;
import com.erp.basedata.mapper.BaseMaterialMapper;
import com.erp.basedata.mapper.BaseUnitMapper;
import com.erp.basedata.service.IBaseUnitService;
import com.erp.common.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 计量单位Service实现类
 *
 * @author ERP System
 * @since 2025-12-14
 */
@Service
public class BaseUnitServiceImpl extends ServiceImpl<BaseUnitMapper, BaseUnit>
        implements IBaseUnitService {

    private final BaseMaterialMapper materialMapper;

    public BaseUnitServiceImpl(BaseMaterialMapper materialMapper) {
        this.materialMapper = materialMapper;
    }

    @Override
    public Page<BaseUnit> getUnitPage(UnitPageParam param) {
        Page<BaseUnit> page = new Page<>(param.getCurrent(), param.getSize());

        LambdaQueryWrapper<BaseUnit> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(param.getName()), BaseUnit::getName, param.getName())
                .eq(param.getStatus() != null, BaseUnit::getStatus, param.getStatus())
                .orderByAsc(BaseUnit::getSort)
                .orderByAsc(BaseUnit::getId);

        return this.page(page, wrapper);
    }

    @Override
    public List<BaseUnit> getAllEnabledUnits() {
        LambdaQueryWrapper<BaseUnit> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BaseUnit::getStatus, 1)
                .orderByAsc(BaseUnit::getSort)
                .orderByAsc(BaseUnit::getId);
        return this.list(wrapper);
    }

    @Override
    public BaseUnit getById(Long id) {
        return super.getById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveUnit(BaseUnit unit) {
        // 1. 生成单位编号(如果未提供)
        if (StrUtil.isBlank(unit.getCode())) {
            unit.setCode(generateUnitCode());
        }

        // 2. 校验编号唯一性
        checkCodeUnique(unit.getCode(), null);

        // 3. 校验名称唯一性
        checkNameUnique(unit.getName(), null);

        // 4. 设置默认值
        if (unit.getSort() == null) {
            unit.setSort(0);
        }
        if (unit.getStatus() == null) {
            unit.setStatus(1);
        }

        // 5. 保存单位
        return super.save(unit);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUnit(BaseUnit unit) {
        // 1. 校验单位是否存在
        BaseUnit existUnit = this.getById(unit.getId());
        if (existUnit == null) {
            throw new BusinessException("单位不存在");
        }

        // 2. 校验编号唯一性
        if (StrUtil.isNotBlank(unit.getCode())) {
            checkCodeUnique(unit.getCode(), unit.getId());
        }

        // 3. 校验名称唯一性
        if (StrUtil.isNotBlank(unit.getName())) {
            checkNameUnique(unit.getName(), unit.getId());
        }

        // 4. 更新单位
        return super.updateById(unit);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUnit(Long id) {
        // 1. 校验单位是否存在
        BaseUnit unit = this.getById(id);
        if (unit == null) {
            throw new BusinessException("单位不存在");
        }

        // 2. 检查是否有物料使用该单位
        LambdaQueryWrapper<BaseMaterial> materialWrapper = new LambdaQueryWrapper<>();
        materialWrapper.eq(BaseMaterial::getUnitId, id);
        long materialCount = materialMapper.selectCount(materialWrapper);
        if (materialCount > 0) {
            throw new BusinessException("该单位下有物料在使用,不能删除");
        }

        // 3. 删除单位(逻辑删除)
        return super.removeById(id);
    }

    /**
     * 生成单位编号
     * 格式: UNIT + 6位数字 (例如: UNIT000001)
     *
     * @return 单位编号
     */
    private String generateUnitCode() {
        // 查询最大的单位编号
        LambdaQueryWrapper<BaseUnit> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(BaseUnit::getCode).last("LIMIT 1");
        BaseUnit lastUnit = this.getOne(wrapper);

        if (lastUnit == null || StrUtil.isBlank(lastUnit.getCode())) {
            // 如果没有单位,从UNIT000001开始
            return "UNIT000001";
        }

        // 提取数字部分并加1
        String lastCode = lastUnit.getCode();
        String numberPart = lastCode.substring(4); // 去掉前缀UNIT
        int nextNumber = Integer.parseInt(numberPart) + 1;

        // 格式化为6位数字
        return String.format("UNIT%06d", nextNumber);
    }

    /**
     * 校验单位编号唯一性
     * 注意: 需要查询所有记录(包括已删除的),避免编号冲突
     *
     * @param code   单位编号
     * @param unitId 单位ID(更新时传入)
     */
    private void checkCodeUnique(String code, Long unitId) {
        // 使用baseMapper直接查询,绕过@TableLogic的自动过滤
        LambdaQueryWrapper<BaseUnit> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BaseUnit::getCode, code);
        if (unitId != null) {
            wrapper.ne(BaseUnit::getId, unitId);
        }
        // 查询所有记录(包括已删除的)
        Long count = baseMapper.selectCount(wrapper);
        if (count != null && count > 0) {
            throw new BusinessException("单位编号已存在(包括已删除的记录)");
        }
    }

    /**
     * 校验单位名称唯一性
     *
     * @param name   单位名称
     * @param unitId 单位ID(更新时传入)
     */
    private void checkNameUnique(String name, Long unitId) {
        LambdaQueryWrapper<BaseUnit> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BaseUnit::getName, name);
        if (unitId != null) {
            wrapper.ne(BaseUnit::getId, unitId);
        }
        long count = this.count(wrapper);
        if (count > 0) {
            throw new BusinessException("单位名称已存在");
        }
    }
}

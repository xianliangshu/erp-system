package com.erp.basedata.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.basedata.dto.MaterialPageParam;
import com.erp.basedata.entity.BaseMaterial;
import com.erp.basedata.mapper.BaseMaterialMapper;
import com.erp.basedata.service.IBaseMaterialService;
import com.erp.common.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 物料信息Service实现类
 *
 * @author ERP System
 * @since 2025-12-14
 */
@Service
public class BaseMaterialServiceImpl extends ServiceImpl<BaseMaterialMapper, BaseMaterial>
        implements IBaseMaterialService {

    @Override
    public Page<BaseMaterial> getMaterialPage(MaterialPageParam param) {
        Page<BaseMaterial> page = new Page<>(param.getCurrent(), param.getSize());

        LambdaQueryWrapper<BaseMaterial> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(param.getCategoryId() != null, BaseMaterial::getCategoryId, param.getCategoryId())
                .like(StrUtil.isNotBlank(param.getName()), BaseMaterial::getName, param.getName())
                .like(StrUtil.isNotBlank(param.getCode()), BaseMaterial::getCode, param.getCode())
                .eq(param.getStatus() != null, BaseMaterial::getStatus, param.getStatus())
                .orderByDesc(BaseMaterial::getCreateTime);

        return this.page(page, wrapper);
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

    /**
     * 生成物料编号
     * 格式: MAT + 6位数字 (例如: MAT000001)
     *
     * @return 物料编号
     */
    private String generateMaterialCode() {
        // 查询最大的物料编号
        LambdaQueryWrapper<BaseMaterial> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(BaseMaterial::getCode).last("LIMIT 1");
        BaseMaterial lastMaterial = this.getOne(wrapper);

        if (lastMaterial == null || StrUtil.isBlank(lastMaterial.getCode())) {
            // 如果没有物料,从MAT000001开始
            return "MAT000001";
        }

        // 提取数字部分并加1
        String lastCode = lastMaterial.getCode();
        String numberPart = lastCode.substring(3); // 去掉前缀MAT
        int nextNumber = Integer.parseInt(numberPart) + 1;

        // 格式化为6位数字
        return String.format("MAT%06d", nextNumber);
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

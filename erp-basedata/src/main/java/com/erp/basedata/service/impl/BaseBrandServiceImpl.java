package com.erp.basedata.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.basedata.dto.BrandPageParam;
import com.erp.basedata.entity.BaseBrand;
import com.erp.basedata.mapper.BaseBrandMapper;
import com.erp.basedata.service.IBaseBrandService;
import com.erp.common.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 品牌信息Service实现类
 *
 * @author ERP System
 * @since 2025-12-21
 */
@Service
public class BaseBrandServiceImpl extends ServiceImpl<BaseBrandMapper, BaseBrand>
        implements IBaseBrandService {

    @Override
    public Page<BaseBrand> getBrandPage(BrandPageParam param) {
        Page<BaseBrand> page = new Page<>(param.getCurrent(), param.getSize());

        LambdaQueryWrapper<BaseBrand> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(param.getName()), BaseBrand::getName, param.getName())
                .like(StrUtil.isNotBlank(param.getCode()), BaseBrand::getCode, param.getCode())
                .eq(param.getStatus() != null, BaseBrand::getStatus, param.getStatus())
                .orderByAsc(BaseBrand::getSort)
                .orderByAsc(BaseBrand::getId);

        return this.page(page, wrapper);
    }

    @Override
    public List<BaseBrand> getAllEnabledBrands() {
        LambdaQueryWrapper<BaseBrand> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BaseBrand::getStatus, 1)
                .orderByAsc(BaseBrand::getSort)
                .orderByAsc(BaseBrand::getId);
        return this.list(wrapper);
    }

    @Override
    public BaseBrand getById(Long id) {
        return super.getById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveBrand(BaseBrand brand) {
        // 1. 生成品牌编号(如果未提供)
        if (StrUtil.isBlank(brand.getCode())) {
            brand.setCode(generateBrandCode());
        }

        // 2. 校验编号唯一性
        checkCodeUnique(brand.getCode(), null);

        // 3. 校验名称唯一性
        checkNameUnique(brand.getName(), null);

        // 4. 设置默认值
        if (brand.getSort() == null) {
            brand.setSort(0);
        }
        if (brand.getStatus() == null) {
            brand.setStatus(1);
        }

        // 5. 保存品牌
        return super.save(brand);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateBrand(BaseBrand brand) {
        // 1. 校验品牌是否存在
        BaseBrand existBrand = this.getById(brand.getId());
        if (existBrand == null) {
            throw new BusinessException("品牌不存在");
        }

        // 2. 校验编号唯一性
        if (StrUtil.isNotBlank(brand.getCode())) {
            checkCodeUnique(brand.getCode(), brand.getId());
        }

        // 3. 校验名称唯一性
        if (StrUtil.isNotBlank(brand.getName())) {
            checkNameUnique(brand.getName(), brand.getId());
        }

        // 4. 更新品牌
        return super.updateById(brand);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteBrand(Long id) {
        // 1. 校验品牌是否存在
        BaseBrand brand = this.getById(id);
        if (brand == null) {
            throw new BusinessException("品牌不存在");
        }

        // 2. TODO: 检查是否有商品使用该品牌

        // 3. 删除品牌(逻辑删除)
        return super.removeById(id);
    }

    /**
     * 生成品牌编号
     * 格式: BRD + 6位数字 (例如: BRD000001)
     *
     * @return 品牌编号
     */
    private String generateBrandCode() {
        // 查询最大的品牌编号
        LambdaQueryWrapper<BaseBrand> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(BaseBrand::getCode).last("LIMIT 1");
        BaseBrand lastBrand = this.getOne(wrapper);

        if (lastBrand == null || StrUtil.isBlank(lastBrand.getCode())) {
            // 如果没有品牌,从BRD000001开始
            return "BRD000001";
        }

        // 提取数字部分并加1
        String lastCode = lastBrand.getCode();
        String numberPart = lastCode.substring(3); // 去掉前缀BRD
        int nextNumber = Integer.parseInt(numberPart) + 1;

        // 格式化为6位数字
        return String.format("BRD%06d", nextNumber);
    }

    /**
     * 校验品牌编号唯一性
     * 注意: 需要查询所有记录(包括已删除的),避免编号冲突
     *
     * @param code    品牌编号
     * @param brandId 品牌ID(更新时传入)
     */
    private void checkCodeUnique(String code, Long brandId) {
        // 使用baseMapper直接查询,绕过@TableLogic的自动过滤
        LambdaQueryWrapper<BaseBrand> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BaseBrand::getCode, code);
        if (brandId != null) {
            wrapper.ne(BaseBrand::getId, brandId);
        }
        // 查询所有记录(包括已删除的)
        Long count = baseMapper.selectCount(wrapper);
        if (count != null && count > 0) {
            throw new BusinessException("品牌编号已存在(包括已删除的记录)");
        }
    }

    /**
     * 校验品牌名称唯一性
     *
     * @param name    品牌名称
     * @param brandId 品牌ID(更新时传入)
     */
    private void checkNameUnique(String name, Long brandId) {
        LambdaQueryWrapper<BaseBrand> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BaseBrand::getName, name);
        if (brandId != null) {
            wrapper.ne(BaseBrand::getId, brandId);
        }
        long count = this.count(wrapper);
        if (count > 0) {
            throw new BusinessException("品牌名称已存在");
        }
    }
}

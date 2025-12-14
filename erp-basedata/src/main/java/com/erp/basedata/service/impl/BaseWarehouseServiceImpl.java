package com.erp.basedata.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.common.exception.BusinessException;
import com.erp.basedata.dto.WarehousePageParam;
import com.erp.basedata.entity.BaseWarehouse;
import com.erp.basedata.mapper.BaseWarehouseMapper;
import com.erp.basedata.service.IBaseWarehouseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 仓库Service实现类
 * 
 * @author ERP System
 * @since 2025-12-14
 */
@Service
public class BaseWarehouseServiceImpl extends ServiceImpl<BaseWarehouseMapper, BaseWarehouse>
        implements IBaseWarehouseService {

    @Override
    public Page<BaseWarehouse> getWarehousePage(WarehousePageParam param) {
        Page<BaseWarehouse> page = new Page<>(param.getCurrent(), param.getSize());

        LambdaQueryWrapper<BaseWarehouse> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(param.getName()), BaseWarehouse::getName, param.getName())
                .eq(param.getStatus() != null, BaseWarehouse::getStatus, param.getStatus())
                .orderByDesc(BaseWarehouse::getCreateTime);

        return this.page(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveWarehouse(BaseWarehouse warehouse) {
        // 1. 生成仓库编号(如果未提供)
        if (StrUtil.isBlank(warehouse.getCode())) {
            warehouse.setCode(generateWarehouseCode());
        }

        // 2. 校验仓库编号唯一性
        checkCodeUnique(warehouse.getCode(), null);

        // 3. 如果设置为默认仓库,取消其他仓库的默认状态
        if (warehouse.getIsDefault() != null && warehouse.getIsDefault() == 1) {
            clearDefaultWarehouse();
        }

        // 4. 设置默认值
        if (warehouse.getStatus() == null) {
            warehouse.setStatus(1); // 默认启用
        }
        if (warehouse.getIsDefault() == null) {
            warehouse.setIsDefault(0); // 默认非默认仓库
        }

        // 5. 保存仓库
        return super.save(warehouse);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateWarehouse(BaseWarehouse warehouse) {
        // 1. 校验仓库是否存在
        BaseWarehouse existWarehouse = this.getById(warehouse.getId());
        if (existWarehouse == null) {
            throw new BusinessException("仓库不存在");
        }

        // 2. 校验仓库编号唯一性
        checkCodeUnique(warehouse.getCode(), warehouse.getId());

        // 3. 如果设置为默认仓库,取消其他仓库的默认状态
        if (warehouse.getIsDefault() != null && warehouse.getIsDefault() == 1) {
            clearDefaultWarehouse();
        }

        // 4. 更新仓库
        return super.updateById(warehouse);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteWarehouse(Long id) {
        // 1. 校验仓库是否存在
        BaseWarehouse warehouse = this.getById(id);
        if (warehouse == null) {
            throw new BusinessException("仓库不存在");
        }

        // 2. 不允许删除默认仓库
        if (warehouse.getIsDefault() == 1) {
            throw new BusinessException("不允许删除默认仓库");
        }

        // 3. TODO: 校验仓库是否有库存数据

        // 4. 删除仓库(逻辑删除)
        return super.removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean setDefaultWarehouse(Long id) {
        // 1. 校验仓库是否存在
        BaseWarehouse warehouse = this.getById(id);
        if (warehouse == null) {
            throw new BusinessException("仓库不存在");
        }

        // 2. 取消其他仓库的默认状态
        clearDefaultWarehouse();

        // 3. 设置当前仓库为默认
        warehouse.setIsDefault(1);
        return super.updateById(warehouse);
    }

    @Override
    public List<BaseWarehouse> getAllEnabledWarehouses() {
        LambdaQueryWrapper<BaseWarehouse> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BaseWarehouse::getStatus, 1)
                .orderByDesc(BaseWarehouse::getIsDefault)
                .orderByAsc(BaseWarehouse::getName);
        return this.list(wrapper);
    }

    /**
     * 校验仓库编号唯一性
     * 注意: 需要查询所有记录(包括已删除的),避免编号冲突
     * 
     * @param code 仓库编号
     * @param id   仓库ID(更新时传入)
     */
    private void checkCodeUnique(String code, Long id) {
        // 使用baseMapper直接查询,绕过@TableLogic的自动过滤
        LambdaQueryWrapper<BaseWarehouse> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BaseWarehouse::getCode, code);
        if (id != null) {
            wrapper.ne(BaseWarehouse::getId, id);
        }
        // 查询所有记录(包括已删除的)
        Long count = baseMapper.selectCount(wrapper);
        if (count != null && count > 0) {
            throw new BusinessException("仓库编号"+code+"已存在(包括已删除的记录)");
        }
    }

    /**
     * 取消所有仓库的默认状态
     */
    private void clearDefaultWarehouse() {
        LambdaUpdateWrapper<BaseWarehouse> wrapper = new LambdaUpdateWrapper<>();
        wrapper.set(BaseWarehouse::getIsDefault, 0)
                .eq(BaseWarehouse::getIsDefault, 1);
        this.update(wrapper);
    }

    /**
     * 生成仓库编号
     * 格式: WH + 6位数字 (例如: WH000001)
     * 
     * @return 仓库编号
     */
    private String generateWarehouseCode() {
        // 查询最大的仓库编号
        LambdaQueryWrapper<BaseWarehouse> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(BaseWarehouse::getCode).last("LIMIT 1");
        BaseWarehouse lastWarehouse = this.getOne(wrapper);

        if (lastWarehouse == null || StrUtil.isBlank(lastWarehouse.getCode())) {
            // 如果没有仓库,从WH000001开始
            return "WH000001";
        }

        // 提取数字部分并加1
        String lastCode = lastWarehouse.getCode();
        String numberPart = lastCode.substring(2); // 去掉前缀WH
        int nextNumber = Integer.parseInt(numberPart) + 1;

        // 格式化为6位数字
        return String.format("WH%06d", nextNumber);
    }
}

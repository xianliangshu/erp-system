package com.erp.basedata.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.basedata.dto.CustomerPageParam;
import com.erp.basedata.entity.BaseCustomer;
import com.erp.basedata.mapper.BaseCustomerMapper;
import com.erp.basedata.service.IBaseCustomerService;
import com.erp.common.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 客户信息Service实现类
 *
 * @author ERP System
 * @since 2025-12-21
 */
@Service
public class BaseCustomerServiceImpl extends ServiceImpl<BaseCustomerMapper, BaseCustomer>
        implements IBaseCustomerService {

    @Override
    public Page<BaseCustomer> getCustomerPage(CustomerPageParam param) {
        Page<BaseCustomer> page = new Page<>(param.getCurrent(), param.getSize());

        LambdaQueryWrapper<BaseCustomer> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(param.getName()), BaseCustomer::getName, param.getName())
                .like(StrUtil.isNotBlank(param.getCode()), BaseCustomer::getCode, param.getCode())
                .eq(param.getType() != null, BaseCustomer::getType, param.getType())
                .eq(param.getLevel() != null, BaseCustomer::getLevel, param.getLevel())
                .eq(param.getStatus() != null, BaseCustomer::getStatus, param.getStatus())
                .orderByAsc(BaseCustomer::getSort)
                .orderByAsc(BaseCustomer::getId);

        return this.page(page, wrapper);
    }

    @Override
    public List<BaseCustomer> getAllEnabledCustomers() {
        LambdaQueryWrapper<BaseCustomer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BaseCustomer::getStatus, 1)
                .orderByAsc(BaseCustomer::getSort)
                .orderByAsc(BaseCustomer::getId);
        return this.list(wrapper);
    }

    @Override
    public BaseCustomer getById(Long id) {
        return super.getById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveCustomer(BaseCustomer customer) {
        // 1. 生成客户编号(如果未提供)
        if (StrUtil.isBlank(customer.getCode())) {
            customer.setCode(generateCustomerCode());
        }

        // 2. 校验编号唯一性
        checkCodeUnique(customer.getCode(), null);

        // 3. 校验名称唯一性
        checkNameUnique(customer.getName(), null);

        // 4. 设置默认值
        if (customer.getSort() == null) {
            customer.setSort(0);
        }
        if (customer.getStatus() == null) {
            customer.setStatus(1);
        }
        if (customer.getType() == null) {
            customer.setType(1);
        }
        if (customer.getLevel() == null) {
            customer.setLevel(3);
        }
        if (customer.getSettlementMethod() == null) {
            customer.setSettlementMethod(1);
        }
        if (customer.getPaymentDays() == null) {
            customer.setPaymentDays(0);
        }

        // 5. 保存客户
        return super.save(customer);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateCustomer(BaseCustomer customer) {
        // 1. 校验客户是否存在
        BaseCustomer existCustomer = this.getById(customer.getId());
        if (existCustomer == null) {
            throw new BusinessException("客户不存在");
        }

        // 2. 校验编号唯一性
        if (StrUtil.isNotBlank(customer.getCode())) {
            checkCodeUnique(customer.getCode(), customer.getId());
        }

        // 3. 校验名称唯一性
        if (StrUtil.isNotBlank(customer.getName())) {
            checkNameUnique(customer.getName(), customer.getId());
        }

        // 4. 更新客户
        return super.updateById(customer);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteCustomer(Long id) {
        // 1. 校验客户是否存在
        BaseCustomer customer = this.getById(id);
        if (customer == null) {
            throw new BusinessException("客户不存在");
        }

        // 2. TODO: 检查是否有销售单使用该客户

        // 3. 删除客户(逻辑删除)
        return super.removeById(id);
    }

    /**
     * 生成客户编号
     * 格式: CUS + 6位数字 (例如: CUS000001)
     *
     * @return 客户编号
     */
    private String generateCustomerCode() {
        // 使用自定义SQL查询最大编号（绕过软删除过滤）
        String maxCode = baseMapper.selectMaxCode();

        if (maxCode == null || StrUtil.isBlank(maxCode)) {
            // 如果没有客户,从CUS000001开始
            return "CUS000001";
        }

        // 提取数字部分并加1
        String numberPart = maxCode.substring(3); // 去掉前缀CUS
        int nextNumber = Integer.parseInt(numberPart) + 1;

        // 格式化为6位数字
        return String.format("CUS%06d", nextNumber);
    }

    /**
     * 校验客户编号唯一性
     * 注意: 需要查询所有记录(包括已删除的),避免编号冲突
     *
     * @param code       客户编号
     * @param customerId 客户ID(更新时传入)
     */
    private void checkCodeUnique(String code, Long customerId) {
        // 使用baseMapper直接查询,绕过@TableLogic的自动过滤
        LambdaQueryWrapper<BaseCustomer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BaseCustomer::getCode, code);
        if (customerId != null) {
            wrapper.ne(BaseCustomer::getId, customerId);
        }
        // 查询所有记录(包括已删除的)
        Long count = baseMapper.selectCount(wrapper);
        if (count != null && count > 0) {
            throw new BusinessException("客户编号已存在(包括已删除的记录)");
        }
    }

    /**
     * 校验客户名称唯一性
     *
     * @param name       客户名称
     * @param customerId 客户ID(更新时传入)
     */
    private void checkNameUnique(String name, Long customerId) {
        LambdaQueryWrapper<BaseCustomer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BaseCustomer::getName, name);
        if (customerId != null) {
            wrapper.ne(BaseCustomer::getId, customerId);
        }
        long count = this.count(wrapper);
        if (count > 0) {
            throw new BusinessException("客户名称已存在");
        }
    }
}

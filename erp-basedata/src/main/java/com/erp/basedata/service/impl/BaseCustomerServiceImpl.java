package com.erp.basedata.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.basedata.dto.CustomerPageParam;
import com.erp.basedata.entity.BaseCustomer;
import com.erp.basedata.mapper.BaseCustomerMapper;
import com.erp.basedata.service.IBaseCustomerService;
import com.erp.basedata.vo.CustomerExcelVO;
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
 * 客户信息Service实现类
 *
 * @author ERP System
 * @since 2025-12-21
 */
@Service
public class BaseCustomerServiceImpl extends ServiceImpl<BaseCustomerMapper, BaseCustomer>
        implements IBaseCustomerService {
    @Autowired
    private ICodeGenerateService codeGenerateService;

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

    @Override
    public List<CustomerExcelVO> listForExport(CustomerPageParam param) {
        LambdaQueryWrapper<BaseCustomer> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(param.getName()), BaseCustomer::getName, param.getName())
                .like(StrUtil.isNotBlank(param.getCode()), BaseCustomer::getCode, param.getCode())
                .eq(param.getType() != null, BaseCustomer::getType, param.getType())
                .eq(param.getLevel() != null, BaseCustomer::getLevel, param.getLevel())
                .eq(param.getStatus() != null, BaseCustomer::getStatus, param.getStatus())
                .orderByAsc(BaseCustomer::getSort)
                .orderByAsc(BaseCustomer::getId);

        return this.list(wrapper).stream().map(this::toExcelVO).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExcelImportResult importExcel(InputStream in) {
        List<CustomerExcelVO> rows = ExcelUtils.read(in, CustomerExcelVO.class);
        rows.removeIf(row -> row == null || (trim(row.getName()).isEmpty() && trim(row.getCode()).isEmpty()));

        ExcelImportResult result = new ExcelImportResult();
        if (rows.isEmpty()) {
            throw new BusinessException("导入文件中没有数据行，请先下载导入模板再填写");
        }
        result.setTotal(rows.size());

        // 库内已存在的名称与编码，用于文件内 / 库内判重
        Set<String> existNames = this.list().stream()
                .map(c -> trim(c.getName())).collect(Collectors.toSet());
        Set<String> existCodes = baseMapper.selectList(null).stream()
                .map(c -> trim(c.getCode())).collect(Collectors.toSet());

        // pass 1: 只校验、不落库
        List<BaseCustomer> pending = new ArrayList<>();
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
        for (BaseCustomer customer : pending) {
            saveCustomer(customer);
        }
        result.setSuccess(pending.size());
        return result;
    }

    /**
     * 实体 → Excel 行模型
     */
    private CustomerExcelVO toExcelVO(BaseCustomer customer) {
        CustomerExcelVO vo = new CustomerExcelVO();
        vo.setCode(customer.getCode());
        vo.setName(customer.getName());
        vo.setShortName(customer.getShortName());
        vo.setType(customer.getType());
        vo.setLevel(customer.getLevel());
        vo.setContactPerson(customer.getContactPerson());
        vo.setContactPhone(customer.getContactPhone());
        vo.setContactEmail(customer.getContactEmail());
        vo.setAddress(customer.getAddress());
        vo.setSettlementMethod(customer.getSettlementMethod());
        vo.setPaymentDays(customer.getPaymentDays());
        vo.setStatusText(ExcelUtils.statusText(customer.getStatus()));
        vo.setRemark(customer.getRemark());
        return vo;
    }

    /**
     * Excel 行 → 客户实体（校验失败抛 BusinessException）
     */
    private BaseCustomer convertRow(CustomerExcelVO row, Set<String> existNames, Set<String> existCodes,
                                    Set<String> fileNames, Set<String> fileCodes) {
        String name = trim(row.getName());
        if (name.isEmpty()) {
            throw new BusinessException("客户名称不能为空");
        }
        if (fileNames.contains(name)) {
            throw new BusinessException("客户名称在本文件中重复: " + name);
        }
        if (existNames.contains(name)) {
            throw new BusinessException("客户名称已存在: " + name);
        }

        BaseCustomer customer = new BaseCustomer();
        customer.setName(name);
        customer.setShortName(row.getShortName());
        customer.setType(row.getType());
        customer.setLevel(row.getLevel());
        customer.setContactPerson(row.getContactPerson());
        customer.setContactPhone(row.getContactPhone());
        customer.setContactEmail(row.getContactEmail());
        customer.setAddress(row.getAddress());
        customer.setSettlementMethod(row.getSettlementMethod());
        customer.setPaymentDays(row.getPaymentDays());
        customer.setStatus(ExcelUtils.parseStatus(row.getStatusText(), 1));
        customer.setRemark(row.getRemark());

        String code = trim(row.getCode());
        if (!code.isEmpty()) {
            if (fileCodes.contains(code)) {
                throw new BusinessException("客户编号在本文件中重复: " + code);
            }
            if (existCodes.contains(code)) {
                throw new BusinessException("客户编号已存在: " + code);
            }
            customer.setCode(code);
        }

        fileNames.add(name);
        fileCodes.add(code);
        return customer;
    }

    /**
     * null 安全的去空格
     */
    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }

    /**
     * 生成客户编号
     * 格式: CUS + 6位数字 (例如: CUS000001)
     *
     * @return 客户编号
     */
    private String generateCustomerCode() {
        return codeGenerateService.generate("BASE_CUSTOMER");
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

package com.erp.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.business.entity.StockAdjustReason;
import com.erp.business.mapper.StockAdjustReasonMapper;
import com.erp.business.service.IStockAdjustReasonService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 库存调整原因 Service 实现
 */
@Service
public class StockAdjustReasonServiceImpl extends ServiceImpl<StockAdjustReasonMapper, StockAdjustReason>
        implements IStockAdjustReasonService {

    @Override
    public IPage<StockAdjustReason> page(IPage<StockAdjustReason> page, String code, String name, Integer status) {
        LambdaQueryWrapper<StockAdjustReason> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(code), StockAdjustReason::getCode, code)
                .like(StringUtils.hasText(name), StockAdjustReason::getName, name)
                .eq(status != null, StockAdjustReason::getStatus, status)
                .orderByAsc(StockAdjustReason::getCode);
        return page(page, wrapper);
    }

    @Override
    public String generateCode() {
        String maxCode = baseMapper.selectMaxCode();
        if (maxCode == null) {
            return "001";
        }
        int nextNum = Integer.parseInt(maxCode) + 1;
        return String.format("%03d", nextNum);
    }

    @Override
    public boolean add(StockAdjustReason reason) {
        if (!StringUtils.hasText(reason.getCode())) {
            reason.setCode(generateCode());
        }
        return save(reason);
    }

    @Override
    public boolean modify(StockAdjustReason reason) {
        return updateById(reason);
    }
}

package com.erp.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.business.entity.StockAdjustSheetDetail;
import com.erp.business.mapper.StockAdjustSheetDetailMapper;
import com.erp.business.service.IStockAdjustSheetDetailService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 库存调整单明细 Service 实现
 */
@Service
public class StockAdjustSheetDetailServiceImpl extends ServiceImpl<StockAdjustSheetDetailMapper, StockAdjustSheetDetail>
        implements IStockAdjustSheetDetailService {

    @Override
    public List<StockAdjustSheetDetail> listBySheetId(Long sheetId) {
        LambdaQueryWrapper<StockAdjustSheetDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StockAdjustSheetDetail::getSheetId, sheetId)
                .orderByAsc(StockAdjustSheetDetail::getSort);
        return list(wrapper);
    }

    @Override
    public boolean deleteBySheetId(Long sheetId) {
        LambdaQueryWrapper<StockAdjustSheetDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StockAdjustSheetDetail::getSheetId, sheetId);
        return remove(wrapper);
    }
}

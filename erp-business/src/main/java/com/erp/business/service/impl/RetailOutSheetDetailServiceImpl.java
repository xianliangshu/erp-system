package com.erp.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.business.entity.RetailOutSheetDetail;
import com.erp.business.mapper.RetailOutSheetDetailMapper;
import com.erp.business.service.IRetailOutSheetDetailService;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * 零售出库单明细 Service 实现
 */
@Service
public class RetailOutSheetDetailServiceImpl extends ServiceImpl<RetailOutSheetDetailMapper, RetailOutSheetDetail>
        implements IRetailOutSheetDetailService {

    @Override
    public List<RetailOutSheetDetail> listBySheetId(Long sheetId) {
        return this.list(new LambdaQueryWrapper<RetailOutSheetDetail>().eq(RetailOutSheetDetail::getSheetId, sheetId));
    }

    @Override
    public void deleteBySheetId(Long sheetId) {
        this.remove(new LambdaQueryWrapper<RetailOutSheetDetail>().eq(RetailOutSheetDetail::getSheetId, sheetId));
    }
}

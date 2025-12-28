package com.erp.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.business.entity.RetailReturnDetail;
import com.erp.business.mapper.RetailReturnDetailMapper;
import com.erp.business.service.IRetailReturnDetailService;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * 零售退货单明细 Service 实现
 */
@Service
public class RetailReturnDetailServiceImpl extends ServiceImpl<RetailReturnDetailMapper, RetailReturnDetail>
        implements IRetailReturnDetailService {

    @Override
    public List<RetailReturnDetail> listByReturnId(Long returnId) {
        return this.list(new LambdaQueryWrapper<RetailReturnDetail>().eq(RetailReturnDetail::getReturnId, returnId));
    }

    @Override
    public void deleteByReturnId(Long returnId) {
        this.remove(new LambdaQueryWrapper<RetailReturnDetail>().eq(RetailReturnDetail::getReturnId, returnId));
    }
}

package com.erp.business.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.business.dto.CustomerFeeSheetDTO;
import com.erp.business.dto.CustomerFeeSheetQueryDTO;
import com.erp.business.entity.CustomerFeeSheet;
import java.util.Map;

public interface ICustomerFeeSheetService extends IService<CustomerFeeSheet> {
    Page<CustomerFeeSheet> queryPage(Long current, Long size, CustomerFeeSheetQueryDTO queryDTO);

    Map<String, Object> getDetailById(Long id);

    Long create(CustomerFeeSheetDTO dto);

    void update(CustomerFeeSheetDTO dto);

    void approve(Long id);

    void refuse(Long id, String reason);

    void deleteById(Long id);

    String generateCode(Integer sheetType);
}

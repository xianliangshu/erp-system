package com.erp.business.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.business.dto.CustomerPreSheetDTO;
import com.erp.business.dto.CustomerPreSheetQueryDTO;
import com.erp.business.entity.CustomerPreSheet;
import java.util.Map;

public interface ICustomerPreSheetService extends IService<CustomerPreSheet> {
    Page<CustomerPreSheet> queryPage(Long current, Long size, CustomerPreSheetQueryDTO queryDTO);

    Map<String, Object> getDetailById(Long id);

    Long create(CustomerPreSheetDTO dto);

    void update(CustomerPreSheetDTO dto);

    void approve(Long id);

    void refuse(Long id, String reason);

    void deleteById(Long id);

    String generateCode();
}

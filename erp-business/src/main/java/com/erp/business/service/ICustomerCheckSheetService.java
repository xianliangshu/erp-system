package com.erp.business.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.business.dto.CustomerCheckSheetDTO;
import com.erp.business.dto.CustomerCheckSheetQueryDTO;
import com.erp.business.entity.CustomerCheckSheet;
import java.util.Map;

public interface ICustomerCheckSheetService extends IService<CustomerCheckSheet> {
    Page<CustomerCheckSheet> queryPage(Long current, Long size, CustomerCheckSheetQueryDTO queryDTO);

    Map<String, Object> getDetailById(Long id);

    Long create(CustomerCheckSheetDTO dto);

    void update(CustomerCheckSheetDTO dto);

    void approve(Long id);

    void refuse(Long id, String reason);

    void deleteById(Long id);

    String generateCode();
}

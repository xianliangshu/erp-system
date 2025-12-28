package com.erp.business.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.business.dto.CustomerSettleSheetDTO;
import com.erp.business.dto.CustomerSettleSheetQueryDTO;
import com.erp.business.entity.CustomerSettleSheet;
import java.util.Map;

public interface ICustomerSettleSheetService extends IService<CustomerSettleSheet> {
    Page<CustomerSettleSheet> queryPage(Long current, Long size, CustomerSettleSheetQueryDTO queryDTO);

    Map<String, Object> getDetailById(Long id);

    Long create(CustomerSettleSheetDTO dto);

    void update(CustomerSettleSheetDTO dto);

    void approve(Long id);

    void refuse(Long id, String reason);

    void deleteById(Long id);

    String generateCode();
}

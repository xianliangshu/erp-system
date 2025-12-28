package com.erp.business.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.business.dto.SettleCheckSheetDTO;
import com.erp.business.dto.SettleCheckSheetQueryDTO;
import com.erp.business.entity.SettleCheckSheet;
import java.util.Map;

public interface ISettleCheckSheetService extends IService<SettleCheckSheet> {
    Page<SettleCheckSheet> queryPage(Long current, Long size, SettleCheckSheetQueryDTO queryDTO);

    Map<String, Object> getDetailById(Long id);

    Long create(SettleCheckSheetDTO dto);

    void update(SettleCheckSheetDTO dto);

    void approve(Long id);

    void refuse(Long id, String reason);

    void deleteById(Long id);

    String generateCode();
}

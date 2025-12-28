package com.erp.business.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.business.dto.SettleSheetDTO;
import com.erp.business.dto.SettleSheetQueryDTO;
import com.erp.business.entity.SettleSheet;
import java.util.Map;

public interface ISettleSheetService extends IService<SettleSheet> {
    Page<SettleSheet> queryPage(Long current, Long size, SettleSheetQueryDTO queryDTO);

    Map<String, Object> getDetailById(Long id);

    Long create(SettleSheetDTO dto);

    void update(SettleSheetDTO dto);

    void approve(Long id);

    void refuse(Long id, String reason);

    void deleteById(Long id);

    String generateCode();
}

package com.erp.business.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.business.dto.SettlePreSheetDTO;
import com.erp.business.dto.SettlePreSheetQueryDTO;
import com.erp.business.entity.SettlePreSheet;

import java.util.Map;

public interface ISettlePreSheetService extends IService<SettlePreSheet> {

    Page<SettlePreSheet> queryPage(Long current, Long size, SettlePreSheetQueryDTO queryDTO);

    Map<String, Object> getDetailById(Long id);

    Long create(SettlePreSheetDTO dto);

    void update(SettlePreSheetDTO dto);

    void approve(Long id);

    void refuse(Long id, String reason);

    void deleteById(Long id);

    String generateCode();
}

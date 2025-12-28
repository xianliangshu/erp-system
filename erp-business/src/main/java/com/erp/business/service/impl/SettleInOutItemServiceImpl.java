package com.erp.business.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.business.dto.SettleInOutItemQueryDTO;
import com.erp.business.entity.SettleInOutItem;
import com.erp.business.mapper.SettleInOutItemMapper;
import com.erp.business.service.ISettleInOutItemService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 收支项目服务实现
 */
@Service
public class SettleInOutItemServiceImpl extends ServiceImpl<SettleInOutItemMapper, SettleInOutItem>
        implements ISettleInOutItemService {

    @Override
    public Page<SettleInOutItem> queryPage(Long current, Long size, SettleInOutItemQueryDTO queryDTO) {
        Page<SettleInOutItem> page = new Page<>(current != null ? current : 1, size != null ? size : 10);
        LambdaQueryWrapper<SettleInOutItem> wrapper = new LambdaQueryWrapper<>();

        if (queryDTO != null) {
            if (StrUtil.isNotBlank(queryDTO.getCode())) {
                wrapper.like(SettleInOutItem::getCode, queryDTO.getCode());
            }
            if (StrUtil.isNotBlank(queryDTO.getName())) {
                wrapper.like(SettleInOutItem::getName, queryDTO.getName());
            }
            if (queryDTO.getItemType() != null) {
                wrapper.eq(SettleInOutItem::getItemType, queryDTO.getItemType());
            }
            if (queryDTO.getStatus() != null) {
                wrapper.eq(SettleInOutItem::getStatus, queryDTO.getStatus());
            }
        }
        wrapper.orderByAsc(SettleInOutItem::getCode);

        return this.page(page, wrapper);
    }

    @Override
    public List<SettleInOutItem> listEnabled(Integer itemType) {
        LambdaQueryWrapper<SettleInOutItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SettleInOutItem::getStatus, 1);
        if (itemType != null) {
            wrapper.eq(SettleInOutItem::getItemType, itemType);
        }
        wrapper.orderByAsc(SettleInOutItem::getCode);
        return this.list(wrapper);
    }

    @Override
    public String generateCode() {
        String prefix = "SZ";
        String maxCode = baseMapper.selectMaxCodeByPrefix(prefix);

        int seq = 1;
        if (maxCode != null && maxCode.length() > prefix.length()) {
            try {
                String seqStr = maxCode.substring(prefix.length());
                seq = Integer.parseInt(seqStr) + 1;
            } catch (NumberFormatException e) {
                // Ignore
            }
        }

        return prefix + String.format("%04d", seq);
    }
}

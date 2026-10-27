package com.erp.basedata.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.basedata.entity.BaseMaterial;
import com.erp.basedata.entity.BaseMaterialCode;
import com.erp.basedata.mapper.BaseMaterialCodeMapper;
import com.erp.basedata.mapper.BaseMaterialMapper;
import com.erp.basedata.service.IMaterialCodeService;
import com.erp.common.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * SKU 一品多码 Service 实现类
 *
 * @author ERP System
 * @since 2026-10-03
 */
@Service
public class MaterialCodeServiceImpl extends ServiceImpl<BaseMaterialCodeMapper, BaseMaterialCode>
        implements IMaterialCodeService {

    @Autowired
    private BaseMaterialMapper materialMapper;

    @Override
    public List<BaseMaterialCode> listByMaterial(Long materialId) {
        LambdaQueryWrapper<BaseMaterialCode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BaseMaterialCode::getMaterialId, materialId)
                .orderByDesc(BaseMaterialCode::getIsMain)
                .orderByAsc(BaseMaterialCode::getCodeType)
                .orderByAsc(BaseMaterialCode::getId);
        return baseMapper.selectList(wrapper);
    }

    @Override
    public List<BaseMaterialCode> listByCode(String code) {
        if (StrUtil.isBlank(code)) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<BaseMaterialCode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BaseMaterialCode::getCode, code.trim())
                .orderByAsc(BaseMaterialCode::getCodeType);
        return baseMapper.selectList(wrapper);
    }

    @Override
    public BaseMaterialCode getByCode(String code, Integer codeType) {
        if (StrUtil.isBlank(code)) {
            return null;
        }
        LambdaQueryWrapper<BaseMaterialCode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BaseMaterialCode::getCode, code.trim())
                .eq(codeType != null, BaseMaterialCode::getCodeType, codeType)
                .orderByAsc(BaseMaterialCode::getCodeType)
                .last("LIMIT 1");
        return baseMapper.selectOne(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveCodes(Long materialId, List<BaseMaterialCode> codes) {
        BaseMaterial material = materialMapper.selectById(materialId);
        if (material == null) {
            throw new BusinessException("SKU 不存在");
        }

        List<BaseMaterialCode> list = codes == null ? new ArrayList<>() : codes.stream()
                .filter(c -> c != null && StrUtil.isNotBlank(c.getCode()))
                .collect(Collectors.toList());

        // 一个 SKU 至少要有一条主编号，入参为空时用 SKU 编码兜底
        if (list.isEmpty()) {
            BaseMaterialCode main = new BaseMaterialCode();
            main.setCode(material.getCode());
            main.setCodeType(BaseMaterialCode.TYPE_MAIN);
            main.setRemark("SKU主编号");
            list.add(main);
        }

        Set<String> keys = new HashSet<>();
        for (BaseMaterialCode code : list) {
            if (code.getCodeType() == null) {
                code.setCodeType(BaseMaterialCode.TYPE_OTHER);
            }
            code.setCode(code.getCode().trim());

            // 主码标记：编码类型为主编号的即为主码，其余强制置 0
            if (BaseMaterialCode.TYPE_MAIN == code.getCodeType()) {
                code.setIsMain(1);
                if (StrUtil.isBlank(code.getRemark())) {
                    code.setRemark("SKU主编号");
                }
            } else {
                code.setIsMain(0);
            }

            String key = code.getCodeType() + ":" + code.getCode();
            if (!keys.add(key)) {
                throw new BusinessException("编码「" + code.getCode() + "」在本次提交中重复");
            }

            Long occupied = baseMapper.selectCount(new LambdaQueryWrapper<BaseMaterialCode>()
                    .eq(BaseMaterialCode::getCode, code.getCode())
                    .eq(BaseMaterialCode::getCodeType, code.getCodeType())
                    .ne(BaseMaterialCode::getMaterialId, materialId));
            if (occupied != null && occupied > 0) {
                throw new BusinessException("编码「" + code.getCode() + "」已被其他商品占用");
            }
        }

        // 整体覆盖（物理删除后重建，唯一键不含 deleted 列，可安全重建）
        baseMapper.delete(new LambdaQueryWrapper<BaseMaterialCode>()
                .eq(BaseMaterialCode::getMaterialId, materialId));
        for (BaseMaterialCode code : list) {
            code.setId(null);
            code.setMaterialId(materialId);
            baseMapper.insert(code);
        }

        // 同步维护 SKU 上的一品多码标记
        int expected = list.size() > 1 ? 1 : 0;
        if (material.getIsMultiCode() == null || material.getIsMultiCode() != expected) {
            BaseMaterial update = new BaseMaterial();
            update.setId(materialId);
            update.setIsMultiCode(expected);
            materialMapper.updateById(update);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteCode(Long id) {
        BaseMaterialCode code = baseMapper.selectById(id);
        if (code == null) {
            throw new BusinessException("编码不存在");
        }
        if (Integer.valueOf(1).equals(code.getIsMain())) {
            throw new BusinessException("主编号不允许删除");
        }
        return baseMapper.deleteById(id) > 0;
    }
}

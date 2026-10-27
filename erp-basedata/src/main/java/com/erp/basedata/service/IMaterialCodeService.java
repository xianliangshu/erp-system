package com.erp.basedata.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.basedata.entity.BaseMaterialCode;

import java.util.List;

/**
 * SKU 一品多码 Service 接口
 *
 * @author ERP System
 * @since 2026-10-03
 */
public interface IMaterialCodeService extends IService<BaseMaterialCode> {

    /**
     * 查询某 SKU 的全部编码
     *
     * @param materialId SKU ID
     * @return 编码列表（主码在前）
     */
    List<BaseMaterialCode> listByMaterial(Long materialId);

    /**
     * 按编码查询（扫码入口，不限类型）
     *
     * @param code 编码/条码
     * @return 命中的编码记录列表
     */
    List<BaseMaterialCode> listByCode(String code);

    /**
     * 按编码 + 类型精确定位
     *
     * @param code     编码/条码
     * @param codeType 类型(1=主编号,2=条码,3=其他)
     * @return 命中的编码记录，未命中返回 null
     */
    BaseMaterialCode getByCode(String code, Integer codeType);

    /**
     * 整体覆盖某 SKU 的编码集合（校验同类型下的编码全局唯一）
     *
     * @param materialId SKU ID
     * @param codes      编码列表
     * @return 是否成功
     */
    boolean saveCodes(Long materialId, List<BaseMaterialCode> codes);

    /**
     * 删除单条编码
     *
     * @param id 编码记录ID
     * @return 是否成功
     */
    boolean deleteCode(Long id);
}

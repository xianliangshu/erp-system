package com.erp.basedata.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.basedata.dto.SpuPageParam;
import com.erp.basedata.dto.SpuSaveDTO;
import com.erp.basedata.entity.BaseMaterial;
import com.erp.basedata.entity.BaseSpu;

import java.util.List;

/**
 * SPU 商品 Service 接口
 *
 * @author ERP System
 * @since 2026-10-03
 */
public interface IBaseSpuService extends IService<BaseSpu> {

    /**
     * 分页查询 SPU
     *
     * @param param 查询参数
     * @return 分页结果（含分类/品牌/单位名称、SKU 数量）
     */
    Page<BaseSpu> getSpuPage(SpuPageParam param);

    /**
     * 获取所有启用的 SPU（下拉选择用）
     *
     * @return SPU 列表
     */
    List<BaseSpu> getAllEnabled();

    /**
     * 获取 SPU 详情（含已绑定的属性组ID列表）
     *
     * @param id SPU ID
     * @return SPU 详情
     */
    BaseSpu getSpuDetail(Long id);

    /**
     * 新增 SPU，并按勾选的属性值生成 SKU 矩阵
     *
     * @param dto 保存参数
     * @return 是否成功
     */
    boolean saveSpu(SpuSaveDTO dto);

    /**
     * 更新 SPU。
     *
     * <p>属性勾选变更时<b>只增不删</b>：已存在的属性组合跳过，不重建 SKU，
     * 避免破坏既有库存与单据引用。</p>
     *
     * @param dto 保存参数
     * @return 是否成功
     */
    boolean updateSpu(SpuSaveDTO dto);

    /**
     * 删除 SPU。存在 SKU 引用时拒绝删除。
     *
     * @param id SPU ID
     * @return 是否成功
     */
    boolean deleteSpu(Long id);

    /**
     * 按属性值组合生成 SKU 矩阵（幂等：已存在的组合跳过）
     *
     * @param spuId      SPU ID
     * @param selections 属性组勾选结果
     * @return 本次新生成的 SKU 数量
     */
    int generateSkus(Long spuId, List<SpuSaveDTO.PropertySelection> selections);

    /**
     * 查询某 SPU 下的 SKU 列表
     *
     * @param spuId SPU ID
     * @return SKU 列表
     */
    List<BaseMaterial> listSkus(Long spuId);
}

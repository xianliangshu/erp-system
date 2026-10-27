package com.erp.business.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.business.entity.RetailReturn;
import java.util.Map;

/**
 * 零售退货单 Service
 */
public interface IRetailReturnService extends IService<RetailReturn> {

    /**
     * 分页查询
     */
    IPage<RetailReturn> getPage(Page<RetailReturn> page, Map<String, Object> params);

    /**
     * 根据ID获取详情（包含明细）
     */
    Map<String, Object> getDetailById(Long id);

    /**
     * 新增
     * <p>
     * 若零售配置开启了「零售退货单自动审核」，本方法会在保存明细后直接完成审核
     * 并增加库存。
     *
     * @return 含 id / code / status / autoApproved 的结果，供前端提示用
     */
    Map<String, Object> add(Map<String, Object> data);

    /**
     * 修改
     */
    void update(Map<String, Object> data);

    /**
     * 审核通过
     */
    void approve(Long id);

    /**
     * 审核拒绝
     */
    void refuse(Long id, String refuseReason);

    /**
     * 删除
     */
    void deleteById(Long id);
}

package com.erp.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.common.core.page.PageResult;
import com.erp.system.entity.SysDictType;
import com.erp.system.param.SysDictTypePageParam;

/**
 * 字典类型Service接口
 * 
 * @author ERP System
 * @since 2025-12-12
 */
public interface ISysDictTypeService extends IService<SysDictType> {

    /**
     * 分页查询字典类型
     * 
     * @param param 查询参数
     * @return 分页结果
     */
    PageResult<SysDictType> pageQuery(SysDictTypePageParam param);
}

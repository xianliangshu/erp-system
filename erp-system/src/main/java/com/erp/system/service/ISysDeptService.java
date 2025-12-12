package com.erp.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.system.entity.SysDept;
import com.erp.system.param.SysDeptQueryParam;

import java.util.List;

/**
 * 部门Service接口
 * 
 * @author ERP System
 * @since 2025-12-12
 */
public interface ISysDeptService extends IService<SysDept> {

    /**
     * 查询部门列表
     * 
     * @param param 查询参数
     * @return 部门列表
     */
    List<SysDept> listQuery(SysDeptQueryParam param);
}

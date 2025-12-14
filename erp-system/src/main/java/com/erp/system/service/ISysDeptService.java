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
     * 列表查询部门
     * 
     * @param param 查询参数
     * @return 部门列表
     */
    List<SysDept> listQuery(SysDeptQueryParam param);

    /**
     * 构建部门树
     * 
     * @return 部门树(顶级部门列表)
     */
    List<SysDept> buildTree();

    /**
     * 统计部门下的用户数量
     * 
     * @param deptId 部门ID
     * @return 用户数量
     */
    long countDeptUsers(Long deptId);
}

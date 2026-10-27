package com.erp.basedata.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.basedata.dto.CustomerPageParam;
import com.erp.basedata.entity.BaseCustomer;
import com.erp.basedata.vo.CustomerExcelVO;
import com.erp.common.core.vo.ExcelImportResult;

import java.io.InputStream;
import java.util.List;

/**
 * 客户信息Service接口
 *
 * @author ERP System
 * @since 2025-12-21
 */
public interface IBaseCustomerService extends IService<BaseCustomer> {

    /**
     * 分页查询客户
     *
     * @param param 查询参数
     * @return 分页结果
     */
    Page<BaseCustomer> getCustomerPage(CustomerPageParam param);

    /**
     * 获取所有启用的客户
     *
     * @return 客户列表
     */
    List<BaseCustomer> getAllEnabledCustomers();

    /**
     * 根据ID获取客户
     *
     * @param id 客户ID
     * @return 客户信息
     */
    BaseCustomer getById(Long id);

    /**
     * 保存客户
     *
     * @param customer 客户信息
     * @return 是否成功
     */
    boolean saveCustomer(BaseCustomer customer);

    /**
     * 更新客户
     *
     * @param customer 客户信息
     * @return 是否成功
     */
    boolean updateCustomer(BaseCustomer customer);

    /**
     * 删除客户
     *
     * @param id 客户ID
     * @return 是否成功
     */
    boolean deleteCustomer(Long id);

    /**
     * 查询需要导出的客户（按与列表页相同的筛选条件，但<b>不分页</b>）
     *
     * @param param 查询参数（只取筛选条件，忽略分页字段）
     * @return Excel 行数据，按排序号、ID 升序
     */
    List<CustomerExcelVO> listForExport(CustomerPageParam param);

    /**
     * Excel 批量导入客户
     *
     * <p>策略是<b>逐行校验、一行不合法则整体不落库</b>。校验规则：名称为必填且不可重复；
     * 编号留空则自动生成，填了则要求唯一；类型/级别/结算方式沿用系统内数字取值，
     * 留空时使用默认值；状态接受「启用/禁用」或「1/0」。</p>
     *
     * @param in 上传文件流
     * @return 导入结果（含总行数、成功数、失败数、带行号的错误明细）
     */
    ExcelImportResult importExcel(InputStream in);
}

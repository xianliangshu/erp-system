package com.erp.business.dto;

import lombok.Data;

/**
 * 收支项目DTO
 */
@Data
public class SettleInOutItemDTO {

    /**
     * ID
     */
    private Long id;

    /**
     * 编号
     */
    private String code;

    /**
     * 名称
     */
    private String name;

    /**
     * 项目类型: 1-收入, 2-支出
     */
    private Integer itemType;

    /**
     * 状态: 0-禁用, 1-启用
     */
    private Integer status;

    /**
     * 备注
     */
    private String description;
}

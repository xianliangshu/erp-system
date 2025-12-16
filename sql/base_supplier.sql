-- 供应商管理表
CREATE TABLE IF NOT EXISTS `base_supplier` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `code` VARCHAR(50) NOT NULL COMMENT '供应商编号',
    `name` VARCHAR(100) NOT NULL COMMENT '供应商名称',
    `short_name` VARCHAR(50) COMMENT '供应商简称',
    `type` TINYINT DEFAULT 1 COMMENT '供应商类型: 1-原材料供应商 2-设备供应商 3-服务供应商',
    `credit_level` TINYINT DEFAULT 3 COMMENT '信用等级: 1-优秀 2-良好 3-一般 4-较差 5-差',
    
    -- 联系信息
    `contact_person` VARCHAR(50) COMMENT '联系人',
    `contact_phone` VARCHAR(20) COMMENT '联系电话',
    `contact_email` VARCHAR(100) COMMENT '联系邮箱',
    `address` VARCHAR(200) COMMENT '联系地址',
    `website` VARCHAR(200) COMMENT '网站',
    
    -- 结算信息
    `settlement_method` TINYINT DEFAULT 1 COMMENT '结算方式: 1-现金 2-月结 3-季结 4-账期',
    `payment_days` INT DEFAULT 0 COMMENT '账期天数',
    `bank_name` VARCHAR(100) COMMENT '开户银行',
    `bank_account` VARCHAR(50) COMMENT '银行账号',
    `tax_number` VARCHAR(50) COMMENT '税号',
    
    -- 状态和备注
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
    `sort` INT DEFAULT 0 COMMENT '排序',
    `remark` VARCHAR(500) COMMENT '备注',
    
    -- 审计字段
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(50) COMMENT '创建人',
    `update_by` VARCHAR(50) COMMENT '更新人',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标记: 0-未删除 1-已删除',
    
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`),
    KEY `idx_name` (`name`),
    KEY `idx_status` (`status`),
    KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商信息表';

-- 插入测试数据
INSERT INTO `base_supplier` (`code`, `name`, `short_name`, `type`, `credit_level`, `contact_person`, `contact_phone`, `contact_email`, `address`, `settlement_method`, `payment_days`, `status`, `sort`, `remark`)
VALUES
('SUP000001', '深圳市华强电子有限公司', '华强电子', 1, 1, '张经理', '0755-12345678', 'zhang@huaqiang.com', '深圳市福田区华强北路1号', 2, 30, 1, 1, '主要供应商'),
('SUP000002', '广州市天河机械设备公司', '天河机械', 2, 2, '李工', '020-87654321', 'li@tianhe.com', '广州市天河区天河路100号', 3, 90, 1, 2, '设备供应商'),
('SUP000003', '东莞市长安物流服务公司', '长安物流', 3, 3, '王主管', '0769-98765432', 'wang@changan.com', '东莞市长安镇长安大道200号', 1, 0, 1, 3, '物流服务商');

-- 客户管理表
CREATE TABLE IF NOT EXISTS `base_customer` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `code` VARCHAR(50) NOT NULL COMMENT '客户编号',
    `name` VARCHAR(100) NOT NULL COMMENT '客户名称',
    `short_name` VARCHAR(50) COMMENT '客户简称',
    `type` TINYINT DEFAULT 1 COMMENT '客户类型: 1-普通客户 2-VIP客户 3-分销商',
    `level` TINYINT DEFAULT 3 COMMENT '客户等级: 1-一级 2-二级 3-三级',
    
    -- 联系信息
    `contact_person` VARCHAR(50) COMMENT '联系人',
    `contact_phone` VARCHAR(20) COMMENT '联系电话',
    `contact_email` VARCHAR(100) COMMENT '联系邮箱',
    `address` VARCHAR(200) COMMENT '联系地址',
    
    -- 结算信息
    `settlement_method` TINYINT DEFAULT 1 COMMENT '结算方式: 1-现金 2-月结 3-季结 4-账期',
    `payment_days` INT DEFAULT 0 COMMENT '账期天数',
    
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户信息表';

-- 插入测试数据
INSERT INTO `base_customer` (`code`, `name`, `short_name`, `type`, `level`, `contact_person`, `contact_phone`, `address`, `status`, `sort`, `remark`)
VALUES
('CUS000001', '北京京东世纪贸易有限公司', '京东', 3, 1, '刘经理', '010-12345678', '北京市大兴区亦庄经济技术开发区', 1, 1, '核心分销商'),
('CUS000002', '阿里巴巴（中国）网络技术有限公司', '阿里', 3, 1, '马经理', '0571-87654321', '杭州市余杭区文一西路969号', 1, 2, '核心分销商'),
('CUS000003', '普通零售客户A', '客户A', 1, 3, '张三', '13800138000', '上海市浦东新区', 1, 3, '普通客户');

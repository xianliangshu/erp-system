-- 品牌管理表
CREATE TABLE IF NOT EXISTS `base_brand` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `code` VARCHAR(50) NOT NULL COMMENT '品牌编号',
    `name` VARCHAR(100) NOT NULL COMMENT '品牌名称',
    `short_name` VARCHAR(50) COMMENT '品牌简称',
    `logo` VARCHAR(200) COMMENT '品牌Logo',
    
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
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='品牌信息表';

-- 插入测试数据
INSERT INTO `base_brand` (`code`, `name`, `short_name`, `status`, `sort`, `remark`)
VALUES
('BRD000001', '苹果', 'Apple', 1, 1, '电子产品品牌'),
('BRD000002', '华为', 'Huawei', 1, 2, '国产之光'),
('BRD000003', '小米', 'Xiaomi', 1, 3, '性价比之王');

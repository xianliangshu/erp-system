-- 计量单位表
CREATE TABLE IF NOT EXISTS `base_unit` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '单位ID',
    `code` VARCHAR(50) NOT NULL COMMENT '单位编号',
    `name` VARCHAR(50) NOT NULL COMMENT '单位名称',
    `sort` INT DEFAULT 0 COMMENT '排序',
    `status` TINYINT DEFAULT 1 COMMENT '状态(0=禁用,1=启用)',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标记(0=未删除,1=已删除)',
    `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`),
    UNIQUE KEY `uk_name` (`name`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='计量单位表';

-- 插入常用单位数据
INSERT INTO `base_unit` (`code`, `name`, `sort`, `status`, `remark`) VALUES
('UNIT000001', '个', 1, 1, '通用计数单位'),
('UNIT000002', '台', 2, 1, '设备类单位'),
('UNIT000003', '件', 3, 1, '通用计件单位'),
('UNIT000004', '套', 4, 1, '成套产品单位'),
('UNIT000005', '千克', 5, 1, '重量单位'),
('UNIT000006', '克', 6, 1, '重量单位'),
('UNIT000007', '吨', 7, 1, '重量单位'),
('UNIT000008', '米', 8, 1, '长度单位'),
('UNIT000009', '厘米', 9, 1, '长度单位'),
('UNIT000010', '平方米', 10, 1, '面积单位'),
('UNIT000011', '立方米', 11, 1, '体积单位'),
('UNIT000012', '升', 12, 1, '容量单位'),
('UNIT000013', '箱', 13, 1, '包装单位'),
('UNIT000014', '包', 14, 1, '包装单位'),
('UNIT000015', '袋', 15, 1, '包装单位');

-- 物料分类表
CREATE TABLE IF NOT EXISTS `base_material_category` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    `parent_id` BIGINT DEFAULT 0 COMMENT '父分类ID(0表示顶级分类)',
    `code` VARCHAR(50) NOT NULL COMMENT '分类编号',
    `name` VARCHAR(100) NOT NULL COMMENT '分类名称',
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
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物料分类表';

-- 插入初始分类数据
INSERT INTO `base_material_category` (`parent_id`, `code`, `name`, `sort`, `status`, `remark`) VALUES
(0, 'MC000001', '原材料', 1, 1, '生产用原材料'),
(0, 'MC000002', '半成品', 2, 1, '生产过程中的半成品'),
(0, 'MC000003', '成品', 3, 1, '最终产品'),
(0, 'MC000004', '辅料', 4, 1, '生产辅助材料'),
(0, 'MC000005', '包装物', 5, 1, '包装用材料');

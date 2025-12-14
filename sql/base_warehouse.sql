-- 仓库管理表
CREATE TABLE IF NOT EXISTS `base_warehouse` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `code` VARCHAR(20) NOT NULL COMMENT '仓库编号',
    `name` VARCHAR(50) NOT NULL COMMENT '仓库名称',
    `contact` VARCHAR(20) DEFAULT NULL COMMENT '联系人',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
    `address` VARCHAR(200) DEFAULT NULL COMMENT '仓库地址',
    `is_default` TINYINT NOT NULL DEFAULT 0 COMMENT '是否默认仓库(0-否 1-是)',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态(0-禁用 1-启用)',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记(0-未删除 1-已删除)',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`),
    KEY `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='仓库信息表';

-- 插入测试数据
INSERT INTO `base_warehouse` (`code`, `name`, `contact`, `phone`, `address`, `is_default`, `status`, `remark`) VALUES
('WH001', '主仓库', '张三', '13800138000', '北京市朝阳区xxx路xxx号', 1, 1, '公司主仓库'),
('WH002', '分仓库A', '李四', '13800138001', '上海市浦东新区xxx路xxx号', 0, 1, '上海分仓'),
('WH003', '分仓库B', '王五', '13800138002', '广州市天河区xxx路xxx号', 0, 1, '广州分仓');

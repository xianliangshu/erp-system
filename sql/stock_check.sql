-- 库存盘点表
CREATE TABLE IF NOT EXISTS `stock_check` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `code` varchar(32) NOT NULL COMMENT '盘点单编号',
    `sc_id` bigint DEFAULT NULL COMMENT '仓库ID',
    `check_date` date DEFAULT NULL COMMENT '盘点日期',
    `total_profit_num` decimal(16,2) DEFAULT '0.00' COMMENT '盘盈数量',
    `total_loss_num` decimal(16,2) DEFAULT '0.00' COMMENT '盘亏数量',
    `status` tinyint DEFAULT '0' COMMENT '状态: 0-待审核 1-已审核',
    `description` varchar(500) DEFAULT NULL COMMENT '备注',
    `create_by` varchar(64) DEFAULT NULL COMMENT '创建者',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` tinyint DEFAULT '0' COMMENT '删除标记',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`),
    KEY `idx_sc_id` (`sc_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存盘点表';

-- 库存盘点明细表
CREATE TABLE IF NOT EXISTS `stock_check_detail` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `check_id` bigint NOT NULL COMMENT '盘点单ID',
    `product_id` bigint NOT NULL COMMENT '商品ID',
    `stock_num` decimal(16,2) DEFAULT '0.00' COMMENT '账面数量',
    `actual_num` decimal(16,2) DEFAULT '0.00' COMMENT '实盘数量',
    `diff_num` decimal(16,2) DEFAULT '0.00' COMMENT '差异数量(实盘-账面)',
    `cost_price` decimal(16,2) DEFAULT '0.00' COMMENT '成本单价',
    `diff_amount` decimal(16,2) DEFAULT '0.00' COMMENT '差异金额',
    `description` varchar(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    KEY `idx_check_id` (`check_id`),
    KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存盘点明细表';

-- 添加库存盘点菜单
SET @stock_parent_id = (SELECT id FROM sys_menu WHERE name = 'stock-query' LIMIT 1);
SET @stock_parent_id = (SELECT parent_id FROM sys_menu WHERE name = 'stock-query' LIMIT 1);

INSERT INTO `sys_menu` (`parent_id`, `code`, `name`, `title`, `menu_type`, `path`, `icon`, `sort`, `visible`, `status`, `permission`, `component`, `remark`) 
VALUES (@stock_parent_id, 'M000502', 'stock-check', '库存盘点', 1, '/business/stock/check', 'AuditOutlined', 2, 1, 1, 'business:stock:check', '/Business/Stock/Check/List', '库存盘点管理');

SET @stock_check_menu_id = LAST_INSERT_ID();
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, @stock_check_menu_id);

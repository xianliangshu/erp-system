-- ========================================
-- 库存调拨表
-- ========================================
CREATE TABLE IF NOT EXISTS `stock_transfer` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `code` varchar(32) NOT NULL COMMENT '调拨单编号',
    `out_sc_id` bigint NOT NULL COMMENT '调出仓库ID',
    `in_sc_id` bigint NOT NULL COMMENT '调入仓库ID',
    `transfer_date` date DEFAULT NULL COMMENT '调拨日期',
    `total_num` decimal(16,4) DEFAULT '0.0000' COMMENT '调拨总数量',
    `status` tinyint DEFAULT '0' COMMENT '状态: 0-待确认 1-已确认',
    `description` varchar(500) DEFAULT NULL COMMENT '备注',
    `create_by` varchar(64) DEFAULT NULL COMMENT '创建者',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` tinyint DEFAULT '0' COMMENT '删除标记',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`),
    KEY `idx_out_sc_id` (`out_sc_id`),
    KEY `idx_in_sc_id` (`in_sc_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存调拨表';

-- ========================================
-- 库存调拨明细表
-- ========================================
CREATE TABLE IF NOT EXISTS `stock_transfer_detail` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `transfer_id` bigint NOT NULL COMMENT '调拨单ID',
    `product_id` bigint NOT NULL COMMENT '商品ID',
    `transfer_num` decimal(16,4) DEFAULT '0.0000' COMMENT '调拨数量',
    `cost_price` decimal(16,2) DEFAULT '0.00' COMMENT '成本单价',
    `description` varchar(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    KEY `idx_transfer_id` (`transfer_id`),
    KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存调拨明细表';

-- ========================================
-- 添加库存调拨菜单
-- ========================================
SET @stock_parent_id = (SELECT parent_id FROM sys_menu WHERE name = 'stock-query' LIMIT 1);

INSERT INTO `sys_menu` (`parent_id`, `code`, `name`, `title`, `menu_type`, `path`, `icon`, `sort`, `visible`, `status`, `permission`, `component`, `remark`) 
VALUES (@stock_parent_id, 'M000503', 'stock-transfer', '库存调拨', 1, '/business/stock/transfer', 'SwapOutlined', 3, 1, 1, 'business:stock:transfer', '/Business/Stock/Transfer/List', '库存调拨管理');

SET @stock_transfer_menu_id = LAST_INSERT_ID();
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, @stock_transfer_menu_id);

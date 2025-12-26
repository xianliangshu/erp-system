-- 销售出库表
CREATE TABLE IF NOT EXISTS `sale_delivery` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `code` varchar(32) NOT NULL COMMENT '出库单编号',
    `order_id` bigint DEFAULT NULL COMMENT '销售订单ID',
    `order_code` varchar(32) DEFAULT NULL COMMENT '销售订单编号',
    `sc_id` bigint DEFAULT NULL COMMENT '仓库ID',
    `customer_id` bigint DEFAULT NULL COMMENT '客户ID',
    `total_num` decimal(16,2) DEFAULT '0.00' COMMENT '出库数量',
    `total_amount` decimal(16,2) DEFAULT '0.00' COMMENT '出库金额',
    `status` tinyint DEFAULT '0' COMMENT '状态: 0-待确认 1-已确认',
    `description` varchar(500) DEFAULT NULL COMMENT '备注',
    `create_by` varchar(64) DEFAULT NULL COMMENT '创建者',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` tinyint DEFAULT '0' COMMENT '删除标记',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_customer_id` (`customer_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='销售出库表';

-- 销售出库明细表
CREATE TABLE IF NOT EXISTS `sale_delivery_detail` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `delivery_id` bigint NOT NULL COMMENT '出库单ID',
    `order_detail_id` bigint DEFAULT NULL COMMENT '订单明细ID',
    `product_id` bigint NOT NULL COMMENT '商品ID',
    `order_num` decimal(16,2) DEFAULT '0.00' COMMENT '订单数量',
    `delivery_num` decimal(16,2) DEFAULT '0.00' COMMENT '出库数量',
    `tax_price` decimal(16,2) DEFAULT '0.00' COMMENT '含税单价',
    `tax_amount` decimal(16,2) DEFAULT '0.00' COMMENT '含税金额',
    `description` varchar(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    KEY `idx_delivery_id` (`delivery_id`),
    KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='销售出库明细表';

-- 添加销售出库菜单
SET @sale_parent_id = (SELECT id FROM sys_menu WHERE code = 'M0004' LIMIT 1);

INSERT INTO `sys_menu` (`parent_id`, `code`, `name`, `title`, `menu_type`, `path`, `icon`, `sort`, `visible`, `status`, `permission`, `component`, `remark`) 
VALUES (@sale_parent_id, 'M000402', 'sale-delivery', '销售出库', 1, '/business/sale/delivery', 'ExportOutlined', 2, 1, 1, 'business:sale:delivery', '/Business/Sale/Delivery/List', '销售出库管理');

SET @sale_delivery_menu_id = LAST_INSERT_ID();
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, @sale_delivery_menu_id);

-- 销售订单表
CREATE TABLE IF NOT EXISTS `sale_order` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `code` varchar(32) NOT NULL COMMENT '订单编号',
    `sc_id` bigint DEFAULT NULL COMMENT '仓库ID',
    `customer_id` bigint DEFAULT NULL COMMENT '客户ID',
    `saler_id` bigint DEFAULT NULL COMMENT '销售员ID',
    `expect_delivery_date` date DEFAULT NULL COMMENT '预计发货日期',
    `total_num` decimal(16,2) DEFAULT '0.00' COMMENT '销售数量',
    `total_amount` decimal(16,2) DEFAULT '0.00' COMMENT '销售金额',
    `status` tinyint DEFAULT '0' COMMENT '状态: 0-待审核 1-已审核 2-已拒绝 3-已完成 4-已取消',
    `description` varchar(500) DEFAULT NULL COMMENT '备注',
    `approve_by` varchar(64) DEFAULT NULL COMMENT '审核人',
    `approve_time` datetime DEFAULT NULL COMMENT '审核时间',
    `refuse_reason` varchar(500) DEFAULT NULL COMMENT '拒绝原因',
    `create_by` varchar(64) DEFAULT NULL COMMENT '创建者',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` tinyint DEFAULT '0' COMMENT '删除标记',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`),
    KEY `idx_customer_id` (`customer_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='销售订单表';

-- 销售订单明细表
CREATE TABLE IF NOT EXISTS `sale_order_detail` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `order_id` bigint NOT NULL COMMENT '订单ID',
    `product_id` bigint NOT NULL COMMENT '商品ID',
    `order_num` decimal(16,2) DEFAULT '0.00' COMMENT '销售数量',
    `tax_price` decimal(16,2) DEFAULT '0.00' COMMENT '含税单价',
    `tax_amount` decimal(16,2) DEFAULT '0.00' COMMENT '含税金额',
    `delivered_num` decimal(16,2) DEFAULT '0.00' COMMENT '已发货数量',
    `description` varchar(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='销售订单明细表';

-- 添加销售管理菜单
SET @sale_parent_id = NULL;

-- 添加销售管理目录
INSERT INTO `sys_menu` (`parent_id`, `code`, `name`, `title`, `menu_type`, `path`, `icon`, `sort`, `visible`, `status`, `permission`, `component`, `remark`) 
VALUES (0, 'M0004', 'sale', '销售管理', 0, '/sale', 'ShoppingOutlined', 4, 1, 1, NULL, NULL, '销售管理模块');

SET @sale_parent_id = LAST_INSERT_ID();

-- 添加销售订单菜单
INSERT INTO `sys_menu` (`parent_id`, `code`, `name`, `title`, `menu_type`, `path`, `icon`, `sort`, `visible`, `status`, `permission`, `component`, `remark`) 
VALUES (@sale_parent_id, 'M000401', 'sale-order', '销售订单', 1, '/business/sale/order', 'FileTextOutlined', 1, 1, 1, 'business:sale:order', '/Business/Sale/Order/List', '销售订单管理');

SET @sale_order_menu_id = LAST_INSERT_ID();

-- 分配给admin角色
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, @sale_parent_id);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, @sale_order_menu_id);

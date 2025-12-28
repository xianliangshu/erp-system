-- 零售管理模块 SQL 脚本 (无注释版)

CREATE TABLE IF NOT EXISTS `retail_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `out_stock_un_approve` tinyint(1) DEFAULT 0,
  `return_stock_un_approve` tinyint(1) DEFAULT 0,
  `create_by` varchar(50) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(50) DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(1) DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `retail_config` (`out_stock_un_approve`, `return_stock_un_approve`)
SELECT 0, 0 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `retail_config`);

CREATE TABLE IF NOT EXISTS `retail_out_sheet` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `code` varchar(50) NOT NULL,
  `sc_id` bigint(20) NOT NULL,
  `customer_id` bigint(20) DEFAULT NULL,
  `total_num` decimal(18,4) DEFAULT 0.0000,
  `total_amount` decimal(18,4) DEFAULT 0.0000,
  `status` tinyint(4) DEFAULT 0,
  `description` varchar(255) DEFAULT NULL,
  `create_by` varchar(50) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(50) DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(1) DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `retail_out_sheet_detail` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `sheet_id` bigint(20) NOT NULL,
  `product_id` bigint(20) NOT NULL,
  `out_num` decimal(18,4) NOT NULL,
  `tax_price` decimal(18,4) DEFAULT 0.0000,
  `tax_amount` decimal(18,4) DEFAULT 0.0000,
  `description` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `retail_return` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `code` varchar(50) NOT NULL,
  `out_sheet_id` bigint(20) DEFAULT NULL,
  `sc_id` bigint(20) NOT NULL,
  `customer_id` bigint(20) DEFAULT NULL,
  `total_num` decimal(18,4) DEFAULT 0.0000,
  `total_amount` decimal(18,4) DEFAULT 0.0000,
  `status` tinyint(4) DEFAULT 0,
  `description` varchar(255) DEFAULT NULL,
  `create_by` varchar(50) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(50) DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(1) DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `retail_return_detail` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `return_id` bigint(20) NOT NULL,
  `product_id` bigint(20) NOT NULL,
  `return_num` decimal(18,4) NOT NULL,
  `tax_price` decimal(18,4) DEFAULT 0.0000,
  `tax_amount` decimal(18,4) DEFAULT 0.0000,
  `description` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `sys_menu` (`parent_id`, `code`, `name`, `title`, `menu_type`, `path`, `icon`, `sort`, `visible`, `status`, `permission`, `component`, `remark`) 
SELECT 0, 'M0006', 'retail', '零售管理', 0, '/retail', 'ShopOutlined', 6, 1, 1, NULL, NULL, '零售管理模块'
FROM DUAL 
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE code = 'M0006');

SET @retail_parent_id = (SELECT id FROM sys_menu WHERE code = 'M0006' LIMIT 1);

INSERT INTO `sys_menu` (`parent_id`, `code`, `name`, `title`, `menu_type`, `path`, `icon`, `sort`, `visible`, `status`, `permission`, `component`, `remark`) 
SELECT @retail_parent_id, 'M000601', 'retail-config', '零售配置', 1, '/business/retail/config', 'SettingOutlined', 1, 1, 1, 'business:retail:config', '/Business/Retail/Config/index', '零售配置管理'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE code = 'M000601');

INSERT INTO `sys_menu` (`parent_id`, `code`, `name`, `title`, `menu_type`, `path`, `icon`, `sort`, `visible`, `status`, `permission`, `component`, `remark`) 
SELECT @retail_parent_id, 'M000602', 'retail-out', '零售出库单', 1, '/business/retail/out', 'ExportOutlined', 2, 1, 1, 'business:retail:out', '/Business/Retail/Outbound/List/index', '零售出库管理'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE code = 'M000602');

INSERT INTO `sys_menu` (`parent_id`, `code`, `name`, `title`, `menu_type`, `path`, `icon`, `sort`, `visible`, `status`, `permission`, `component`, `remark`) 
SELECT @retail_parent_id, 'M000603', 'retail-return', '零售退货单', 1, '/business/retail/return', 'ImportOutlined', 3, 1, 1, 'business:retail:return', '/Business/Retail/Return/List/index', '零售退货管理'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE code = 'M000603');

SET @admin_role_id = (SELECT id FROM sys_role WHERE code = 'R000001' LIMIT 1);

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) 
SELECT @admin_role_id, m.id
FROM sys_menu m
WHERE m.code LIKE 'M0006%'
AND NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = @admin_role_id AND menu_id = m.id);

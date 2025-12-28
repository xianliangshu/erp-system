-- 闆跺敭绠＄悊妯″潡 SQL 鑴氭湰

-- 1. 鍒涘缓琛?

-- 闆跺敭閰嶇疆琛?
CREATE TABLE IF NOT EXISTS `retail_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `out_stock_un_approve` tinyint(1) DEFAULT '0' COMMENT '闆跺敭鍑哄簱鍗曟槸鍚﹁嚜鍔ㄥ鏍?,
  `return_stock_un_approve` tinyint(1) DEFAULT '0' COMMENT '闆跺敭閫€璐у崟鏄惁鑷姩瀹℃牳',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='闆跺敭閰嶇疆';

-- 鍒濆鍖栭浂鍞厤缃?
INSERT INTO `retail_config` (`out_stock_un_approve`, `return_stock_un_approve`)
SELECT 0, 0 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `retail_config`);

-- 闆跺敭鍑哄簱鍗?
CREATE TABLE IF NOT EXISTS `retail_out_sheet` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `code` varchar(50) NOT NULL COMMENT '鍗曟嵁缂栧彿',
  `sc_id` bigint(20) NOT NULL COMMENT '浠撳簱ID',
  `customer_id` bigint(20) DEFAULT NULL COMMENT '瀹㈡埛ID',
  `total_num` decimal(18,4) DEFAULT '0.0000' COMMENT '鎬绘暟閲?,
  `total_amount` decimal(18,4) DEFAULT '0.0000' COMMENT '鎬婚噾棰?,
  `status` tinyint(4) DEFAULT '0' COMMENT '鐘舵€? 0-寰呭鏍? 1-宸插鏍? 2-宸叉嫆缁?,
  `description` varchar(255) DEFAULT NULL COMMENT '澶囨敞',
  `create_by` varchar(50) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(50) DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='闆跺敭鍑哄簱鍗?;

-- 闆跺敭鍑哄簱鍗曟槑缁?
CREATE TABLE IF NOT EXISTS `retail_out_sheet_detail` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `sheet_id` bigint(20) NOT NULL COMMENT '涓昏〃ID',
  `product_id` bigint(20) NOT NULL COMMENT '鍟嗗搧ID',
  `out_num` decimal(18,4) NOT NULL COMMENT '鍑哄簱鏁伴噺',
  `tax_price` decimal(18,4) DEFAULT '0.0000' COMMENT '鍚◣鍗曚环',
  `tax_amount` decimal(18,4) DEFAULT '0.0000' COMMENT '鍚◣閲戦',
  `description` varchar(255) DEFAULT NULL COMMENT '澶囨敞',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='闆跺敭鍑哄簱鍗曟槑缁?;

-- 闆跺敭閫€璐у崟
CREATE TABLE IF NOT EXISTS `retail_return` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `code` varchar(50) NOT NULL COMMENT '鍗曟嵁缂栧彿',
  `out_sheet_id` bigint(20) DEFAULT NULL COMMENT '鍏宠仈鍑哄簱鍗旾D',
  `sc_id` bigint(20) NOT NULL COMMENT '浠撳簱ID',
  `customer_id` bigint(20) DEFAULT NULL COMMENT '瀹㈡埛ID',
  `total_num` decimal(18,4) DEFAULT '0.0000' COMMENT '鎬绘暟閲?,
  `total_amount` decimal(18,4) DEFAULT '0.0000' COMMENT '鎬婚噾棰?,
  `status` tinyint(4) DEFAULT '0' COMMENT '鐘舵€? 0-寰呭鏍? 1-宸插鏍? 2-宸叉嫆缁?,
  `description` varchar(255) DEFAULT NULL COMMENT '澶囨敞',
  `create_by` varchar(50) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(50) DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='闆跺敭閫€璐у崟';

-- 闆跺敭閫€璐у崟鏄庣粏
CREATE TABLE IF NOT EXISTS `retail_return_detail` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `return_id` bigint(20) NOT NULL COMMENT '涓昏〃ID',
  `product_id` bigint(20) NOT NULL COMMENT '鍟嗗搧ID',
  `return_num` decimal(18,4) NOT NULL COMMENT '閫€璐ф暟閲?,
  `tax_price` decimal(18,4) DEFAULT '0.0000' COMMENT '鍚◣鍗曚环',
  `tax_amount` decimal(18,4) DEFAULT '0.0000' COMMENT '鍚◣閲戦',
  `description` varchar(255) DEFAULT NULL COMMENT '澶囨敞',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='闆跺敭閫€璐у崟鏄庣粏';

-- 2. 娣诲姞鑿滃崟

-- 闆跺敭绠＄悊鐩綍
INSERT INTO `sys_menu` (`parent_id`, `code`, `name`, `title`, `menu_type`, `path`, `icon`, `sort`, `visible`, `status`, `permission`, `component`, `remark`) 
SELECT 0, 'M0006', 'retail', '闆跺敭绠＄悊', 0, '/retail', 'ShopOutlined', 6, 1, 1, NULL, NULL, '闆跺敭绠＄悊妯″潡'
FROM DUAL 
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE code = 'M0006');

SET @retail_parent_id = (SELECT id FROM sys_menu WHERE code = 'M0006' LIMIT 1);

-- 闆跺敭閰嶇疆
INSERT INTO `sys_menu` (`parent_id`, `code`, `name`, `title`, `menu_type`, `path`, `icon`, `sort`, `visible`, `status`, `permission`, `component`, `remark`) 
SELECT @retail_parent_id, 'M000601', 'retail-config', '闆跺敭閰嶇疆', 1, '/business/retail/config', 'SettingOutlined', 1, 1, 1, 'business:retail:config', '/Business/Retail/Config/index', '闆跺敭閰嶇疆绠＄悊'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE code = 'M000601');

-- 闆跺敭鍑哄簱鍗?
INSERT INTO `sys_menu` (`parent_id`, `code`, `name`, `title`, `menu_type`, `path`, `icon`, `sort`, `visible`, `status`, `permission`, `component`, `remark`) 
SELECT @retail_parent_id, 'M000602', 'retail-out', '闆跺敭鍑哄簱鍗?, 1, '/business/retail/out', 'ExportOutlined', 2, 1, 1, 'business:retail:out', '/Business/Retail/Outbound/List/index', '闆跺敭鍑哄簱绠＄悊'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE code = 'M000602');

-- 闆跺敭閫€璐у崟
INSERT INTO `sys_menu` (`parent_id`, `code`, `name`, `title`, `menu_type`, `path`, `icon`, `sort`, `visible`, `status`, `permission`, `component`, `remark`) 
SELECT @retail_parent_id, 'M000603', 'retail-return', '闆跺敭閫€璐у崟', 1, '/business/retail/return', 'ImportOutlined', 3, 1, 1, 'business:retail:return', '/Business/Retail/Return/List/index', '闆跺敭閫€璐х鐞?
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE code = 'M000603');

-- 3. 鍒嗛厤鏉冮檺缁?admin 瑙掕壊
SET @admin_role_id = (SELECT id FROM sys_role WHERE code = 'R000001' LIMIT 1);

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) 
SELECT @admin_role_id, m.id
FROM sys_menu m
WHERE m.code LIKE 'M0006%'
AND NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = @admin_role_id AND menu_id = m.id);

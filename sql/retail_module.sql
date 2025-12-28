-- 零售管理模块 SQL 脚本

-- 1. 创建表

-- 零售配置表
CREATE TABLE IF NOT EXISTS `retail_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `out_stock_un_approve` tinyint(1) DEFAULT '0' COMMENT '零售出库单是否自动审核',
  `return_stock_un_approve` tinyint(1) DEFAULT '0' COMMENT '零售退货单是否自动审核',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='零售配置';

-- 初始化零售配置
INSERT INTO `retail_config` (`out_stock_un_approve`, `return_stock_un_approve`)
SELECT 0, 0 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `retail_config`);

-- 零售出库单
CREATE TABLE IF NOT EXISTS `retail_out_sheet` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `code` varchar(50) NOT NULL COMMENT '单据编号',
  `sc_id` bigint(20) NOT NULL COMMENT '仓库ID',
  `customer_id` bigint(20) DEFAULT NULL COMMENT '客户ID',
  `total_num` decimal(18,4) DEFAULT '0.0000' COMMENT '总数量',
  `total_amount` decimal(18,4) DEFAULT '0.0000' COMMENT '总金额',
  `status` tinyint(4) DEFAULT '0' COMMENT '状态: 0-待审核, 1-已审核, 2-已拒绝',
  `description` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(50) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(50) DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='零售出库单';

-- 零售出库单明细
CREATE TABLE IF NOT EXISTS `retail_out_sheet_detail` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `sheet_id` bigint(20) NOT NULL COMMENT '主表ID',
  `product_id` bigint(20) NOT NULL COMMENT '商品ID',
  `out_num` decimal(18,4) NOT NULL COMMENT '出库数量',
  `tax_price` decimal(18,4) DEFAULT '0.0000' COMMENT '含税单价',
  `tax_amount` decimal(18,4) DEFAULT '0.0000' COMMENT '含税金额',
  `description` varchar(255) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='零售出库单明细';

-- 零售退货单
CREATE TABLE IF NOT EXISTS `retail_return` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `code` varchar(50) NOT NULL COMMENT '单据编号',
  `out_sheet_id` bigint(20) DEFAULT NULL COMMENT '关联出库单ID',
  `sc_id` bigint(20) NOT NULL COMMENT '仓库ID',
  `customer_id` bigint(20) DEFAULT NULL COMMENT '客户ID',
  `total_num` decimal(18,4) DEFAULT '0.0000' COMMENT '总数量',
  `total_amount` decimal(18,4) DEFAULT '0.0000' COMMENT '总金额',
  `status` tinyint(4) DEFAULT '0' COMMENT '状态: 0-待审核, 1-已审核, 2-已拒绝',
  `description` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(50) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(50) DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='零售退货单';

-- 零售退货单明细
CREATE TABLE IF NOT EXISTS `retail_return_detail` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `return_id` bigint(20) NOT NULL COMMENT '主表ID',
  `product_id` bigint(20) NOT NULL COMMENT '商品ID',
  `return_num` decimal(18,4) NOT NULL COMMENT '退货数量',
  `tax_price` decimal(18,4) DEFAULT '0.0000' COMMENT '含税单价',
  `tax_amount` decimal(18,4) DEFAULT '0.0000' COMMENT '含税金额',
  `description` varchar(255) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='零售退货单明细';

-- 2. 添加菜单

-- 零售管理目录
INSERT INTO `sys_menu` (`parent_id`, `code`, `name`, `title`, `menu_type`, `path`, `icon`, `sort`, `visible`, `status`, `permission`, `component`, `remark`) 
SELECT 0, 'M0006', 'retail', '零售管理', 0, '/retail', 'ShopOutlined', 6, 1, 1, NULL, NULL, '零售管理模块'
FROM DUAL 
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE code = 'M0006');

SET @retail_parent_id = (SELECT id FROM sys_menu WHERE code = 'M0006' LIMIT 1);

-- 零售配置
INSERT INTO `sys_menu` (`parent_id`, `code`, `name`, `title`, `menu_type`, `path`, `icon`, `sort`, `visible`, `status`, `permission`, `component`, `remark`) 
SELECT @retail_parent_id, 'M000601', 'retail-config', '零售配置', 1, '/business/retail/config', 'SettingOutlined', 1, 1, 1, 'business:retail:config', '/Business/Retail/Config/index', '零售配置管理'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE code = 'M000601');

-- 零售出库单
INSERT INTO `sys_menu` (`parent_id`, `code`, `name`, `title`, `menu_type`, `path`, `icon`, `sort`, `visible`, `status`, `permission`, `component`, `remark`) 
SELECT @retail_parent_id, 'M000602', 'retail-out', '零售出库单', 1, '/business/retail/out', 'ExportOutlined', 2, 1, 1, 'business:retail:out', '/Business/Retail/Outbound/List/index', '零售出库管理'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE code = 'M000602');

-- 零售退货单
INSERT INTO `sys_menu` (`parent_id`, `code`, `name`, `title`, `menu_type`, `path`, `icon`, `sort`, `visible`, `status`, `permission`, `component`, `remark`) 
SELECT @retail_parent_id, 'M000603', 'retail-return', '零售退货单', 1, '/business/retail/return', 'ImportOutlined', 3, 1, 1, 'business:retail:return', '/Business/Retail/Return/List/index', '零售退货管理'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE code = 'M000603');

-- 3. 分配权限给 admin 角色
SET @admin_role_id = (SELECT id FROM sys_role WHERE code = 'R000001' LIMIT 1);

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) 
SELECT @admin_role_id, m.id
FROM sys_menu m
WHERE m.code LIKE 'M0006%'
AND NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = @admin_role_id AND menu_id = m.id);

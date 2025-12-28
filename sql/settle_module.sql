-- 结算管理模块 SQL 脚本

-- 1. 创建表

-- 收支项目表
CREATE TABLE IF NOT EXISTS `settle_in_out_item` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `code` varchar(50) NOT NULL COMMENT '编号',
  `name` varchar(100) NOT NULL COMMENT '名称',
  `item_type` tinyint(4) NOT NULL COMMENT '项目类型: 1-收入, 2-支出',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态: 0-禁用, 1-启用',
  `description` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(50) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(50) DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(1) DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收支项目';

-- 2. 添加菜单

-- 结算管理目录
INSERT INTO `sys_menu` (`parent_id`, `code`, `name`, `title`, `menu_type`, `path`, `icon`, `sort`, `visible`, `status`, `permission`, `component`, `remark`) 
SELECT 0, 'M0007', 'settle', '结算管理', 0, '/settle', 'DollarOutlined', 7, 1, 1, NULL, NULL, '结算管理模块'
FROM DUAL 
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE code = 'M0007');

SET @settle_parent_id = (SELECT id FROM sys_menu WHERE code = 'M0007' LIMIT 1);

-- 收支项目
INSERT INTO `sys_menu` (`parent_id`, `code`, `name`, `title`, `menu_type`, `path`, `icon`, `sort`, `visible`, `status`, `permission`, `component`, `remark`) 
SELECT @settle_parent_id, 'M000701', 'settle-in-out-item', '收支项目', 1, '/business/settle/item', 'BarsOutlined', 1, 1, 1, 'business:settle:item', '/Business/Settle/IncomeExpenseItem/List/index', '收支项目管理'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE code = 'M000701');

-- 3. 分配权限给 admin 角色
SET @admin_role_id = (SELECT id FROM sys_role WHERE code = 'R000001' LIMIT 1);

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) 
SELECT @admin_role_id, m.id
FROM sys_menu m
WHERE m.code LIKE 'M0007%'
AND NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = @admin_role_id AND menu_id = m.id);

-- =============================================
-- 供应商费用单
-- =============================================

-- 供应商费用单主表
CREATE TABLE IF NOT EXISTS `settle_fee_sheet` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `code` varchar(50) NOT NULL COMMENT '单据编号',
  `supplier_id` bigint(20) NOT NULL COMMENT '供应商ID',
  `sheet_type` tinyint(4) NOT NULL COMMENT '单据类型: 1-付款, 2-扣款',
  `total_amount` decimal(18,2) DEFAULT '0.00' COMMENT '总金额',
  `status` tinyint(4) DEFAULT '0' COMMENT '状态: 0-待审核, 1-已审核, 2-已拒绝',
  `description` varchar(255) DEFAULT NULL COMMENT '备注',
  `approve_by` varchar(50) DEFAULT NULL COMMENT '审核人',
  `approve_time` datetime DEFAULT NULL COMMENT '审核时间',
  `refuse_reason` varchar(255) DEFAULT NULL COMMENT '拒绝原因',
  `create_by` varchar(50) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(50) DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(1) DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商费用单';

-- 供应商费用单明细表
CREATE TABLE IF NOT EXISTS `settle_fee_sheet_detail` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `sheet_id` bigint(20) NOT NULL COMMENT '费用单ID',
  `item_id` bigint(20) NOT NULL COMMENT '收支项目ID',
  `amount` decimal(18,2) NOT NULL COMMENT '金额',
  `order_no` int(11) DEFAULT '0' COMMENT '排序',
  PRIMARY KEY (`id`),
  KEY `idx_sheet_id` (`sheet_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商费用单明细';

-- 供应商费用单菜单
INSERT INTO `sys_menu` (`parent_id`, `code`, `name`, `title`, `menu_type`, `path`, `icon`, `sort`, `visible`, `status`, `permission`, `component`, `remark`) 
SELECT @settle_parent_id, 'M000702', 'settle-fee-sheet', '供应商费用单', 1, '/business/settle/fee', 'AccountBookOutlined', 2, 1, 1, 'business:settle:fee', '/Business/Settle/FeeSheet/List/index', '供应商费用单管理'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE code = 'M000702');

-- 再次分配权限给 admin 角色（确保新菜单也被分配）
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) 
SELECT @admin_role_id, m.id
FROM sys_menu m
WHERE m.code LIKE 'M0007%'
AND NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = @admin_role_id AND menu_id = m.id);

-- =============================================
-- 供应商预付款单
-- =============================================

-- 供应商预付款单主表
CREATE TABLE IF NOT EXISTS `settle_pre_sheet` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `code` varchar(50) NOT NULL COMMENT '单据编号',
  `supplier_id` bigint(20) NOT NULL COMMENT '供应商ID',
  `total_amount` decimal(18,2) DEFAULT '0.00' COMMENT '总金额',
  `status` tinyint(4) DEFAULT '0' COMMENT '状态: 0-待审核, 1-已审核, 2-已拒绝',
  `description` varchar(255) DEFAULT NULL COMMENT '备注',
  `approve_by` varchar(50) DEFAULT NULL COMMENT '审核人',
  `approve_time` datetime DEFAULT NULL COMMENT '审核时间',
  `refuse_reason` varchar(255) DEFAULT NULL COMMENT '拒绝原因',
  `create_by` varchar(50) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(50) DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(1) DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商预付款单';

-- 供应商预付款单明细表
CREATE TABLE IF NOT EXISTS `settle_pre_sheet_detail` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `sheet_id` bigint(20) NOT NULL COMMENT '预付款单ID',
  `item_id` bigint(20) NOT NULL COMMENT '收支项目ID',
  `amount` decimal(18,2) NOT NULL COMMENT '金额',
  `order_no` int(11) DEFAULT '0' COMMENT '排序',
  PRIMARY KEY (`id`),
  KEY `idx_sheet_id` (`sheet_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商预付款单明细';

-- 供应商预付款单菜单
INSERT INTO `sys_menu` (`parent_id`, `code`, `name`, `title`, `menu_type`, `path`, `icon`, `sort`, `visible`, `status`, `permission`, `component`, `remark`) 
SELECT @settle_parent_id, 'M000703', 'settle-pre-sheet', '供应商预付款单', 1, '/business/settle/pre', 'MoneyCollectOutlined', 3, 1, 1, 'business:settle:pre', '/Business/Settle/PreSheet/List/index', '供应商预付款单管理'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE code = 'M000703');

-- 再次分配权限给 admin 角色
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) 
SELECT @admin_role_id, m.id
FROM sys_menu m
WHERE m.code LIKE 'M0007%'
AND NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = @admin_role_id AND menu_id = m.id);

-- =============================================
-- 供应商对账单
-- =============================================

-- 供应商对账单主表
CREATE TABLE IF NOT EXISTS `settle_check_sheet` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `code` varchar(50) NOT NULL COMMENT '单据编号',
  `supplier_id` bigint(20) NOT NULL COMMENT '供应商ID',
  `total_amount` decimal(18,2) DEFAULT '0.00' COMMENT '应付总金额',
  `total_payed_amount` decimal(18,2) DEFAULT '0.00' COMMENT '已付金额',
  `total_discount_amount` decimal(18,2) DEFAULT '0.00' COMMENT '优惠金额',
  `start_date` date DEFAULT NULL COMMENT '起始日期',
  `end_date` date DEFAULT NULL COMMENT '截止日期',
  `status` tinyint(4) DEFAULT '0' COMMENT '状态: 0-待审核, 1-已审核, 2-已拒绝',
  `description` varchar(255) DEFAULT NULL COMMENT '备注',
  `approve_by` varchar(50) DEFAULT NULL COMMENT '审核人',
  `approve_time` datetime DEFAULT NULL COMMENT '审核时间',
  `refuse_reason` varchar(255) DEFAULT NULL COMMENT '拒绝原因',
  `create_by` varchar(50) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(50) DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(1) DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商对账单';

-- 供应商对账单明细表
CREATE TABLE IF NOT EXISTS `settle_check_sheet_detail` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `sheet_id` bigint(20) NOT NULL COMMENT '对账单ID',
  `biz_id` bigint(20) NOT NULL COMMENT '业务单据ID',
  `biz_type` tinyint(4) NOT NULL COMMENT '业务类型: 1-采购入库, 2-采购退货, 3-费用单, 4-预付款',
  `biz_code` varchar(50) DEFAULT NULL COMMENT '业务单据编号',
  `pay_amount` decimal(18,2) NOT NULL COMMENT '应付金额',
  `description` varchar(255) DEFAULT NULL COMMENT '备注',
  `order_no` int(11) DEFAULT '0' COMMENT '排序',
  PRIMARY KEY (`id`),
  KEY `idx_sheet_id` (`sheet_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商对账单明细';

-- 供应商对账单菜单
INSERT INTO `sys_menu` (`parent_id`, `code`, `name`, `title`, `menu_type`, `path`, `icon`, `sort`, `visible`, `status`, `permission`, `component`, `remark`) 
SELECT @settle_parent_id, 'M000704', 'settle-check-sheet', '供应商对账单', 1, '/business/settle/check', 'ReconciliationOutlined', 4, 1, 1, 'business:settle:check', '/Business/Settle/CheckSheet/List/index', '供应商对账单管理'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE code = 'M000704');

-- 再次分配权限给 admin 角色
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) 
SELECT @admin_role_id, m.id
FROM sys_menu m
WHERE m.code LIKE 'M0007%'
AND NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = @admin_role_id AND menu_id = m.id);

-- =============================================
-- 供应商结算单
-- =============================================

-- 供应商结算单主表
CREATE TABLE IF NOT EXISTS `settle_sheet` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `code` varchar(50) NOT NULL COMMENT '单据编号',
  `supplier_id` bigint(20) NOT NULL COMMENT '供应商ID',
  `total_amount` decimal(18,2) DEFAULT '0.00' COMMENT '应付总金额',
  `total_discount_amount` decimal(18,2) DEFAULT '0.00' COMMENT '优惠金额',
  `start_date` date DEFAULT NULL COMMENT '起始日期',
  `end_date` date DEFAULT NULL COMMENT '截止日期',
  `status` tinyint(4) DEFAULT '0' COMMENT '状态: 0-待审核, 1-已审核, 2-已拒绝',
  `description` varchar(255) DEFAULT NULL COMMENT '备注',
  `approve_by` varchar(50) DEFAULT NULL COMMENT '审核人',
  `approve_time` datetime DEFAULT NULL COMMENT '审核时间',
  `refuse_reason` varchar(255) DEFAULT NULL COMMENT '拒绝原因',
  `create_by` varchar(50) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(50) DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(1) DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商结算单';

-- 供应商结算单明细表
CREATE TABLE IF NOT EXISTS `settle_sheet_detail` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `sheet_id` bigint(20) NOT NULL COMMENT '结算单ID',
  `biz_id` bigint(20) NOT NULL COMMENT '对账单ID',
  `biz_code` varchar(50) DEFAULT NULL COMMENT '对账单编号',
  `pay_amount` decimal(18,2) NOT NULL COMMENT '实付金额',
  `discount_amount` decimal(18,2) DEFAULT '0.00' COMMENT '优惠金额',
  `description` varchar(255) DEFAULT NULL COMMENT '备注',
  `order_no` int(11) DEFAULT '0' COMMENT '排序',
  PRIMARY KEY (`id`),
  KEY `idx_sheet_id` (`sheet_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商结算单明细';

-- 供应商结算单菜单
INSERT INTO `sys_menu` (`parent_id`, `code`, `name`, `title`, `menu_type`, `path`, `icon`, `sort`, `visible`, `status`, `permission`, `component`, `remark`) 
SELECT @settle_parent_id, 'M000705', 'settle-sheet', '供应商结算单', 1, '/business/settle/sheet', 'TransactionOutlined', 5, 1, 1, 'business:settle:sheet', '/Business/Settle/SettleSheet/List/index', '供应商结算单管理'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE code = 'M000705');

-- 再次分配权限给 admin 角色
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) 
SELECT @admin_role_id, m.id
FROM sys_menu m
WHERE m.code LIKE 'M0007%'
AND NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = @admin_role_id AND menu_id = m.id);

-- =============================================
-- 客户结算模块
-- =============================================

-- 客户费用单主表
CREATE TABLE IF NOT EXISTS `customer_fee_sheet` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `code` varchar(50) NOT NULL COMMENT '单据编号',
  `customer_id` bigint(20) NOT NULL COMMENT '客户ID',
  `sheet_type` tinyint(4) NOT NULL COMMENT '单据类型: 1-收款, 2-扣款',
  `total_amount` decimal(18,2) DEFAULT '0.00' COMMENT '总金额',
  `status` tinyint(4) DEFAULT '0' COMMENT '状态: 0-待审核, 1-已审核, 2-已拒绝',
  `description` varchar(255) DEFAULT NULL,
  `approve_by` varchar(50) DEFAULT NULL,
  `approve_time` datetime DEFAULT NULL,
  `refuse_reason` varchar(255) DEFAULT NULL,
  `create_by` varchar(50) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(50) DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户费用单';

CREATE TABLE IF NOT EXISTS `customer_fee_sheet_detail` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `sheet_id` bigint(20) NOT NULL,
  `item_id` bigint(20) NOT NULL,
  `amount` decimal(18,2) NOT NULL,
  `order_no` int(11) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_sheet_id` (`sheet_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户费用单明细';

-- 客户预收款单
CREATE TABLE IF NOT EXISTS `customer_pre_sheet` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `code` varchar(50) NOT NULL,
  `customer_id` bigint(20) NOT NULL,
  `total_amount` decimal(18,2) DEFAULT '0.00',
  `status` tinyint(4) DEFAULT '0',
  `description` varchar(255) DEFAULT NULL,
  `approve_by` varchar(50) DEFAULT NULL,
  `approve_time` datetime DEFAULT NULL,
  `refuse_reason` varchar(255) DEFAULT NULL,
  `create_by` varchar(50) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(50) DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户预收款单';

CREATE TABLE IF NOT EXISTS `customer_pre_sheet_detail` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `sheet_id` bigint(20) NOT NULL,
  `item_id` bigint(20) NOT NULL,
  `amount` decimal(18,2) NOT NULL,
  `order_no` int(11) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_sheet_id` (`sheet_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户预收款单明细';

-- 客户对账单
CREATE TABLE IF NOT EXISTS `customer_check_sheet` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `code` varchar(50) NOT NULL,
  `customer_id` bigint(20) NOT NULL,
  `total_amount` decimal(18,2) DEFAULT '0.00',
  `total_payed_amount` decimal(18,2) DEFAULT '0.00',
  `total_discount_amount` decimal(18,2) DEFAULT '0.00',
  `start_date` date DEFAULT NULL,
  `end_date` date DEFAULT NULL,
  `status` tinyint(4) DEFAULT '0',
  `description` varchar(255) DEFAULT NULL,
  `approve_by` varchar(50) DEFAULT NULL,
  `approve_time` datetime DEFAULT NULL,
  `refuse_reason` varchar(255) DEFAULT NULL,
  `create_by` varchar(50) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(50) DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户对账单';

CREATE TABLE IF NOT EXISTS `customer_check_sheet_detail` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `sheet_id` bigint(20) NOT NULL,
  `biz_id` bigint(20) NOT NULL,
  `biz_type` tinyint(4) NOT NULL,
  `biz_code` varchar(50) DEFAULT NULL,
  `pay_amount` decimal(18,2) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `order_no` int(11) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_sheet_id` (`sheet_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户对账单明细';

-- 客户结算单
CREATE TABLE IF NOT EXISTS `customer_settle_sheet` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `code` varchar(50) NOT NULL,
  `customer_id` bigint(20) NOT NULL,
  `total_amount` decimal(18,2) DEFAULT '0.00',
  `total_discount_amount` decimal(18,2) DEFAULT '0.00',
  `start_date` date DEFAULT NULL,
  `end_date` date DEFAULT NULL,
  `status` tinyint(4) DEFAULT '0',
  `description` varchar(255) DEFAULT NULL,
  `approve_by` varchar(50) DEFAULT NULL,
  `approve_time` datetime DEFAULT NULL,
  `refuse_reason` varchar(255) DEFAULT NULL,
  `create_by` varchar(50) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(50) DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户结算单';

CREATE TABLE IF NOT EXISTS `customer_settle_sheet_detail` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `sheet_id` bigint(20) NOT NULL,
  `biz_id` bigint(20) NOT NULL,
  `biz_code` varchar(50) DEFAULT NULL,
  `pay_amount` decimal(18,2) NOT NULL,
  `discount_amount` decimal(18,2) DEFAULT '0.00',
  `description` varchar(255) DEFAULT NULL,
  `order_no` int(11) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_sheet_id` (`sheet_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户结算单明细';

-- 客户结算菜单
INSERT INTO `sys_menu` (`parent_id`, `code`, `name`, `title`, `menu_type`, `path`, `icon`, `sort`, `visible`, `status`, `permission`, `component`, `remark`) 
SELECT @settle_parent_id, 'M000706', 'customer-fee-sheet', '客户费用单', 1, '/business/settle/customer/fee', 'AccountBookOutlined', 6, 1, 1, 'business:settle:customer:fee', '/Business/Settle/CustomerFeeSheet/List/index', '客户费用单'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE code = 'M000706');

INSERT INTO `sys_menu` (`parent_id`, `code`, `name`, `title`, `menu_type`, `path`, `icon`, `sort`, `visible`, `status`, `permission`, `component`, `remark`) 
SELECT @settle_parent_id, 'M000707', 'customer-pre-sheet', '客户预收款单', 1, '/business/settle/customer/pre', 'MoneyCollectOutlined', 7, 1, 1, 'business:settle:customer:pre', '/Business/Settle/CustomerPreSheet/List/index', '客户预收款单'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE code = 'M000707');

INSERT INTO `sys_menu` (`parent_id`, `code`, `name`, `title`, `menu_type`, `path`, `icon`, `sort`, `visible`, `status`, `permission`, `component`, `remark`) 
SELECT @settle_parent_id, 'M000708', 'customer-check-sheet', '客户对账单', 1, '/business/settle/customer/check', 'ReconciliationOutlined', 8, 1, 1, 'business:settle:customer:check', '/Business/Settle/CustomerCheckSheet/List/index', '客户对账单'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE code = 'M000708');

INSERT INTO `sys_menu` (`parent_id`, `code`, `name`, `title`, `menu_type`, `path`, `icon`, `sort`, `visible`, `status`, `permission`, `component`, `remark`) 
SELECT @settle_parent_id, 'M000709', 'customer-settle-sheet', '客户结算单', 1, '/business/settle/customer/sheet', 'TransactionOutlined', 9, 1, 1, 'business:settle:customer:sheet', '/Business/Settle/CustomerSettleSheet/List/index', '客户结算单'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE code = 'M000709');

-- 分配权限给 admin 角色
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) 
SELECT @admin_role_id, m.id
FROM sys_menu m
WHERE m.code LIKE 'M0007%'
AND NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = @admin_role_id AND menu_id = m.id);

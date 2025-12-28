-- ================================================================
-- 库存调整模块数据库脚本
-- 创建时间: 2025-12-28
-- ================================================================

-- ================================================================
-- 1. 库存调整原因表
-- ================================================================
DROP TABLE IF EXISTS `stock_adjust_reason`;
CREATE TABLE `stock_adjust_reason` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `code` varchar(32) NOT NULL COMMENT '编号',
    `name` varchar(50) NOT NULL COMMENT '名称',
    `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
    `remark` varchar(200) DEFAULT NULL COMMENT '备注',
    `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除: 0-否 1-是',
    `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `update_by` varchar(32) DEFAULT NULL COMMENT '修改人',
    `update_time` datetime DEFAULT NULL COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存调整原因';

-- 初始化调整原因数据
INSERT INTO `stock_adjust_reason` (`code`, `name`, `status`, `remark`, `create_by`, `create_time`) VALUES
('001', '初始化数据', 1, '系统内置', 'admin', NOW()),
('002', '盘盈入库', 1, '盘点盈余入库', 'admin', NOW()),
('003', '盘亏出库', 1, '盘点亏损出库', 'admin', NOW()),
('004', '报损出库', 1, '商品报损出库', 'admin', NOW()),
('005', '其他调整', 1, '其他原因调整', 'admin', NOW());

-- ================================================================
-- 2. 库存调整单表
-- ================================================================
DROP TABLE IF EXISTS `stock_adjust_sheet`;
CREATE TABLE `stock_adjust_sheet` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `code` varchar(32) NOT NULL COMMENT '调整单编号',
    `sc_id` bigint NOT NULL COMMENT '仓库ID',
    `reason_id` bigint NOT NULL COMMENT '调整原因ID',
    `biz_type` tinyint NOT NULL COMMENT '业务类型: 0-入库调整 1-出库调整',
    `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态: 0-待审核 1-审核通过 2-审核拒绝',
    `description` varchar(200) DEFAULT NULL COMMENT '备注',
    `approve_by` varchar(32) DEFAULT NULL COMMENT '审核人',
    `approve_time` datetime DEFAULT NULL COMMENT '审核时间',
    `refuse_reason` varchar(200) DEFAULT NULL COMMENT '拒绝原因',
    `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除: 0-否 1-是',
    `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `update_by` varchar(32) DEFAULT NULL COMMENT '修改人',
    `update_time` datetime DEFAULT NULL COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`),
    KEY `idx_sc_id` (`sc_id`),
    KEY `idx_reason_id` (`reason_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存调整单';

-- ================================================================
-- 3. 库存调整单明细表
-- ================================================================
DROP TABLE IF EXISTS `stock_adjust_sheet_detail`;
CREATE TABLE `stock_adjust_sheet_detail` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `sheet_id` bigint NOT NULL COMMENT '调整单ID',
    `product_id` bigint NOT NULL COMMENT '商品ID',
    `stock_num` decimal(16,2) NOT NULL COMMENT '调整库存数量',
    `description` varchar(200) DEFAULT NULL COMMENT '备注',
    `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
    PRIMARY KEY (`id`),
    KEY `idx_sheet_id` (`sheet_id`),
    UNIQUE KEY `uk_sheet_product` (`sheet_id`, `product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存调整单明细';

-- ================================================================
-- 4. 菜单数据
-- ================================================================

-- 获取库存管理的父菜单ID
SET @stock_parent_id = (SELECT id FROM sys_menu WHERE name = '库存查询' LIMIT 1);
SET @stock_manage_id = (SELECT id FROM sys_menu WHERE title = '库存管理' LIMIT 1);

-- 如果找不到库存管理菜单，使用固定值
SET @parent_id = IFNULL(@stock_manage_id, 5);

-- 插入库存调整原因菜单
INSERT INTO `sys_menu` (`parent_id`, `title`, `name`, `path`, `component`, `icon`, `sort`, `hidden`, `status`, `permission`, `menu_type`, `create_by`, `create_time`) VALUES
(@parent_id, '调整原因', 'StockAdjustReason', '/stock/adjust/reason', '/stock/adjust/reason/index', 'SettingOutlined', 60, 0, 1, 'stock:adjust:reason:query', 1, 'admin', NOW());

SET @reason_menu_id = LAST_INSERT_ID();

-- 调整原因按钮权限
INSERT INTO `sys_menu` (`parent_id`, `title`, `name`, `path`, `component`, `icon`, `sort`, `hidden`, `status`, `permission`, `menu_type`, `create_by`, `create_time`) VALUES
(@reason_menu_id, '新增', '', '', '', '', 1, 0, 1, 'stock:adjust:reason:add', 2, 'admin', NOW()),
(@reason_menu_id, '修改', '', '', '', '', 2, 0, 1, 'stock:adjust:reason:modify', 2, 'admin', NOW()),
(@reason_menu_id, '删除', '', '', '', '', 3, 0, 1, 'stock:adjust:reason:delete', 2, 'admin', NOW());

-- 插入库存调整单菜单
INSERT INTO `sys_menu` (`parent_id`, `title`, `name`, `path`, `component`, `icon`, `sort`, `hidden`, `status`, `permission`, `menu_type`, `create_by`, `create_time`) VALUES
(@parent_id, '库存调整', 'StockAdjustSheet', '/stock/adjust/sheet', '/stock/adjust/sheet/index', 'SwapOutlined', 61, 0, 1, 'stock:adjust:query', 1, 'admin', NOW());

SET @sheet_menu_id = LAST_INSERT_ID();

-- 调整单按钮权限
INSERT INTO `sys_menu` (`parent_id`, `title`, `name`, `path`, `component`, `icon`, `sort`, `hidden`, `status`, `permission`, `menu_type`, `create_by`, `create_time`) VALUES
(@sheet_menu_id, '新增', '', '', '', '', 1, 0, 1, 'stock:adjust:add', 2, 'admin', NOW()),
(@sheet_menu_id, '修改', '', '', '', '', 2, 0, 1, 'stock:adjust:modify', 2, 'admin', NOW()),
(@sheet_menu_id, '删除', '', '', '', '', 3, 0, 1, 'stock:adjust:delete', 2, 'admin', NOW()),
(@sheet_menu_id, '审核', '', '', '', '', 4, 0, 1, 'stock:adjust:approve', 2, 'admin', NOW());

-- 为管理员角色分配新菜单权限
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `create_time`)
SELECT 1, id, NOW() FROM sys_menu WHERE permission LIKE 'stock:adjust%';

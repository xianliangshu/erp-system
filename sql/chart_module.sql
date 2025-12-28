-- =============================================
-- 报表统计模块 SQL
-- =============================================

-- 获取结算管理父菜单ID（用于定位）
SET @admin_role_id = (SELECT id FROM sys_role WHERE code = 'R000001' LIMIT 1);

-- 统计报表父菜单
INSERT INTO `sys_menu` (`parent_id`, `code`, `name`, `title`, `menu_type`, `path`, `icon`, `sort`, `visible`, `status`, `permission`, `component`, `remark`) 
SELECT 0, 'M000800', 'chart', '统计报表', 0, '/chart', 'AreaChartOutlined', 80, 1, 1, NULL, NULL, '统计报表'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE code = 'M000800');

SET @chart_parent_id = (SELECT id FROM sys_menu WHERE code = 'M000800');

-- 仪表盘
INSERT INTO `sys_menu` (`parent_id`, `code`, `name`, `title`, `menu_type`, `path`, `icon`, `sort`, `visible`, `status`, `permission`, `component`, `remark`) 
SELECT @chart_parent_id, 'M000801', 'chart-dashboard', '数据看板', 1, '/chart/dashboard', 'DashboardOutlined', 1, 1, 1, 'chart:dashboard', '/Business/Chart/Dashboard/index', '数据看板'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE code = 'M000801');

-- 采购统计
INSERT INTO `sys_menu` (`parent_id`, `code`, `name`, `title`, `menu_type`, `path`, `icon`, `sort`, `visible`, `status`, `permission`, `component`, `remark`) 
SELECT @chart_parent_id, 'M000802', 'chart-purchase', '采购统计', 1, '/chart/purchase', 'ShoppingCartOutlined', 2, 1, 1, 'chart:purchase', '/Business/Chart/Purchase/index', '采购统计'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE code = 'M000802');

-- 销售统计
INSERT INTO `sys_menu` (`parent_id`, `code`, `name`, `title`, `menu_type`, `path`, `icon`, `sort`, `visible`, `status`, `permission`, `component`, `remark`) 
SELECT @chart_parent_id, 'M000803', 'chart-sales', '销售统计', 1, '/chart/sales', 'RiseOutlined', 3, 1, 1, 'chart:sales', '/Business/Chart/Sales/index', '销售统计'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE code = 'M000803');

-- 库存报表
INSERT INTO `sys_menu` (`parent_id`, `code`, `name`, `title`, `menu_type`, `path`, `icon`, `sort`, `visible`, `status`, `permission`, `component`, `remark`) 
SELECT @chart_parent_id, 'M000804', 'chart-stock', '库存报表', 1, '/chart/stock', 'DatabaseOutlined', 4, 1, 1, 'chart:stock', '/Business/Chart/Stock/index', '库存报表'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE code = 'M000804');

-- 进销存汇总
INSERT INTO `sys_menu` (`parent_id`, `code`, `name`, `title`, `menu_type`, `path`, `icon`, `sort`, `visible`, `status`, `permission`, `component`, `remark`) 
SELECT @chart_parent_id, 'M000805', 'chart-summary', '进销存汇总', 1, '/chart/summary', 'FundOutlined', 5, 1, 1, 'chart:summary', '/Business/Chart/Summary/index', '进销存汇总'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE code = 'M000805');

-- 分配权限给 admin 角色
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) 
SELECT @admin_role_id, m.id
FROM sys_menu m
WHERE m.code LIKE 'M0008%'
AND NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = @admin_role_id AND menu_id = m.id);

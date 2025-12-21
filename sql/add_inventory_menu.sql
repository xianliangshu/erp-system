-- 添加库存管理菜单 (修正版)

-- 1. 添加库存管理目录 (如果不存在)
INSERT INTO `sys_menu` (`parent_id`, `code`, `name`, `title`, `menu_type`, `path`, `icon`, `sort`, `visible`, `status`, `permission`, `component`, `remark`) 
SELECT 0, 'M0002', 'inventory', '库存管理', 0, '/inventory', 'StockOutlined', 2, 1, 1, NULL, NULL, '库存管理模块'
FROM DUAL 
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE name = 'inventory' OR title = '库存管理');

-- 2. 获取库存管理目录ID
SET @inventory_parent_id = (SELECT id FROM sys_menu WHERE name = 'inventory' OR title = '库存管理' LIMIT 1);

-- 3. 添加库存查询菜单 (如果不存在)
INSERT INTO `sys_menu` (`parent_id`, `code`, `name`, `title`, `menu_type`, `path`, `icon`, `sort`, `visible`, `status`, `permission`, `component`, `remark`) 
SELECT @inventory_parent_id, 'M000201', 'stock-query', '库存查询', 1, '/business/stock/query', 'SearchOutlined', 1, 1, 1, 'business:stock:query', '/Business/Stock/Query', '实时库存查询'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE code = 'M000201' OR name = 'stock-query');

-- 4. 获取库存查询菜单ID
SET @stock_query_menu_id = (SELECT id FROM sys_menu WHERE code = 'M000201' OR name = 'stock-query' LIMIT 1);

-- 5. 为admin角色分配权限 (如果不存在关联)
SET @admin_role_id = (SELECT id FROM sys_role WHERE code = 'R000001' LIMIT 1);

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) 
SELECT @admin_role_id, @stock_query_menu_id
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = @admin_role_id AND menu_id = @stock_query_menu_id);

-- 6. 验证结果
SELECT m.id, m.parent_id, m.code, m.name, m.title, m.path, m.icon, m.sort 
FROM sys_menu m 
WHERE m.code LIKE 'M0002%';

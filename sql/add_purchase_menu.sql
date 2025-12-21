-- 添加采购管理菜单

-- 1. 添加采购管理目录 (如果不存在)
INSERT INTO `sys_menu` (`parent_id`, `code`, `name`, `title`, `menu_type`, `path`, `icon`, `sort`, `visible`, `status`, `permission`, `component`, `remark`) 
SELECT 0, 'M0003', 'purchase', '采购管理', 0, '/purchase', 'ShoppingCartOutlined', 3, 1, 1, NULL, NULL, '采购管理模块'
FROM DUAL 
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE code = 'M0003' OR name = 'purchase');

-- 2. 获取采购管理目录ID
SET @purchase_parent_id = (SELECT id FROM sys_menu WHERE code = 'M0003' OR name = 'purchase' LIMIT 1);

-- 3. 添加采购订单菜单 (如果不存在)
INSERT INTO `sys_menu` (`parent_id`, `code`, `name`, `title`, `menu_type`, `path`, `icon`, `sort`, `visible`, `status`, `permission`, `component`, `remark`) 
SELECT @purchase_parent_id, 'M000301', 'purchase-order', '采购订单', 1, '/business/purchase/order', 'FileTextOutlined', 1, 1, 1, 'business:purchase:order', '/Business/Purchase/Order/List', '采购订单管理'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE code = 'M000301' OR name = 'purchase-order');

-- 4. 获取采购订单菜单ID
SET @purchase_order_menu_id = (SELECT id FROM sys_menu WHERE code = 'M000301' OR name = 'purchase-order' LIMIT 1);

-- 5. 为admin角色分配权限 (如果不存在关联)
SET @admin_role_id = (SELECT id FROM sys_role WHERE code = 'R000001' LIMIT 1);

-- 分配采购管理目录权限
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) 
SELECT @admin_role_id, @purchase_parent_id
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = @admin_role_id AND menu_id = @purchase_parent_id);

-- 分配采购订单菜单权限
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) 
SELECT @admin_role_id, @purchase_order_menu_id
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = @admin_role_id AND menu_id = @purchase_order_menu_id);

-- 6. 验证结果
SELECT m.id, m.parent_id, m.code, m.name, m.title, m.path, m.sort 
FROM sys_menu m 
WHERE m.code LIKE 'M0003%';

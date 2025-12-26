-- 添加采购退货菜单

-- 1. 获取采购管理目录ID
SET @purchase_parent_id = (SELECT id FROM sys_menu WHERE code = 'M0003' OR name = 'purchase' LIMIT 1);

-- 2. 添加采购退货菜单 (如果不存在)
INSERT INTO `sys_menu` (`parent_id`, `code`, `name`, `title`, `menu_type`, `path`, `icon`, `sort`, `visible`, `status`, `permission`, `component`, `remark`) 
SELECT @purchase_parent_id, 'M000303', 'purchase-return', '采购退货', 1, '/business/purchase/return', 'RollbackOutlined', 3, 1, 1, 'business:purchase:return', '/Business/Purchase/Return/List', '采购退货管理'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE code = 'M000303' OR name = 'purchase-return');

-- 3. 获取采购退货菜单ID
SET @purchase_return_menu_id = (SELECT id FROM sys_menu WHERE code = 'M000303' OR name = 'purchase-return' LIMIT 1);

-- 4. 为admin角色分配权限 (如果不存在关联)
SET @admin_role_id = (SELECT id FROM sys_role WHERE code = 'R000001' LIMIT 1);

-- 分配采购退货菜单权限
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) 
SELECT @admin_role_id, @purchase_return_menu_id
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = @admin_role_id AND menu_id = @purchase_return_menu_id);

-- 5. 验证结果
SELECT m.id, m.parent_id, m.code, m.name, m.title, m.path, m.sort 
FROM sys_menu m 
WHERE m.code LIKE 'M0003%';

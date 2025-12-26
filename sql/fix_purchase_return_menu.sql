-- 检查采购相关菜单
SELECT m.id, m.parent_id, m.code, m.name, m.title, m.path, m.sort, m.visible, m.status
FROM sys_menu m 
WHERE m.code LIKE 'M0003%' OR m.path LIKE '%purchase%';

-- 检查角色菜单关联
SELECT rm.role_id, rm.menu_id, m.name, m.title, m.path
FROM sys_role_menu rm
LEFT JOIN sys_menu m ON rm.menu_id = m.id
WHERE m.path LIKE '%purchase%';

-- 如果上面查询没有采购退货，手动插入
-- 先获取采购管理的ID
SELECT @purchase_parent_id := id FROM sys_menu WHERE code = 'M0003' OR (name = 'purchase' AND parent_id = 0) LIMIT 1;
SELECT @purchase_parent_id AS purchase_parent_id;

-- 如果采购退货不存在，直接插入
INSERT INTO `sys_menu` (`parent_id`, `code`, `name`, `title`, `menu_type`, `path`, `icon`, `sort`, `visible`, `status`, `permission`, `component`, `remark`) 
VALUES (@purchase_parent_id, 'M000303', 'purchase-return', '采购退货', 1, '/business/purchase/return', 'RollbackOutlined', 3, 1, 1, 'business:purchase:return', '/Business/Purchase/Return/List', '采购退货管理');

-- 获取新插入的菜单ID
SELECT @purchase_return_menu_id := id FROM sys_menu WHERE code = 'M000303' LIMIT 1;
SELECT @purchase_return_menu_id AS purchase_return_menu_id;

-- 获取admin角色ID
SELECT @admin_role_id := id FROM sys_role WHERE code = 'R000001' LIMIT 1;
SELECT @admin_role_id AS admin_role_id;

-- 分配权限给admin
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (@admin_role_id, @purchase_return_menu_id);

-- 再次确认
SELECT m.id, m.parent_id, m.code, m.name, m.title, m.path, m.sort 
FROM sys_menu m 
WHERE m.path LIKE '%purchase%';

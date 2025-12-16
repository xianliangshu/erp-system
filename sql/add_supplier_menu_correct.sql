-- 添加供应商管理菜单

-- 1. 获取基础数据菜单ID
SET @basedata_menu_id = (SELECT id FROM sys_menu WHERE code = 'M000100');

-- 2. 添加供应商管理菜单
INSERT INTO `sys_menu` (`parent_id`, `code`, `name`, `title`, `menu_type`, `path`, `icon`, `sort`, `visible`, `status`, `permission`, `component`, `remark`) 
SELECT @basedata_menu_id, 'M000105', 'Supplier', '供应商管理', 1, '/basedata/supplier', 'TeamOutlined', 4, 1, 1, 'basedata:supplier:list', '/Basedata/Supplier', '供应商信息管理'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE code = 'M000105');

-- 3. 获取供应商管理菜单ID
SET @supplier_menu_id = (SELECT id FROM sys_menu WHERE code = 'M000105');

-- 4. 为admin角色分配权限
SET @admin_role_id = (SELECT id FROM sys_role WHERE code = 'R000001' LIMIT 1);

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) 
SELECT @admin_role_id, @supplier_menu_id 
FROM DUAL
WHERE @admin_role_id IS NOT NULL 
AND @supplier_menu_id IS NOT NULL
AND NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = @admin_role_id AND menu_id = @supplier_menu_id);

-- 5. 验证结果
SELECT '新增的供应商菜单:' as info;
SELECT m.id, m.parent_id, m.code, m.name, m.title, m.menu_type, m.path, m.icon, m.sort 
FROM sys_menu m 
WHERE m.code = 'M000105';

SELECT '角色菜单关联:' as info;
SELECT rm.role_id, r.name as role_name, rm.menu_id, m.title as menu_title
FROM sys_role_menu rm
JOIN sys_role r ON rm.role_id = r.id
JOIN sys_menu m ON rm.menu_id = m.id
WHERE m.code = 'M000105';

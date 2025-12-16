-- 添加供应商管理菜单 (修正版)

-- 1. 先查看基础数据菜单的parent_id
SELECT '查询基础数据相关菜单:' as info;
SELECT id, parent_id, code, name, title FROM sys_menu WHERE name LIKE '%material%' OR code LIKE '%M0001%' ORDER BY id;

-- 2. 根据物料管理的parent_id来确定基础数据菜单ID
-- 从截图看,物料分类(material-category)的parent_id是27
SET @material_parent_id = 27;

-- 3. 添加供应商管理菜单 (与物料管理同级)
INSERT INTO `sys_menu` (`parent_id`, `code`, `name`, `title`, `menu_type`, `path`, `icon`, `sort`, `visible`, `status`, `permission`, `component`, `remark`) 
VALUES (@material_parent_id, 'M000106', 'supplier', '供应商管理', 1, '/basedata/supplier', 'TeamOutlined', 5, 1, 1, 'basedata:supplier:list', '/Basedata/Supplier', '供应商信息管理');

-- 4. 获取刚插入的供应商菜单ID
SET @supplier_menu_id = LAST_INSERT_ID();

-- 5. 为admin角色分配权限
SET @admin_role_id = (SELECT id FROM sys_role WHERE code = 'R000001' LIMIT 1);

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) 
VALUES (@admin_role_id, @supplier_menu_id);

-- 6. 验证结果
SELECT '新增的供应商菜单:' as info;
SELECT m.id, m.parent_id, m.code, m.name, m.title, m.menu_type, m.path, m.icon, m.sort 
FROM sys_menu m 
WHERE m.code = 'M000106' OR m.name = 'supplier';

SELECT '角色菜单关联:' as info;
SELECT rm.role_id, r.name as role_name, rm.menu_id, m.title as menu_title
FROM sys_role_menu rm
JOIN sys_role r ON rm.role_id = r.id
JOIN sys_menu m ON rm.menu_id = m.id
WHERE m.code = 'M000106' OR m.name = 'supplier';

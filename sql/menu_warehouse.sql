-- 添加基础数据管理菜单 (可重复执行)
-- 清理可能存在的旧数据(可选,如果需要重新导入)
-- DELETE FROM sys_menu WHERE code IN ('M000100', 'M000101', 'B000101', 'B000102', 'B000103', 'B000104');

-- 1. 添加"基础数据"一级菜单(目录) - 如果不存在则插入
INSERT INTO `sys_menu` (`parent_id`, `code`, `name`, `title`, `menu_type`, `path`, `icon`, `sort`, `visible`, `status`, `permission`, `component`, `remark`) 
SELECT 0, 'M000100', 'Basedata', '基础数据', 0, '/basedata', 'ant-design:database-outlined', 2, 1, 1, NULL, NULL, '基础数据管理模块'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE code = 'M000100');

-- 获取基础数据菜单ID
SET @basedata_menu_id = (SELECT id FROM sys_menu WHERE code = 'M000100');

-- 2. 添加"仓库管理"二级菜单 - 如果不存在则插入
INSERT INTO `sys_menu` (`parent_id`, `code`, `name`, `title`, `menu_type`, `path`, `icon`, `sort`, `visible`, `status`, `permission`, `component`, `remark`) 
SELECT @basedata_menu_id, 'M000101', 'Warehouse', '仓库管理', 1, '/basedata/warehouse', 'ant-design:home-outlined', 1, 1, 1, 'basedata:warehouse:list', 'basedata/warehouse/index', '仓库信息管理'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE code = 'M000101');

-- 获取仓库管理菜单ID
SET @warehouse_menu_id = (SELECT id FROM sys_menu WHERE code = 'M000101');

-- 3. 添加仓库管理的按钮权限 - 如果不存在则插入
INSERT INTO `sys_menu` (`parent_id`, `code`, `name`, `title`, `menu_type`, `path`, `icon`, `sort`, `visible`, `status`, `permission`, `component`, `remark`) 
SELECT @warehouse_menu_id, 'B000101', 'WarehouseAdd', '新增仓库', 2, NULL, NULL, 1, 1, 1, 'basedata:warehouse:add', NULL, '新增仓库按钮'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE code = 'B000101');

INSERT INTO `sys_menu` (`parent_id`, `code`, `name`, `title`, `menu_type`, `path`, `icon`, `sort`, `visible`, `status`, `permission`, `component`, `remark`) 
SELECT @warehouse_menu_id, 'B000102', 'WarehouseEdit', '编辑仓库', 2, NULL, NULL, 2, 1, 1, 'basedata:warehouse:edit', NULL, '编辑仓库按钮'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE code = 'B000102');

INSERT INTO `sys_menu` (`parent_id`, `code`, `name`, `title`, `menu_type`, `path`, `icon`, `sort`, `visible`, `status`, `permission`, `component`, `remark`) 
SELECT @warehouse_menu_id, 'B000103', 'WarehouseDelete', '删除仓库', 2, NULL, NULL, 3, 1, 1, 'basedata:warehouse:delete', NULL, '删除仓库按钮'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE code = 'B000103');

INSERT INTO `sys_menu` (`parent_id`, `code`, `name`, `title`, `menu_type`, `path`, `icon`, `sort`, `visible`, `status`, `permission`, `component`, `remark`) 
SELECT @warehouse_menu_id, 'B000104', 'WarehouseSetDefault', '设置默认', 2, NULL, NULL, 4, 1, 1, 'basedata:warehouse:setDefault', NULL, '设置默认仓库按钮'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE code = 'B000104');

-- 4. 为admin角色分配这些菜单权限
-- 查询admin角色
SELECT '当前角色列表:' as info;
SELECT id, code, name FROM sys_role;

-- 尝试多种方式查找admin角色
SET @admin_role_id = (
    SELECT id FROM sys_role 
    WHERE code = 'admin' OR code = 'ADMIN' OR name LIKE '%管理员%' OR name = 'admin'
    LIMIT 1
);

-- 显示找到的角色ID
SELECT CONCAT('找到的角色ID: ', IFNULL(@admin_role_id, 'NULL')) as info;

-- 只有当找到角色且权限不存在时才插入
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) 
SELECT @admin_role_id, @basedata_menu_id 
FROM DUAL
WHERE @admin_role_id IS NOT NULL 
AND NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = @admin_role_id AND menu_id = @basedata_menu_id);

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) 
SELECT @admin_role_id, @warehouse_menu_id 
FROM DUAL
WHERE @admin_role_id IS NOT NULL 
AND NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = @admin_role_id AND menu_id = @warehouse_menu_id);

-- 分配按钮权限
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) 
SELECT @admin_role_id, m.id 
FROM sys_menu m
WHERE m.parent_id = @warehouse_menu_id 
AND m.menu_type = 2 
AND @admin_role_id IS NOT NULL
AND NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = @admin_role_id AND menu_id = m.id);

-- 查询验证
SELECT '新增的菜单:' as info;
SELECT m.id, m.parent_id, m.code, m.name, m.title, m.menu_type, m.path, m.icon, m.sort 
FROM sys_menu m 
WHERE m.code IN ('M000100', 'M000101', 'B000101', 'B000102', 'B000103', 'B000104')
ORDER BY m.parent_id, m.sort;

SELECT '角色菜单关联:' as info;
SELECT rm.role_id, r.name as role_name, rm.menu_id, m.title as menu_title
FROM sys_role_menu rm
JOIN sys_role r ON rm.role_id = r.id
JOIN sys_menu m ON rm.menu_id = m.id
WHERE m.code IN ('M000100', 'M000101', 'B000101', 'B000102', 'B000103', 'B000104');

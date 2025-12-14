-- 物料管理菜单配置脚本
-- 可重复执行,不会报错

-- 1. 物料管理父菜单 (目录类型: 0) - 如果不存在则插入
INSERT INTO `sys_menu` (`parent_id`, `code`, `name`, `title`, `menu_type`, `path`, `component`, `icon`, `sort`, `visible`, `status`, `remark`) 
SELECT 20, 'M000102', 'material', '物料管理', 0, '/basedata/material', NULL, 'shopping', 2, 1, 1, '物料管理模块'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE code = 'M000102');

-- 获取物料管理菜单ID
SET @material_menu_id = (SELECT id FROM sys_menu WHERE code = 'M000102');

-- 2. 物料分类菜单 (菜单类型: 1) - 如果不存在则插入
INSERT INTO `sys_menu` (`parent_id`, `code`, `name`, `title`, `menu_type`, `path`, `component`, `permission`, `icon`, `sort`, `visible`, `status`, `remark`) 
SELECT @material_menu_id, 'M000103', 'material-category', '物料分类', 1, '/basedata/material/category', 'Basedata/Material/Category', 'basedata:material:category', 'folder-tree', 1, 1, 1, '物料分类管理'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE code = 'M000103');

-- 3. 计量单位菜单 (菜单类型: 1) - 如果不存在则插入
INSERT INTO `sys_menu` (`parent_id`, `code`, `name`, `title`, `menu_type`, `path`, `component`, `permission`, `icon`, `sort`, `visible`, `status`, `remark`) 
SELECT @material_menu_id, 'M000104', 'unit', '计量单位', 1, '/basedata/material/unit', 'Basedata/Material/Unit', 'basedata:material:unit', 'calculator', 2, 1, 1, '计量单位管理'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE code = 'M000104');

-- 4. 物料信息菜单 (菜单类型: 1) - 如果不存在则插入
INSERT INTO `sys_menu` (`parent_id`, `code`, `name`, `title`, `menu_type`, `path`, `component`, `permission`, `icon`, `sort`, `visible`, `status`, `remark`) 
SELECT @material_menu_id, 'M000105', 'material-info', '物料信息', 1, '/basedata/material/info', 'Basedata/Material/Info', 'basedata:material:info', 'box', 3, 1, 1, '物料信息管理'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE code = 'M000105');

-- 5. 分配菜单权限给管理员角色(角色ID=1) - 避免重复
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, m.id 
FROM sys_menu m
WHERE m.code IN ('M000102', 'M000103', 'M000104', 'M000105')
AND NOT EXISTS (
    SELECT 1 FROM sys_role_menu rm 
    WHERE rm.role_id = 1 AND rm.menu_id = m.id
);

-- 添加客户管理和品牌管理菜单

-- 1. 设置基础数据菜单ID (根据之前的脚本,parent_id是27)
SET @basedata_parent_id = 27;

-- 2. 获取admin角色ID
SET @admin_role_id = (SELECT id FROM sys_role WHERE code = 'R000001' LIMIT 1);

-- 3. 添加客户管理菜单
INSERT INTO `sys_menu` (`parent_id`, `code`, `name`, `title`, `menu_type`, `path`, `icon`, `sort`, `visible`, `status`, `permission`, `component`, `remark`) 
VALUES (@basedata_parent_id, 'M000107', 'customer', '客户管理', 1, '/basedata/customer', 'UserOutlined', 6, 1, 1, 'basedata:customer:list', '/Basedata/Customer', '客户信息管理');

SET @customer_menu_id = LAST_INSERT_ID();

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) 
VALUES (@admin_role_id, @customer_menu_id);

-- 4. 添加品牌管理菜单
INSERT INTO `sys_menu` (`parent_id`, `code`, `name`, `title`, `menu_type`, `path`, `icon`, `sort`, `visible`, `status`, `permission`, `component`, `remark`) 
VALUES (@basedata_parent_id, 'M000108', 'brand', '品牌管理', 1, '/basedata/brand', 'TagOutlined', 7, 1, 1, 'basedata:brand:list', '/Basedata/Brand', '品牌信息管理');

SET @brand_menu_id = LAST_INSERT_ID();

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) 
VALUES (@admin_role_id, @brand_menu_id);

-- 5. 验证结果
SELECT m.id, m.parent_id, m.code, m.name, m.title, m.path, m.icon, m.sort 
FROM sys_menu m 
WHERE m.code IN ('M000107', 'M000108');

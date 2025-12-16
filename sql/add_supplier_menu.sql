-- 添加供应商管理菜单
INSERT INTO sys_menu (parent_id, name, path, component, icon, sort, type, status, create_time, update_time)
VALUES
(2, '供应商管理', '/basedata/supplier', '/Basedata/Supplier', 'TeamOutlined', 4, 1, 1, NOW(), NOW());

-- 获取刚插入的菜单ID
SET @supplier_menu_id = LAST_INSERT_ID();

-- 为admin角色分配供应商管理菜单权限
INSERT INTO sys_role_menu (role_id, menu_id, create_time)
VALUES (1, @supplier_menu_id, NOW());

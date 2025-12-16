-- 请手动在MySQL中执行以下SQL语句来添加供应商管理菜单

-- 1. 添加供应商管理菜单
INSERT INTO sys_menu (parent_id, name, path, component, icon, sort, type, status, create_time, update_time)
VALUES (2, '供应商管理', '/basedata/supplier', '/Basedata/Supplier', 'TeamOutlined', 4, 1, 1, NOW(), NOW());

-- 2. 获取刚插入的菜单ID并为admin角色分配权限
-- 注意: 请先执行上面的INSERT,然后查看插入的ID,将下面的 <menu_id> 替换为实际ID
-- SELECT LAST_INSERT_ID(); -- 执行此语句获取ID
-- INSERT INTO sys_role_menu (role_id, menu_id, create_time) VALUES (1, <menu_id>, NOW());

-- 或者直接使用子查询:
INSERT INTO sys_role_menu (role_id, menu_id, create_time)
SELECT 1, id, NOW() FROM sys_menu WHERE name = '供应商管理' AND parent_id = 2 LIMIT 1;

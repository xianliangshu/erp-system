-- 检查菜单数据
SELECT id, parent_id, code, title, menu_type, path, icon, sort, visible, status 
FROM sys_menu 
WHERE code IN ('M000100', 'M000101') OR parent_id IN (SELECT id FROM sys_menu WHERE code = 'M000100')
ORDER BY sort;

-- 检查admin用户的角色
SELECT u.id as user_id, u.username, ur.role_id, r.code as role_code, r.name as role_name
FROM sys_user u
LEFT JOIN sys_user_role ur ON u.id = ur.user_id
LEFT JOIN sys_role r ON ur.role_id = r.id
WHERE u.username = 'admin';

-- 检查角色菜单关联
SELECT rm.role_id, r.name as role_name, rm.menu_id, m.code as menu_code, m.title as menu_title
FROM sys_role_menu rm
JOIN sys_role r ON rm.role_id = r.id
JOIN sys_menu m ON rm.menu_id = m.id
WHERE r.code = 'admin'
ORDER BY m.sort;

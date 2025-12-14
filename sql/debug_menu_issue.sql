-- 检查数据库名称
SELECT DATABASE();

-- 检查admin用户信息
SELECT * FROM sys_user WHERE username = 'admin';

-- 检查用户角色关联
SELECT * FROM sys_user_role WHERE user_id = 1;

-- 检查角色信息
SELECT * FROM sys_role;

-- 检查角色菜单关联
SELECT * FROM sys_role_menu WHERE role_id IN (SELECT role_id FROM sys_user_role WHERE user_id = 1);

-- 检查菜单信息
SELECT id, code, title FROM sys_menu WHERE code LIKE 'M0001%' ORDER BY code;

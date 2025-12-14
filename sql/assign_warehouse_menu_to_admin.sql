-- 手动为admin用户的角色分配仓库管理菜单权限

-- 1. 查看当前admin用户的角色
SELECT '当前admin用户的角色:' as info;
SELECT u.id as user_id, u.username, ur.role_id, r.code as role_code, r.name as role_name
FROM sys_user u
LEFT JOIN sys_user_role ur ON u.id = ur.user_id
LEFT JOIN sys_role r ON ur.role_id = r.id
WHERE u.username = 'admin';

-- 2. 获取admin用户的角色ID
SET @user_role_id = (
    SELECT ur.role_id 
    FROM sys_user u
    JOIN sys_user_role ur ON u.id = ur.user_id
    WHERE u.username = 'admin'
    LIMIT 1
);

SELECT CONCAT('Admin用户的角色ID: ', IFNULL(@user_role_id, 'NULL')) as info;

-- 3. 获取菜单ID
SET @basedata_menu_id = (SELECT id FROM sys_menu WHERE code = 'M000100');
SET @warehouse_menu_id = (SELECT id FROM sys_menu WHERE code = 'M000101');

SELECT CONCAT('基础数据菜单ID: ', IFNULL(@basedata_menu_id, 'NULL')) as info;
SELECT CONCAT('仓库管理菜单ID: ', IFNULL(@warehouse_menu_id, 'NULL')) as info;

-- 4. 为角色分配菜单权限(如果不存在)
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) 
SELECT @user_role_id, @basedata_menu_id 
FROM DUAL
WHERE @user_role_id IS NOT NULL 
AND @basedata_menu_id IS NOT NULL
AND NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = @user_role_id AND menu_id = @basedata_menu_id);

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) 
SELECT @user_role_id, @warehouse_menu_id 
FROM DUAL
WHERE @user_role_id IS NOT NULL 
AND @warehouse_menu_id IS NOT NULL
AND NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = @user_role_id AND menu_id = @warehouse_menu_id);

-- 5. 分配按钮权限
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) 
SELECT @user_role_id, m.id 
FROM sys_menu m
WHERE m.parent_id = @warehouse_menu_id 
AND m.menu_type = 2 
AND @user_role_id IS NOT NULL
AND NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = @user_role_id AND menu_id = m.id);

-- 6. 验证结果
SELECT '分配后的角色菜单关联:' as info;
SELECT rm.role_id, r.name as role_name, rm.menu_id, m.code as menu_code, m.title as menu_title
FROM sys_role_menu rm
JOIN sys_role r ON rm.role_id = r.id
JOIN sys_menu m ON rm.menu_id = m.id
WHERE rm.role_id = @user_role_id
AND m.code IN ('M000100', 'M000101', 'B000101', 'B000102', 'B000103', 'B000104')
ORDER BY m.sort;

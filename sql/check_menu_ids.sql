-- 查询所有菜单的ID和CODE对应关系
SELECT id, code, title, parent_id, menu_type, status, visible 
FROM sys_menu 
ORDER BY id;

-- 特别查询仓库相关菜单
SELECT id, code, title, parent_id 
FROM sys_menu 
WHERE code IN ('M000100', 'M000101') 
   OR code LIKE 'B0001%';

-- 修正基础数据子菜单层级
-- 将 供应商管理、客户管理、品牌管理 从 "物料管理" 下移动到 "基础数据" 下，使其与 "物料管理" 平级

-- 1. 获取 "基础数据" 菜单的 ID
SET @basedata_id = (SELECT id FROM sys_menu WHERE code = 'M000100' LIMIT 1);

-- 2. 更新 供应商管理 (M000106)、客户管理 (M000107)、品牌管理 (M000108) 的 parent_id
UPDATE `sys_menu` 
SET `parent_id` = @basedata_id 
WHERE `code` IN ('M000106', 'M000107', 'M000108');

-- 3. 调整排序 (可选，为了美观)
-- 仓库管理: 1, 物料管理: 2, 供应商管理: 3, 客户管理: 4, 品牌管理: 5
UPDATE `sys_menu` SET `sort` = 1 WHERE `code` = 'M000101'; -- 仓库管理
UPDATE `sys_menu` SET `sort` = 2 WHERE `code` = 'M000102'; -- 物料管理
UPDATE `sys_menu` SET `sort` = 3 WHERE `code` = 'M000106'; -- 供应商管理
UPDATE `sys_menu` SET `sort` = 4 WHERE `code` = 'M000107'; -- 客户管理
UPDATE `sys_menu` SET `sort` = 5 WHERE `code` = 'M000108'; -- 品牌管理

-- 4. 验证结果
SELECT id, parent_id, code, name, title, sort 
FROM sys_menu 
WHERE parent_id = @basedata_id 
ORDER BY sort;

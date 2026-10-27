-- =============================================================================
-- 商品中心菜单：商品SPU + 销售属性
-- -----------------------------------------------------------------------------
-- 执行：
--   mysql -h192.168.31.199 -uroot -p123456 --default-character-set=utf8mb4 \
--         erp-data < sql/add_product_center_menu.sql
--
-- 设计要点（务必遵守）：
--   1. 菜单 code 一旦占用不可复用，这里用 M000109 / M000110（M0001xx 段属基础数据）。
--   2. 权限串 **先入库再写注解** —— 注解里的串必须已存在于 sys_menu.permission，
--      否则非超管永远 403 且授权页勾不到。
--   3. ⚠️ **不要改动物料信息菜单（id=8, M000105）的 code/path/permission**：
--      动权限串就要重授所有角色，且会撞上面第 2 条。本批只改它的 title。
--   4. 一品多码不新增权限串，复用 basedata:material:info。
--
-- 特性：全量幂等，可重复执行。
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. 新增菜单（挂在「物料管理」目录 id=5 下）
-- -----------------------------------------------------------------------------
INSERT INTO `sys_menu` (`parent_id`, `code`, `name`, `title`, `menu_type`, `path`, `component`,
                        `permission`, `icon`, `sort`, `visible`, `status`, `remark`)
SELECT 5, 'M000109', 'product-spu', '商品SPU', 1, '/basedata/product/spu',
       'Basedata/Product/Spu', 'basedata:spu:list', NULL, 0, 1, 1, '商品中心-SPU'
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `code` = 'M000109');

INSERT INTO `sys_menu` (`parent_id`, `code`, `name`, `title`, `menu_type`, `path`, `component`,
                        `permission`, `icon`, `sort`, `visible`, `status`, `remark`)
SELECT 5, 'M000110', 'sale-property', '销售属性', 1, '/basedata/product/sale-property',
       'Basedata/Product/SaleProperty', 'basedata:sale-property:list', NULL, 4, 1, 1, '商品中心-销售属性'
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `code` = 'M000110');

-- -----------------------------------------------------------------------------
-- 2. 物料信息菜单改名（只动 title；code/path/permission 全部冻结）
-- -----------------------------------------------------------------------------
UPDATE `sys_menu` SET `title` = '商品SKU'
WHERE `code` = 'M000105' AND `title` = '物料信息';

-- -----------------------------------------------------------------------------
-- 3. 授权：超管（role_id=1）+ 采购经理（2）+ 销售经理（3）+ 仓库管理员（4）
--    与既有基础数据菜单的授权口径保持一致：所有业务角色都能查商品档案。
--    注意祖先目录 id=5（物料管理）必须一并授权，否则侧边栏整棵子树不显示。
-- -----------------------------------------------------------------------------
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT r.`id`, m.`id`
FROM (SELECT 1 AS `id` UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4) r
CROSS JOIN `sys_menu` m
WHERE m.`code` IN ('M000109', 'M000110')
  AND NOT EXISTS (SELECT 1 FROM `sys_role_menu` rm
                  WHERE rm.`role_id` = r.`id` AND rm.`menu_id` = m.`id`);

-- 祖先目录闭包：确保四个角色都有「物料管理」(id=5) 与「基础数据」(id=9)
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT r.`id`, m.`id`
FROM (SELECT 1 AS `id` UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4) r
CROSS JOIN `sys_menu` m
WHERE m.`id` IN (5, 9)
  AND NOT EXISTS (SELECT 1 FROM `sys_role_menu` rm
                  WHERE rm.`role_id` = r.`id` AND rm.`menu_id` = m.`id`);

-- -----------------------------------------------------------------------------
-- 4. 核对
-- -----------------------------------------------------------------------------
SELECT m.`id`, m.`parent_id`, m.`code`, m.`title`, m.`path`, m.`component`, m.`permission`
FROM `sys_menu` m
WHERE m.`code` IN ('M000105', 'M000109', 'M000110')
   OR m.`id` IN (5, 9)
ORDER BY m.`id`;

SELECT r.`id` AS `role_id`, r.`code`, COUNT(rm.`id`) AS `已授权菜单数`
FROM `sys_role` r
LEFT JOIN `sys_role_menu` rm ON rm.`role_id` = r.`id`
GROUP BY r.`id`, r.`code`
ORDER BY r.`id`;

-- ⚠️ 执行完必须清一次 Sa-Token 权限缓存（或重启后端），否则新权限串最长 30 分钟才生效：
--    redis-cli -h 192.168.31.199 -a 123456 -n 0 --scan --pattern 'erp:auth:*' | xargs redis-cli ... DEL

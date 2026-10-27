-- =============================================================================
-- 库存体系补齐 —— 第二段菜单与按钮授权（盘点任务 / 预先盘点单）
--
-- 背景：第一段（add_stock_system.sql）只建了 M000207 库存预警 / M000208 库存成本调整 /
--       M000211 盘点参数设置，本脚本补齐第二段两个页面 + 7 个按钮权限。
--
-- 设计约束（同第一段）：
--   1. 只【新增】菜单行，绝不修改任何存量行的 code/path/permission ⇒ 不会撞
--      「@SaCheckPermission 的串必须已存在于 sys_menu.permission」这条铁律。
--   2. 全部用 WHERE NOT EXISTS 实现幂等，可重复执行。
--   3. ⚠️ 顺序：本脚本必须先于后端 JAR 执行，执行后清 erp:auth:* 缓存。
--   4. 顶级目录 id=1（库存管理）必须一并授权，否则侧边栏整棵子树不显示。
--
-- 执行：mysql -h192.168.31.199 -uroot -p123456 --default-character-set=utf8mb4 erp-data < sql/add_stock_stage2_menu.sql
-- 编写日期：2026-10-03
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 8.1 页面菜单（menu_type=1）
-- -----------------------------------------------------------------------------
INSERT INTO `sys_menu` (`parent_id`, `code`, `name`, `title`, `menu_type`, `path`, `component`,
                        `permission`, `icon`, `sort`, `visible`, `status`, `remark`)
SELECT 1, 'M000209', 'stock-take-plan', '盘点任务', 1, '/stock/take/plan', 'Business/Stock/Take/Plan',
       'stock:take:plan:query', NULL, 9, 1, 1, '库存体系-盘点任务'
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `code` = 'M000209');

INSERT INTO `sys_menu` (`parent_id`, `code`, `name`, `title`, `menu_type`, `path`, `component`,
                        `permission`, `icon`, `sort`, `visible`, `status`, `remark`)
SELECT 1, 'M000210', 'stock-take-pre', '预先盘点单', 1, '/stock/take/pre', 'Business/Stock/Take/Pre',
       'stock:take:pre:query', NULL, 10, 1, 1, '库存体系-预先盘点单'
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `code` = 'M000210');

-- -----------------------------------------------------------------------------
-- 8.2 按钮权限（menu_type=2），B0003xx 段（第一段已用到 B000308）
-- -----------------------------------------------------------------------------
INSERT INTO `sys_menu` (`parent_id`, `code`, `name`, `title`, `menu_type`, `path`, `component`,
                        `permission`, `icon`, `sort`, `visible`, `status`, `remark`)
SELECT (SELECT `id` FROM (SELECT `id` FROM `sys_menu` WHERE `code` = 'M000209' LIMIT 1) t),
       'B000309', '', '新增盘点任务', 2, NULL, NULL, 'stock:take:plan:add', NULL, 1, 1, 1, NULL
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `code` = 'B000309');

INSERT INTO `sys_menu` (`parent_id`, `code`, `name`, `title`, `menu_type`, `path`, `component`,
                        `permission`, `icon`, `sort`, `visible`, `status`, `remark`)
SELECT (SELECT `id` FROM (SELECT `id` FROM `sys_menu` WHERE `code` = 'M000209' LIMIT 1) t),
       'B000310', '', '录入盘点数量', 2, NULL, NULL, 'stock:take:plan:modify', NULL, 2, 1, 1, NULL
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `code` = 'B000310');

INSERT INTO `sys_menu` (`parent_id`, `code`, `name`, `title`, `menu_type`, `path`, `component`,
                        `permission`, `icon`, `sort`, `visible`, `status`, `remark`)
SELECT (SELECT `id` FROM (SELECT `id` FROM `sys_menu` WHERE `code` = 'M000209' LIMIT 1) t),
       'B000311', '', '删除盘点任务', 2, NULL, NULL, 'stock:take:plan:delete', NULL, 3, 1, 1, NULL
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `code` = 'B000311');

INSERT INTO `sys_menu` (`parent_id`, `code`, `name`, `title`, `menu_type`, `path`, `component`,
                        `permission`, `icon`, `sort`, `visible`, `status`, `remark`)
SELECT (SELECT `id` FROM (SELECT `id` FROM `sys_menu` WHERE `code` = 'M000209' LIMIT 1) t),
       'B000312', '', '生成差异/作废', 2, NULL, NULL, 'stock:take:plan:approve', NULL, 4, 1, 1, NULL
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `code` = 'B000312');

INSERT INTO `sys_menu` (`parent_id`, `code`, `name`, `title`, `menu_type`, `path`, `component`,
                        `permission`, `icon`, `sort`, `visible`, `status`, `remark`)
SELECT (SELECT `id` FROM (SELECT `id` FROM `sys_menu` WHERE `code` = 'M000210' LIMIT 1) t),
       'B000313', '', '新增预先盘点单', 2, NULL, NULL, 'stock:take:pre:add', NULL, 1, 1, 1, NULL
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `code` = 'B000313');

INSERT INTO `sys_menu` (`parent_id`, `code`, `name`, `title`, `menu_type`, `path`, `component`,
                        `permission`, `icon`, `sort`, `visible`, `status`, `remark`)
SELECT (SELECT `id` FROM (SELECT `id` FROM `sys_menu` WHERE `code` = 'M000210' LIMIT 1) t),
       'B000314', '', '编辑预先盘点单', 2, NULL, NULL, 'stock:take:pre:modify', NULL, 2, 1, 1, NULL
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `code` = 'B000314');

INSERT INTO `sys_menu` (`parent_id`, `code`, `name`, `title`, `menu_type`, `path`, `component`,
                        `permission`, `icon`, `sort`, `visible`, `status`, `remark`)
SELECT (SELECT `id` FROM (SELECT `id` FROM `sys_menu` WHERE `code` = 'M000210' LIMIT 1) t),
       'B000315', '', '删除预先盘点单', 2, NULL, NULL, 'stock:take:pre:delete', NULL, 3, 1, 1, NULL
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `code` = 'B000315');

-- -----------------------------------------------------------------------------
-- 8.3 授权：超管(1) + 仓库管理员(4)
--     ⚠️ 注意祖先目录 id=1（库存管理）必须一并授权，否则侧边栏整棵子树不显示。
-- -----------------------------------------------------------------------------
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT r.`id`, m.`id`
FROM (SELECT 1 AS `id` UNION ALL SELECT 4) r
CROSS JOIN `sys_menu` m
WHERE m.`code` IN ('M000209', 'M000210',
                   'B000309', 'B000310', 'B000311', 'B000312',
                   'B000313', 'B000314', 'B000315')
  AND NOT EXISTS (SELECT 1 FROM `sys_role_menu` rm
                  WHERE rm.`role_id` = r.`id` AND rm.`menu_id` = m.`id`);

-- 顶级目录「库存管理」兜底授权（正常已存在，幂等）
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT r.`id`, m.`id`
FROM (SELECT 1 AS `id` UNION ALL SELECT 4) r
CROSS JOIN `sys_menu` m
WHERE m.`code` = 'M000200'
  AND NOT EXISTS (SELECT 1 FROM `sys_role_menu` rm
                  WHERE rm.`role_id` = r.`id` AND rm.`menu_id` = m.`id`);

-- -----------------------------------------------------------------------------
-- 8.4 验收查询
-- -----------------------------------------------------------------------------
SELECT '第二段新菜单数(应为9)' AS `项`, COUNT(*) AS `值`
    FROM `sys_menu`
    WHERE `code` IN ('M000209', 'M000210',
                     'B000309', 'B000310', 'B000311', 'B000312',
                     'B000313', 'B000314', 'B000315')
UNION ALL SELECT '库存管理目录下菜单数(应为14)', COUNT(*) FROM `sys_menu` WHERE `parent_id` = 1
UNION ALL SELECT '第二段权限串数(应为9)', COUNT(DISTINCT `permission`)
    FROM `sys_menu`
    WHERE `permission` LIKE 'stock:take:%'
UNION ALL SELECT '超管已授权(应为9)', COUNT(*)
    FROM `sys_role_menu` rm JOIN `sys_menu` m ON m.`id` = rm.`menu_id`
    WHERE rm.`role_id` = 1 AND m.`code` IN ('M000209', 'M000210',
          'B000309', 'B000310', 'B000311', 'B000312', 'B000313', 'B000314', 'B000315')
UNION ALL SELECT '仓库管理员已授权(应为9)', COUNT(*)
    FROM `sys_role_menu` rm JOIN `sys_menu` m ON m.`id` = rm.`menu_id`
    WHERE rm.`role_id` = 4 AND m.`code` IN ('M000209', 'M000210',
          'B000309', 'B000310', 'B000311', 'B000312', 'B000313', 'B000314', 'B000315');

SELECT m.`id`, m.`parent_id`, m.`code`, m.`title`, m.`menu_type`, m.`path`, m.`permission`
FROM `sys_menu` m
WHERE m.`code` IN ('M000209', 'M000210',
                   'B000309', 'B000310', 'B000311', 'B000312',
                   'B000313', 'B000314', 'B000315')
ORDER BY m.`code`;

-- =====================================================================
-- 登录日志 / 操作日志 —— 菜单与授权
--
-- 背景：sys_login_log 与 sys_operation_log 两张表建库时就有，
--       但一直没有 Service、Controller 与页面，属于「空置的表」。
--       本脚本补齐菜单入口。
--
-- 幂等：直接用 INSERT IGNORE。
--       sys_menu 有 uk_code、sys_role_menu 有 uk_role_menu 唯一键，
--       重复执行只会命中 IGNORE，不会报错也不会产生重复数据。
--
-- ⚠️ 执行完必须清一次 Redis 鉴权缓存，否则最长 30 分钟内
--    StpInterface 仍返回旧权限（新增的 system:loginlog:list 等串拿不到）：
--      redis-cli -a 123456 -n 0 --scan --pattern 'erp:auth:*' | xargs redis-cli -a 123456 -n 0 del
--    更简单的做法：重启后端服务（重启不会清 Redis，但 30 分钟 TTL 会自动过期），
--    或者直接在应用里调一次任意「角色授权」保存操作触发 evictAll()。
-- =====================================================================

USE `erp-data`;

-- 取「系统管理」目录的 id 作为父级。不硬编码 17，避免不同环境自增 id 不同。
-- IFNULL(...,0)：万一套不到目录，退化成顶级目录（顶级菜单 parent_id 约定为 0，
-- 若写成 NULL 会被 buildTreeFromList 当成非根节点，菜单直接从侧边栏消失）。
SET @system_dir_id = IFNULL(
        (SELECT id FROM `sys_menu` WHERE code = 'M000001' AND is_deleted = 0 LIMIT 1),
        0);

INSERT IGNORE INTO `sys_menu`
(`parent_id`, `code`, `name`, `title`, `menu_type`, `path`, `component`, `permission`,
 `icon`, `sort`, `visible`, `status`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES
(@system_dir_id, 'M000806', 'loginlog', '登录日志', 1,
 '/system/loginlog', 'System/LoginLog', 'system:loginlog:list',
 'LoginOutlined', 6, 1, 1, '登录成功/失败记录', 'system', NOW(), 'system', NOW()),

(@system_dir_id, 'M000807', 'operationlog', '操作日志', 1,
 '/system/operationlog', 'System/OperationLog', 'system:operationlog:list',
 'FileTextOutlined', 7, 1, 1, '后台写操作审计记录', 'system', NOW(), 'system', NOW());

-- 授权给超级管理员。运维/审计角色如需查看，另外在「角色管理」页面勾选即可。
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`, `create_time`)
SELECT r.id, m.id, NOW()
FROM `sys_role` r
         JOIN `sys_menu` m ON m.code IN ('M000806', 'M000807') AND m.is_deleted = 0
WHERE r.code = 'R000001'
  AND r.is_deleted = 0;

-- ===================== 执行结果自检 =====================
SELECT '=== 新增菜单 ===' AS step;
SELECT id, parent_id, code, title, menu_type, path, component, permission, sort
FROM `sys_menu`
WHERE code IN ('M000806', 'M000807')
ORDER BY sort;

SELECT '=== 各角色授权菜单数（超级管理员应为 73）===' AS step;
SELECT r.code AS role_code, r.name AS role_name, COUNT(rm.menu_id) AS menu_count
FROM `sys_role` r
         LEFT JOIN `sys_role_menu` rm ON r.id = rm.role_id
WHERE r.is_deleted = 0
GROUP BY r.id, r.code, r.name;

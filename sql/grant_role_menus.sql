-- =====================================================================
-- ERP 角色菜单授权脚本（幂等，可重复执行）
-- 目标库：erp-data
-- 生成日期：2026-10-01
--
-- 配套脚本：
--   sql/fix_menu_full.sql     —— 菜单数据修复（补菜单、修孤儿、编码规范化）
--   sql/grant_role_menus.sql  —— 本脚本，为 采购经理/销售经理/仓库管理员 授权
--
-- 设计要点：
--   1. 按「角色编码 + 菜单编码」映射授权，**不写死自增 id**，可跨库重复执行。
--   2. 先 DELETE 后 INSERT（与 SysRoleServiceImpl.assignMenus 行为一致）。
--   3. 递归 CTE 做「祖先闭包」：只要授了子节点，其所有上级目录自动补上。
--      这是必须的 —— SysMenuServiceImpl.buildTreeFromList() 只从授权集合里
--      挑 parent_id 为 0 的节点当根，目录漏授会导致整棵子树在侧边栏消失。
--   4. 脚本末尾附校验查询，期望：三个角色均无「父级未授权」记录。
--
-- ⚠️ 执行前请先备份：
--   mysqldump ... erp-data sys_role_menu > sys_role_menu_bak.sql
-- =====================================================================

SET NAMES utf8mb4;

-- ---------------------------------------------------------------------
-- 0. 期望的授权矩阵（role_code -> menu_code）
-- ---------------------------------------------------------------------
DROP TEMPORARY TABLE IF EXISTS tmp_role_menu_map;
CREATE TEMPORARY TABLE tmp_role_menu_map (
    role_code VARCHAR(32)  NOT NULL COMMENT '角色编码',
    menu_code VARCHAR(32)  NOT NULL COMMENT '菜单编码',
    PRIMARY KEY (role_code, menu_code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '角色菜单授权映射(临时)';

-- R000002 采购经理：采购全流程 + 供应商侧结算 + 采购相关基础数据 + 采购/库存报表
INSERT INTO tmp_role_menu_map (role_code, menu_code) VALUES
-- 采购管理
('R000002','M000300'),('R000002','M000301'),('R000002','M000302'),('R000002','M000303'),
-- 库存查询（只读）
('R000002','M000200'),('R000002','M000201'),
-- 基础数据：仓库 / 物料体系 / 供应商
('R000002','M000100'),('R000002','M000101'),('R000002','M000102'),
('R000002','M000103'),('R000002','M000104'),('R000002','M000105'),('R000002','M000106'),
-- 结算管理：收支项目 + 供应商四单
('R000002','M000700'),('R000002','M000701'),('R000002','M000702'),
('R000002','M000703'),('R000002','M000704'),('R000002','M000705'),
-- 报表：采购统计 / 库存报表 / 进销存汇总
('R000002','M000800'),('R000002','M000802'),('R000002','M000804'),('R000002','M000805');

-- R000003 销售经理：销售 + 零售 + 客户侧结算 + 客户相关基础数据 + 销售/库存报表
INSERT INTO tmp_role_menu_map (role_code, menu_code) VALUES
-- 销售管理
('R000003','M000400'),('R000003','M000401'),('R000003','M000402'),('R000003','M000403'),
-- 零售管理
('R000003','M000600'),('R000003','M000601'),('R000003','M000602'),('R000003','M000603'),
-- 库存查询（只读）
('R000003','M000200'),('R000003','M000201'),
-- 基础数据：物料体系 / 客户 / 品牌
('R000003','M000100'),('R000003','M000102'),
('R000003','M000103'),('R000003','M000104'),('R000003','M000105'),
('R000003','M000107'),('R000003','M000108'),
-- 结算管理：收支项目 + 客户四单
('R000003','M000700'),('R000003','M000701'),('R000003','M000706'),
('R000003','M000707'),('R000003','M000708'),('R000003','M000709'),
-- 报表：销售统计 / 库存报表 / 进销存汇总
('R000003','M000800'),('R000003','M000803'),('R000003','M000804'),('R000003','M000805');

-- R000004 仓库管理员：库存全流程（含调整原因维护）+ 仓库档案含按钮 + 收发货
INSERT INTO tmp_role_menu_map (role_code, menu_code) VALUES
-- 库存管理：全部六个菜单
('R000004','M000200'),('R000004','M000201'),('R000004','M000202'),('R000004','M000203'),
('R000004','M000204'),('R000004','M000205'),('R000004','M000206'),
-- 基础数据：仓库管理（含 4 个按钮）/ 物料体系
('R000004','M000100'),
('R000004','M000101'),
('R000004','B000101'),('R000004','B000102'),('R000004','B000103'),('R000004','B000104'),
('R000004','M000102'),('R000004','M000103'),('R000004','M000104'),('R000004','M000105'),
-- 收货 / 出库
('R000004','M000300'),('R000004','M000302'),
('R000004','M000400'),('R000004','M000402'),
('R000004','M000600'),('R000004','M000602'),
-- 报表：库存报表 / 进销存汇总
('R000004','M000800'),('R000004','M000804'),('R000004','M000805');

-- ---------------------------------------------------------------------
-- 1. 计算授权集合 + 祖先闭包
-- ---------------------------------------------------------------------
-- 注意：这里刻意用**普通表**而非 TEMPORARY TABLE。
-- MySQL 不允许在同一条语句里读写同一张临时表（ERROR 1137 Can't reopen table），
-- 而下面的递归 CTE 既要读它又要写它，因此必须用普通表（脚本首尾各 DROP 一次）。
DROP TABLE IF EXISTS tmp_role_closure;
CREATE TABLE tmp_role_closure (
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, menu_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '授权集合+祖先闭包(中间表,脚本末尾删除)';

-- 1.1 映射表 -> 实际 id（角色编码/菜单编码若不存在则静默跳过，保证幂等）
INSERT IGNORE INTO tmp_role_closure (role_id, menu_id)
SELECT r.id, m.id
FROM tmp_role_menu_map t
         JOIN sys_role r ON r.code = t.role_code
         JOIN sys_menu m ON m.code = t.menu_code AND m.is_deleted = 0;

-- 1.2 递归补全所有上级目录（可跨多层）
INSERT IGNORE INTO tmp_role_closure (role_id, menu_id)
WITH RECURSIVE up AS (SELECT c.role_id, m.parent_id AS pid
                      FROM tmp_role_closure c
                               JOIN sys_menu m ON m.id = c.menu_id
                      WHERE m.parent_id IS NOT NULL
                        AND m.parent_id <> 0
                      UNION
                      SELECT u.role_id, p.parent_id
                      FROM up u
                               JOIN sys_menu p ON p.id = u.pid
                      WHERE p.parent_id IS NOT NULL
                        AND p.parent_id <> 0)
SELECT role_id, pid FROM up;

-- ---------------------------------------------------------------------
-- 2. 写入 sys_role_menu（仅覆盖本脚本涉及的角色，不动其他角色）
-- ---------------------------------------------------------------------
DELETE
FROM sys_role_menu
WHERE role_id IN (SELECT id FROM sys_role WHERE code IN ('R000002', 'R000003', 'R000004'));

INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT role_id, menu_id FROM tmp_role_closure;

-- ---------------------------------------------------------------------
-- 3. 校验
-- ---------------------------------------------------------------------
SELECT '=== 1) 各角色菜单数 ===' AS info;
SELECT r.id, r.code, r.name, COUNT(rm.menu_id) AS menu_cnt
FROM sys_role r
         LEFT JOIN sys_role_menu rm ON r.id = rm.role_id
WHERE r.code IN ('R000001', 'R000002', 'R000003', 'R000004')
GROUP BY r.id, r.code, r.name
ORDER BY r.id;

SELECT '=== 2) 缺失父级校验（必须 0 行，否则侧边栏会断）===' AS info;
SELECT rm.role_id, m.code, m.title, m.parent_id
FROM sys_role_menu rm
         JOIN sys_menu m ON m.id = rm.menu_id
WHERE m.parent_id <> 0
  AND NOT EXISTS (SELECT 1
                  FROM sys_role_menu x
                  WHERE x.role_id = rm.role_id
                    AND x.menu_id = m.parent_id);

SELECT '=== 3) 授权明细（角色 x 域 x 菜单数）===' AS info;
SELECT r.name AS role_name,
       COALESCE(p.title, m.title) AS domain,
       SUM(m.menu_type = 1)       AS menu_cnt,
       SUM(m.menu_type = 2)       AS btn_cnt
FROM sys_role_menu rm
         JOIN sys_role r ON r.id = rm.role_id AND r.code IN ('R000002', 'R000003', 'R000004')
         JOIN sys_menu m ON m.id = rm.menu_id
         LEFT JOIN sys_menu p ON p.id = m.parent_id AND p.parent_id = 0
WHERE m.menu_type IN (1, 2)
GROUP BY r.name, COALESCE(p.title, m.title)
ORDER BY r.name, domain;

SELECT '=== 4) 未授权任何角色的菜单（仅信息参考）===' AS info;
SELECT m.code, m.title, m.menu_type, m.permission
FROM sys_menu m
WHERE m.is_deleted = 0
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.menu_id = m.id)
ORDER BY m.code;

-- ---------------------------------------------------------------------
-- 4. 清理中间表
-- ---------------------------------------------------------------------
DROP TEMPORARY TABLE IF EXISTS tmp_role_menu_map;
DROP TABLE IF EXISTS tmp_role_closure;

-- =====================================================================
-- ERP 零售模块表结构修复脚本（幂等，可重复执行）
-- 目标库：erp-data
-- 生成日期：2026-10-01
--
-- 问题现象：
--   GET /api/retail/config          -> Unknown column 'create_by' in 'field list'
--   GET /api/retail/out/page        -> Unknown column 'deleted' in 'where clause'
--   GET /api/retail/return/page     -> Unknown column 'deleted' in 'where clause'
--
-- 根因：
--   RetailConfig / RetailOutSheet / RetailReturn 三个实体均 extends
--   com.erp.business.entity.BaseBusinessEntity，该基类带 6 个公共字段：
--       id, create_time, update_time, create_by, update_by, deleted
--   而 sql/retail_module*.sql 建表时漏加，导致 MyBatis-Plus 生成的 SQL 落库即失败。
--
--   注：零售明细表（retail_out_sheet_detail / retail_return_detail）的实体是
--   独立实现 Serializable、不继承基类，因此明细表无需这些列，本脚本不改动。
--
-- 说明：
--   全库 65 个实体交叉校验后，仅这 3 张表存在此问题，本脚本只修这 3 张。
--   列一律加在 create_time / update_time 附近，与 base_warehouse 等既有表的
--   列顺序保持一致。deleted 使用 NOT NULL DEFAULT 0，避免历史行被逻辑删除过滤掉。
--
-- ⚠️ 执行前请先备份：
--   mysqldump ... erp-data retail_config retail_out_sheet retail_return > retail_bak.sql
-- =====================================================================

SET @db := DATABASE();

-- ---------------------------------------------------------------------
-- 1. retail_config：缺 create_by / update_by / deleted
-- ---------------------------------------------------------------------

SET @s := (SELECT IF(COUNT(*) = 0,
    'ALTER TABLE retail_config ADD COLUMN create_by varchar(50) DEFAULT NULL COMMENT ''创建人'' AFTER create_time',
    'SELECT ''[skip] retail_config.create_by 已存在''')
  FROM information_schema.columns
  WHERE table_schema = @db AND table_name = 'retail_config' AND column_name = 'create_by');
PREPARE s FROM @s; EXECUTE s; DEALLOCATE PREPARE s;

SET @s := (SELECT IF(COUNT(*) = 0,
    'ALTER TABLE retail_config ADD COLUMN update_by varchar(50) DEFAULT NULL COMMENT ''更新人'' AFTER update_time',
    'SELECT ''[skip] retail_config.update_by 已存在''')
  FROM information_schema.columns
  WHERE table_schema = @db AND table_name = 'retail_config' AND column_name = 'update_by');
PREPARE s FROM @s; EXECUTE s; DEALLOCATE PREPARE s;

SET @s := (SELECT IF(COUNT(*) = 0,
    'ALTER TABLE retail_config ADD COLUMN deleted tinyint NOT NULL DEFAULT 0 COMMENT ''删除标记: 0-未删除 1-已删除'' AFTER update_by',
    'SELECT ''[skip] retail_config.deleted 已存在''')
  FROM information_schema.columns
  WHERE table_schema = @db AND table_name = 'retail_config' AND column_name = 'deleted');
PREPARE s FROM @s; EXECUTE s; DEALLOCATE PREPARE s;

-- ---------------------------------------------------------------------
-- 2. retail_out_sheet：缺 deleted（create_by / update_by 已有）
-- ---------------------------------------------------------------------

SET @s := (SELECT IF(COUNT(*) = 0,
    'ALTER TABLE retail_out_sheet ADD COLUMN deleted tinyint NOT NULL DEFAULT 0 COMMENT ''删除标记: 0-未删除 1-已删除'' AFTER update_time',
    'SELECT ''[skip] retail_out_sheet.deleted 已存在''')
  FROM information_schema.columns
  WHERE table_schema = @db AND table_name = 'retail_out_sheet' AND column_name = 'deleted');
PREPARE s FROM @s; EXECUTE s; DEALLOCATE PREPARE s;

-- ---------------------------------------------------------------------
-- 3. retail_return：缺 deleted（create_by / update_by 已有）
-- ---------------------------------------------------------------------

SET @s := (SELECT IF(COUNT(*) = 0,
    'ALTER TABLE retail_return ADD COLUMN deleted tinyint NOT NULL DEFAULT 0 COMMENT ''删除标记: 0-未删除 1-已删除'' AFTER update_time',
    'SELECT ''[skip] retail_return.deleted 已存在''')
  FROM information_schema.columns
  WHERE table_schema = @db AND table_name = 'retail_return' AND column_name = 'deleted');
PREPARE s FROM @s; EXECUTE s; DEALLOCATE PREPARE s;

-- ---------------------------------------------------------------------
-- 4. 校验：三张表的公共列应完整
-- ---------------------------------------------------------------------

SELECT '=== 修复后校验（每张表应有 6 行公共列）===' AS info;
SELECT table_name, COUNT(*) AS common_cols
  FROM information_schema.columns
 WHERE table_schema = @db
   AND table_name IN ('retail_config', 'retail_out_sheet', 'retail_return')
   AND column_name IN ('id', 'create_time', 'update_time', 'create_by', 'update_by', 'deleted')
 GROUP BY table_name;

SELECT '=== 残留问题（应为空）===' AS info;
SELECT t.table_name, c.column_name AS missing_col
  FROM information_schema.tables t
  JOIN (SELECT 'id' AS column_name UNION ALL SELECT 'create_time' UNION ALL SELECT 'update_time'
        UNION ALL SELECT 'create_by' UNION ALL SELECT 'update_by' UNION ALL SELECT 'deleted') c
 WHERE t.table_schema = @db
   AND t.table_name IN ('retail_config', 'retail_out_sheet', 'retail_return')
   AND NOT EXISTS (SELECT 1 FROM information_schema.columns x
                    WHERE x.table_schema = t.table_schema AND x.table_name = t.table_name
                      AND x.column_name = c.column_name);

SELECT '=== retail_config 结构 ===' AS info;
SHOW COLUMNS FROM retail_config;

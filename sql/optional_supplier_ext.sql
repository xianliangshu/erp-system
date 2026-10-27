-- =============================================================================
-- 【建议方案 · 未执行】供应商表轮胎业务扩展
-- -----------------------------------------------------------------------------
-- 现状问题：base_supplier 无法表达轮胎经销最关键的三个属性
--   1) 渠道层级（厂家直供 / 省代 / 区域批发 / 平台外贸）—— 现有 type 只有
--      「1原材料 2设备 3服务」，轮胎商品供应全部挤在 type=1 里
--   2) 代理/供应品牌 —— 一个供应商常同时供多个品牌（如赛轮集团供 SAILUN + ROADX），
--      现在只能塞在 remark 里，无法按品牌筛选、无法做「该品牌有哪些供应商」
--   3) 代理区域 —— 同一品牌不同省代，区域是核心区别
--
-- 方案：加 2 个列 + 1 张关联表，**不动已有字段语义**，前端老页面无感。
-- 幂等：建表 IF NOT EXISTS，加列走 information_schema + PREPARE 动态 DDL。
--
-- 执行方式（确认后再跑）：
--   mysql -h192.168.31.199 -uroot -p123456 --default-character-set=utf8mb4 \
--         erp-data < sql/optional_supplier_ext.sql
-- =============================================================================

SET @db := DATABASE();

-- -----------------------------------------------------------------------------
-- 1. base_supplier 加「渠道层级」与「代理区域」
--    channel_level: 1-厂家直供 2-省级代理 3-区域批发 4-平台/外贸 5-服务商
-- -----------------------------------------------------------------------------
SET @sql := (SELECT IF(COUNT(*) = 0,
    'ALTER TABLE `base_supplier` ADD COLUMN `channel_level` TINYINT DEFAULT NULL COMMENT ''渠道层级(1=厂家直供,2=省级代理,3=区域批发,4=平台/外贸,5=服务商)'' AFTER `type`',
    'DO 0')
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'base_supplier' AND COLUMN_NAME = 'channel_level');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := (SELECT IF(COUNT(*) = 0,
    'ALTER TABLE `base_supplier` ADD COLUMN `agent_region` VARCHAR(100) DEFAULT NULL COMMENT ''代理区域(如:华东/广东省/上海市)'' AFTER `channel_level`',
    'DO 0')
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'base_supplier' AND COLUMN_NAME = 'agent_region');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- -----------------------------------------------------------------------------
-- 2. 供应商 ↔ 品牌 关联表（多对多）
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `base_supplier_brand` (
    `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `supplier_id` BIGINT      NOT NULL COMMENT '供应商ID',
    `brand_id`    BIGINT      NOT NULL COMMENT '品牌ID',
    `is_main`     TINYINT     DEFAULT 0 COMMENT '是否主供品牌(0=否,1=是)',
    `deleted`     TINYINT     DEFAULT 0 COMMENT '删除标记(0=未删除,1=已删除)',
    `create_by`   VARCHAR(50) DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`   VARCHAR(50) DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_supplier_brand` (`supplier_id`, `brand_id`),
    KEY `idx_brand_id` (`brand_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商供应品牌关系';

-- -----------------------------------------------------------------------------
-- 3. 回填已录入的 12 家供应商（按供应商编号 + 品牌编号精确匹配，幂等）
-- -----------------------------------------------------------------------------
-- 3.1 渠道层级与代理区域
UPDATE `base_supplier` SET `channel_level` = 1, `agent_region` = '全国'      WHERE `code` IN ('SUP000004','SUP000005','SUP000006','SUP000007','SUP000008','SUP000009','SUP000010');
UPDATE `base_supplier` SET `channel_level` = 2, `agent_region` = '华南'      WHERE `code` = 'SUP000011';
UPDATE `base_supplier` SET `channel_level` = 4, `agent_region` = '全国(线上)' WHERE `code` = 'SUP000012';
UPDATE `base_supplier` SET `channel_level` = 3, `agent_region` = '华东'      WHERE `code` = 'SUP000013';
UPDATE `base_supplier` SET `channel_level` = 4, `agent_region` = '山东(外贸)' WHERE `code` = 'SUP000014';
UPDATE `base_supplier` SET `channel_level` = 5, `agent_region` = '全国'      WHERE `code` = 'SUP000015';
UPDATE `base_supplier` SET `channel_level` = 5, `agent_region` = '全国'      WHERE `code` = 'SUP000016';

-- 3.2 供应品牌（一一对应该品牌在 base_brand 的编号）
INSERT INTO `base_supplier_brand` (`supplier_id`, `brand_id`, `is_main`, `deleted`, `create_time`, `update_time`)
SELECT s.`id`, b.`id`, 1, 0, NOW(), NOW()
FROM `base_supplier` s, `base_brand` b
WHERE s.`code` = 'SUP000004' AND b.`code` = 'BRD000004'
  AND NOT EXISTS (SELECT 1 FROM `base_supplier_brand` x WHERE x.`supplier_id` = s.`id` AND x.`brand_id` = b.`id`);

INSERT INTO `base_supplier_brand` (`supplier_id`, `brand_id`, `is_main`, `deleted`, `create_time`, `update_time`)
SELECT s.`id`, b.`id`, 1, 0, NOW(), NOW()
FROM `base_supplier` s, `base_brand` b
WHERE s.`code` = 'SUP000005' AND b.`code` = 'BRD000005'
  AND NOT EXISTS (SELECT 1 FROM `base_supplier_brand` x WHERE x.`supplier_id` = s.`id` AND x.`brand_id` = b.`id`);

INSERT INTO `base_supplier_brand` (`supplier_id`, `brand_id`, `is_main`, `deleted`, `create_time`, `update_time`)
SELECT s.`id`, b.`id`, 1, 0, NOW(), NOW()
FROM `base_supplier` s, `base_brand` b
WHERE s.`code` = 'SUP000006' AND b.`code` = 'BRD000006'
  AND NOT EXISTS (SELECT 1 FROM `base_supplier_brand` x WHERE x.`supplier_id` = s.`id` AND x.`brand_id` = b.`id`);

INSERT INTO `base_supplier_brand` (`supplier_id`, `brand_id`, `is_main`, `deleted`, `create_time`, `update_time`)
SELECT s.`id`, b.`id`, 1, 0, NOW(), NOW()
FROM `base_supplier` s, `base_brand` b
WHERE s.`code` = 'SUP000007' AND b.`code` = 'BRD000007'
  AND NOT EXISTS (SELECT 1 FROM `base_supplier_brand` x WHERE x.`supplier_id` = s.`id` AND x.`brand_id` = b.`id`);

INSERT INTO `base_supplier_brand` (`supplier_id`, `brand_id`, `is_main`, `deleted`, `create_time`, `update_time`)
SELECT s.`id`, b.`id`, 1, 0, NOW(), NOW()
FROM `base_supplier` s, `base_brand` b
WHERE s.`code` = 'SUP000008' AND b.`code` = 'BRD000008'
  AND NOT EXISTS (SELECT 1 FROM `base_supplier_brand` x WHERE x.`supplier_id` = s.`id` AND x.`brand_id` = b.`id`);

-- 赛轮集团同时供「赛轮路极」品牌
INSERT INTO `base_supplier_brand` (`supplier_id`, `brand_id`, `is_main`, `deleted`, `create_time`, `update_time`)
SELECT s.`id`, b.`id`, 1, 0, NOW(), NOW()
FROM `base_supplier` s, `base_brand` b
WHERE s.`code` = 'SUP000009' AND b.`code` = 'BRD000010'
  AND NOT EXISTS (SELECT 1 FROM `base_supplier_brand` x WHERE x.`supplier_id` = s.`id` AND x.`brand_id` = b.`id`);

INSERT INTO `base_supplier_brand` (`supplier_id`, `brand_id`, `is_main`, `deleted`, `create_time`, `update_time`)
SELECT s.`id`, b.`id`, 1, 0, NOW(), NOW()
FROM `base_supplier` s, `base_brand` b
WHERE s.`code` = 'SUP000010' AND b.`code` = 'BRD000011'
  AND NOT EXISTS (SELECT 1 FROM `base_supplier_brand` x WHERE x.`supplier_id` = s.`id` AND x.`brand_id` = b.`id`);

INSERT INTO `base_supplier_brand` (`supplier_id`, `brand_id`, `is_main`, `deleted`, `create_time`, `update_time`)
SELECT s.`id`, b.`id`, 1, 0, NOW(), NOW()
FROM `base_supplier` s, `base_brand` b
WHERE s.`code` = 'SUP000011' AND b.`code` = 'BRD000009'
  AND NOT EXISTS (SELECT 1 FROM `base_supplier_brand` x WHERE x.`supplier_id` = s.`id` AND x.`brand_id` = b.`id`);

-- -----------------------------------------------------------------------------
-- 4. 核对
-- -----------------------------------------------------------------------------
SELECT s.`code`, s.`short_name`, s.`channel_level`, s.`agent_region`,
       GROUP_CONCAT(b.`name` SEPARATOR '、') AS `供应品牌`
FROM `base_supplier` s
LEFT JOIN `base_supplier_brand` sb ON sb.`supplier_id` = s.`id` AND sb.`deleted` = 0
LEFT JOIN `base_brand` b ON b.`id` = sb.`brand_id`
WHERE s.`deleted` = 0 AND s.`code` >= 'SUP000004'
GROUP BY s.`id`, s.`code`, s.`short_name`, s.`channel_level`, s.`agent_region`
ORDER BY s.`code`;

-- =============================================================================
-- 商品中心 SKU 改造 —— S1 表结构与数据迁移
-- -----------------------------------------------------------------------------
-- 执行方式（必须指定库，脚本内不写 USE）：
--   mysql -h192.168.31.199 -uroot -p123456 --default-character-set=utf8mb4 \
--         erp-data < sql/add_sku_center.sql
--
-- 特性：**全量幂等**，可重复执行。
--   建表用 IF NOT EXISTS；补列/补索引走 information_schema + PREPARE 动态 DDL
--   （MySQL 8 不支持 ADD COLUMN IF NOT EXISTS）；迁移用 NOT EXISTS 守卫。
--
-- 设计要点：base_material 即 SKU，其 id 沿用单据/库存表的 product_id。
--   因此 13 张业务表、58 处 Java 引用、23 个前端文件全部零改动。
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. SPU 商品主表（聚合层）
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `base_spu` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'SPU ID',
    `category_id`   BIGINT       NOT NULL COMMENT '分类ID',
    `brand_id`      BIGINT       DEFAULT NULL COMMENT '品牌ID',
    `unit_id`       BIGINT       NOT NULL COMMENT '基本计量单位ID',
    `code`          VARCHAR(50)  NOT NULL COMMENT 'SPU编号',
    `name`          VARCHAR(200) NOT NULL COMMENT '商品名称',
    `short_name`    VARCHAR(100) DEFAULT NULL COMMENT '商品简称',
    `specification` VARCHAR(200) DEFAULT NULL COMMENT '主体规格描述',
    `main_image`    VARCHAR(500) DEFAULT NULL COMMENT '商品主图',
    `status`        TINYINT      DEFAULT 1 COMMENT '状态(0=禁用,1=启用)',
    `remark`        VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `deleted`       TINYINT      DEFAULT 0 COMMENT '删除标记(0=未删除,1=已删除)',
    `create_by`     VARCHAR(50)  DEFAULT NULL COMMENT '创建人',
    `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`     VARCHAR(50)  DEFAULT NULL COMMENT '更新人',
    `update_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_brand_id` (`brand_id`),
    KEY `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SPU商品主表';

-- -----------------------------------------------------------------------------
-- 2. 销售属性组（如"颜色"、"尺码"）
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `base_sale_property` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '属性组ID',
    `code`        VARCHAR(50)  NOT NULL COMMENT '属性组编码',
    `name`        VARCHAR(50)  NOT NULL COMMENT '属性组名称',
    `sort`        INT          DEFAULT 0 COMMENT '排序',
    `status`      TINYINT      DEFAULT 1 COMMENT '状态(0=禁用,1=启用)',
    `remark`      VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `deleted`     TINYINT      DEFAULT 0 COMMENT '删除标记(0=未删除,1=已删除)',
    `create_by`   VARCHAR(50)  DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`   VARCHAR(50)  DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品销售属性组';

-- -----------------------------------------------------------------------------
-- 3. 销售属性值（如"红色"、"XL"）
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `base_sale_property_item` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '属性值ID',
    `property_id` BIGINT       NOT NULL COMMENT '属性组ID',
    `code`        VARCHAR(50)  NOT NULL COMMENT '属性值编码',
    `name`        VARCHAR(50)  NOT NULL COMMENT '属性值名称',
    `sort`        INT          DEFAULT 0 COMMENT '排序',
    `status`      TINYINT      DEFAULT 1 COMMENT '状态(0=禁用,1=启用)',
    `remark`      VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `deleted`     TINYINT      DEFAULT 0 COMMENT '删除标记(0=未删除,1=已删除)',
    `create_by`   VARCHAR(50)  DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`   VARCHAR(50)  DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_prop_code` (`property_id`, `code`),
    KEY `idx_property_id` (`property_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品销售属性值';

-- -----------------------------------------------------------------------------
-- 4. SPU 与属性组绑定（决定该商品用哪几个维度生成 SKU 矩阵）
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `base_spu_sale_property` (
    `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `spu_id`      BIGINT      NOT NULL COMMENT 'SPU ID',
    `property_id` BIGINT      NOT NULL COMMENT '属性组ID',
    `deleted`     TINYINT     DEFAULT 0 COMMENT '删除标记(0=未删除,1=已删除)',
    `create_by`   VARCHAR(50) DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`   VARCHAR(50) DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_spu_property` (`spu_id`, `property_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SPU销售属性绑定';

-- -----------------------------------------------------------------------------
-- 5. SKU 与属性值绑定（拼出 sale_property_text）
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `base_material_property_rel` (
    `id`               BIGINT      NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `material_id`      BIGINT      NOT NULL COMMENT 'SKU ID(=base_material.id)',
    `property_id`      BIGINT      NOT NULL COMMENT '属性组ID',
    `property_item_id` BIGINT      NOT NULL COMMENT '属性值ID',
    `deleted`          TINYINT     DEFAULT 0 COMMENT '删除标记(0=未删除,1=已删除)',
    `create_by`        VARCHAR(50) DEFAULT NULL COMMENT '创建人',
    `create_time`      DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`        VARCHAR(50) DEFAULT NULL COMMENT '更新人',
    `update_time`      DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_mat_property` (`material_id`, `property_id`),
    KEY `idx_item` (`property_item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SKU销售属性关系';

-- -----------------------------------------------------------------------------
-- 6. 一品多码
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `base_material_code` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `material_id` BIGINT       NOT NULL COMMENT 'SKU ID',
    `code`        VARCHAR(50)  NOT NULL COMMENT '编码/条码',
    `code_type`   TINYINT      NOT NULL DEFAULT 1 COMMENT '类型(1=主编号,2=条码,3=其他)',
    `is_main`     TINYINT      DEFAULT 0 COMMENT '是否主码(0=否,1=是)',
    `remark`      VARCHAR(200) DEFAULT NULL COMMENT '备注',
    `deleted`     TINYINT      DEFAULT 0 COMMENT '删除标记(0=未删除,1=已删除)',
    `create_by`   VARCHAR(50)  DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`   VARCHAR(50)  DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code_type` (`code`, `code_type`),
    KEY `idx_material_id` (`material_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SKU一品多码';

-- -----------------------------------------------------------------------------
-- 7. base_material 补 4 列（全部可空/有默认值 ⇒ 老代码不感知，可先后端再前端）
-- -----------------------------------------------------------------------------
SET @db := DATABASE();

SET @sql := (SELECT IF(COUNT(*) = 0,
    'ALTER TABLE `base_material` ADD COLUMN `spu_id` BIGINT DEFAULT NULL COMMENT ''SPU ID'' AFTER `category_id`',
    'DO 0')
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'base_material' AND COLUMN_NAME = 'spu_id');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := (SELECT IF(COUNT(*) = 0,
    'ALTER TABLE `base_material` ADD COLUMN `sale_property_text` VARCHAR(500) DEFAULT '''' COMMENT ''销售属性文本(如:颜色:红 / 尺码:XL)'' AFTER `specification`',
    'DO 0')
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'base_material' AND COLUMN_NAME = 'sale_property_text');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := (SELECT IF(COUNT(*) = 0,
    'ALTER TABLE `base_material` ADD COLUMN `main_image` VARCHAR(500) DEFAULT NULL COMMENT ''SKU图片'' AFTER `sale_property_text`',
    'DO 0')
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'base_material' AND COLUMN_NAME = 'main_image');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := (SELECT IF(COUNT(*) = 0,
    'ALTER TABLE `base_material` ADD COLUMN `is_multi_code` TINYINT DEFAULT 0 COMMENT ''是否一品多码(0=否,1=是)'' AFTER `main_image`',
    'DO 0')
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'base_material' AND COLUMN_NAME = 'is_multi_code');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := (SELECT IF(COUNT(*) = 0,
    'ALTER TABLE `base_material` ADD KEY `idx_spu_id` (`spu_id`)',
    'DO 0')
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'base_material' AND INDEX_NAME = 'idx_spu_id');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- -----------------------------------------------------------------------------
-- 8. 数据迁移
--    存量物料 1:1 生成 SPU（编号走 SPU 规则，与原 MAT 编号脱钩，语义上是两个维度）
--    用临时表建立 material → spu 的映射，保证回填准确；重跑时临时表为空，天然幂等。
-- -----------------------------------------------------------------------------
DROP TEMPORARY TABLE IF EXISTS `tmp_mat_spu`;
CREATE TEMPORARY TABLE `tmp_mat_spu` AS
SELECT m.`id` AS `material_id`,
       CONCAT('SPU', LPAD(ROW_NUMBER() OVER (ORDER BY m.`id`), 6, '0')) AS `spu_code`
FROM `base_material` m
WHERE m.`deleted` = 0 AND m.`spu_id` IS NULL;

INSERT INTO `base_spu` (`category_id`, `brand_id`, `unit_id`, `code`, `name`, `short_name`,
                        `specification`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT m.`category_id`, NULL, m.`unit_id`, t.`spu_code`, m.`name`, m.`short_name`,
       m.`specification`, m.`status`, m.`remark`, 0, NOW(), NOW()
FROM `base_material` m
INNER JOIN `tmp_mat_spu` t ON t.`material_id` = m.`id`;

UPDATE `base_material` m
INNER JOIN `tmp_mat_spu` t ON t.`material_id` = m.`id`
INNER JOIN `base_spu` s ON s.`code` = t.`spu_code`
SET m.`spu_id` = s.`id`;

DROP TEMPORARY TABLE IF EXISTS `tmp_mat_spu`;

-- 每个存量物料生成一条主码记录（code_type=1 主编号）
INSERT INTO `base_material_code` (`material_id`, `code`, `code_type`, `is_main`,
                                  `remark`, `deleted`, `create_time`, `update_time`)
SELECT m.`id`, m.`code`, 1, 1, '系统迁移生成', 0, NOW(), NOW()
FROM `base_material` m
WHERE m.`deleted` = 0
  AND NOT EXISTS (SELECT 1 FROM `base_material_code` c
                  WHERE c.`material_id` = m.`id` AND c.`code` = m.`code` AND c.`code_type` = 1);

-- -----------------------------------------------------------------------------
-- 9. 编号规则：新增 SPU（主数据编码，不按日重置），并把流水初始化到当前最大值
-- -----------------------------------------------------------------------------
INSERT INTO `sys_code_rule` (`rule_code`, `rule_name`, `prefix`, `date_format`, `seq_length`,
                             `seq_start`, `seq_step`, `current_value`, `seq_date`, `reset_by_date`,
                             `status`, `remark`, `is_deleted`, `create_time`, `update_time`)
SELECT 'SPU', '商品SPU', 'SPU', '', 6, 1, 1, 0, '', 0, 1, '商品中心SPU编号', 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_code_rule` WHERE `rule_code` = 'SPU');

UPDATE `sys_code_rule`
SET `current_value` = (SELECT COALESCE(MAX(CAST(REGEXP_REPLACE(RIGHT(`code`, 6), '[^0-9]', '') AS UNSIGNED)), 0)
                       FROM `base_spu` WHERE `code` LIKE 'SPU%')
WHERE `rule_code` = 'SPU'
  AND EXISTS (SELECT 1 FROM `base_spu` WHERE `code` LIKE 'SPU%');

-- 销售属性组 / 属性值同样走表驱动编号（主数据编码，不按日重置）
INSERT INTO `sys_code_rule` (`rule_code`, `rule_name`, `prefix`, `date_format`, `seq_length`,
                             `seq_start`, `seq_step`, `current_value`, `seq_date`, `reset_by_date`,
                             `status`, `remark`, `is_deleted`, `create_time`, `update_time`)
SELECT 'SALE_PROPERTY', '销售属性组', 'SP', '', 6, 1, 1, 0, '', 0, 1, '商品销售属性组编号', 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_code_rule` WHERE `rule_code` = 'SALE_PROPERTY');

INSERT INTO `sys_code_rule` (`rule_code`, `rule_name`, `prefix`, `date_format`, `seq_length`,
                             `seq_start`, `seq_step`, `current_value`, `seq_date`, `reset_by_date`,
                             `status`, `remark`, `is_deleted`, `create_time`, `update_time`)
SELECT 'SALE_PROPERTY_ITEM', '销售属性值', 'SI', '', 6, 1, 1, 0, '', 0, 1, '商品销售属性值编号', 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_code_rule` WHERE `rule_code` = 'SALE_PROPERTY_ITEM');

-- -----------------------------------------------------------------------------
-- 10. 迁移结果核对（执行后请人工确认输出）
-- -----------------------------------------------------------------------------
SELECT 'base_spu 行数'           AS `检查项`, COUNT(*) AS `值` FROM `base_spu`
UNION ALL SELECT 'base_material 总数',   COUNT(*) FROM `base_material` WHERE `deleted` = 0
UNION ALL SELECT 'spu_id 为空的物料数',  COUNT(*) FROM `base_material` WHERE `deleted` = 0 AND `spu_id` IS NULL
UNION ALL SELECT 'base_material_code',   COUNT(*) FROM `base_material_code`
UNION ALL SELECT '编号规则(SPU/SP/SI)',  COUNT(*) FROM `sys_code_rule` WHERE `rule_code` IN ('SPU', 'SALE_PROPERTY', 'SALE_PROPERTY_ITEM');

-- -----------------------------------------------------------------------------
-- 11. 编号流水加固（重要）—— 主数据编码必须取「包含已逻辑删除记录」的最大编号
-- -----------------------------------------------------------------------------
-- 背景：主数据表是逻辑删除，被删的行仍然留在表里，而唯一键 (code) 不含 deleted 列。
-- 若 current_value 只按「未删除记录」初始化或因为回滚而落后，下一次生成的编号就会
-- 撞上已删除行，报 `Duplicate entry 'MAT000005' for key 'base_material.uk_code'`。
-- （2026-10-03 实测踩到：库里存在一条 deleted=1 的 MAT000005，导致新增 SKU 直接 500。）
-- 下面统一按「全表最大编号」校正，幂等且可随时重跑。
UPDATE `sys_code_rule` SET `current_value` = (SELECT COALESCE(MAX(CAST(REGEXP_REPLACE(RIGHT(`code`, 6), '[^0-9]', '') AS UNSIGNED)), 0) FROM `base_material`           WHERE `code` LIKE 'MAT%')  WHERE `rule_code` = 'BASE_MATERIAL';
UPDATE `sys_code_rule` SET `current_value` = (SELECT COALESCE(MAX(CAST(REGEXP_REPLACE(RIGHT(`code`, 6), '[^0-9]', '') AS UNSIGNED)), 0) FROM `base_material_category` WHERE `code` LIKE 'MC%')   WHERE `rule_code` = 'BASE_MATERIAL_CATEGORY';
UPDATE `sys_code_rule` SET `current_value` = (SELECT COALESCE(MAX(CAST(REGEXP_REPLACE(RIGHT(`code`, 6), '[^0-9]', '') AS UNSIGNED)), 0) FROM `base_customer`           WHERE `code` LIKE 'CUS%')  WHERE `rule_code` = 'BASE_CUSTOMER';
UPDATE `sys_code_rule` SET `current_value` = (SELECT COALESCE(MAX(CAST(REGEXP_REPLACE(RIGHT(`code`, 6), '[^0-9]', '') AS UNSIGNED)), 0) FROM `base_supplier`           WHERE `code` LIKE 'SUP%')  WHERE `rule_code` = 'BASE_SUPPLIER';
UPDATE `sys_code_rule` SET `current_value` = (SELECT COALESCE(MAX(CAST(REGEXP_REPLACE(RIGHT(`code`, 6), '[^0-9]', '') AS UNSIGNED)), 0) FROM `base_warehouse`          WHERE `code` LIKE 'WH%')   WHERE `rule_code` = 'BASE_WAREHOUSE';
UPDATE `sys_code_rule` SET `current_value` = (SELECT COALESCE(MAX(CAST(REGEXP_REPLACE(RIGHT(`code`, 6), '[^0-9]', '') AS UNSIGNED)), 0) FROM `base_brand`              WHERE `code` LIKE 'BRD%')  WHERE `rule_code` = 'BASE_BRAND';
UPDATE `sys_code_rule` SET `current_value` = (SELECT COALESCE(MAX(CAST(REGEXP_REPLACE(RIGHT(`code`, 6), '[^0-9]', '') AS UNSIGNED)), 0) FROM `base_unit`               WHERE `code` LIKE 'UNIT%') WHERE `rule_code` = 'BASE_UNIT';
UPDATE `sys_code_rule` SET `current_value` = (SELECT COALESCE(MAX(CAST(REGEXP_REPLACE(RIGHT(`code`, 6), '[^0-9]', '') AS UNSIGNED)), 0) FROM `base_spu`                WHERE `code` LIKE 'SPU%')  WHERE `rule_code` = 'SPU';
UPDATE `sys_code_rule` SET `current_value` = (SELECT COALESCE(MAX(CAST(REGEXP_REPLACE(RIGHT(`code`, 6), '[^0-9]', '') AS UNSIGNED)), 0) FROM `base_sale_property`      WHERE `code` LIKE 'SP%')   WHERE `rule_code` = 'SALE_PROPERTY';
UPDATE `sys_code_rule` SET `current_value` = (SELECT COALESCE(MAX(CAST(REGEXP_REPLACE(RIGHT(`code`, 6), '[^0-9]', '') AS UNSIGNED)), 0) FROM `base_sale_property_item` WHERE `code` LIKE 'SI%')   WHERE `rule_code` = 'SALE_PROPERTY_ITEM';

SELECT `rule_code`, `prefix`, `current_value` FROM `sys_code_rule` WHERE `reset_by_date` = 0 ORDER BY `rule_code`;

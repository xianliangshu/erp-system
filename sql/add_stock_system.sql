-- =============================================================================
-- 库存体系补齐 —— S1 表结构（第一段：盘点参数 / 库存预警 / 库存成本调整）
-- -----------------------------------------------------------------------------
-- 执行方式（必须指定库，脚本内不写 USE）：
--   mysql -h192.168.31.199 -uroot -p123456 --default-character-set=utf8mb4 \
--         erp-data < sql/add_stock_system.sql
--
-- 特性：**全量幂等**，可重复执行。
--   建表用 IF NOT EXISTS；补列/补索引走 information_schema + PREPARE 动态 DDL
--   （MySQL 8 不支持 ADD COLUMN IF NOT EXISTS）；菜单与编号规则用 NOT EXISTS 守卫。
--
-- 设计边界（重要）：
--   1. 本批**不改动** product_stock 的读写维度（仍是 sc_id + product_id 两维）。
--      库位（仓位商品）会引入第三维，风险等级不同，已确认单列为下一批。
--   2. 唯一会写 product_stock 的新功能是「库存成本调整单」，且只改 tax_price/tax_amount，
--      **不改 stock_num**，因此绝不能走 addStock（那会把数量加一遍）。
--   3. 本批全部是新增表/新增列，无存量数据迁移，回滚只需 DROP。
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. stock_warning 库存预警（仓库 × 商品 的上下限）
--    注：base_material.min_stock/max_stock 是物料级的老字段，全库无代码读取，
--        本表是更细的仓库级预警，二者并存，老字段保留不动。
--    ⚠️ 刻意**不加唯一键** (sc_id, product_id)：带逻辑删除的表加唯一键，删除后重建必撞键。
--       判重放在应用层（保存前先查同仓库同商品是否已有配置）。
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `stock_warning` (
    `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '预警配置ID',
    `sc_id`       BIGINT        NOT NULL COMMENT '仓库ID',
    `product_id`  BIGINT        NOT NULL COMMENT '商品ID(即 SKU，沿用 base_material.id)',
    `max_limit`   DECIMAL(16,4) DEFAULT NULL COMMENT '预警上限，NULL=不限上限',
    `min_limit`   DECIMAL(16,4) DEFAULT NULL COMMENT '预警下限，NULL=不限下限',
    `status`      TINYINT       NOT NULL DEFAULT 1 COMMENT '状态(0=停用,1=启用)',
    `remark`      VARCHAR(500)  DEFAULT NULL COMMENT '备注',
    `deleted`     TINYINT       DEFAULT 0 COMMENT '删除标记(0=未删除,1=已删除)',
    `create_by`   VARCHAR(50)   DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`   VARCHAR(50)   DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_sc_product` (`sc_id`, `product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存预警配置';

-- -----------------------------------------------------------------------------
-- 2. stock_cost_adjust_sheet 库存成本调整单（只改成本价，不改数量）
--    与既有「库存调整单」的分工：调整单改数量，本单改价格。
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `stock_cost_adjust_sheet` (
    `id`            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '单据ID',
    `code`          VARCHAR(32)   NOT NULL COMMENT '单据编号',
    `sc_id`         BIGINT        NOT NULL COMMENT '仓库ID',
    `product_num`   INT           NOT NULL DEFAULT 0 COMMENT '调价品种数',
    `diff_amount`   DECIMAL(16,4) NOT NULL DEFAULT 0 COMMENT '库存调价差额合计',
    `status`        TINYINT       NOT NULL DEFAULT 0 COMMENT '状态(0=待审核,1=审核通过,2=审核拒绝)',
    `description`   VARCHAR(500)  DEFAULT NULL COMMENT '备注',
    `approve_by`    VARCHAR(50)   DEFAULT NULL COMMENT '审核人',
    `approve_time`  DATETIME      DEFAULT NULL COMMENT '审核时间',
    `refuse_reason` VARCHAR(500)  DEFAULT NULL COMMENT '拒绝原因',
    `deleted`       TINYINT       DEFAULT 0 COMMENT '删除标记(0=未删除,1=已删除)',
    `create_by`     VARCHAR(50)   DEFAULT NULL COMMENT '创建人',
    `create_time`   DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`     VARCHAR(50)   DEFAULT NULL COMMENT '更新人',
    `update_time`   DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`),
    KEY `idx_sc_id` (`sc_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存成本调整单';

-- 明细表不继承实体基类（项目约定：全部 *_detail 表都是独立实现，无 deleted 列）
CREATE TABLE IF NOT EXISTS `stock_cost_adjust_sheet_detail` (
    `id`             BIGINT        NOT NULL AUTO_INCREMENT COMMENT '明细ID',
    `sheet_id`       BIGINT        NOT NULL COMMENT '单据ID',
    `product_id`     BIGINT        NOT NULL COMMENT '商品ID',
    `stock_num`      DECIMAL(16,4) NOT NULL DEFAULT 0 COMMENT '库存数量(制单时快照，审核前二次校验)',
    `purchase_price` DECIMAL(16,4) DEFAULT NULL COMMENT '档案采购价(参考值)',
    `ori_price`      DECIMAL(16,4) NOT NULL DEFAULT 0 COMMENT '调整前成本价',
    `price`          DECIMAL(16,4) NOT NULL DEFAULT 0 COMMENT '调整后成本价',
    `diff_amount`    DECIMAL(16,4) NOT NULL DEFAULT 0 COMMENT '调价差额=(price-ori_price)*stock_num',
    `description`    VARCHAR(500)  DEFAULT NULL COMMENT '备注',
    `order_no`       INT           NOT NULL DEFAULT 0 COMMENT '排序',
    PRIMARY KEY (`id`),
    KEY `idx_sheet_id` (`sheet_id`),
    KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存成本调整单明细';

-- -----------------------------------------------------------------------------
-- 3. take_stock_config 盘点参数设置（固定单行 id=1）
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `take_stock_config` (
    `id`                BIGINT      NOT NULL COMMENT '配置ID(固定为1)',
    `show_product`      TINYINT     NOT NULL DEFAULT 1 COMMENT '盘点单关联任务后是否带出任务商品(0=否,1=是)',
    `show_stock`        TINYINT     NOT NULL DEFAULT 1 COMMENT '是否显示任务创建时的系统库存快照(0=否,1=是)',
    `auto_change_stock` TINYINT     NOT NULL DEFAULT 0 COMMENT '差异生成时是否自动调整库存(0=否,1=是)',
    `allow_change_num`  TINYINT     NOT NULL DEFAULT 1 COMMENT '盘点数量是否允许手工修改(0=否,1=是)',
    `cancel_hours`      INT         NOT NULL DEFAULT 24 COMMENT '盘点任务超时自动作废小时数',
    `deleted`           TINYINT     DEFAULT 0 COMMENT '删除标记(0=未删除,1=已删除)',
    `create_by`         VARCHAR(50) DEFAULT NULL COMMENT '创建人',
    `create_time`       DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`         VARCHAR(50) DEFAULT NULL COMMENT '更新人',
    `update_time`       DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存盘点参数设置';

-- 种子数据：安全默认 = 不自动调账（auto_change_stock=0），需人工审核
INSERT INTO `take_stock_config` (`id`, `show_product`, `show_stock`, `auto_change_stock`,
                                 `allow_change_num`, `cancel_hours`, `deleted`, `create_time`, `update_time`)
SELECT 1, 1, 1, 0, 1, 24, 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM `take_stock_config` WHERE `id` = 1);

-- -----------------------------------------------------------------------------
-- 4. take_stock_plan 盘点任务（第二段实现，表先建好）
--    创建任务时把当前库存**快照**进明细 stock_num —— 这是"动碰盘点"的基础：
--    盘点期间的出入库不改变比对基准。
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `take_stock_plan` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '盘点任务ID',
    `code`        VARCHAR(32)  NOT NULL COMMENT '任务编号',
    `sc_id`       BIGINT       NOT NULL COMMENT '仓库ID',
    `take_type`   TINYINT      NOT NULL DEFAULT 0 COMMENT '盘点类别(0=全场,1=单品,2=分类,3=品牌)',
    `biz_id`      VARCHAR(500) DEFAULT NULL COMMENT '盘点范围ID串(逗号分隔：分类/品牌/商品ID)',
    `take_status` TINYINT      NOT NULL DEFAULT 0 COMMENT '状态(0=任务生成,6=差异已生成,9=盘点完成,12=已作废)',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `deleted`     TINYINT      DEFAULT 0 COMMENT '删除标记(0=未删除,1=已删除)',
    `create_by`   VARCHAR(50)  DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`   VARCHAR(50)  DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`),
    KEY `idx_sc_id` (`sc_id`),
    KEY `idx_take_status` (`take_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存盘点任务';

CREATE TABLE IF NOT EXISTS `take_stock_plan_detail` (
    `id`            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '明细ID',
    `plan_id`       BIGINT        NOT NULL COMMENT '盘点任务ID',
    `product_id`    BIGINT        NOT NULL COMMENT '商品ID',
    `stock_num`     DECIMAL(16,4) NOT NULL DEFAULT 0 COMMENT '创建任务时的系统库存快照',
    `ori_take_num`  DECIMAL(16,4) DEFAULT NULL COMMENT '盘点数量(原始录入)',
    `take_num`      DECIMAL(16,4) DEFAULT NULL COMMENT '修正后盘点数量',
    `total_out_num` DECIMAL(16,4) NOT NULL DEFAULT 0 COMMENT '盘点期间出库累计',
    `total_in_num`  DECIMAL(16,4) NOT NULL DEFAULT 0 COMMENT '盘点期间入库累计',
    `description`   VARCHAR(500)  DEFAULT NULL COMMENT '备注',
    `order_no`      INT           NOT NULL DEFAULT 0 COMMENT '排序',
    PRIMARY KEY (`id`),
    KEY `idx_plan_id` (`plan_id`),
    KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存盘点任务明细';

-- -----------------------------------------------------------------------------
-- 5. pre_take_stock_sheet 预先盘点单（第二段实现，表先建好）
--    初盘 / 复盘 / 抽盘 三个数量独立保存，纯记录，**不产生任何库存变动**。
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `pre_take_stock_sheet` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '预先盘点单ID',
    `code`        VARCHAR(32)  NOT NULL COMMENT '单据编号',
    `sc_id`       BIGINT       NOT NULL COMMENT '仓库ID',
    `take_status` TINYINT      NOT NULL DEFAULT 0 COMMENT '盘点轮次(0=初盘,1=复盘,2=抽盘)',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `deleted`     TINYINT      DEFAULT 0 COMMENT '删除标记(0=未删除,1=已删除)',
    `create_by`   VARCHAR(50)  DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`   VARCHAR(50)  DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`),
    KEY `idx_sc_id` (`sc_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存预先盘点单';

CREATE TABLE IF NOT EXISTS `pre_take_stock_sheet_detail` (
    `id`         BIGINT        NOT NULL AUTO_INCREMENT COMMENT '明细ID',
    `sheet_id`   BIGINT        NOT NULL COMMENT '预先盘点单ID',
    `product_id` BIGINT        NOT NULL COMMENT '商品ID',
    `first_num`  DECIMAL(16,4) DEFAULT NULL COMMENT '初盘数量',
    `second_num` DECIMAL(16,4) DEFAULT NULL COMMENT '复盘数量',
    `rand_num`   DECIMAL(16,4) DEFAULT NULL COMMENT '抽盘数量',
    `order_no`   INT           NOT NULL DEFAULT 0 COMMENT '排序',
    PRIMARY KEY (`id`),
    KEY `idx_sheet_id` (`sheet_id`),
    KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存预先盘点单明细';

-- -----------------------------------------------------------------------------
-- 6. stock_check 补 2 列：关联盘点任务 / 预先盘点单
--    两列全部可空 ⇒ 现有盘点单代码与 SQL 完全不受影响。
-- -----------------------------------------------------------------------------
SET @db := DATABASE();

SET @sql := (SELECT IF(COUNT(*) = 0,
    'ALTER TABLE `stock_check` ADD COLUMN `plan_id` BIGINT DEFAULT NULL COMMENT ''关联盘点任务ID'' AFTER `status`',
    'DO 0')
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'stock_check' AND COLUMN_NAME = 'plan_id');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := (SELECT IF(COUNT(*) = 0,
    'ALTER TABLE `stock_check` ADD COLUMN `pre_sheet_id` BIGINT DEFAULT NULL COMMENT ''关联预先盘点单ID'' AFTER `plan_id`',
    'DO 0')
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'stock_check' AND COLUMN_NAME = 'pre_sheet_id');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- -----------------------------------------------------------------------------
-- 7. 编号规则：3 条单据编号（按日重置）
-- -----------------------------------------------------------------------------
INSERT INTO `sys_code_rule` (`rule_code`, `rule_name`, `prefix`, `date_format`, `seq_length`,
                             `seq_start`, `seq_step`, `current_value`, `seq_date`, `reset_by_date`,
                             `status`, `remark`, `is_deleted`, `create_time`, `update_time`)
SELECT 'STOCK_TAKE_PLAN', '库存盘点任务', 'PD', 'yyyyMMdd', 4, 1, 1, 0, '', 1, 1, '库存盘点任务编号', 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_code_rule` WHERE `rule_code` = 'STOCK_TAKE_PLAN');

INSERT INTO `sys_code_rule` (`rule_code`, `rule_name`, `prefix`, `date_format`, `seq_length`,
                             `seq_start`, `seq_step`, `current_value`, `seq_date`, `reset_by_date`,
                             `status`, `remark`, `is_deleted`, `create_time`, `update_time`)
SELECT 'STOCK_TAKE_PRE', '预先盘点单', 'YPD', 'yyyyMMdd', 4, 1, 1, 0, '', 1, 1, '预先盘点单编号', 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_code_rule` WHERE `rule_code` = 'STOCK_TAKE_PRE');

INSERT INTO `sys_code_rule` (`rule_code`, `rule_name`, `prefix`, `date_format`, `seq_length`,
                             `seq_start`, `seq_step`, `current_value`, `seq_date`, `reset_by_date`,
                             `status`, `remark`, `is_deleted`, `create_time`, `update_time`)
SELECT 'STOCK_COST_ADJUST', '库存成本调整单', 'CB', 'yyyyMMdd', 4, 1, 1, 0, '', 1, 1, '库存成本调整单编号', 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_code_rule` WHERE `rule_code` = 'STOCK_COST_ADJUST');

-- -----------------------------------------------------------------------------
-- 8. 菜单（挂在「库存管理」id=1 / M000200 下）
--    ⚠️ 只**新增**菜单，绝不改动任何既有行的 code/path/permission ——
--       动权限串要重授所有角色，且会撞"注解里的串必须已存在于 sys_menu.permission"的铁律。
--    ⚠️ 顺序：本脚本必须先于后端 JAR 执行，执行后清 erp:auth:* 缓存。
-- -----------------------------------------------------------------------------

-- 8.1 页面菜单（menu_type=1）
INSERT INTO `sys_menu` (`parent_id`, `code`, `name`, `title`, `menu_type`, `path`, `component`,
                        `permission`, `icon`, `sort`, `visible`, `status`, `remark`)
SELECT 1, 'M000207', 'stock-warning', '库存预警', 1, '/stock/warning', 'Business/Stock/Warning',
       'stock:warning:query', NULL, 7, 1, 1, '库存体系-库存预警'
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `code` = 'M000207');

INSERT INTO `sys_menu` (`parent_id`, `code`, `name`, `title`, `menu_type`, `path`, `component`,
                        `permission`, `icon`, `sort`, `visible`, `status`, `remark`)
SELECT 1, 'M000208', 'stock-cost-adjust', '库存成本调整', 1, '/stock/adjust/cost', 'Business/Stock/Adjust/Cost/List',
       'stock:adjust:cost:query', NULL, 8, 1, 1, '库存体系-库存成本调整'
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `code` = 'M000208');

INSERT INTO `sys_menu` (`parent_id`, `code`, `name`, `title`, `menu_type`, `path`, `component`,
                        `permission`, `icon`, `sort`, `visible`, `status`, `remark`)
SELECT 1, 'M000211', 'stock-take-config', '盘点参数设置', 1, '/stock/take/config', 'Business/Stock/Take/Config',
       'stock:take:config:query', NULL, 11, 1, 1, '库存体系-盘点参数设置'
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `code` = 'M000211');

-- 8.2 按钮权限（menu_type=2），B0003xx 段
INSERT INTO `sys_menu` (`parent_id`, `code`, `name`, `title`, `menu_type`, `path`, `component`,
                        `permission`, `icon`, `sort`, `visible`, `status`, `remark`)
SELECT (SELECT `id` FROM (SELECT `id` FROM `sys_menu` WHERE `code` = 'M000207' LIMIT 1) t),
       'B000301', '', '新增库存预警', 2, NULL, NULL, 'stock:warning:add', NULL, 1, 1, 1, NULL
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `code` = 'B000301');

INSERT INTO `sys_menu` (`parent_id`, `code`, `name`, `title`, `menu_type`, `path`, `component`,
                        `permission`, `icon`, `sort`, `visible`, `status`, `remark`)
SELECT (SELECT `id` FROM (SELECT `id` FROM `sys_menu` WHERE `code` = 'M000207' LIMIT 1) t),
       'B000302', '', '编辑库存预警', 2, NULL, NULL, 'stock:warning:modify', NULL, 2, 1, 1, NULL
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `code` = 'B000302');

INSERT INTO `sys_menu` (`parent_id`, `code`, `name`, `title`, `menu_type`, `path`, `component`,
                        `permission`, `icon`, `sort`, `visible`, `status`, `remark`)
SELECT (SELECT `id` FROM (SELECT `id` FROM `sys_menu` WHERE `code` = 'M000207' LIMIT 1) t),
       'B000303', '', '删除库存预警', 2, NULL, NULL, 'stock:warning:delete', NULL, 3, 1, 1, NULL
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `code` = 'B000303');

INSERT INTO `sys_menu` (`parent_id`, `code`, `name`, `title`, `menu_type`, `path`, `component`,
                        `permission`, `icon`, `sort`, `visible`, `status`, `remark`)
SELECT (SELECT `id` FROM (SELECT `id` FROM `sys_menu` WHERE `code` = 'M000208' LIMIT 1) t),
       'B000304', '', '新增成本调整单', 2, NULL, NULL, 'stock:adjust:cost:add', NULL, 1, 1, 1, NULL
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `code` = 'B000304');

INSERT INTO `sys_menu` (`parent_id`, `code`, `name`, `title`, `menu_type`, `path`, `component`,
                        `permission`, `icon`, `sort`, `visible`, `status`, `remark`)
SELECT (SELECT `id` FROM (SELECT `id` FROM `sys_menu` WHERE `code` = 'M000208' LIMIT 1) t),
       'B000305', '', '修改成本调整单', 2, NULL, NULL, 'stock:adjust:cost:modify', NULL, 2, 1, 1, NULL
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `code` = 'B000305');

INSERT INTO `sys_menu` (`parent_id`, `code`, `name`, `title`, `menu_type`, `path`, `component`,
                        `permission`, `icon`, `sort`, `visible`, `status`, `remark`)
SELECT (SELECT `id` FROM (SELECT `id` FROM `sys_menu` WHERE `code` = 'M000208' LIMIT 1) t),
       'B000306', '', '删除成本调整单', 2, NULL, NULL, 'stock:adjust:cost:delete', NULL, 3, 1, 1, NULL
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `code` = 'B000306');

INSERT INTO `sys_menu` (`parent_id`, `code`, `name`, `title`, `menu_type`, `path`, `component`,
                        `permission`, `icon`, `sort`, `visible`, `status`, `remark`)
SELECT (SELECT `id` FROM (SELECT `id` FROM `sys_menu` WHERE `code` = 'M000208' LIMIT 1) t),
       'B000307', '', '审核成本调整单', 2, NULL, NULL, 'stock:adjust:cost:approve', NULL, 4, 1, 1, NULL
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `code` = 'B000307');

INSERT INTO `sys_menu` (`parent_id`, `code`, `name`, `title`, `menu_type`, `path`, `component`,
                        `permission`, `icon`, `sort`, `visible`, `status`, `remark`)
SELECT (SELECT `id` FROM (SELECT `id` FROM `sys_menu` WHERE `code` = 'M000211' LIMIT 1) t),
       'B000308', '', '修改盘点参数', 2, NULL, NULL, 'stock:take:config:modify', NULL, 1, 1, 1, NULL
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `code` = 'B000308');

-- -----------------------------------------------------------------------------
-- 9. 授权：超管(1) + 仓库管理员(4)
--    注意祖先目录 id=1（库存管理）必须一并授权，否则侧边栏整棵子树不显示。
-- -----------------------------------------------------------------------------
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT r.`id`, m.`id`
FROM (SELECT 1 AS `id` UNION ALL SELECT 4) r
CROSS JOIN `sys_menu` m
WHERE m.`code` IN ('M000207', 'M000208', 'M000211',
                   'B000301', 'B000302', 'B000303', 'B000304', 'B000305', 'B000306', 'B000307', 'B000308')
  AND NOT EXISTS (SELECT 1 FROM `sys_role_menu` rm
                  WHERE rm.`role_id` = r.`id` AND rm.`menu_id` = m.`id`);

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT r.`id`, m.`id`
FROM (SELECT 1 AS `id` UNION ALL SELECT 4) r
CROSS JOIN `sys_menu` m
WHERE m.`id` = 1
  AND NOT EXISTS (SELECT 1 FROM `sys_role_menu` rm
                  WHERE rm.`role_id` = r.`id` AND rm.`menu_id` = m.`id`);

-- -----------------------------------------------------------------------------
-- 10. 核对（执行后请人工确认输出）
-- -----------------------------------------------------------------------------
SELECT '新表数量(应为8)'      AS `检查项`, COUNT(*) AS `值`
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = @db AND TABLE_NAME IN
      ('stock_warning', 'stock_cost_adjust_sheet', 'stock_cost_adjust_sheet_detail',
       'take_stock_config', 'take_stock_plan', 'take_stock_plan_detail',
       'pre_take_stock_sheet', 'pre_take_stock_sheet_detail')
UNION ALL SELECT 'stock_check 新增列(应为2)', COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'stock_check'
      AND COLUMN_NAME IN ('plan_id', 'pre_sheet_id')
UNION ALL SELECT '盘点参数种子行(应为1)', COUNT(*) FROM `take_stock_config` WHERE `id` = 1
UNION ALL SELECT '编号规则(应为3)', COUNT(*)
    FROM `sys_code_rule`
    WHERE `rule_code` IN ('STOCK_TAKE_PLAN', 'STOCK_TAKE_PRE', 'STOCK_COST_ADJUST')
UNION ALL SELECT '新菜单数(应为11)', COUNT(*)
    FROM `sys_menu`
    WHERE `code` IN ('M000207', 'M000208', 'M000211',
                     'B000301', 'B000302', 'B000303', 'B000304', 'B000305', 'B000306', 'B000307', 'B000308');

SELECT m.`id`, m.`parent_id`, m.`code`, m.`title`, m.`menu_type`, m.`path`, m.`permission`
FROM `sys_menu` m
WHERE m.`code` IN ('M000207', 'M000208', 'M000211',
                   'B000301', 'B000302', 'B000303', 'B000304', 'B000305', 'B000306', 'B000307', 'B000308')
ORDER BY m.`code`;

SELECT r.`id` AS `role_id`, r.`code`, COUNT(rm.`id`) AS `已授权菜单数`
FROM `sys_role` r
LEFT JOIN `sys_role_menu` rm ON rm.`role_id` = r.`id`
GROUP BY r.`id`, r.`code`
ORDER BY r.`id`;

-- ⚠️ 执行完必须清一次 Sa-Token 权限缓存（或重启后端），否则新权限串最长 30 分钟才生效：
--    python .workbuddy/backup/clear_auth_cache.py

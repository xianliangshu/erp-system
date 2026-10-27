-- ============================================================================
-- 单据编号规则统一化  ——  建表 + 规则登记 + 存量流水初始化
--
-- 背景：全项目原有 28 个 ServiceImpl 各自实现一份编号生成逻辑（方法名有 20+ 种），
--       且 21 个 Mapper 各写一条几乎相同的 @Select("SELECT MAX(code) ... ")。
--       三个缺陷：① 大量重复；② 「先查 MAX 再 +1 再插入」并发必然重号；
--       ③ 前缀/位数硬编码在 Java 里，改规则要改代码重新发版。
--
-- 本脚本建立 sys_code_rule 统一规则表，配合 ICodeGenerateService
-- （SELECT ... FOR UPDATE 行锁串行化）解决上述问题。
--
-- 执行：
--   mysql -h<host> -uroot -p --default-character-set=utf8mb4 < sql/fix_code_rule.sql
--
-- ⚠️ 幂等：可重复执行。初始化只在 current_value = 0 时发生，不会把已发出的流水号倒回去。
-- ⚠️ 本脚本独立，不依赖其他脚本。
-- ============================================================================

USE `erp-data`;

CREATE TABLE IF NOT EXISTS `sys_code_rule` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `rule_code`     VARCHAR(64)  NOT NULL COMMENT '规则编码(业务唯一标识,如 PURCHASE_ORDER)',
  `rule_name`     VARCHAR(100) NOT NULL COMMENT '单据/对象名称',
  `prefix`        VARCHAR(20)  NOT NULL DEFAULT '' COMMENT '编号前缀',
  `date_format`   VARCHAR(20)  NOT NULL DEFAULT '' COMMENT '日期段格式(yyyyMMdd),空=编号不含日期段',
  `seq_length`    INT          NOT NULL DEFAULT 4 COMMENT '流水号位数',
  `seq_start`     INT          NOT NULL DEFAULT 1 COMMENT '起始流水值',
  `seq_step`      INT          NOT NULL DEFAULT 1 COMMENT '步长',
  `current_value` INT          NOT NULL DEFAULT 0 COMMENT '当前流水值(0=未初始化)',
  `seq_date`      VARCHAR(20)  NOT NULL DEFAULT '' COMMENT '流水号所属日期(yyyyMMdd,日切判定)。注意: 不可命名为 current_date, 它是 MySQL 保留字',
  `reset_by_date` TINYINT      NOT NULL DEFAULT 1 COMMENT '1=按日重置 0=不重置(主数据编码用0)',
  `status`        TINYINT      NOT NULL DEFAULT 1 COMMENT '1=启用 0=停用',
  `remark`        VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `is_deleted`    TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除(与 erp-system 模块一致)',
  `create_by`     VARCHAR(50)  DEFAULT NULL,
  `create_time`   DATETIME     DEFAULT NULL,
  `update_by`     VARCHAR(50)  DEFAULT NULL,
  `update_time`   DATETIME     DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_rule_code` (`rule_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='单据编号规则表';

-- ---------------------------------------------------------------------------
-- 规则登记：前缀 / 日期格式 / 位数与原实现逐一对应，保证编号格式零变化
-- ---------------------------------------------------------------------------
INSERT INTO `sys_code_rule`
  (`rule_code`,`rule_name`,`prefix`,`date_format`,`seq_length`,`seq_start`,`seq_step`,`reset_by_date`,`status`,`remark`)
VALUES
  ('PURCHASE_ORDER','采购订单','PO','yyyyMMdd',4,1,1,1,1,'原 PurchaseOrderServiceImpl'),
  ('PURCHASE_RECEIPT','采购收货单','PR','yyyyMMdd',4,1,1,1,1,'原 PurchaseReceiptServiceImpl'),
  ('PURCHASE_RETURN','采购退货单','PTH','yyyyMMdd',4,1,1,1,1,'原 PurchaseReturnServiceImpl'),
  ('SALE_ORDER','销售订单','SO','yyyyMMdd',4,1,1,1,1,'原 SaleOrderServiceImpl'),
  ('SALE_DELIVERY','销售出库单','SD','yyyyMMdd',4,1,1,1,1,'原 SaleDeliveryServiceImpl'),
  ('SALE_RETURN','销售退货单','SR','yyyyMMdd',4,1,1,1,1,'原 SaleReturnServiceImpl'),
  ('RETAIL_OUT','零售出库单','LS','yyyyMMdd',4,1,1,1,1,'原 RetailOutSheetServiceImpl'),
  ('RETAIL_RETURN','零售退货单','LT','yyyyMMdd',4,1,1,1,1,'原 RetailReturnServiceImpl'),
  ('STOCK_CHECK','库存盘点单','SC','yyyyMMdd',4,1,1,1,1,'原 StockCheckServiceImpl'),
  ('STOCK_TRANSFER','库存调拨单','ST','yyyyMMdd',4,1,1,1,1,'原 StockTransferServiceImpl'),
  ('STOCK_ADJUST','库存调整单','SA','yyyyMMdd',4,1,1,1,1,'原 StockAdjustSheetServiceImpl'),
  ('SETTLE_SHEET','供应商结算单','JS','yyyyMMdd',4,1,1,1,1,'原 SettleSheetServiceImpl'),
  ('SETTLE_CHECK','供应商对账单','DZ','yyyyMMdd',4,1,1,1,1,'原 SettleCheckSheetServiceImpl'),
  ('SETTLE_FEE','供应商费用单','FY','yyyyMMdd',4,1,1,1,1,'原 SettleFeeSheetServiceImpl'),
  ('SETTLE_PRE','供应商预收预付单','YF','yyyyMMdd',4,1,1,1,1,'原 SettlePreSheetServiceImpl'),
  ('CUSTOMER_SETTLE','客户结算单','KJS','yyyyMMdd',4,1,1,1,1,'原 CustomerSettleSheetServiceImpl'),
  ('CUSTOMER_CHECK','客户对账单','KDZ','yyyyMMdd',4,1,1,1,1,'原 CustomerCheckSheetServiceImpl'),
  ('CUSTOMER_FEE_IN','客户费用单(收入)','KSK','yyyyMMdd',4,1,1,1,1,'原 CustomerFeeSheetServiceImpl sheetType=1'),
  ('CUSTOMER_FEE_OUT','客户费用单(支出)','KKK','yyyyMMdd',4,1,1,1,1,'原 CustomerFeeSheetServiceImpl sheetType!=1'),
  ('CUSTOMER_PRE','客户预收预付单','KYS','yyyyMMdd',4,1,1,1,1,'原 CustomerPreSheetServiceImpl'),
  ('SETTLE_IN_OUT_ITEM','收支项目','SZ','',4,1,1,0,1,'原 SettleInOutItemServiceImpl 无日期段'),
  ('STOCK_ADJUST_REASON','库存调整原因','','',3,1,1,0,1,'原 StockAdjustReasonServiceImpl 3位纯流水'),
  ('BASE_MATERIAL','物料','MAT','',6,1,1,0,1,'原 BaseMaterialServiceImpl'),
  ('BASE_CUSTOMER','客户','CUS','',6,1,1,0,1,'原 BaseCustomerServiceImpl'),
  ('BASE_SUPPLIER','供应商','SUP','',6,1,1,0,1,'原 BaseSupplierServiceImpl'),
  ('BASE_WAREHOUSE','仓库','WH','',6,1,1,0,1,'原 BaseWarehouseServiceImpl'),
  ('BASE_BRAND','品牌','BRD','',6,1,1,0,1,'原 BaseBrandServiceImpl'),
  ('BASE_UNIT','计量单位','UNIT','',6,1,1,0,1,'原 BaseUnitServiceImpl'),
  ('BASE_MATERIAL_CATEGORY','物料分类','MC','',6,1,1,0,1,'原 BaseMaterialCategoryServiceImpl')
ON DUPLICATE KEY UPDATE
  `rule_name`=VALUES(`rule_name`), `prefix`=VALUES(`prefix`), `date_format`=VALUES(`date_format`),
  `seq_length`=VALUES(`seq_length`), `reset_by_date`=VALUES(`reset_by_date`), `remark`=VALUES(`remark`);

-- ---------------------------------------------------------------------------
-- 存量初始化：仅在 current_value = 0（未初始化）时执行
--   current_value ← 该前缀下已有单据的最大流水。取后 seq_length 位再剔除非数字，
--                   以兼容 WH001 这类历史手工插入的不规范编号。
--   seq_date  ← 该前缀下最大编号的日期段。这样今天首次生成时：
--                   日期不同 → 从 1 重新开始（不跳号）；日期相同 → 继续递增（不撞号）。
-- ---------------------------------------------------------------------------
UPDATE `sys_code_rule` SET `current_value`=COALESCE((SELECT MAX(CAST(REGEXP_REPLACE(RIGHT(`code`,4),'[^0-9]','') AS UNSIGNED)) FROM `purchase_order` WHERE `code` LIKE 'PO%'),0), `seq_date`=COALESCE((SELECT SUBSTRING(`code`,3,8) FROM `purchase_order` WHERE `code` LIKE 'PO%' ORDER BY `code` DESC LIMIT 1),'') WHERE `rule_code`='PURCHASE_ORDER' AND `current_value`=0;
UPDATE `sys_code_rule` SET `current_value`=COALESCE((SELECT MAX(CAST(REGEXP_REPLACE(RIGHT(`code`,4),'[^0-9]','') AS UNSIGNED)) FROM `purchase_receipt` WHERE `code` LIKE 'PR%'),0), `seq_date`=COALESCE((SELECT SUBSTRING(`code`,3,8) FROM `purchase_receipt` WHERE `code` LIKE 'PR%' ORDER BY `code` DESC LIMIT 1),'') WHERE `rule_code`='PURCHASE_RECEIPT' AND `current_value`=0;
UPDATE `sys_code_rule` SET `current_value`=COALESCE((SELECT MAX(CAST(REGEXP_REPLACE(RIGHT(`code`,4),'[^0-9]','') AS UNSIGNED)) FROM `purchase_return` WHERE `code` LIKE 'PTH%'),0), `seq_date`=COALESCE((SELECT SUBSTRING(`code`,4,8) FROM `purchase_return` WHERE `code` LIKE 'PTH%' ORDER BY `code` DESC LIMIT 1),'') WHERE `rule_code`='PURCHASE_RETURN' AND `current_value`=0;
UPDATE `sys_code_rule` SET `current_value`=COALESCE((SELECT MAX(CAST(REGEXP_REPLACE(RIGHT(`code`,4),'[^0-9]','') AS UNSIGNED)) FROM `sale_order` WHERE `code` LIKE 'SO%'),0), `seq_date`=COALESCE((SELECT SUBSTRING(`code`,3,8) FROM `sale_order` WHERE `code` LIKE 'SO%' ORDER BY `code` DESC LIMIT 1),'') WHERE `rule_code`='SALE_ORDER' AND `current_value`=0;
UPDATE `sys_code_rule` SET `current_value`=COALESCE((SELECT MAX(CAST(REGEXP_REPLACE(RIGHT(`code`,4),'[^0-9]','') AS UNSIGNED)) FROM `sale_delivery` WHERE `code` LIKE 'SD%'),0), `seq_date`=COALESCE((SELECT SUBSTRING(`code`,3,8) FROM `sale_delivery` WHERE `code` LIKE 'SD%' ORDER BY `code` DESC LIMIT 1),'') WHERE `rule_code`='SALE_DELIVERY' AND `current_value`=0;
UPDATE `sys_code_rule` SET `current_value`=COALESCE((SELECT MAX(CAST(REGEXP_REPLACE(RIGHT(`code`,4),'[^0-9]','') AS UNSIGNED)) FROM `sale_return` WHERE `code` LIKE 'SR%'),0), `seq_date`=COALESCE((SELECT SUBSTRING(`code`,3,8) FROM `sale_return` WHERE `code` LIKE 'SR%' ORDER BY `code` DESC LIMIT 1),'') WHERE `rule_code`='SALE_RETURN' AND `current_value`=0;
UPDATE `sys_code_rule` SET `current_value`=COALESCE((SELECT MAX(CAST(REGEXP_REPLACE(RIGHT(`code`,4),'[^0-9]','') AS UNSIGNED)) FROM `retail_out_sheet` WHERE `code` LIKE 'LS%'),0), `seq_date`=COALESCE((SELECT SUBSTRING(`code`,3,8) FROM `retail_out_sheet` WHERE `code` LIKE 'LS%' ORDER BY `code` DESC LIMIT 1),'') WHERE `rule_code`='RETAIL_OUT' AND `current_value`=0;
UPDATE `sys_code_rule` SET `current_value`=COALESCE((SELECT MAX(CAST(REGEXP_REPLACE(RIGHT(`code`,4),'[^0-9]','') AS UNSIGNED)) FROM `retail_return` WHERE `code` LIKE 'LT%'),0), `seq_date`=COALESCE((SELECT SUBSTRING(`code`,3,8) FROM `retail_return` WHERE `code` LIKE 'LT%' ORDER BY `code` DESC LIMIT 1),'') WHERE `rule_code`='RETAIL_RETURN' AND `current_value`=0;
UPDATE `sys_code_rule` SET `current_value`=COALESCE((SELECT MAX(CAST(REGEXP_REPLACE(RIGHT(`code`,4),'[^0-9]','') AS UNSIGNED)) FROM `stock_check` WHERE `code` LIKE 'SC%'),0), `seq_date`=COALESCE((SELECT SUBSTRING(`code`,3,8) FROM `stock_check` WHERE `code` LIKE 'SC%' ORDER BY `code` DESC LIMIT 1),'') WHERE `rule_code`='STOCK_CHECK' AND `current_value`=0;
UPDATE `sys_code_rule` SET `current_value`=COALESCE((SELECT MAX(CAST(REGEXP_REPLACE(RIGHT(`code`,4),'[^0-9]','') AS UNSIGNED)) FROM `stock_transfer` WHERE `code` LIKE 'ST%'),0), `seq_date`=COALESCE((SELECT SUBSTRING(`code`,3,8) FROM `stock_transfer` WHERE `code` LIKE 'ST%' ORDER BY `code` DESC LIMIT 1),'') WHERE `rule_code`='STOCK_TRANSFER' AND `current_value`=0;
UPDATE `sys_code_rule` SET `current_value`=COALESCE((SELECT MAX(CAST(REGEXP_REPLACE(RIGHT(`code`,4),'[^0-9]','') AS UNSIGNED)) FROM `stock_adjust_sheet` WHERE `code` LIKE 'SA%'),0), `seq_date`=COALESCE((SELECT SUBSTRING(`code`,3,8) FROM `stock_adjust_sheet` WHERE `code` LIKE 'SA%' ORDER BY `code` DESC LIMIT 1),'') WHERE `rule_code`='STOCK_ADJUST' AND `current_value`=0;
UPDATE `sys_code_rule` SET `current_value`=COALESCE((SELECT MAX(CAST(REGEXP_REPLACE(RIGHT(`code`,4),'[^0-9]','') AS UNSIGNED)) FROM `settle_sheet` WHERE `code` LIKE 'JS%'),0), `seq_date`=COALESCE((SELECT SUBSTRING(`code`,3,8) FROM `settle_sheet` WHERE `code` LIKE 'JS%' ORDER BY `code` DESC LIMIT 1),'') WHERE `rule_code`='SETTLE_SHEET' AND `current_value`=0;
UPDATE `sys_code_rule` SET `current_value`=COALESCE((SELECT MAX(CAST(REGEXP_REPLACE(RIGHT(`code`,4),'[^0-9]','') AS UNSIGNED)) FROM `settle_check_sheet` WHERE `code` LIKE 'DZ%'),0), `seq_date`=COALESCE((SELECT SUBSTRING(`code`,3,8) FROM `settle_check_sheet` WHERE `code` LIKE 'DZ%' ORDER BY `code` DESC LIMIT 1),'') WHERE `rule_code`='SETTLE_CHECK' AND `current_value`=0;
UPDATE `sys_code_rule` SET `current_value`=COALESCE((SELECT MAX(CAST(REGEXP_REPLACE(RIGHT(`code`,4),'[^0-9]','') AS UNSIGNED)) FROM `settle_fee_sheet` WHERE `code` LIKE 'FY%'),0), `seq_date`=COALESCE((SELECT SUBSTRING(`code`,3,8) FROM `settle_fee_sheet` WHERE `code` LIKE 'FY%' ORDER BY `code` DESC LIMIT 1),'') WHERE `rule_code`='SETTLE_FEE' AND `current_value`=0;
UPDATE `sys_code_rule` SET `current_value`=COALESCE((SELECT MAX(CAST(REGEXP_REPLACE(RIGHT(`code`,4),'[^0-9]','') AS UNSIGNED)) FROM `settle_pre_sheet` WHERE `code` LIKE 'YF%'),0), `seq_date`=COALESCE((SELECT SUBSTRING(`code`,3,8) FROM `settle_pre_sheet` WHERE `code` LIKE 'YF%' ORDER BY `code` DESC LIMIT 1),'') WHERE `rule_code`='SETTLE_PRE' AND `current_value`=0;
UPDATE `sys_code_rule` SET `current_value`=COALESCE((SELECT MAX(CAST(REGEXP_REPLACE(RIGHT(`code`,4),'[^0-9]','') AS UNSIGNED)) FROM `customer_settle_sheet` WHERE `code` LIKE 'KJS%'),0), `seq_date`=COALESCE((SELECT SUBSTRING(`code`,4,8) FROM `customer_settle_sheet` WHERE `code` LIKE 'KJS%' ORDER BY `code` DESC LIMIT 1),'') WHERE `rule_code`='CUSTOMER_SETTLE' AND `current_value`=0;
UPDATE `sys_code_rule` SET `current_value`=COALESCE((SELECT MAX(CAST(REGEXP_REPLACE(RIGHT(`code`,4),'[^0-9]','') AS UNSIGNED)) FROM `customer_check_sheet` WHERE `code` LIKE 'KDZ%'),0), `seq_date`=COALESCE((SELECT SUBSTRING(`code`,4,8) FROM `customer_check_sheet` WHERE `code` LIKE 'KDZ%' ORDER BY `code` DESC LIMIT 1),'') WHERE `rule_code`='CUSTOMER_CHECK' AND `current_value`=0;
UPDATE `sys_code_rule` SET `current_value`=COALESCE((SELECT MAX(CAST(REGEXP_REPLACE(RIGHT(`code`,4),'[^0-9]','') AS UNSIGNED)) FROM `customer_fee_sheet` WHERE `code` LIKE 'KSK%'),0), `seq_date`=COALESCE((SELECT SUBSTRING(`code`,4,8) FROM `customer_fee_sheet` WHERE `code` LIKE 'KSK%' ORDER BY `code` DESC LIMIT 1),'') WHERE `rule_code`='CUSTOMER_FEE_IN' AND `current_value`=0;
UPDATE `sys_code_rule` SET `current_value`=COALESCE((SELECT MAX(CAST(REGEXP_REPLACE(RIGHT(`code`,4),'[^0-9]','') AS UNSIGNED)) FROM `customer_fee_sheet` WHERE `code` LIKE 'KKK%'),0), `seq_date`=COALESCE((SELECT SUBSTRING(`code`,4,8) FROM `customer_fee_sheet` WHERE `code` LIKE 'KKK%' ORDER BY `code` DESC LIMIT 1),'') WHERE `rule_code`='CUSTOMER_FEE_OUT' AND `current_value`=0;
UPDATE `sys_code_rule` SET `current_value`=COALESCE((SELECT MAX(CAST(REGEXP_REPLACE(RIGHT(`code`,4),'[^0-9]','') AS UNSIGNED)) FROM `customer_pre_sheet` WHERE `code` LIKE 'KYS%'),0), `seq_date`=COALESCE((SELECT SUBSTRING(`code`,4,8) FROM `customer_pre_sheet` WHERE `code` LIKE 'KYS%' ORDER BY `code` DESC LIMIT 1),'') WHERE `rule_code`='CUSTOMER_PRE' AND `current_value`=0;
UPDATE `sys_code_rule` SET `current_value`=COALESCE((SELECT MAX(CAST(REGEXP_REPLACE(RIGHT(`code`,4),'[^0-9]','') AS UNSIGNED)) FROM `settle_in_out_item` WHERE `code` LIKE 'SZ%'),0), `seq_date`='' WHERE `rule_code`='SETTLE_IN_OUT_ITEM' AND `current_value`=0;
UPDATE `sys_code_rule` SET `current_value`=COALESCE((SELECT MAX(CAST(REGEXP_REPLACE(RIGHT(`code`,3),'[^0-9]','') AS UNSIGNED)) FROM `stock_adjust_reason` WHERE 1=1),0), `seq_date`='' WHERE `rule_code`='STOCK_ADJUST_REASON' AND `current_value`=0;
UPDATE `sys_code_rule` SET `current_value`=COALESCE((SELECT MAX(CAST(REGEXP_REPLACE(RIGHT(`code`,6),'[^0-9]','') AS UNSIGNED)) FROM `base_material` WHERE `code` LIKE 'MAT%'),0), `seq_date`='' WHERE `rule_code`='BASE_MATERIAL' AND `current_value`=0;
UPDATE `sys_code_rule` SET `current_value`=COALESCE((SELECT MAX(CAST(REGEXP_REPLACE(RIGHT(`code`,6),'[^0-9]','') AS UNSIGNED)) FROM `base_customer` WHERE `code` LIKE 'CUS%'),0), `seq_date`='' WHERE `rule_code`='BASE_CUSTOMER' AND `current_value`=0;
UPDATE `sys_code_rule` SET `current_value`=COALESCE((SELECT MAX(CAST(REGEXP_REPLACE(RIGHT(`code`,6),'[^0-9]','') AS UNSIGNED)) FROM `base_supplier` WHERE `code` LIKE 'SUP%'),0), `seq_date`='' WHERE `rule_code`='BASE_SUPPLIER' AND `current_value`=0;
UPDATE `sys_code_rule` SET `current_value`=COALESCE((SELECT MAX(CAST(REGEXP_REPLACE(RIGHT(`code`,6),'[^0-9]','') AS UNSIGNED)) FROM `base_warehouse` WHERE `code` LIKE 'WH%'),0), `seq_date`='' WHERE `rule_code`='BASE_WAREHOUSE' AND `current_value`=0;
UPDATE `sys_code_rule` SET `current_value`=COALESCE((SELECT MAX(CAST(REGEXP_REPLACE(RIGHT(`code`,6),'[^0-9]','') AS UNSIGNED)) FROM `base_brand` WHERE `code` LIKE 'BRD%'),0), `seq_date`='' WHERE `rule_code`='BASE_BRAND' AND `current_value`=0;
UPDATE `sys_code_rule` SET `current_value`=COALESCE((SELECT MAX(CAST(REGEXP_REPLACE(RIGHT(`code`,6),'[^0-9]','') AS UNSIGNED)) FROM `base_unit` WHERE `code` LIKE 'UNIT%'),0), `seq_date`='' WHERE `rule_code`='BASE_UNIT' AND `current_value`=0;
UPDATE `sys_code_rule` SET `current_value`=COALESCE((SELECT MAX(CAST(REGEXP_REPLACE(RIGHT(`code`,6),'[^0-9]','') AS UNSIGNED)) FROM `base_material_category` WHERE `code` LIKE 'MC%'),0), `seq_date`='' WHERE `rule_code`='BASE_MATERIAL_CATEGORY' AND `current_value`=0;

-- ---------------------------------------------------------------------------
-- 校验：应输出 29 条规则，current_value 与各表现有最大流水一致
-- ---------------------------------------------------------------------------
SELECT `rule_code`,`rule_name`,`prefix`,`date_format`,`seq_length`,`current_value`,`seq_date`,`reset_by_date`
FROM `sys_code_rule` WHERE `is_deleted`=0 ORDER BY `id`;

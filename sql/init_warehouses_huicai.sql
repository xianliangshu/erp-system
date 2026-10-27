-- =============================================================================
-- 慧采云仓仓库初始化（6 个实体仓：广东 5 + 山西 1）
-- -----------------------------------------------------------------------------
-- 执行方式（必须指定库，脚本内不写 USE）：
--   mysql -h192.168.31.199 -uroot -p123456 --default-character-set=utf8mb4 \
--         erp-data < sql/init_warehouses_huicai.sql
--
-- 特性：**全量幂等**，可重复执行（写入带 NOT EXISTS 守卫）。
--
-- 编号：沿用 BASE_WAREHOUSE 规则（前缀 WH，6 位流水，不按日重置），
--       从当前最大值 4 之后续编 ⇒ WH000005 ~ WH000010。
--
-- 字段口径：
--   · name     —— 保留用户提供的原始名称（招牌/营业执照口径，未做规范化改写）
--   · address  —— 完全按提供的原文写入，未补字/未改写
--   · contact / phone —— 未提供，一律留空，**不编造**
--   · is_default —— 全部 0（默认仓仍为 WH001 主仓库）
--   · remark   —— 记录「省份 / 城市」，便于列表页按区域筛选识别
-- =============================================================================

SET NAMES utf8mb4;

-- -----------------------------------------------------------------------------
-- 广东 · 深圳（龙华）
-- -----------------------------------------------------------------------------
INSERT INTO `base_warehouse` (`code`, `name`, `contact`, `phone`, `address`, `is_default`, `status`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`)
SELECT 'WH000005', '慧采云仓（龙华区）', NULL, NULL, '深圳龙华区清湖综合楼', 0, 1, '广东省深圳市 | 慧采云仓体系区域仓', 'system', NOW(), 'system', NOW(), 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_warehouse) x WHERE x.`code` = 'WH000005');

-- -----------------------------------------------------------------------------
-- 广东 · 广州（花都）
-- -----------------------------------------------------------------------------
INSERT INTO `base_warehouse` (`code`, `name`, `contact`, `phone`, `address`, `is_default`, `status`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`)
SELECT 'WH000006', '慧采云仓（花都区）', NULL, NULL, '广州市花都区花东镇金谷北路30号', 0, 1, '广东省广州市 | 慧采云仓体系区域仓', 'system', NOW(), 'system', NOW(), 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_warehouse) x WHERE x.`code` = 'WH000006');

-- -----------------------------------------------------------------------------
-- 广东 · 佛山（南海）
-- -----------------------------------------------------------------------------
INSERT INTO `base_warehouse` (`code`, `name`, `contact`, `phone`, `address`, `is_default`, `status`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`)
SELECT 'WH000007', '佛山市南海区广通慧采云仓轮胎经营部', NULL, NULL, '佛山市南海区狮山镇罗村联星旺南工业区旺达路40号', 0, 1, '广东省佛山市 | 慧采云仓体系区域仓', 'system', NOW(), 'system', NOW(), 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_warehouse) x WHERE x.`code` = 'WH000007');

-- -----------------------------------------------------------------------------
-- 广东 · 韶关（浈江）
-- -----------------------------------------------------------------------------
INSERT INTO `base_warehouse` (`code`, `name`, `contact`, `phone`, `address`, `is_default`, `status`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`)
SELECT 'WH000008', '浈江区慧采云仓轮胎销售经营部', NULL, NULL, '韶关市浈江区南郊五公里韶关浪奇有限公司（成品仓）101室', 0, 1, '广东省韶关市 | 慧采云仓体系区域仓', 'system', NOW(), 'system', NOW(), 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_warehouse) x WHERE x.`code` = 'WH000008');

-- -----------------------------------------------------------------------------
-- 广东 · 揭阳（普宁）
-- -----------------------------------------------------------------------------
INSERT INTO `base_warehouse` (`code`, `name`, `contact`, `phone`, `address`, `is_default`, `status`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`)
SELECT 'WH000009', '普宁市流沙慧采云仓仓储服务部', NULL, NULL, '普宁市流沙南街道泗竹埔村新乡片康美大道东侧第三街东起第1间首层', 0, 1, '广东省揭阳市（普宁） | 慧采云仓体系区域仓', 'system', NOW(), 'system', NOW(), 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_warehouse) x WHERE x.`code` = 'WH000009');

-- -----------------------------------------------------------------------------
-- 山西 · 太原（迎泽）
-- -----------------------------------------------------------------------------
INSERT INTO `base_warehouse` (`code`, `name`, `contact`, `phone`, `address`, `is_default`, `status`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`)
SELECT 'WH000010', '慧采云仓（迎泽区）', NULL, NULL, '山西省太原市迎泽区郝庄镇东太堡5号', 0, 1, '山西省太原市 | 慧采云仓体系区域仓', 'system', NOW(), 'system', NOW(), 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_warehouse) x WHERE x.`code` = 'WH000010');

-- -----------------------------------------------------------------------------
-- 编号流水校正（取「含已逻辑删除记录」的全表最大值，防止后续新增撞号）
-- -----------------------------------------------------------------------------
UPDATE `sys_code_rule`
SET `current_value` = (SELECT COALESCE(MAX(CAST(REGEXP_REPLACE(RIGHT(`code`, 6), '[^0-9]', '') AS UNSIGNED)), 0) FROM `base_warehouse` WHERE `code` LIKE 'WH%')
WHERE `rule_code` = 'BASE_WAREHOUSE';

-- -----------------------------------------------------------------------------
-- 执行结果核对
-- -----------------------------------------------------------------------------
SELECT `code` AS `仓库编号`, `name` AS `仓库名称`, `address` AS `地址`,
       CASE `is_default` WHEN 1 THEN '是' ELSE '否' END AS `默认仓`,
       CASE `status` WHEN 1 THEN '启用' ELSE '禁用' END AS `状态`, `remark` AS `备注`
FROM `base_warehouse`
WHERE `deleted` = 0
ORDER BY `code`;

SELECT '慧采云仓已入库' AS `检查项`, COUNT(*) AS `值`
FROM `base_warehouse`
WHERE `deleted` = 0 AND `code` BETWEEN 'WH000005' AND 'WH000010'
UNION ALL SELECT '仓库总数', COUNT(*) FROM `base_warehouse` WHERE `deleted` = 0;

SELECT `rule_code`, `prefix`, `current_value` FROM `sys_code_rule` WHERE `rule_code` = 'BASE_WAREHOUSE';

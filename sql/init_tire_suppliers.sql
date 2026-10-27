-- =============================================================================
-- 轮胎业务供应商初始化数据（12 家）
-- -----------------------------------------------------------------------------
-- 执行方式（必须指定库，脚本内不写 USE）：
--   mysql -h192.168.31.199 -uroot -p123456 --default-character-set=utf8mb4 \
--         erp-data < sql/init_tire_suppliers.sql
--
-- 特性：**全量幂等**，可重复执行（所有写入带 NOT EXISTS 守卫）。
--
-- 供应商分层（按轮胎经销真实渠道结构）：
--   一级 品牌厂家直供（7 家）—— 各品牌在中国的销售法人主体
--   二级 区域代理 / 批发平台（3 家）—— 中小商家实际进货口
--   配套 设备与服务商（2 家）—— 物流、拆装设备
--
-- ⚠️ 数据口径声明：
--   · 公司主体名称、所在地为公开可核实信息；
--   · **联系人/电话/邮箱为占位示例**（统一用「区号-8888xxxx」与品牌域名邮箱），
--     上线前请替换为真实商务联系人；
--   · 开户行 / 银行账号 / 税号一律留空，未编造；
--   · 结算方式与账期按行业惯例设定（品牌厂家账期长、批发平台现款短账期）。
-- =============================================================================

SET NAMES utf8mb4;

-- -----------------------------------------------------------------------------
-- 一级：品牌厂家直供（type=1 商品供应，结算方式=4 账期）
-- -----------------------------------------------------------------------------
INSERT INTO `base_supplier` (`code`, `name`, `short_name`, `type`, `credit_level`, `contact_person`, `contact_phone`, `contact_email`, `address`, `settlement_method`, `payment_days`, `status`, `sort`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT 'SUP000004', '大陆马牌轮胎（中国）有限公司', '马牌中国', 1, 1, '渠道销售部 李经理', '021-8888 0101', 'channel@continental-corp.com', '上海市静安区（中国区总部）', 4, 45, 1, 4,
       '渠道层级:厂家直供 | 供应品牌:德国马牌 Continental | 政策:年度返利+季度促销 | 主要工厂:合肥', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_supplier) x WHERE x.`code` = 'SUP000004');

INSERT INTO `base_supplier` (`code`, `name`, `short_name`, `type`, `credit_level`, `contact_person`, `contact_phone`, `contact_email`, `address`, `settlement_method`, `payment_days`, `status`, `sort`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT 'SUP000005', '普利司通（中国）投资有限公司', '普利司通中国', 1, 1, '替换市场部 王经理', '021-8888 0102', 'sales@bridgestone-corp.com', '上海市黄浦区（中国区总部）', 4, 45, 1, 5,
       '渠道层级:厂家直供 | 供应品牌:普利司通 Bridgestone | 政策:年度返利+店招支持 | 工厂:沈阳/天津/无锡', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_supplier) x WHERE x.`code` = 'SUP000005');

INSERT INTO `base_supplier` (`code`, `name`, `short_name`, `type`, `credit_level`, `contact_person`, `contact_phone`, `contact_email`, `address`, `settlement_method`, `payment_days`, `status`, `sort`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT 'SUP000006', '优科豪马橡胶（中国）有限公司', '优科豪马中国', 1, 1, '销售部 陈经理', '021-8888 0103', 'sales@yokohama-corp.com', '上海市长宁区（中国区总部）', 4, 45, 1, 6,
       '渠道层级:厂家直供 | 供应品牌:优科豪马 Yokohama | 政策:年度返利+广告支持 | 工厂:苏州/杭州', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_supplier) x WHERE x.`code` = 'SUP000006');

INSERT INTO `base_supplier` (`code`, `name`, `short_name`, `type`, `credit_level`, `contact_person`, `contact_phone`, `contact_email`, `address`, `settlement_method`, `payment_days`, `status`, `sort`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT 'SUP000007', '韩泰轮胎（中国）有限公司', '韩泰中国', 1, 1, '替换市场部 刘经理', '021-8888 0104', 'sales@hankook-corp.com', '上海市闵行区（中国区总部）', 4, 60, 1, 7,
       '渠道层级:厂家直供 | 供应品牌:韩泰 Hankook | 政策:年度返利+季度促销 | 工厂:嘉兴/重庆', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_supplier) x WHERE x.`code` = 'SUP000007');

INSERT INTO `base_supplier` (`code`, `name`, `short_name`, `type`, `credit_level`, `contact_person`, `contact_phone`, `contact_email`, `address`, `settlement_method`, `payment_days`, `status`, `sort`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT 'SUP000008', '固铂轮胎（中国）投资有限公司', '固铂中国', 1, 2, '渠道部 赵经理', '021-8888 0105', 'sales@cooper-corp.com', '上海市（中国区总部）', 4, 45, 1, 8,
       '渠道层级:厂家直供 | 供应品牌:美国固铂 Cooper | 说明:固铂已并入固特异集团，走固特异渠道体系', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_supplier) x WHERE x.`code` = 'SUP000008');

INSERT INTO `base_supplier` (`code`, `name`, `short_name`, `type`, `credit_level`, `contact_person`, `contact_phone`, `contact_email`, `address`, `settlement_method`, `payment_days`, `status`, `sort`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT 'SUP000009', '赛轮集团股份有限公司', '赛轮集团', 1, 1, '国内销售部 孙经理', '0532-8888 0106', 'sales@sailungroup.com', '山东省青岛市（集团总部）', 4, 60, 1, 9,
       '渠道层级:厂家直供 | 供应品牌:赛轮 SAILUN / 路极 ROADX | A股上市(601058) | 工厂:青岛/东营/沈阳', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_supplier) x WHERE x.`code` = 'SUP000009');

INSERT INTO `base_supplier` (`code`, `name`, `short_name`, `type`, `credit_level`, `contact_person`, `contact_phone`, `contact_email`, `address`, `settlement_method`, `payment_days`, `status`, `sort`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT 'SUP000010', '青岛胜驰轮胎有限公司', '青岛胜驰', 1, 2, '销售部 周经理', '0532-8888 0107', 'sales@shengchityre.com', '山东省青岛市', 4, 30, 1, 10,
       '渠道层级:厂家直供 | 供应品牌:达迈 DAMAI | 生产基地:山东广饶 | 定位:中低端高性价比', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_supplier) x WHERE x.`code` = 'SUP000010');

-- 沃凯途：公开资料查不到确切的中国区销售法人主体，用占位主体并标注「待核实」
INSERT INTO `base_supplier` (`code`, `name`, `short_name`, `type`, `credit_level`, `contact_person`, `contact_phone`, `contact_email`, `address`, `settlement_method`, `payment_days`, `status`, `sort`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT 'SUP000011', '沃凯途轮胎（中国）销售有限公司', '沃凯途中国', 1, 3, '渠道部 吴经理', '020-8888 0108', 'sales@vokatu.com', '广东省广州市', 2, 30, 1, 11,
       '渠道层级:区域总代 | 供应品牌:沃凯途 | ⚠️主体名称待核实，请替换为实际合作方营业执照名称', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_supplier) x WHERE x.`code` = 'SUP000011');

-- -----------------------------------------------------------------------------
-- 二级：区域代理 / 批发平台
-- -----------------------------------------------------------------------------
INSERT INTO `base_supplier` (`code`, `name`, `short_name`, `type`, `credit_level`, `contact_person`, `contact_phone`, `contact_email`, `address`, `settlement_method`, `payment_days`, `status`, `sort`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT 'SUP000012', '北京甲乙丙丁科技有限公司', '甲乙丙丁', 1, 2, '商户运营 郑经理', '010-8888 0109', 'merchant@jijiabingding.com', '北京市朝阳区', 2, 30, 1, 12,
       '渠道层级:B2B供应链平台 | 品类:多品牌轮胎一站式采购 | 适合小批量补货/调货', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_supplier) x WHERE x.`code` = 'SUP000012');

INSERT INTO `base_supplier` (`code`, `name`, `short_name`, `type`, `credit_level`, `contact_person`, `contact_phone`, `contact_email`, `address`, `settlement_method`, `payment_days`, `status`, `sort`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT 'SUP000013', '华东轮胎区域代理（示例）', '华东区域代理', 1, 2, '渠道经理 冯经理', '021-8888 0110', 'sales@hdtyre-agent.example', '上海市闵行区', 2, 30, 1, 13,
       '渠道层级:省级/区域代理 | 代理品牌:多品牌混合 | ⚠️示例数据，请替换为实际合作代理商', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_supplier) x WHERE x.`code` = 'SUP000013');

INSERT INTO `base_supplier` (`code`, `name`, `short_name`, `type`, `credit_level`, `contact_person`, `contact_phone`, `contact_email`, `address`, `settlement_method`, `payment_days`, `status`, `sort`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT 'SUP000014', '广饶轮胎外贸货源（示例）', '广饶外贸货源', 1, 3, '外贸部 何经理', '0546-8888 0111', 'export@grtyre.example', '山东省东营市广饶县', 1, 0, 1, 14,
       '渠道层级:外贸/出口尾单 | 品类:国产外贸胎、平行进口 | 现款现货、价格随行情浮动 | ⚠️示例数据', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_supplier) x WHERE x.`code` = 'SUP000014');

-- -----------------------------------------------------------------------------
-- 配套：设备与服务商
-- -----------------------------------------------------------------------------
INSERT INTO `base_supplier` (`code`, `name`, `short_name`, `type`, `credit_level`, `contact_person`, `contact_phone`, `contact_email`, `address`, `settlement_method`, `payment_days`, `status`, `sort`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT 'SUP000015', '德邦物流股份有限公司', '德邦物流', 3, 2, '大客户部 许经理', '021-8888 0112', 'ka@deppon.com', '上海市青浦区', 2, 30, 1, 15,
       '服务类型:整车/零担运输 | 用途:轮胎大件重货干线运输与门店配送', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_supplier) x WHERE x.`code` = 'SUP000015');

INSERT INTO `base_supplier` (`code`, `name`, `short_name`, `type`, `credit_level`, `contact_person`, `contact_phone`, `contact_email`, `address`, `settlement_method`, `payment_days`, `status`, `sort`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT 'SUP000016', '深圳市元征科技股份有限公司', '元征科技', 2, 2, '渠道部 曹经理', '0755-8888 0113', 'sales@cnlaunch.com', '广东省深圳市龙岗区', 1, 0, 1, 16,
       '供应品类:扒胎机/动平衡仪/四轮定位仪/举升机 | 门店设备一次性采购，现款结算', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_supplier) x WHERE x.`code` = 'SUP000016');

-- -----------------------------------------------------------------------------
-- 编号流水校正（主数据编号取「含已逻辑删除记录」的全表最大值）
-- -----------------------------------------------------------------------------
UPDATE `sys_code_rule`
SET `current_value` = (SELECT COALESCE(MAX(CAST(REGEXP_REPLACE(RIGHT(`code`, 6), '[^0-9]', '') AS UNSIGNED)), 0) FROM `base_supplier` WHERE `code` LIKE 'SUP%')
WHERE `rule_code` = 'BASE_SUPPLIER';

-- -----------------------------------------------------------------------------
-- 执行结果核对
-- -----------------------------------------------------------------------------
SELECT CASE `type` WHEN 1 THEN '商品供应(厂家/渠道)' WHEN 2 THEN '设备供应商' ELSE '服务商' END AS `类型`,
       COUNT(*) AS `家数`,
       GROUP_CONCAT(`short_name` ORDER BY `code` SEPARATOR '、') AS `供应商`
FROM `base_supplier`
WHERE `deleted` = 0 AND `code` >= 'SUP000004'
GROUP BY `type`
ORDER BY `type`;

SELECT `code`, `short_name`, `name`,
       CASE `settlement_method` WHEN 1 THEN '现金' WHEN 2 THEN '月结' WHEN 3 THEN '季结' ELSE '账期' END AS `结算方式`,
       `payment_days` AS `账期天数`, `credit_level` AS `信用等级`, `remark`
FROM `base_supplier`
WHERE `deleted` = 0 AND `code` >= 'SUP000004'
ORDER BY `code`;

SELECT `rule_code`, `prefix`, `current_value` FROM `sys_code_rule` WHERE `rule_code` = 'BASE_SUPPLIER';

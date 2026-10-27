-- =============================================================================
-- 轮胎商品初始化数据（SPU + SKU + 销售属性 + 库存）
-- -----------------------------------------------------------------------------
-- 执行方式（必须指定库，脚本内不写 USE）：
--   mysql -h192.168.31.199 -uroot -p123456 --default-character-set=utf8mb4 \
--         erp-data < sql/init_tire_products.sql
--
-- 特性：**全量幂等**，可重复执行（所有写入均带 NOT EXISTS 守卫）。
--
-- 数据口径说明：
--   · SPU = 品牌 + 花纹系列（如「德国马牌 ComfortContact CC7」）
--   · SKU = SPU + 轮胎规格 + 载重速度级别（两条销售属性维度）
--   · base_material.id 即 SKU ID，单据/库存表的 product_id 语义不变
--   · 价格单位为 元/条，采购价 ≈ 零售价*0.62，销售价(批发) ≈ 零售价*0.80
--     仅为演示口径，实际请以供应商报价为准
-- =============================================================================

SET NAMES utf8mb4;

-- -----------------------------------------------------------------------------
-- 1. 物料分类：轮胎 / 轿车轮胎(PCR) / SUV越野轮胎 / 卡客车轮胎(TBR)
-- -----------------------------------------------------------------------------
INSERT INTO `base_material_category` (`parent_id`, `code`, `name`, `sort`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT 0, 'MC000006', '轮胎', 6, 1, '轮胎商品总分类', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_material_category) x WHERE x.`code` = 'MC000006');
INSERT INTO `base_material_category` (`parent_id`, `code`, `name`, `sort`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = 'MC000006'), 'MC000007', '轿车轮胎(PCR)', 7, 1, '乘用车轿车轮胎', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_material_category) x WHERE x.`code` = 'MC000007');
INSERT INTO `base_material_category` (`parent_id`, `code`, `name`, `sort`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = 'MC000006'), 'MC000008', 'SUV越野轮胎', 8, 1, '城市SUV/越野轮胎', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_material_category) x WHERE x.`code` = 'MC000008');
INSERT INTO `base_material_category` (`parent_id`, `code`, `name`, `sort`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = 'MC000006'), 'MC000009', '卡客车轮胎(TBR)', 9, 1, '卡客车/商用车轮胎（本期暂无商品）', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_material_category) x WHERE x.`code` = 'MC000009');

-- -----------------------------------------------------------------------------
-- 2. 计量单位：条
-- -----------------------------------------------------------------------------
INSERT INTO `base_unit` (`code`, `name`, `sort`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT 'UNIT000016', '条', 16, 1, '轮胎计量单位', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_unit) x WHERE x.`code` = 'UNIT000016');

-- -----------------------------------------------------------------------------
-- 3. 轮胎品牌（8 个）
-- -----------------------------------------------------------------------------
INSERT INTO `base_brand` (`code`, `name`, `short_name`, `status`, `sort`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT 'BRD000004', '德国马牌', 'Continental', 1, 4, '德国大陆集团（Continental AG）旗下轮胎品牌，1871年创立于汉诺威', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_brand) x WHERE x.`code` = 'BRD000004');
INSERT INTO `base_brand` (`code`, `name`, `short_name`, `status`, `sort`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT 'BRD000005', '普利司通', 'Bridgestone', 1, 5, '日本株式会社普利司通，1931年创立于福冈县久留米市', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_brand) x WHERE x.`code` = 'BRD000005');
INSERT INTO `base_brand` (`code`, `name`, `short_name`, `status`, `sort`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT 'BRD000006', '优科豪马', 'Yokohama', 1, 6, '日本横滨橡胶株式会社，1917年创立；2017年起中文品牌名由「横滨」改为「优科豪马」', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_brand) x WHERE x.`code` = 'BRD000006');
INSERT INTO `base_brand` (`code`, `name`, `short_name`, `status`, `sort`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT 'BRD000007', '韩泰轮胎', 'Hankook', 1, 7, '韩国韩泰轮胎株式会社，1941年创立，总部首尔', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_brand) x WHERE x.`code` = 'BRD000007');
INSERT INTO `base_brand` (`code`, `name`, `short_name`, `status`, `sort`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT 'BRD000008', '美国固铂', 'Cooper', 1, 8, '美国固铂轮胎橡胶公司（Cooper Tire），1914年创立，总部俄亥俄州芬德利', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_brand) x WHERE x.`code` = 'BRD000008');
INSERT INTO `base_brand` (`code`, `name`, `short_name`, `status`, `sort`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT 'BRD000009', '沃凯途轮胎', 'VOKATU', 1, 9, '德系技术背景轮胎品牌，主打静音舒适与中高端替换市场', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_brand) x WHERE x.`code` = 'BRD000009');
INSERT INTO `base_brand` (`code`, `name`, `short_name`, `status`, `sort`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT 'BRD000010', '赛轮路极', 'ROADX', 1, 10, '赛轮集团（SAILUN）旗下路极 ROADX 品牌，轿车胎/SUV胎/营运胎', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_brand) x WHERE x.`code` = 'BRD000010');
INSERT INTO `base_brand` (`code`, `name`, `short_name`, `status`, `sort`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT 'BRD000011', '达迈轮胎', 'DAMAI', 1, 11, '青岛胜驰轮胎旗下品牌，山东广饶生产基地，主打高性价比静音耐磨胎', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_brand) x WHERE x.`code` = 'BRD000011');

-- -----------------------------------------------------------------------------
-- 4. 销售属性组与属性值
--    轮胎规格（18 个值）+ 载重速度级别（18 个值）
-- -----------------------------------------------------------------------------
INSERT INTO `base_sale_property` (`code`, `name`, `sort`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT 'SP000005', '轮胎规格', 0, 1, '轮胎商品销售属性', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_sale_property) x WHERE x.`code` = 'SP000005');
INSERT INTO `base_sale_property` (`code`, `name`, `sort`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT 'SP000006', '载重速度级别', 0, 1, '轮胎商品销售属性', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_sale_property) x WHERE x.`code` = 'SP000006');
INSERT INTO `base_sale_property_item` (`property_id`, `code`, `name`, `sort`, `status`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_sale_property WHERE code = 'SP000005'), 'SI000013', '195/65R15', 1, 1, 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_sale_property_item i2 WHERE i2.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005') AND i2.`name` = '195/65R15');
INSERT INTO `base_sale_property_item` (`property_id`, `code`, `name`, `sort`, `status`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_sale_property WHERE code = 'SP000005'), 'SI000014', '205/55R16', 2, 1, 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_sale_property_item i2 WHERE i2.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005') AND i2.`name` = '205/55R16');
INSERT INTO `base_sale_property_item` (`property_id`, `code`, `name`, `sort`, `status`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_sale_property WHERE code = 'SP000005'), 'SI000015', '215/60R16', 3, 1, 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_sale_property_item i2 WHERE i2.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005') AND i2.`name` = '215/60R16');
INSERT INTO `base_sale_property_item` (`property_id`, `code`, `name`, `sort`, `status`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_sale_property WHERE code = 'SP000005'), 'SI000016', '225/50R17', 4, 1, 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_sale_property_item i2 WHERE i2.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005') AND i2.`name` = '225/50R17');
INSERT INTO `base_sale_property_item` (`property_id`, `code`, `name`, `sort`, `status`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_sale_property WHERE code = 'SP000005'), 'SI000017', '225/45R17', 5, 1, 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_sale_property_item i2 WHERE i2.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005') AND i2.`name` = '225/45R17');
INSERT INTO `base_sale_property_item` (`property_id`, `code`, `name`, `sort`, `status`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_sale_property WHERE code = 'SP000005'), 'SI000018', '225/40R18', 6, 1, 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_sale_property_item i2 WHERE i2.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005') AND i2.`name` = '225/40R18');
INSERT INTO `base_sale_property_item` (`property_id`, `code`, `name`, `sort`, `status`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_sale_property WHERE code = 'SP000005'), 'SI000019', '225/60R18', 7, 1, 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_sale_property_item i2 WHERE i2.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005') AND i2.`name` = '225/60R18');
INSERT INTO `base_sale_property_item` (`property_id`, `code`, `name`, `sort`, `status`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_sale_property WHERE code = 'SP000005'), 'SI000020', '235/55R19', 8, 1, 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_sale_property_item i2 WHERE i2.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005') AND i2.`name` = '235/55R19');
INSERT INTO `base_sale_property_item` (`property_id`, `code`, `name`, `sort`, `status`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_sale_property WHERE code = 'SP000005'), 'SI000021', '245/45R20', 9, 1, 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_sale_property_item i2 WHERE i2.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005') AND i2.`name` = '245/45R20');
INSERT INTO `base_sale_property_item` (`property_id`, `code`, `name`, `sort`, `status`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_sale_property WHERE code = 'SP000005'), 'SI000022', '235/60R18', 10, 1, 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_sale_property_item i2 WHERE i2.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005') AND i2.`name` = '235/60R18');
INSERT INTO `base_sale_property_item` (`property_id`, `code`, `name`, `sort`, `status`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_sale_property WHERE code = 'SP000005'), 'SI000023', '215/55R17', 11, 1, 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_sale_property_item i2 WHERE i2.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005') AND i2.`name` = '215/55R17');
INSERT INTO `base_sale_property_item` (`property_id`, `code`, `name`, `sort`, `status`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_sale_property WHERE code = 'SP000005'), 'SI000024', '235/45R18', 12, 1, 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_sale_property_item i2 WHERE i2.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005') AND i2.`name` = '235/45R18');
INSERT INTO `base_sale_property_item` (`property_id`, `code`, `name`, `sort`, `status`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_sale_property WHERE code = 'SP000005'), 'SI000025', '245/40R19', 13, 1, 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_sale_property_item i2 WHERE i2.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005') AND i2.`name` = '245/40R19');
INSERT INTO `base_sale_property_item` (`property_id`, `code`, `name`, `sort`, `status`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_sale_property WHERE code = 'SP000005'), 'SI000026', '255/55R19', 14, 1, 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_sale_property_item i2 WHERE i2.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005') AND i2.`name` = '255/55R19');
INSERT INTO `base_sale_property_item` (`property_id`, `code`, `name`, `sort`, `status`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_sale_property WHERE code = 'SP000005'), 'SI000027', '175/65R15', 15, 1, 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_sale_property_item i2 WHERE i2.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005') AND i2.`name` = '175/65R15');
INSERT INTO `base_sale_property_item` (`property_id`, `code`, `name`, `sort`, `status`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_sale_property WHERE code = 'SP000005'), 'SI000028', '215/55R18', 16, 1, 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_sale_property_item i2 WHERE i2.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005') AND i2.`name` = '215/55R18');
INSERT INTO `base_sale_property_item` (`property_id`, `code`, `name`, `sort`, `status`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_sale_property WHERE code = 'SP000005'), 'SI000029', '255/60R18', 17, 1, 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_sale_property_item i2 WHERE i2.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005') AND i2.`name` = '255/60R18');
INSERT INTO `base_sale_property_item` (`property_id`, `code`, `name`, `sort`, `status`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_sale_property WHERE code = 'SP000005'), 'SI000030', '215/60R17', 18, 1, 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_sale_property_item i2 WHERE i2.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005') AND i2.`name` = '215/60R17');
INSERT INTO `base_sale_property_item` (`property_id`, `code`, `name`, `sort`, `status`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_sale_property WHERE code = 'SP000006'), 'SI000031', '91H', 1, 1, 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_sale_property_item i2 WHERE i2.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006') AND i2.`name` = '91H');
INSERT INTO `base_sale_property_item` (`property_id`, `code`, `name`, `sort`, `status`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_sale_property WHERE code = 'SP000006'), 'SI000032', '91V', 2, 1, 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_sale_property_item i2 WHERE i2.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006') AND i2.`name` = '91V');
INSERT INTO `base_sale_property_item` (`property_id`, `code`, `name`, `sort`, `status`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_sale_property WHERE code = 'SP000006'), 'SI000033', '95V', 3, 1, 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_sale_property_item i2 WHERE i2.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006') AND i2.`name` = '95V');
INSERT INTO `base_sale_property_item` (`property_id`, `code`, `name`, `sort`, `status`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_sale_property WHERE code = 'SP000006'), 'SI000034', '94V', 4, 1, 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_sale_property_item i2 WHERE i2.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006') AND i2.`name` = '94V');
INSERT INTO `base_sale_property_item` (`property_id`, `code`, `name`, `sort`, `status`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_sale_property WHERE code = 'SP000006'), 'SI000035', '91W', 5, 1, 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_sale_property_item i2 WHERE i2.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006') AND i2.`name` = '91W');
INSERT INTO `base_sale_property_item` (`property_id`, `code`, `name`, `sort`, `status`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_sale_property WHERE code = 'SP000006'), 'SI000036', '94W', 6, 1, 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_sale_property_item i2 WHERE i2.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006') AND i2.`name` = '94W');
INSERT INTO `base_sale_property_item` (`property_id`, `code`, `name`, `sort`, `status`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_sale_property WHERE code = 'SP000006'), 'SI000037', '92Y', 7, 1, 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_sale_property_item i2 WHERE i2.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006') AND i2.`name` = '92Y');
INSERT INTO `base_sale_property_item` (`property_id`, `code`, `name`, `sort`, `status`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_sale_property WHERE code = 'SP000006'), 'SI000038', '100H', 8, 1, 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_sale_property_item i2 WHERE i2.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006') AND i2.`name` = '100H');
INSERT INTO `base_sale_property_item` (`property_id`, `code`, `name`, `sort`, `status`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_sale_property WHERE code = 'SP000006'), 'SI000039', '101V', 9, 1, 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_sale_property_item i2 WHERE i2.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006') AND i2.`name` = '101V');
INSERT INTO `base_sale_property_item` (`property_id`, `code`, `name`, `sort`, `status`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_sale_property WHERE code = 'SP000006'), 'SI000040', '103V', 10, 1, 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_sale_property_item i2 WHERE i2.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006') AND i2.`name` = '103V');
INSERT INTO `base_sale_property_item` (`property_id`, `code`, `name`, `sort`, `status`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_sale_property WHERE code = 'SP000006'), 'SI000041', '95H', 11, 1, 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_sale_property_item i2 WHERE i2.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006') AND i2.`name` = '95H');
INSERT INTO `base_sale_property_item` (`property_id`, `code`, `name`, `sort`, `status`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_sale_property WHERE code = 'SP000006'), 'SI000042', '103H', 12, 1, 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_sale_property_item i2 WHERE i2.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006') AND i2.`name` = '103H');
INSERT INTO `base_sale_property_item` (`property_id`, `code`, `name`, `sort`, `status`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_sale_property WHERE code = 'SP000006'), 'SI000043', '98Y', 13, 1, 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_sale_property_item i2 WHERE i2.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006') AND i2.`name` = '98Y');
INSERT INTO `base_sale_property_item` (`property_id`, `code`, `name`, `sort`, `status`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_sale_property WHERE code = 'SP000006'), 'SI000044', '111H', 14, 1, 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_sale_property_item i2 WHERE i2.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006') AND i2.`name` = '111H');
INSERT INTO `base_sale_property_item` (`property_id`, `code`, `name`, `sort`, `status`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_sale_property WHERE code = 'SP000006'), 'SI000045', '88H', 15, 1, 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_sale_property_item i2 WHERE i2.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006') AND i2.`name` = '88H');
INSERT INTO `base_sale_property_item` (`property_id`, `code`, `name`, `sort`, `status`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_sale_property WHERE code = 'SP000006'), 'SI000046', '99V', 16, 1, 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_sale_property_item i2 WHERE i2.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006') AND i2.`name` = '99V');
INSERT INTO `base_sale_property_item` (`property_id`, `code`, `name`, `sort`, `status`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_sale_property WHERE code = 'SP000006'), 'SI000047', '112V', 17, 1, 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_sale_property_item i2 WHERE i2.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006') AND i2.`name` = '112V');
INSERT INTO `base_sale_property_item` (`property_id`, `code`, `name`, `sort`, `status`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_sale_property WHERE code = 'SP000006'), 'SI000048', '96V', 18, 1, 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_sale_property_item i2 WHERE i2.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006') AND i2.`name` = '96V');

-- -----------------------------------------------------------------------------
-- 5. SPU（22 个花纹系列）
-- -----------------------------------------------------------------------------
INSERT INTO `base_spu` (`category_id`, `brand_id`, `unit_id`, `code`, `name`, `short_name`, `specification`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = 'MC000007'),
       (SELECT id FROM base_brand            WHERE code = 'BRD000004'),
       (SELECT id FROM base_unit             WHERE code = 'UNIT000016'),
       'SPU000007', '德国马牌 ComfortContact CC7', '马牌CC7', '轿车轮胎 舒适静音 15-17寸', 1, '马牌舒适静音系列主力花纹，主打降噪与湿地制动', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_spu) x WHERE x.`code` = 'SPU000007');
INSERT INTO `base_spu` (`category_id`, `brand_id`, `unit_id`, `code`, `name`, `short_name`, `specification`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = 'MC000007'),
       (SELECT id FROM base_brand            WHERE code = 'BRD000004'),
       (SELECT id FROM base_unit             WHERE code = 'UNIT000016'),
       'SPU000008', '德国马牌 PremiumContact 7', '马牌PC7', '轿车轮胎 运动操控 16-18寸', 1, '马牌第七代运动操控花纹，干湿地抓地与转向响应为主', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_spu) x WHERE x.`code` = 'SPU000008');
INSERT INTO `base_spu` (`category_id`, `brand_id`, `unit_id`, `code`, `name`, `short_name`, `specification`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = 'MC000008'),
       (SELECT id FROM base_brand            WHERE code = 'BRD000004'),
       (SELECT id FROM base_unit             WHERE code = 'UNIT000016'),
       'SPU000009', '德国马牌 UltraContact UC7 SUV', '马牌UC7 SUV', 'SUV轮胎 均衡操控 18-20寸', 1, '马牌城市SUV花纹，兼顾舒适、湿地安全与操控', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_spu) x WHERE x.`code` = 'SPU000009');
INSERT INTO `base_spu` (`category_id`, `brand_id`, `unit_id`, `code`, `name`, `short_name`, `specification`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = 'MC000007'),
       (SELECT id FROM base_brand            WHERE code = 'BRD000005'),
       (SELECT id FROM base_unit             WHERE code = 'UNIT000016'),
       'SPU000010', '普利司通 TURANZA T005 泰然者', '普利司通T005', '轿车轮胎 舒适静逸 15-17寸', 1, '泰然者T005，舒适静音与湿地性能均衡', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_spu) x WHERE x.`code` = 'SPU000010');
INSERT INTO `base_spu` (`category_id`, `brand_id`, `unit_id`, `code`, `name`, `short_name`, `specification`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = 'MC000007'),
       (SELECT id FROM base_brand            WHERE code = 'BRD000005'),
       (SELECT id FROM base_unit             WHERE code = 'UNIT000016'),
       'SPU000011', '普利司通 ECOPIA EP150 御乘', '普利司通EP150', '轿车轮胎 节能环保 15-16寸', 1, 'ECOPIA 环保系列，低滚阻节油', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_spu) x WHERE x.`code` = 'SPU000011');
INSERT INTO `base_spu` (`category_id`, `brand_id`, `unit_id`, `code`, `name`, `short_name`, `specification`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = 'MC000008'),
       (SELECT id FROM base_brand            WHERE code = 'BRD000005'),
       (SELECT id FROM base_unit             WHERE code = 'UNIT000016'),
       'SPU000012', '普利司通 ALENZA 001', '普利司通ALENZA001', 'SUV轮胎 豪华舒适 18-19寸', 1, 'ALENZA 001 豪华SUV公路胎，静音舒适取向', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_spu) x WHERE x.`code` = 'SPU000012');
INSERT INTO `base_spu` (`category_id`, `brand_id`, `unit_id`, `code`, `name`, `short_name`, `specification`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = 'MC000007'),
       (SELECT id FROM base_brand            WHERE code = 'BRD000006'),
       (SELECT id FROM base_unit             WHERE code = 'UNIT000016'),
       'SPU000013', '优科豪马 BluEarth AE50 蔚弛', '优科豪马AE50', '轿车轮胎 节能环保 15-16寸', 1, 'BluEarth 蔚弛系列，低滚阻 + 湿地安全', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_spu) x WHERE x.`code` = 'SPU000013');
INSERT INTO `base_spu` (`category_id`, `brand_id`, `unit_id`, `code`, `name`, `short_name`, `specification`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = 'MC000007'),
       (SELECT id FROM base_brand            WHERE code = 'BRD000006'),
       (SELECT id FROM base_unit             WHERE code = 'UNIT000016'),
       'SPU000014', '优科豪马 ADVAN dB V551', '优科豪马V551', '轿车轮胎 静音舒适 16-17寸', 1, 'ADVAN dB 静音系列，主打低噪与舒适', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_spu) x WHERE x.`code` = 'SPU000014');
INSERT INTO `base_spu` (`category_id`, `brand_id`, `unit_id`, `code`, `name`, `short_name`, `specification`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = 'MC000008'),
       (SELECT id FROM base_brand            WHERE code = 'BRD000006'),
       (SELECT id FROM base_unit             WHERE code = 'UNIT000016'),
       'SPU000015', '优科豪马 GEOLANDAR X-CV', '优科豪马X-CV', 'SUV轮胎 公路操控 18-20寸', 1, 'GEOLANDAR 城市SUV公路胎，操控与湿地制动见长', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_spu) x WHERE x.`code` = 'SPU000015');
INSERT INTO `base_spu` (`category_id`, `brand_id`, `unit_id`, `code`, `name`, `short_name`, `specification`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = 'MC000007'),
       (SELECT id FROM base_brand            WHERE code = 'BRD000007'),
       (SELECT id FROM base_unit             WHERE code = 'UNIT000016'),
       'SPU000016', '韩泰 KINERGY ECO K435 傲特马', '韩泰K435', '轿车轮胎 节能环保 15-16寸', 1, 'Kinergy Eco 系列，耐磨节油，A级湿地', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_spu) x WHERE x.`code` = 'SPU000016');
INSERT INTO `base_spu` (`category_id`, `brand_id`, `unit_id`, `code`, `name`, `short_name`, `specification`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = 'MC000007'),
       (SELECT id FROM base_brand            WHERE code = 'BRD000007'),
       (SELECT id FROM base_unit             WHERE code = 'UNIT000016'),
       'SPU000017', '韩泰 VENTUS S1 evo3 K127 万途仕', '韩泰K127', '轿车轮胎 运动操控 17-19寸', 1, 'Ventus S1 evo3 高性能运动胎，干湿地操控', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_spu) x WHERE x.`code` = 'SPU000017');
INSERT INTO `base_spu` (`category_id`, `brand_id`, `unit_id`, `code`, `name`, `short_name`, `specification`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = 'MC000008'),
       (SELECT id FROM base_brand            WHERE code = 'BRD000007'),
       (SELECT id FROM base_unit             WHERE code = 'UNIT000016'),
       'SPU000018', '韩泰 DynaPro HP2 RA33', '韩泰RA33', 'SUV轮胎 舒适静音 18-19寸', 1, 'Dynapro HP2 城市SUV公路胎，舒适静音取向', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_spu) x WHERE x.`code` = 'SPU000018');
INSERT INTO `base_spu` (`category_id`, `brand_id`, `unit_id`, `code`, `name`, `short_name`, `specification`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = 'MC000007'),
       (SELECT id FROM base_brand            WHERE code = 'BRD000008'),
       (SELECT id FROM base_unit             WHERE code = 'UNIT000016'),
       'SPU000019', '美国固铂 ZEON CS8 泽昂', '固铂CS8', '轿车轮胎 静音舒适 16-17寸', 1, 'Zeon CS8 静音舒适轿车胎', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_spu) x WHERE x.`code` = 'SPU000019');
INSERT INTO `base_spu` (`category_id`, `brand_id`, `unit_id`, `code`, `name`, `short_name`, `specification`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = 'MC000007'),
       (SELECT id FROM base_brand            WHERE code = 'BRD000008'),
       (SELECT id FROM base_unit             WHERE code = 'UNIT000016'),
       'SPU000020', '美国固铂 EVOLUTION CTT 纵横者', '固铂CTT', '轿车轮胎 操控运动 16-18寸', 1, 'Evolution CTT 操控型轿车胎', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_spu) x WHERE x.`code` = 'SPU000020');
INSERT INTO `base_spu` (`category_id`, `brand_id`, `unit_id`, `code`, `name`, `short_name`, `specification`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = 'MC000008'),
       (SELECT id FROM base_brand            WHERE code = 'BRD000008'),
       (SELECT id FROM base_unit             WHERE code = 'UNIT000016'),
       'SPU000021', '美国固铂 DISCOVERER HTS', '固铂HTS', 'SUV轮胎 公路舒适 18-19寸', 1, 'Discoverer HTS 城市SUV公路胎，耐磨与湿地主打', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_spu) x WHERE x.`code` = 'SPU000021');
INSERT INTO `base_spu` (`category_id`, `brand_id`, `unit_id`, `code`, `name`, `short_name`, `specification`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = 'MC000007'),
       (SELECT id FROM base_brand            WHERE code = 'BRD000009'),
       (SELECT id FROM base_unit             WHERE code = 'UNIT000016'),
       'SPU000022', '沃凯途 静音舒适系列 VK5', '沃凯途VK5', '轿车轮胎 静音舒适 15-17寸', 1, '沃凯途轿车胎主力花纹，静音舒适 + 耐磨', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_spu) x WHERE x.`code` = 'SPU000022');
INSERT INTO `base_spu` (`category_id`, `brand_id`, `unit_id`, `code`, `name`, `short_name`, `specification`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = 'MC000008'),
       (SELECT id FROM base_brand            WHERE code = 'BRD000009'),
       (SELECT id FROM base_unit             WHERE code = 'UNIT000016'),
       'SPU000023', '沃凯途 城市SUV系列 VK8', '沃凯途VK8', 'SUV轮胎 城市公路 18-19寸', 1, '沃凯途城市SUV胎，舒适静音与承载兼顾', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_spu) x WHERE x.`code` = 'SPU000023');
INSERT INTO `base_spu` (`category_id`, `brand_id`, `unit_id`, `code`, `name`, `short_name`, `specification`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = 'MC000007'),
       (SELECT id FROM base_brand            WHERE code = 'BRD000010'),
       (SELECT id FROM base_unit             WHERE code = 'UNIT000016'),
       'SPU000024', '赛轮路极 ROADX DH12', '路极DH12', '轿车轮胎 结实耐磨 15-16寸', 1, 'ROADX DH12，结实耐磨取向的经济型轿车胎', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_spu) x WHERE x.`code` = 'SPU000024');
INSERT INTO `base_spu` (`category_id`, `brand_id`, `unit_id`, `code`, `name`, `short_name`, `specification`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = 'MC000007'),
       (SELECT id FROM base_brand            WHERE code = 'BRD000010'),
       (SELECT id FROM base_unit             WHERE code = 'UNIT000016'),
       'SPU000025', '赛轮路极 ROADX DT21 营运胎', '路极DT21', '轿车轮胎 营运耐磨 16-18寸', 1, 'ROADX DT21 营运胎，高里程耐磨，适配网约车/营运车辆', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_spu) x WHERE x.`code` = 'SPU000025');
INSERT INTO `base_spu` (`category_id`, `brand_id`, `unit_id`, `code`, `name`, `short_name`, `specification`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = 'MC000008'),
       (SELECT id FROM base_brand            WHERE code = 'BRD000010'),
       (SELECT id FROM base_unit             WHERE code = 'UNIT000016'),
       'SPU000026', '赛轮路极 ROADX DSU01', '路极DSU01', 'SUV轮胎 舒适静音耐磨 18寸', 1, 'ROADX DSU01 城市SUV胎，舒适静音 + 耐磨', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_spu) x WHERE x.`code` = 'SPU000026');
INSERT INTO `base_spu` (`category_id`, `brand_id`, `unit_id`, `code`, `name`, `short_name`, `specification`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = 'MC000007'),
       (SELECT id FROM base_brand            WHERE code = 'BRD000011'),
       (SELECT id FROM base_unit             WHERE code = 'UNIT000016'),
       'SPU000027', '达迈 GLEDE 静音系列', '达迈GLEDE', '轿车轮胎 静音舒适 15-17寸', 1, '达迈 GLEDE 系列，静音舒适，对标国产主流价位', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_spu) x WHERE x.`code` = 'SPU000027');
INSERT INTO `base_spu` (`category_id`, `brand_id`, `unit_id`, `code`, `name`, `short_name`, `specification`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = 'MC000008'),
       (SELECT id FROM base_brand            WHERE code = 'BRD000011'),
       (SELECT id FROM base_unit             WHERE code = 'UNIT000016'),
       'SPU000028', '达迈 宝石 耐磨系列', '达迈宝石', 'SUV轮胎 耐磨承载 18-19寸', 1, '达迈宝石系列，双层帘线结构，耐磨防鼓包', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_spu) x WHERE x.`code` = 'SPU000028');

-- -----------------------------------------------------------------------------
-- 6. SPU 绑定两条销售属性维度（轮胎规格 / 载重速度级别）
-- -----------------------------------------------------------------------------
INSERT INTO `base_spu_sale_property` (`spu_id`, `property_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_spu             WHERE code = 'SPU000007'),
       (SELECT id FROM base_sale_property   WHERE code = 'SP000005'), 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_spu_sale_property p
                            WHERE p.`spu_id` = (SELECT id FROM base_spu WHERE code = 'SPU000007')
                              AND p.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005'));
INSERT INTO `base_spu_sale_property` (`spu_id`, `property_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_spu             WHERE code = 'SPU000007'),
       (SELECT id FROM base_sale_property   WHERE code = 'SP000006'), 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_spu_sale_property p
                            WHERE p.`spu_id` = (SELECT id FROM base_spu WHERE code = 'SPU000007')
                              AND p.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006'));
INSERT INTO `base_spu_sale_property` (`spu_id`, `property_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_spu             WHERE code = 'SPU000008'),
       (SELECT id FROM base_sale_property   WHERE code = 'SP000005'), 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_spu_sale_property p
                            WHERE p.`spu_id` = (SELECT id FROM base_spu WHERE code = 'SPU000008')
                              AND p.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005'));
INSERT INTO `base_spu_sale_property` (`spu_id`, `property_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_spu             WHERE code = 'SPU000008'),
       (SELECT id FROM base_sale_property   WHERE code = 'SP000006'), 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_spu_sale_property p
                            WHERE p.`spu_id` = (SELECT id FROM base_spu WHERE code = 'SPU000008')
                              AND p.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006'));
INSERT INTO `base_spu_sale_property` (`spu_id`, `property_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_spu             WHERE code = 'SPU000009'),
       (SELECT id FROM base_sale_property   WHERE code = 'SP000005'), 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_spu_sale_property p
                            WHERE p.`spu_id` = (SELECT id FROM base_spu WHERE code = 'SPU000009')
                              AND p.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005'));
INSERT INTO `base_spu_sale_property` (`spu_id`, `property_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_spu             WHERE code = 'SPU000009'),
       (SELECT id FROM base_sale_property   WHERE code = 'SP000006'), 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_spu_sale_property p
                            WHERE p.`spu_id` = (SELECT id FROM base_spu WHERE code = 'SPU000009')
                              AND p.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006'));
INSERT INTO `base_spu_sale_property` (`spu_id`, `property_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_spu             WHERE code = 'SPU000010'),
       (SELECT id FROM base_sale_property   WHERE code = 'SP000005'), 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_spu_sale_property p
                            WHERE p.`spu_id` = (SELECT id FROM base_spu WHERE code = 'SPU000010')
                              AND p.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005'));
INSERT INTO `base_spu_sale_property` (`spu_id`, `property_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_spu             WHERE code = 'SPU000010'),
       (SELECT id FROM base_sale_property   WHERE code = 'SP000006'), 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_spu_sale_property p
                            WHERE p.`spu_id` = (SELECT id FROM base_spu WHERE code = 'SPU000010')
                              AND p.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006'));
INSERT INTO `base_spu_sale_property` (`spu_id`, `property_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_spu             WHERE code = 'SPU000011'),
       (SELECT id FROM base_sale_property   WHERE code = 'SP000005'), 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_spu_sale_property p
                            WHERE p.`spu_id` = (SELECT id FROM base_spu WHERE code = 'SPU000011')
                              AND p.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005'));
INSERT INTO `base_spu_sale_property` (`spu_id`, `property_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_spu             WHERE code = 'SPU000011'),
       (SELECT id FROM base_sale_property   WHERE code = 'SP000006'), 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_spu_sale_property p
                            WHERE p.`spu_id` = (SELECT id FROM base_spu WHERE code = 'SPU000011')
                              AND p.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006'));
INSERT INTO `base_spu_sale_property` (`spu_id`, `property_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_spu             WHERE code = 'SPU000012'),
       (SELECT id FROM base_sale_property   WHERE code = 'SP000005'), 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_spu_sale_property p
                            WHERE p.`spu_id` = (SELECT id FROM base_spu WHERE code = 'SPU000012')
                              AND p.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005'));
INSERT INTO `base_spu_sale_property` (`spu_id`, `property_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_spu             WHERE code = 'SPU000012'),
       (SELECT id FROM base_sale_property   WHERE code = 'SP000006'), 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_spu_sale_property p
                            WHERE p.`spu_id` = (SELECT id FROM base_spu WHERE code = 'SPU000012')
                              AND p.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006'));
INSERT INTO `base_spu_sale_property` (`spu_id`, `property_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_spu             WHERE code = 'SPU000013'),
       (SELECT id FROM base_sale_property   WHERE code = 'SP000005'), 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_spu_sale_property p
                            WHERE p.`spu_id` = (SELECT id FROM base_spu WHERE code = 'SPU000013')
                              AND p.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005'));
INSERT INTO `base_spu_sale_property` (`spu_id`, `property_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_spu             WHERE code = 'SPU000013'),
       (SELECT id FROM base_sale_property   WHERE code = 'SP000006'), 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_spu_sale_property p
                            WHERE p.`spu_id` = (SELECT id FROM base_spu WHERE code = 'SPU000013')
                              AND p.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006'));
INSERT INTO `base_spu_sale_property` (`spu_id`, `property_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_spu             WHERE code = 'SPU000014'),
       (SELECT id FROM base_sale_property   WHERE code = 'SP000005'), 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_spu_sale_property p
                            WHERE p.`spu_id` = (SELECT id FROM base_spu WHERE code = 'SPU000014')
                              AND p.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005'));
INSERT INTO `base_spu_sale_property` (`spu_id`, `property_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_spu             WHERE code = 'SPU000014'),
       (SELECT id FROM base_sale_property   WHERE code = 'SP000006'), 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_spu_sale_property p
                            WHERE p.`spu_id` = (SELECT id FROM base_spu WHERE code = 'SPU000014')
                              AND p.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006'));
INSERT INTO `base_spu_sale_property` (`spu_id`, `property_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_spu             WHERE code = 'SPU000015'),
       (SELECT id FROM base_sale_property   WHERE code = 'SP000005'), 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_spu_sale_property p
                            WHERE p.`spu_id` = (SELECT id FROM base_spu WHERE code = 'SPU000015')
                              AND p.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005'));
INSERT INTO `base_spu_sale_property` (`spu_id`, `property_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_spu             WHERE code = 'SPU000015'),
       (SELECT id FROM base_sale_property   WHERE code = 'SP000006'), 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_spu_sale_property p
                            WHERE p.`spu_id` = (SELECT id FROM base_spu WHERE code = 'SPU000015')
                              AND p.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006'));
INSERT INTO `base_spu_sale_property` (`spu_id`, `property_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_spu             WHERE code = 'SPU000016'),
       (SELECT id FROM base_sale_property   WHERE code = 'SP000005'), 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_spu_sale_property p
                            WHERE p.`spu_id` = (SELECT id FROM base_spu WHERE code = 'SPU000016')
                              AND p.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005'));
INSERT INTO `base_spu_sale_property` (`spu_id`, `property_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_spu             WHERE code = 'SPU000016'),
       (SELECT id FROM base_sale_property   WHERE code = 'SP000006'), 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_spu_sale_property p
                            WHERE p.`spu_id` = (SELECT id FROM base_spu WHERE code = 'SPU000016')
                              AND p.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006'));
INSERT INTO `base_spu_sale_property` (`spu_id`, `property_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_spu             WHERE code = 'SPU000017'),
       (SELECT id FROM base_sale_property   WHERE code = 'SP000005'), 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_spu_sale_property p
                            WHERE p.`spu_id` = (SELECT id FROM base_spu WHERE code = 'SPU000017')
                              AND p.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005'));
INSERT INTO `base_spu_sale_property` (`spu_id`, `property_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_spu             WHERE code = 'SPU000017'),
       (SELECT id FROM base_sale_property   WHERE code = 'SP000006'), 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_spu_sale_property p
                            WHERE p.`spu_id` = (SELECT id FROM base_spu WHERE code = 'SPU000017')
                              AND p.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006'));
INSERT INTO `base_spu_sale_property` (`spu_id`, `property_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_spu             WHERE code = 'SPU000018'),
       (SELECT id FROM base_sale_property   WHERE code = 'SP000005'), 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_spu_sale_property p
                            WHERE p.`spu_id` = (SELECT id FROM base_spu WHERE code = 'SPU000018')
                              AND p.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005'));
INSERT INTO `base_spu_sale_property` (`spu_id`, `property_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_spu             WHERE code = 'SPU000018'),
       (SELECT id FROM base_sale_property   WHERE code = 'SP000006'), 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_spu_sale_property p
                            WHERE p.`spu_id` = (SELECT id FROM base_spu WHERE code = 'SPU000018')
                              AND p.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006'));
INSERT INTO `base_spu_sale_property` (`spu_id`, `property_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_spu             WHERE code = 'SPU000019'),
       (SELECT id FROM base_sale_property   WHERE code = 'SP000005'), 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_spu_sale_property p
                            WHERE p.`spu_id` = (SELECT id FROM base_spu WHERE code = 'SPU000019')
                              AND p.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005'));
INSERT INTO `base_spu_sale_property` (`spu_id`, `property_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_spu             WHERE code = 'SPU000019'),
       (SELECT id FROM base_sale_property   WHERE code = 'SP000006'), 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_spu_sale_property p
                            WHERE p.`spu_id` = (SELECT id FROM base_spu WHERE code = 'SPU000019')
                              AND p.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006'));
INSERT INTO `base_spu_sale_property` (`spu_id`, `property_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_spu             WHERE code = 'SPU000020'),
       (SELECT id FROM base_sale_property   WHERE code = 'SP000005'), 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_spu_sale_property p
                            WHERE p.`spu_id` = (SELECT id FROM base_spu WHERE code = 'SPU000020')
                              AND p.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005'));
INSERT INTO `base_spu_sale_property` (`spu_id`, `property_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_spu             WHERE code = 'SPU000020'),
       (SELECT id FROM base_sale_property   WHERE code = 'SP000006'), 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_spu_sale_property p
                            WHERE p.`spu_id` = (SELECT id FROM base_spu WHERE code = 'SPU000020')
                              AND p.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006'));
INSERT INTO `base_spu_sale_property` (`spu_id`, `property_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_spu             WHERE code = 'SPU000021'),
       (SELECT id FROM base_sale_property   WHERE code = 'SP000005'), 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_spu_sale_property p
                            WHERE p.`spu_id` = (SELECT id FROM base_spu WHERE code = 'SPU000021')
                              AND p.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005'));
INSERT INTO `base_spu_sale_property` (`spu_id`, `property_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_spu             WHERE code = 'SPU000021'),
       (SELECT id FROM base_sale_property   WHERE code = 'SP000006'), 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_spu_sale_property p
                            WHERE p.`spu_id` = (SELECT id FROM base_spu WHERE code = 'SPU000021')
                              AND p.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006'));
INSERT INTO `base_spu_sale_property` (`spu_id`, `property_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_spu             WHERE code = 'SPU000022'),
       (SELECT id FROM base_sale_property   WHERE code = 'SP000005'), 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_spu_sale_property p
                            WHERE p.`spu_id` = (SELECT id FROM base_spu WHERE code = 'SPU000022')
                              AND p.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005'));
INSERT INTO `base_spu_sale_property` (`spu_id`, `property_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_spu             WHERE code = 'SPU000022'),
       (SELECT id FROM base_sale_property   WHERE code = 'SP000006'), 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_spu_sale_property p
                            WHERE p.`spu_id` = (SELECT id FROM base_spu WHERE code = 'SPU000022')
                              AND p.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006'));
INSERT INTO `base_spu_sale_property` (`spu_id`, `property_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_spu             WHERE code = 'SPU000023'),
       (SELECT id FROM base_sale_property   WHERE code = 'SP000005'), 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_spu_sale_property p
                            WHERE p.`spu_id` = (SELECT id FROM base_spu WHERE code = 'SPU000023')
                              AND p.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005'));
INSERT INTO `base_spu_sale_property` (`spu_id`, `property_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_spu             WHERE code = 'SPU000023'),
       (SELECT id FROM base_sale_property   WHERE code = 'SP000006'), 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_spu_sale_property p
                            WHERE p.`spu_id` = (SELECT id FROM base_spu WHERE code = 'SPU000023')
                              AND p.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006'));
INSERT INTO `base_spu_sale_property` (`spu_id`, `property_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_spu             WHERE code = 'SPU000024'),
       (SELECT id FROM base_sale_property   WHERE code = 'SP000005'), 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_spu_sale_property p
                            WHERE p.`spu_id` = (SELECT id FROM base_spu WHERE code = 'SPU000024')
                              AND p.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005'));
INSERT INTO `base_spu_sale_property` (`spu_id`, `property_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_spu             WHERE code = 'SPU000024'),
       (SELECT id FROM base_sale_property   WHERE code = 'SP000006'), 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_spu_sale_property p
                            WHERE p.`spu_id` = (SELECT id FROM base_spu WHERE code = 'SPU000024')
                              AND p.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006'));
INSERT INTO `base_spu_sale_property` (`spu_id`, `property_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_spu             WHERE code = 'SPU000025'),
       (SELECT id FROM base_sale_property   WHERE code = 'SP000005'), 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_spu_sale_property p
                            WHERE p.`spu_id` = (SELECT id FROM base_spu WHERE code = 'SPU000025')
                              AND p.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005'));
INSERT INTO `base_spu_sale_property` (`spu_id`, `property_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_spu             WHERE code = 'SPU000025'),
       (SELECT id FROM base_sale_property   WHERE code = 'SP000006'), 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_spu_sale_property p
                            WHERE p.`spu_id` = (SELECT id FROM base_spu WHERE code = 'SPU000025')
                              AND p.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006'));
INSERT INTO `base_spu_sale_property` (`spu_id`, `property_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_spu             WHERE code = 'SPU000026'),
       (SELECT id FROM base_sale_property   WHERE code = 'SP000005'), 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_spu_sale_property p
                            WHERE p.`spu_id` = (SELECT id FROM base_spu WHERE code = 'SPU000026')
                              AND p.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005'));
INSERT INTO `base_spu_sale_property` (`spu_id`, `property_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_spu             WHERE code = 'SPU000026'),
       (SELECT id FROM base_sale_property   WHERE code = 'SP000006'), 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_spu_sale_property p
                            WHERE p.`spu_id` = (SELECT id FROM base_spu WHERE code = 'SPU000026')
                              AND p.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006'));
INSERT INTO `base_spu_sale_property` (`spu_id`, `property_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_spu             WHERE code = 'SPU000027'),
       (SELECT id FROM base_sale_property   WHERE code = 'SP000005'), 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_spu_sale_property p
                            WHERE p.`spu_id` = (SELECT id FROM base_spu WHERE code = 'SPU000027')
                              AND p.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005'));
INSERT INTO `base_spu_sale_property` (`spu_id`, `property_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_spu             WHERE code = 'SPU000027'),
       (SELECT id FROM base_sale_property   WHERE code = 'SP000006'), 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_spu_sale_property p
                            WHERE p.`spu_id` = (SELECT id FROM base_spu WHERE code = 'SPU000027')
                              AND p.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006'));
INSERT INTO `base_spu_sale_property` (`spu_id`, `property_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_spu             WHERE code = 'SPU000028'),
       (SELECT id FROM base_sale_property   WHERE code = 'SP000005'), 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_spu_sale_property p
                            WHERE p.`spu_id` = (SELECT id FROM base_spu WHERE code = 'SPU000028')
                              AND p.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005'));
INSERT INTO `base_spu_sale_property` (`spu_id`, `property_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_spu             WHERE code = 'SPU000028'),
       (SELECT id FROM base_sale_property   WHERE code = 'SP000006'), 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_spu_sale_property p
                            WHERE p.`spu_id` = (SELECT id FROM base_spu WHERE code = 'SPU000028')
                              AND p.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006'));

-- -----------------------------------------------------------------------------
-- 7. SKU（base_material，即商品SKU）
--    命名规则：SPU名称 + 规格 + 载重速度级别
-- -----------------------------------------------------------------------------
INSERT INTO `base_material` (`category_id`, `spu_id`, `code`, `name`, `short_name`, `specification`, `sale_property_text`, `unit_id`, `purchase_price`, `sale_price`, `retail_price`, `min_stock`, `max_stock`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = 'MC000007'),
       (SELECT id FROM base_spu WHERE code = 'SPU000007'),
       'MAT000024', '德国马牌 ComfortContact CC7 195/65R15 91H', '德国马牌 ComfortContact CC7 195/65R15 91H', '195/65R15 91H', '轮胎规格:195/65R15 / 载重速度级别:91H',
       (SELECT id FROM base_unit WHERE code = 'UNIT000016'),
       278.00, 359.00, 449.00, 20.00, 300.00, 1, 'SPU000007 轮胎商品', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_material) x WHERE x.`code` = 'MAT000024');
INSERT INTO `base_material` (`category_id`, `spu_id`, `code`, `name`, `short_name`, `specification`, `sale_property_text`, `unit_id`, `purchase_price`, `sale_price`, `retail_price`, `min_stock`, `max_stock`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = 'MC000007'),
       (SELECT id FROM base_spu WHERE code = 'SPU000007'),
       'MAT000025', '德国马牌 ComfortContact CC7 205/55R16 91V', '德国马牌 ComfortContact CC7 205/55R16 91V', '205/55R16 91V', '轮胎规格:205/55R16 / 载重速度级别:91V',
       (SELECT id FROM base_unit WHERE code = 'UNIT000016'),
       328.00, 423.00, 529.00, 20.00, 300.00, 1, 'SPU000007 轮胎商品', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_material) x WHERE x.`code` = 'MAT000025');
INSERT INTO `base_material` (`category_id`, `spu_id`, `code`, `name`, `short_name`, `specification`, `sale_property_text`, `unit_id`, `purchase_price`, `sale_price`, `retail_price`, `min_stock`, `max_stock`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = 'MC000007'),
       (SELECT id FROM base_spu WHERE code = 'SPU000007'),
       'MAT000026', '德国马牌 ComfortContact CC7 215/60R16 95V', '德国马牌 ComfortContact CC7 215/60R16 95V', '215/60R16 95V', '轮胎规格:215/60R16 / 载重速度级别:95V',
       (SELECT id FROM base_unit WHERE code = 'UNIT000016'),
       409.00, 527.00, 659.00, 20.00, 300.00, 1, 'SPU000007 轮胎商品', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_material) x WHERE x.`code` = 'MAT000026');
INSERT INTO `base_material` (`category_id`, `spu_id`, `code`, `name`, `short_name`, `specification`, `sale_property_text`, `unit_id`, `purchase_price`, `sale_price`, `retail_price`, `min_stock`, `max_stock`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = 'MC000007'),
       (SELECT id FROM base_spu WHERE code = 'SPU000007'),
       'MAT000027', '德国马牌 ComfortContact CC7 225/50R17 94V', '德国马牌 ComfortContact CC7 225/50R17 94V', '225/50R17 94V', '轮胎规格:225/50R17 / 载重速度级别:94V',
       (SELECT id FROM base_unit WHERE code = 'UNIT000016'),
       514.00, 663.00, 829.00, 20.00, 300.00, 1, 'SPU000007 轮胎商品', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_material) x WHERE x.`code` = 'MAT000027');
INSERT INTO `base_material` (`category_id`, `spu_id`, `code`, `name`, `short_name`, `specification`, `sale_property_text`, `unit_id`, `purchase_price`, `sale_price`, `retail_price`, `min_stock`, `max_stock`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = 'MC000007'),
       (SELECT id FROM base_spu WHERE code = 'SPU000008'),
       'MAT000028', '德国马牌 PremiumContact 7 205/55R16 91W', '德国马牌 PremiumContact 7 205/55R16 91W', '205/55R16 91W', '轮胎规格:205/55R16 / 载重速度级别:91W',
       (SELECT id FROM base_unit WHERE code = 'UNIT000016'),
       359.00, 463.00, 579.00, 20.00, 300.00, 1, 'SPU000008 轮胎商品', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_material) x WHERE x.`code` = 'MAT000028');
INSERT INTO `base_material` (`category_id`, `spu_id`, `code`, `name`, `short_name`, `specification`, `sale_property_text`, `unit_id`, `purchase_price`, `sale_price`, `retail_price`, `min_stock`, `max_stock`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = 'MC000007'),
       (SELECT id FROM base_spu WHERE code = 'SPU000008'),
       'MAT000029', '德国马牌 PremiumContact 7 225/45R17 94W', '德国马牌 PremiumContact 7 225/45R17 94W', '225/45R17 94W', '轮胎规格:225/45R17 / 载重速度级别:94W',
       (SELECT id FROM base_unit WHERE code = 'UNIT000016'),
       533.00, 687.00, 859.00, 20.00, 300.00, 1, 'SPU000008 轮胎商品', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_material) x WHERE x.`code` = 'MAT000029');
INSERT INTO `base_material` (`category_id`, `spu_id`, `code`, `name`, `short_name`, `specification`, `sale_property_text`, `unit_id`, `purchase_price`, `sale_price`, `retail_price`, `min_stock`, `max_stock`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = 'MC000007'),
       (SELECT id FROM base_spu WHERE code = 'SPU000008'),
       'MAT000030', '德国马牌 PremiumContact 7 225/40R18 92Y', '德国马牌 PremiumContact 7 225/40R18 92Y', '225/40R18 92Y', '轮胎规格:225/40R18 / 载重速度级别:92Y',
       (SELECT id FROM base_unit WHERE code = 'UNIT000016'),
       719.00, 927.00, 1159.00, 20.00, 300.00, 1, 'SPU000008 轮胎商品', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_material) x WHERE x.`code` = 'MAT000030');
INSERT INTO `base_material` (`category_id`, `spu_id`, `code`, `name`, `short_name`, `specification`, `sale_property_text`, `unit_id`, `purchase_price`, `sale_price`, `retail_price`, `min_stock`, `max_stock`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = 'MC000008'),
       (SELECT id FROM base_spu WHERE code = 'SPU000009'),
       'MAT000031', '德国马牌 UltraContact UC7 SUV 225/60R18 100H', '德国马牌 UltraContact UC7 SUV 225/60R18 100H', '225/60R18 100H', '轮胎规格:225/60R18 / 载重速度级别:100H',
       (SELECT id FROM base_unit WHERE code = 'UNIT000016'),
       681.00, 879.00, 1099.00, 20.00, 300.00, 1, 'SPU000009 轮胎商品', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_material) x WHERE x.`code` = 'MAT000031');
INSERT INTO `base_material` (`category_id`, `spu_id`, `code`, `name`, `short_name`, `specification`, `sale_property_text`, `unit_id`, `purchase_price`, `sale_price`, `retail_price`, `min_stock`, `max_stock`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = 'MC000008'),
       (SELECT id FROM base_spu WHERE code = 'SPU000009'),
       'MAT000032', '德国马牌 UltraContact UC7 SUV 235/55R19 101V', '德国马牌 UltraContact UC7 SUV 235/55R19 101V', '235/55R19 101V', '轮胎规格:235/55R19 / 载重速度级别:101V',
       (SELECT id FROM base_unit WHERE code = 'UNIT000016'),
       867.00, 1119.00, 1399.00, 20.00, 300.00, 1, 'SPU000009 轮胎商品', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_material) x WHERE x.`code` = 'MAT000032');
INSERT INTO `base_material` (`category_id`, `spu_id`, `code`, `name`, `short_name`, `specification`, `sale_property_text`, `unit_id`, `purchase_price`, `sale_price`, `retail_price`, `min_stock`, `max_stock`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = 'MC000008'),
       (SELECT id FROM base_spu WHERE code = 'SPU000009'),
       'MAT000033', '德国马牌 UltraContact UC7 SUV 245/45R20 103V', '德国马牌 UltraContact UC7 SUV 245/45R20 103V', '245/45R20 103V', '轮胎规格:245/45R20 / 载重速度级别:103V',
       (SELECT id FROM base_unit WHERE code = 'UNIT000016'),
       1091.00, 1407.00, 1759.00, 20.00, 300.00, 1, 'SPU000009 轮胎商品', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_material) x WHERE x.`code` = 'MAT000033');
INSERT INTO `base_material` (`category_id`, `spu_id`, `code`, `name`, `short_name`, `specification`, `sale_property_text`, `unit_id`, `purchase_price`, `sale_price`, `retail_price`, `min_stock`, `max_stock`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = 'MC000007'),
       (SELECT id FROM base_spu WHERE code = 'SPU000010'),
       'MAT000034', '普利司通 TURANZA T005 泰然者 195/65R15 91H', '普利司通 TURANZA T005 泰然者 195/65R15 91H', '195/65R15 91H', '轮胎规格:195/65R15 / 载重速度级别:91H',
       (SELECT id FROM base_unit WHERE code = 'UNIT000016'),
       272.00, 351.00, 439.00, 20.00, 300.00, 1, 'SPU000010 轮胎商品', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_material) x WHERE x.`code` = 'MAT000034');
INSERT INTO `base_material` (`category_id`, `spu_id`, `code`, `name`, `short_name`, `specification`, `sale_property_text`, `unit_id`, `purchase_price`, `sale_price`, `retail_price`, `min_stock`, `max_stock`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = 'MC000007'),
       (SELECT id FROM base_spu WHERE code = 'SPU000010'),
       'MAT000035', '普利司通 TURANZA T005 泰然者 205/55R16 91V', '普利司通 TURANZA T005 泰然者 205/55R16 91V', '205/55R16 91V', '轮胎规格:205/55R16 / 载重速度级别:91V',
       (SELECT id FROM base_unit WHERE code = 'UNIT000016'),
       322.00, 415.00, 519.00, 20.00, 300.00, 1, 'SPU000010 轮胎商品', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_material) x WHERE x.`code` = 'MAT000035');
INSERT INTO `base_material` (`category_id`, `spu_id`, `code`, `name`, `short_name`, `specification`, `sale_property_text`, `unit_id`, `purchase_price`, `sale_price`, `retail_price`, `min_stock`, `max_stock`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = 'MC000007'),
       (SELECT id FROM base_spu WHERE code = 'SPU000010'),
       'MAT000036', '普利司通 TURANZA T005 泰然者 215/60R16 95V', '普利司通 TURANZA T005 泰然者 215/60R16 95V', '215/60R16 95V', '轮胎规格:215/60R16 / 载重速度级别:95V',
       (SELECT id FROM base_unit WHERE code = 'UNIT000016'),
       402.00, 519.00, 649.00, 20.00, 300.00, 1, 'SPU000010 轮胎商品', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_material) x WHERE x.`code` = 'MAT000036');
INSERT INTO `base_material` (`category_id`, `spu_id`, `code`, `name`, `short_name`, `specification`, `sale_property_text`, `unit_id`, `purchase_price`, `sale_price`, `retail_price`, `min_stock`, `max_stock`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = 'MC000007'),
       (SELECT id FROM base_spu WHERE code = 'SPU000010'),
       'MAT000037', '普利司通 TURANZA T005 泰然者 225/45R17 91W', '普利司通 TURANZA T005 泰然者 225/45R17 91W', '225/45R17 91W', '轮胎规格:225/45R17 / 载重速度级别:91W',
       (SELECT id FROM base_unit WHERE code = 'UNIT000016'),
       514.00, 663.00, 829.00, 20.00, 300.00, 1, 'SPU000010 轮胎商品', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_material) x WHERE x.`code` = 'MAT000037');
INSERT INTO `base_material` (`category_id`, `spu_id`, `code`, `name`, `short_name`, `specification`, `sale_property_text`, `unit_id`, `purchase_price`, `sale_price`, `retail_price`, `min_stock`, `max_stock`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = 'MC000007'),
       (SELECT id FROM base_spu WHERE code = 'SPU000011'),
       'MAT000038', '普利司通 ECOPIA EP150 御乘 195/65R15 91H', '普利司通 ECOPIA EP150 御乘 195/65R15 91H', '195/65R15 91H', '轮胎规格:195/65R15 / 载重速度级别:91H',
       (SELECT id FROM base_unit WHERE code = 'UNIT000016'),
       241.00, 311.00, 389.00, 20.00, 300.00, 1, 'SPU000011 轮胎商品', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_material) x WHERE x.`code` = 'MAT000038');
INSERT INTO `base_material` (`category_id`, `spu_id`, `code`, `name`, `short_name`, `specification`, `sale_property_text`, `unit_id`, `purchase_price`, `sale_price`, `retail_price`, `min_stock`, `max_stock`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = 'MC000007'),
       (SELECT id FROM base_spu WHERE code = 'SPU000011'),
       'MAT000039', '普利司通 ECOPIA EP150 御乘 205/55R16 91V', '普利司通 ECOPIA EP150 御乘 205/55R16 91V', '205/55R16 91V', '轮胎规格:205/55R16 / 载重速度级别:91V',
       (SELECT id FROM base_unit WHERE code = 'UNIT000016'),
       285.00, 367.00, 459.00, 20.00, 300.00, 1, 'SPU000011 轮胎商品', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_material) x WHERE x.`code` = 'MAT000039');
INSERT INTO `base_material` (`category_id`, `spu_id`, `code`, `name`, `short_name`, `specification`, `sale_property_text`, `unit_id`, `purchase_price`, `sale_price`, `retail_price`, `min_stock`, `max_stock`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = 'MC000007'),
       (SELECT id FROM base_spu WHERE code = 'SPU000011'),
       'MAT000040', '普利司通 ECOPIA EP150 御乘 215/60R16 95H', '普利司通 ECOPIA EP150 御乘 215/60R16 95H', '215/60R16 95H', '轮胎规格:215/60R16 / 载重速度级别:95H',
       (SELECT id FROM base_unit WHERE code = 'UNIT000016'),
       347.00, 447.00, 559.00, 20.00, 300.00, 1, 'SPU000011 轮胎商品', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_material) x WHERE x.`code` = 'MAT000040');
INSERT INTO `base_material` (`category_id`, `spu_id`, `code`, `name`, `short_name`, `specification`, `sale_property_text`, `unit_id`, `purchase_price`, `sale_price`, `retail_price`, `min_stock`, `max_stock`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = 'MC000008'),
       (SELECT id FROM base_spu WHERE code = 'SPU000012'),
       'MAT000041', '普利司通 ALENZA 001 225/60R18 100H', '普利司通 ALENZA 001 225/60R18 100H', '225/60R18 100H', '轮胎规格:225/60R18 / 载重速度级别:100H',
       (SELECT id FROM base_unit WHERE code = 'UNIT000016'),
       719.00, 927.00, 1159.00, 20.00, 300.00, 1, 'SPU000012 轮胎商品', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_material) x WHERE x.`code` = 'MAT000041');
INSERT INTO `base_material` (`category_id`, `spu_id`, `code`, `name`, `short_name`, `specification`, `sale_property_text`, `unit_id`, `purchase_price`, `sale_price`, `retail_price`, `min_stock`, `max_stock`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = 'MC000008'),
       (SELECT id FROM base_spu WHERE code = 'SPU000012'),
       'MAT000042', '普利司通 ALENZA 001 235/55R19 101V', '普利司通 ALENZA 001 235/55R19 101V', '235/55R19 101V', '轮胎规格:235/55R19 / 载重速度级别:101V',
       (SELECT id FROM base_unit WHERE code = 'UNIT000016'),
       905.00, 1167.00, 1459.00, 20.00, 300.00, 1, 'SPU000012 轮胎商品', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_material) x WHERE x.`code` = 'MAT000042');
INSERT INTO `base_material` (`category_id`, `spu_id`, `code`, `name`, `short_name`, `specification`, `sale_property_text`, `unit_id`, `purchase_price`, `sale_price`, `retail_price`, `min_stock`, `max_stock`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = 'MC000008'),
       (SELECT id FROM base_spu WHERE code = 'SPU000012'),
       'MAT000043', '普利司通 ALENZA 001 235/60R18 103H', '普利司通 ALENZA 001 235/60R18 103H', '235/60R18 103H', '轮胎规格:235/60R18 / 载重速度级别:103H',
       (SELECT id FROM base_unit WHERE code = 'UNIT000016'),
       781.00, 1007.00, 1259.00, 20.00, 300.00, 1, 'SPU000012 轮胎商品', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_material) x WHERE x.`code` = 'MAT000043');
INSERT INTO `base_material` (`category_id`, `spu_id`, `code`, `name`, `short_name`, `specification`, `sale_property_text`, `unit_id`, `purchase_price`, `sale_price`, `retail_price`, `min_stock`, `max_stock`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = 'MC000007'),
       (SELECT id FROM base_spu WHERE code = 'SPU000013'),
       'MAT000044', '优科豪马 BluEarth AE50 蔚弛 195/65R15 91H', '优科豪马 BluEarth AE50 蔚弛 195/65R15 91H', '195/65R15 91H', '轮胎规格:195/65R15 / 载重速度级别:91H',
       (SELECT id FROM base_unit WHERE code = 'UNIT000016'),
       247.00, 319.00, 399.00, 20.00, 300.00, 1, 'SPU000013 轮胎商品', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_material) x WHERE x.`code` = 'MAT000044');
INSERT INTO `base_material` (`category_id`, `spu_id`, `code`, `name`, `short_name`, `specification`, `sale_property_text`, `unit_id`, `purchase_price`, `sale_price`, `retail_price`, `min_stock`, `max_stock`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = 'MC000007'),
       (SELECT id FROM base_spu WHERE code = 'SPU000013'),
       'MAT000045', '优科豪马 BluEarth AE50 蔚弛 205/55R16 91V', '优科豪马 BluEarth AE50 蔚弛 205/55R16 91V', '205/55R16 91V', '轮胎规格:205/55R16 / 载重速度级别:91V',
       (SELECT id FROM base_unit WHERE code = 'UNIT000016'),
       291.00, 375.00, 469.00, 20.00, 300.00, 1, 'SPU000013 轮胎商品', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_material) x WHERE x.`code` = 'MAT000045');
INSERT INTO `base_material` (`category_id`, `spu_id`, `code`, `name`, `short_name`, `specification`, `sale_property_text`, `unit_id`, `purchase_price`, `sale_price`, `retail_price`, `min_stock`, `max_stock`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = 'MC000007'),
       (SELECT id FROM base_spu WHERE code = 'SPU000013'),
       'MAT000046', '优科豪马 BluEarth AE50 蔚弛 215/60R16 95H', '优科豪马 BluEarth AE50 蔚弛 215/60R16 95H', '215/60R16 95H', '轮胎规格:215/60R16 / 载重速度级别:95H',
       (SELECT id FROM base_unit WHERE code = 'UNIT000016'),
       353.00, 455.00, 569.00, 20.00, 300.00, 1, 'SPU000013 轮胎商品', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_material) x WHERE x.`code` = 'MAT000046');
INSERT INTO `base_material` (`category_id`, `spu_id`, `code`, `name`, `short_name`, `specification`, `sale_property_text`, `unit_id`, `purchase_price`, `sale_price`, `retail_price`, `min_stock`, `max_stock`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = 'MC000007'),
       (SELECT id FROM base_spu WHERE code = 'SPU000014'),
       'MAT000047', '优科豪马 ADVAN dB V551 205/55R16 91V', '优科豪马 ADVAN dB V551 205/55R16 91V', '205/55R16 91V', '轮胎规格:205/55R16 / 载重速度级别:91V',
       (SELECT id FROM base_unit WHERE code = 'UNIT000016'),
       328.00, 423.00, 529.00, 20.00, 300.00, 1, 'SPU000014 轮胎商品', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_material) x WHERE x.`code` = 'MAT000047');
INSERT INTO `base_material` (`category_id`, `spu_id`, `code`, `name`, `short_name`, `specification`, `sale_property_text`, `unit_id`, `purchase_price`, `sale_price`, `retail_price`, `min_stock`, `max_stock`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = 'MC000007'),
       (SELECT id FROM base_spu WHERE code = 'SPU000014'),
       'MAT000048', '优科豪马 ADVAN dB V551 215/55R17 94V', '优科豪马 ADVAN dB V551 215/55R17 94V', '215/55R17 94V', '轮胎规格:215/55R17 / 载重速度级别:94V',
       (SELECT id FROM base_unit WHERE code = 'UNIT000016'),
       452.00, 583.00, 729.00, 20.00, 300.00, 1, 'SPU000014 轮胎商品', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_material) x WHERE x.`code` = 'MAT000048');
INSERT INTO `base_material` (`category_id`, `spu_id`, `code`, `name`, `short_name`, `specification`, `sale_property_text`, `unit_id`, `purchase_price`, `sale_price`, `retail_price`, `min_stock`, `max_stock`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = 'MC000007'),
       (SELECT id FROM base_spu WHERE code = 'SPU000014'),
       'MAT000049', '优科豪马 ADVAN dB V551 225/45R17 91W', '优科豪马 ADVAN dB V551 225/45R17 91W', '225/45R17 91W', '轮胎规格:225/45R17 / 载重速度级别:91W',
       (SELECT id FROM base_unit WHERE code = 'UNIT000016'),
       489.00, 631.00, 789.00, 20.00, 300.00, 1, 'SPU000014 轮胎商品', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_material) x WHERE x.`code` = 'MAT000049');
INSERT INTO `base_material` (`category_id`, `spu_id`, `code`, `name`, `short_name`, `specification`, `sale_property_text`, `unit_id`, `purchase_price`, `sale_price`, `retail_price`, `min_stock`, `max_stock`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = 'MC000008'),
       (SELECT id FROM base_spu WHERE code = 'SPU000015'),
       'MAT000050', '优科豪马 GEOLANDAR X-CV 225/60R18 100H', '优科豪马 GEOLANDAR X-CV 225/60R18 100H', '225/60R18 100H', '轮胎规格:225/60R18 / 载重速度级别:100H',
       (SELECT id FROM base_unit WHERE code = 'UNIT000016'),
       657.00, 847.00, 1059.00, 20.00, 300.00, 1, 'SPU000015 轮胎商品', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_material) x WHERE x.`code` = 'MAT000050');
INSERT INTO `base_material` (`category_id`, `spu_id`, `code`, `name`, `short_name`, `specification`, `sale_property_text`, `unit_id`, `purchase_price`, `sale_price`, `retail_price`, `min_stock`, `max_stock`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = 'MC000008'),
       (SELECT id FROM base_spu WHERE code = 'SPU000015'),
       'MAT000051', '优科豪马 GEOLANDAR X-CV 235/55R19 101V', '优科豪马 GEOLANDAR X-CV 235/55R19 101V', '235/55R19 101V', '轮胎规格:235/55R19 / 载重速度级别:101V',
       (SELECT id FROM base_unit WHERE code = 'UNIT000016'),
       843.00, 1087.00, 1359.00, 20.00, 300.00, 1, 'SPU000015 轮胎商品', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_material) x WHERE x.`code` = 'MAT000051');
INSERT INTO `base_material` (`category_id`, `spu_id`, `code`, `name`, `short_name`, `specification`, `sale_property_text`, `unit_id`, `purchase_price`, `sale_price`, `retail_price`, `min_stock`, `max_stock`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = 'MC000008'),
       (SELECT id FROM base_spu WHERE code = 'SPU000015'),
       'MAT000052', '优科豪马 GEOLANDAR X-CV 245/45R20 103V', '优科豪马 GEOLANDAR X-CV 245/45R20 103V', '245/45R20 103V', '轮胎规格:245/45R20 / 载重速度级别:103V',
       (SELECT id FROM base_unit WHERE code = 'UNIT000016'),
       1047.00, 1351.00, 1689.00, 20.00, 300.00, 1, 'SPU000015 轮胎商品', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_material) x WHERE x.`code` = 'MAT000052');
INSERT INTO `base_material` (`category_id`, `spu_id`, `code`, `name`, `short_name`, `specification`, `sale_property_text`, `unit_id`, `purchase_price`, `sale_price`, `retail_price`, `min_stock`, `max_stock`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = 'MC000007'),
       (SELECT id FROM base_spu WHERE code = 'SPU000016'),
       'MAT000053', '韩泰 KINERGY ECO K435 傲特马 195/65R15 91H', '韩泰 KINERGY ECO K435 傲特马 195/65R15 91H', '195/65R15 91H', '轮胎规格:195/65R15 / 载重速度级别:91H',
       (SELECT id FROM base_unit WHERE code = 'UNIT000016'),
       229.00, 295.00, 369.00, 20.00, 300.00, 1, 'SPU000016 轮胎商品', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_material) x WHERE x.`code` = 'MAT000053');
INSERT INTO `base_material` (`category_id`, `spu_id`, `code`, `name`, `short_name`, `specification`, `sale_property_text`, `unit_id`, `purchase_price`, `sale_price`, `retail_price`, `min_stock`, `max_stock`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = 'MC000007'),
       (SELECT id FROM base_spu WHERE code = 'SPU000016'),
       'MAT000054', '韩泰 KINERGY ECO K435 傲特马 205/55R16 91V', '韩泰 KINERGY ECO K435 傲特马 205/55R16 91V', '205/55R16 91V', '轮胎规格:205/55R16 / 载重速度级别:91V',
       (SELECT id FROM base_unit WHERE code = 'UNIT000016'),
       266.00, 343.00, 429.00, 20.00, 300.00, 1, 'SPU000016 轮胎商品', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_material) x WHERE x.`code` = 'MAT000054');
INSERT INTO `base_material` (`category_id`, `spu_id`, `code`, `name`, `short_name`, `specification`, `sale_property_text`, `unit_id`, `purchase_price`, `sale_price`, `retail_price`, `min_stock`, `max_stock`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = 'MC000007'),
       (SELECT id FROM base_spu WHERE code = 'SPU000016'),
       'MAT000055', '韩泰 KINERGY ECO K435 傲特马 215/60R16 95H', '韩泰 KINERGY ECO K435 傲特马 215/60R16 95H', '215/60R16 95H', '轮胎规格:215/60R16 / 载重速度级别:95H',
       (SELECT id FROM base_unit WHERE code = 'UNIT000016'),
       328.00, 423.00, 529.00, 20.00, 300.00, 1, 'SPU000016 轮胎商品', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_material) x WHERE x.`code` = 'MAT000055');
INSERT INTO `base_material` (`category_id`, `spu_id`, `code`, `name`, `short_name`, `specification`, `sale_property_text`, `unit_id`, `purchase_price`, `sale_price`, `retail_price`, `min_stock`, `max_stock`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = 'MC000007'),
       (SELECT id FROM base_spu WHERE code = 'SPU000017'),
       'MAT000056', '韩泰 VENTUS S1 evo3 K127 万途仕 225/45R17 91W', '韩泰 VENTUS S1 evo3 K127 万途仕 225/45R17 91W', '225/45R17 91W', '轮胎规格:225/45R17 / 载重速度级别:91W',
       (SELECT id FROM base_unit WHERE code = 'UNIT000016'),
       464.00, 599.00, 749.00, 20.00, 300.00, 1, 'SPU000017 轮胎商品', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_material) x WHERE x.`code` = 'MAT000056');
INSERT INTO `base_material` (`category_id`, `spu_id`, `code`, `name`, `short_name`, `specification`, `sale_property_text`, `unit_id`, `purchase_price`, `sale_price`, `retail_price`, `min_stock`, `max_stock`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = 'MC000007'),
       (SELECT id FROM base_spu WHERE code = 'SPU000017'),
       'MAT000057', '韩泰 VENTUS S1 evo3 K127 万途仕 235/45R18 98Y', '韩泰 VENTUS S1 evo3 K127 万途仕 235/45R18 98Y', '235/45R18 98Y', '轮胎规格:235/45R18 / 载重速度级别:98Y',
       (SELECT id FROM base_unit WHERE code = 'UNIT000016'),
       681.00, 879.00, 1099.00, 20.00, 300.00, 1, 'SPU000017 轮胎商品', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_material) x WHERE x.`code` = 'MAT000057');
INSERT INTO `base_material` (`category_id`, `spu_id`, `code`, `name`, `short_name`, `specification`, `sale_property_text`, `unit_id`, `purchase_price`, `sale_price`, `retail_price`, `min_stock`, `max_stock`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = 'MC000007'),
       (SELECT id FROM base_spu WHERE code = 'SPU000017'),
       'MAT000058', '韩泰 VENTUS S1 evo3 K127 万途仕 245/40R19 98Y', '韩泰 VENTUS S1 evo3 K127 万途仕 245/40R19 98Y', '245/40R19 98Y', '轮胎规格:245/40R19 / 载重速度级别:98Y',
       (SELECT id FROM base_unit WHERE code = 'UNIT000016'),
       867.00, 1119.00, 1399.00, 20.00, 300.00, 1, 'SPU000017 轮胎商品', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_material) x WHERE x.`code` = 'MAT000058');
INSERT INTO `base_material` (`category_id`, `spu_id`, `code`, `name`, `short_name`, `specification`, `sale_property_text`, `unit_id`, `purchase_price`, `sale_price`, `retail_price`, `min_stock`, `max_stock`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = 'MC000008'),
       (SELECT id FROM base_spu WHERE code = 'SPU000018'),
       'MAT000059', '韩泰 DynaPro HP2 RA33 225/60R18 100H', '韩泰 DynaPro HP2 RA33 225/60R18 100H', '225/60R18 100H', '轮胎规格:225/60R18 / 载重速度级别:100H',
       (SELECT id FROM base_unit WHERE code = 'UNIT000016'),
       595.00, 767.00, 959.00, 20.00, 300.00, 1, 'SPU000018 轮胎商品', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_material) x WHERE x.`code` = 'MAT000059');
INSERT INTO `base_material` (`category_id`, `spu_id`, `code`, `name`, `short_name`, `specification`, `sale_property_text`, `unit_id`, `purchase_price`, `sale_price`, `retail_price`, `min_stock`, `max_stock`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = 'MC000008'),
       (SELECT id FROM base_spu WHERE code = 'SPU000018'),
       'MAT000060', '韩泰 DynaPro HP2 RA33 235/55R19 101V', '韩泰 DynaPro HP2 RA33 235/55R19 101V', '235/55R19 101V', '轮胎规格:235/55R19 / 载重速度级别:101V',
       (SELECT id FROM base_unit WHERE code = 'UNIT000016'),
       743.00, 959.00, 1199.00, 20.00, 300.00, 1, 'SPU000018 轮胎商品', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_material) x WHERE x.`code` = 'MAT000060');
INSERT INTO `base_material` (`category_id`, `spu_id`, `code`, `name`, `short_name`, `specification`, `sale_property_text`, `unit_id`, `purchase_price`, `sale_price`, `retail_price`, `min_stock`, `max_stock`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = 'MC000008'),
       (SELECT id FROM base_spu WHERE code = 'SPU000018'),
       'MAT000061', '韩泰 DynaPro HP2 RA33 235/60R18 103H', '韩泰 DynaPro HP2 RA33 235/60R18 103H', '235/60R18 103H', '轮胎规格:235/60R18 / 载重速度级别:103H',
       (SELECT id FROM base_unit WHERE code = 'UNIT000016'),
       657.00, 847.00, 1059.00, 20.00, 300.00, 1, 'SPU000018 轮胎商品', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_material) x WHERE x.`code` = 'MAT000061');
INSERT INTO `base_material` (`category_id`, `spu_id`, `code`, `name`, `short_name`, `specification`, `sale_property_text`, `unit_id`, `purchase_price`, `sale_price`, `retail_price`, `min_stock`, `max_stock`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = 'MC000007'),
       (SELECT id FROM base_spu WHERE code = 'SPU000019'),
       'MAT000062', '美国固铂 ZEON CS8 泽昂 205/55R16 91V', '美国固铂 ZEON CS8 泽昂 205/55R16 91V', '205/55R16 91V', '轮胎规格:205/55R16 / 载重速度级别:91V',
       (SELECT id FROM base_unit WHERE code = 'UNIT000016'),
       272.00, 351.00, 439.00, 20.00, 300.00, 1, 'SPU000019 轮胎商品', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_material) x WHERE x.`code` = 'MAT000062');
INSERT INTO `base_material` (`category_id`, `spu_id`, `code`, `name`, `short_name`, `specification`, `sale_property_text`, `unit_id`, `purchase_price`, `sale_price`, `retail_price`, `min_stock`, `max_stock`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = 'MC000007'),
       (SELECT id FROM base_spu WHERE code = 'SPU000019'),
       'MAT000063', '美国固铂 ZEON CS8 泽昂 215/60R16 95H', '美国固铂 ZEON CS8 泽昂 215/60R16 95H', '215/60R16 95H', '轮胎规格:215/60R16 / 载重速度级别:95H',
       (SELECT id FROM base_unit WHERE code = 'UNIT000016'),
       340.00, 439.00, 549.00, 20.00, 300.00, 1, 'SPU000019 轮胎商品', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_material) x WHERE x.`code` = 'MAT000063');
INSERT INTO `base_material` (`category_id`, `spu_id`, `code`, `name`, `short_name`, `specification`, `sale_property_text`, `unit_id`, `purchase_price`, `sale_price`, `retail_price`, `min_stock`, `max_stock`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = 'MC000007'),
       (SELECT id FROM base_spu WHERE code = 'SPU000019'),
       'MAT000064', '美国固铂 ZEON CS8 泽昂 225/45R17 91W', '美国固铂 ZEON CS8 泽昂 225/45R17 91W', '225/45R17 91W', '轮胎规格:225/45R17 / 载重速度级别:91W',
       (SELECT id FROM base_unit WHERE code = 'UNIT000016'),
       433.00, 559.00, 699.00, 20.00, 300.00, 1, 'SPU000019 轮胎商品', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_material) x WHERE x.`code` = 'MAT000064');
INSERT INTO `base_material` (`category_id`, `spu_id`, `code`, `name`, `short_name`, `specification`, `sale_property_text`, `unit_id`, `purchase_price`, `sale_price`, `retail_price`, `min_stock`, `max_stock`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = 'MC000007'),
       (SELECT id FROM base_spu WHERE code = 'SPU000020'),
       'MAT000065', '美国固铂 EVOLUTION CTT 纵横者 205/55R16 91W', '美国固铂 EVOLUTION CTT 纵横者 205/55R16 91W', '205/55R16 91W', '轮胎规格:205/55R16 / 载重速度级别:91W',
       (SELECT id FROM base_unit WHERE code = 'UNIT000016'),
       291.00, 375.00, 469.00, 20.00, 300.00, 1, 'SPU000020 轮胎商品', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_material) x WHERE x.`code` = 'MAT000065');
INSERT INTO `base_material` (`category_id`, `spu_id`, `code`, `name`, `short_name`, `specification`, `sale_property_text`, `unit_id`, `purchase_price`, `sale_price`, `retail_price`, `min_stock`, `max_stock`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = 'MC000007'),
       (SELECT id FROM base_spu WHERE code = 'SPU000020'),
       'MAT000066', '美国固铂 EVOLUTION CTT 纵横者 225/45R17 94W', '美国固铂 EVOLUTION CTT 纵横者 225/45R17 94W', '225/45R17 94W', '轮胎规格:225/45R17 / 载重速度级别:94W',
       (SELECT id FROM base_unit WHERE code = 'UNIT000016'),
       452.00, 583.00, 729.00, 20.00, 300.00, 1, 'SPU000020 轮胎商品', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_material) x WHERE x.`code` = 'MAT000066');
INSERT INTO `base_material` (`category_id`, `spu_id`, `code`, `name`, `short_name`, `specification`, `sale_property_text`, `unit_id`, `purchase_price`, `sale_price`, `retail_price`, `min_stock`, `max_stock`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = 'MC000007'),
       (SELECT id FROM base_spu WHERE code = 'SPU000020'),
       'MAT000067', '美国固铂 EVOLUTION CTT 纵横者 235/45R18 98Y', '美国固铂 EVOLUTION CTT 纵横者 235/45R18 98Y', '235/45R18 98Y', '轮胎规格:235/45R18 / 载重速度级别:98Y',
       (SELECT id FROM base_unit WHERE code = 'UNIT000016'),
       619.00, 799.00, 999.00, 20.00, 300.00, 1, 'SPU000020 轮胎商品', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_material) x WHERE x.`code` = 'MAT000067');
INSERT INTO `base_material` (`category_id`, `spu_id`, `code`, `name`, `short_name`, `specification`, `sale_property_text`, `unit_id`, `purchase_price`, `sale_price`, `retail_price`, `min_stock`, `max_stock`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = 'MC000008'),
       (SELECT id FROM base_spu WHERE code = 'SPU000021'),
       'MAT000068', '美国固铂 DISCOVERER HTS 235/60R18 103H', '美国固铂 DISCOVERER HTS 235/60R18 103H', '235/60R18 103H', '轮胎规格:235/60R18 / 载重速度级别:103H',
       (SELECT id FROM base_unit WHERE code = 'UNIT000016'),
       557.00, 719.00, 899.00, 20.00, 300.00, 1, 'SPU000021 轮胎商品', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_material) x WHERE x.`code` = 'MAT000068');
INSERT INTO `base_material` (`category_id`, `spu_id`, `code`, `name`, `short_name`, `specification`, `sale_property_text`, `unit_id`, `purchase_price`, `sale_price`, `retail_price`, `min_stock`, `max_stock`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = 'MC000008'),
       (SELECT id FROM base_spu WHERE code = 'SPU000021'),
       'MAT000069', '美国固铂 DISCOVERER HTS 255/55R19 111H', '美国固铂 DISCOVERER HTS 255/55R19 111H', '255/55R19 111H', '轮胎规格:255/55R19 / 载重速度级别:111H',
       (SELECT id FROM base_unit WHERE code = 'UNIT000016'),
       719.00, 927.00, 1159.00, 20.00, 300.00, 1, 'SPU000021 轮胎商品', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_material) x WHERE x.`code` = 'MAT000069');
INSERT INTO `base_material` (`category_id`, `spu_id`, `code`, `name`, `short_name`, `specification`, `sale_property_text`, `unit_id`, `purchase_price`, `sale_price`, `retail_price`, `min_stock`, `max_stock`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = 'MC000007'),
       (SELECT id FROM base_spu WHERE code = 'SPU000022'),
       'MAT000070', '沃凯途 静音舒适系列 VK5 195/65R15 91H', '沃凯途 静音舒适系列 VK5 195/65R15 91H', '195/65R15 91H', '轮胎规格:195/65R15 / 载重速度级别:91H',
       (SELECT id FROM base_unit WHERE code = 'UNIT000016'),
       204.00, 263.00, 329.00, 20.00, 300.00, 1, 'SPU000022 轮胎商品', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_material) x WHERE x.`code` = 'MAT000070');
INSERT INTO `base_material` (`category_id`, `spu_id`, `code`, `name`, `short_name`, `specification`, `sale_property_text`, `unit_id`, `purchase_price`, `sale_price`, `retail_price`, `min_stock`, `max_stock`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = 'MC000007'),
       (SELECT id FROM base_spu WHERE code = 'SPU000022'),
       'MAT000071', '沃凯途 静音舒适系列 VK5 205/55R16 91V', '沃凯途 静音舒适系列 VK5 205/55R16 91V', '205/55R16 91V', '轮胎规格:205/55R16 / 载重速度级别:91V',
       (SELECT id FROM base_unit WHERE code = 'UNIT000016'),
       235.00, 303.00, 379.00, 20.00, 300.00, 1, 'SPU000022 轮胎商品', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_material) x WHERE x.`code` = 'MAT000071');
INSERT INTO `base_material` (`category_id`, `spu_id`, `code`, `name`, `short_name`, `specification`, `sale_property_text`, `unit_id`, `purchase_price`, `sale_price`, `retail_price`, `min_stock`, `max_stock`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = 'MC000007'),
       (SELECT id FROM base_spu WHERE code = 'SPU000022'),
       'MAT000072', '沃凯途 静音舒适系列 VK5 215/55R17 94V', '沃凯途 静音舒适系列 VK5 215/55R17 94V', '215/55R17 94V', '轮胎规格:215/55R17 / 载重速度级别:94V',
       (SELECT id FROM base_unit WHERE code = 'UNIT000016'),
       309.00, 399.00, 499.00, 20.00, 300.00, 1, 'SPU000022 轮胎商品', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_material) x WHERE x.`code` = 'MAT000072');
INSERT INTO `base_material` (`category_id`, `spu_id`, `code`, `name`, `short_name`, `specification`, `sale_property_text`, `unit_id`, `purchase_price`, `sale_price`, `retail_price`, `min_stock`, `max_stock`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = 'MC000008'),
       (SELECT id FROM base_spu WHERE code = 'SPU000023'),
       'MAT000073', '沃凯途 城市SUV系列 VK8 225/60R18 100H', '沃凯途 城市SUV系列 VK8 225/60R18 100H', '225/60R18 100H', '轮胎规格:225/60R18 / 载重速度级别:100H',
       (SELECT id FROM base_unit WHERE code = 'UNIT000016'),
       409.00, 527.00, 659.00, 20.00, 300.00, 1, 'SPU000023 轮胎商品', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_material) x WHERE x.`code` = 'MAT000073');
INSERT INTO `base_material` (`category_id`, `spu_id`, `code`, `name`, `short_name`, `specification`, `sale_property_text`, `unit_id`, `purchase_price`, `sale_price`, `retail_price`, `min_stock`, `max_stock`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = 'MC000008'),
       (SELECT id FROM base_spu WHERE code = 'SPU000023'),
       'MAT000074', '沃凯途 城市SUV系列 VK8 235/55R19 101V', '沃凯途 城市SUV系列 VK8 235/55R19 101V', '235/55R19 101V', '轮胎规格:235/55R19 / 载重速度级别:101V',
       (SELECT id FROM base_unit WHERE code = 'UNIT000016'),
       495.00, 639.00, 799.00, 20.00, 300.00, 1, 'SPU000023 轮胎商品', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_material) x WHERE x.`code` = 'MAT000074');
INSERT INTO `base_material` (`category_id`, `spu_id`, `code`, `name`, `short_name`, `specification`, `sale_property_text`, `unit_id`, `purchase_price`, `sale_price`, `retail_price`, `min_stock`, `max_stock`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = 'MC000007'),
       (SELECT id FROM base_spu WHERE code = 'SPU000024'),
       'MAT000075', '赛轮路极 ROADX DH12 175/65R15 88H', '赛轮路极 ROADX DH12 175/65R15 88H', '175/65R15 88H', '轮胎规格:175/65R15 / 载重速度级别:88H',
       (SELECT id FROM base_unit WHERE code = 'UNIT000016'),
       235.00, 303.00, 379.00, 20.00, 300.00, 1, 'SPU000024 轮胎商品', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_material) x WHERE x.`code` = 'MAT000075');
INSERT INTO `base_material` (`category_id`, `spu_id`, `code`, `name`, `short_name`, `specification`, `sale_property_text`, `unit_id`, `purchase_price`, `sale_price`, `retail_price`, `min_stock`, `max_stock`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = 'MC000007'),
       (SELECT id FROM base_spu WHERE code = 'SPU000024'),
       'MAT000076', '赛轮路极 ROADX DH12 195/65R15 91H', '赛轮路极 ROADX DH12 195/65R15 91H', '195/65R15 91H', '轮胎规格:195/65R15 / 载重速度级别:91H',
       (SELECT id FROM base_unit WHERE code = 'UNIT000016'),
       266.00, 343.00, 429.00, 20.00, 300.00, 1, 'SPU000024 轮胎商品', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_material) x WHERE x.`code` = 'MAT000076');
INSERT INTO `base_material` (`category_id`, `spu_id`, `code`, `name`, `short_name`, `specification`, `sale_property_text`, `unit_id`, `purchase_price`, `sale_price`, `retail_price`, `min_stock`, `max_stock`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = 'MC000007'),
       (SELECT id FROM base_spu WHERE code = 'SPU000024'),
       'MAT000077', '赛轮路极 ROADX DH12 205/55R16 91V', '赛轮路极 ROADX DH12 205/55R16 91V', '205/55R16 91V', '轮胎规格:205/55R16 / 载重速度级别:91V',
       (SELECT id FROM base_unit WHERE code = 'UNIT000016'),
       303.00, 391.00, 489.00, 20.00, 300.00, 1, 'SPU000024 轮胎商品', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_material) x WHERE x.`code` = 'MAT000077');
INSERT INTO `base_material` (`category_id`, `spu_id`, `code`, `name`, `short_name`, `specification`, `sale_property_text`, `unit_id`, `purchase_price`, `sale_price`, `retail_price`, `min_stock`, `max_stock`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = 'MC000007'),
       (SELECT id FROM base_spu WHERE code = 'SPU000025'),
       'MAT000078', '赛轮路极 ROADX DT21 营运胎 205/55R16 91V', '赛轮路极 ROADX DT21 营运胎 205/55R16 91V', '205/55R16 91V', '轮胎规格:205/55R16 / 载重速度级别:91V',
       (SELECT id FROM base_unit WHERE code = 'UNIT000016'),
       272.00, 351.00, 439.00, 20.00, 300.00, 1, 'SPU000025 轮胎商品', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_material) x WHERE x.`code` = 'MAT000078');
INSERT INTO `base_material` (`category_id`, `spu_id`, `code`, `name`, `short_name`, `specification`, `sale_property_text`, `unit_id`, `purchase_price`, `sale_price`, `retail_price`, `min_stock`, `max_stock`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = 'MC000007'),
       (SELECT id FROM base_spu WHERE code = 'SPU000025'),
       'MAT000079', '赛轮路极 ROADX DT21 营运胎 215/55R18 99V', '赛轮路极 ROADX DT21 营运胎 215/55R18 99V', '215/55R18 99V', '轮胎规格:215/55R18 / 载重速度级别:99V',
       (SELECT id FROM base_unit WHERE code = 'UNIT000016'),
       303.00, 391.00, 489.00, 20.00, 300.00, 1, 'SPU000025 轮胎商品', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_material) x WHERE x.`code` = 'MAT000079');
INSERT INTO `base_material` (`category_id`, `spu_id`, `code`, `name`, `short_name`, `specification`, `sale_property_text`, `unit_id`, `purchase_price`, `sale_price`, `retail_price`, `min_stock`, `max_stock`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = 'MC000008'),
       (SELECT id FROM base_spu WHERE code = 'SPU000026'),
       'MAT000080', '赛轮路极 ROADX DSU01 225/60R18 100H', '赛轮路极 ROADX DSU01 225/60R18 100H', '225/60R18 100H', '轮胎规格:225/60R18 / 载重速度级别:100H',
       (SELECT id FROM base_unit WHERE code = 'UNIT000016'),
       433.00, 559.00, 699.00, 20.00, 300.00, 1, 'SPU000026 轮胎商品', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_material) x WHERE x.`code` = 'MAT000080');
INSERT INTO `base_material` (`category_id`, `spu_id`, `code`, `name`, `short_name`, `specification`, `sale_property_text`, `unit_id`, `purchase_price`, `sale_price`, `retail_price`, `min_stock`, `max_stock`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = 'MC000008'),
       (SELECT id FROM base_spu WHERE code = 'SPU000026'),
       'MAT000081', '赛轮路极 ROADX DSU01 255/60R18 112V', '赛轮路极 ROADX DSU01 255/60R18 112V', '255/60R18 112V', '轮胎规格:255/60R18 / 载重速度级别:112V',
       (SELECT id FROM base_unit WHERE code = 'UNIT000016'),
       508.00, 655.00, 819.00, 20.00, 300.00, 1, 'SPU000026 轮胎商品', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_material) x WHERE x.`code` = 'MAT000081');
INSERT INTO `base_material` (`category_id`, `spu_id`, `code`, `name`, `short_name`, `specification`, `sale_property_text`, `unit_id`, `purchase_price`, `sale_price`, `retail_price`, `min_stock`, `max_stock`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = 'MC000007'),
       (SELECT id FROM base_spu WHERE code = 'SPU000027'),
       'MAT000082', '达迈 GLEDE 静音系列 195/65R15 91H', '达迈 GLEDE 静音系列 195/65R15 91H', '195/65R15 91H', '轮胎规格:195/65R15 / 载重速度级别:91H',
       (SELECT id FROM base_unit WHERE code = 'UNIT000016'),
       223.00, 287.00, 359.00, 20.00, 300.00, 1, 'SPU000027 轮胎商品', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_material) x WHERE x.`code` = 'MAT000082');
INSERT INTO `base_material` (`category_id`, `spu_id`, `code`, `name`, `short_name`, `specification`, `sale_property_text`, `unit_id`, `purchase_price`, `sale_price`, `retail_price`, `min_stock`, `max_stock`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = 'MC000007'),
       (SELECT id FROM base_spu WHERE code = 'SPU000027'),
       'MAT000083', '达迈 GLEDE 静音系列 205/55R16 91V', '达迈 GLEDE 静音系列 205/55R16 91V', '205/55R16 91V', '轮胎规格:205/55R16 / 载重速度级别:91V',
       (SELECT id FROM base_unit WHERE code = 'UNIT000016'),
       260.00, 335.00, 419.00, 20.00, 300.00, 1, 'SPU000027 轮胎商品', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_material) x WHERE x.`code` = 'MAT000083');
INSERT INTO `base_material` (`category_id`, `spu_id`, `code`, `name`, `short_name`, `specification`, `sale_property_text`, `unit_id`, `purchase_price`, `sale_price`, `retail_price`, `min_stock`, `max_stock`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = 'MC000007'),
       (SELECT id FROM base_spu WHERE code = 'SPU000027'),
       'MAT000084', '达迈 GLEDE 静音系列 215/60R17 96V', '达迈 GLEDE 静音系列 215/60R17 96V', '215/60R17 96V', '轮胎规格:215/60R17 / 载重速度级别:96V',
       (SELECT id FROM base_unit WHERE code = 'UNIT000016'),
       322.00, 415.00, 519.00, 20.00, 300.00, 1, 'SPU000027 轮胎商品', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_material) x WHERE x.`code` = 'MAT000084');
INSERT INTO `base_material` (`category_id`, `spu_id`, `code`, `name`, `short_name`, `specification`, `sale_property_text`, `unit_id`, `purchase_price`, `sale_price`, `retail_price`, `min_stock`, `max_stock`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = 'MC000008'),
       (SELECT id FROM base_spu WHERE code = 'SPU000028'),
       'MAT000085', '达迈 宝石 耐磨系列 225/60R18 100H', '达迈 宝石 耐磨系列 225/60R18 100H', '225/60R18 100H', '轮胎规格:225/60R18 / 载重速度级别:100H',
       (SELECT id FROM base_unit WHERE code = 'UNIT000016'),
       390.00, 503.00, 629.00, 20.00, 300.00, 1, 'SPU000028 轮胎商品', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_material) x WHERE x.`code` = 'MAT000085');
INSERT INTO `base_material` (`category_id`, `spu_id`, `code`, `name`, `short_name`, `specification`, `sale_property_text`, `unit_id`, `purchase_price`, `sale_price`, `retail_price`, `min_stock`, `max_stock`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = 'MC000008'),
       (SELECT id FROM base_spu WHERE code = 'SPU000028'),
       'MAT000086', '达迈 宝石 耐磨系列 235/55R19 101V', '达迈 宝石 耐磨系列 235/55R19 101V', '235/55R19 101V', '轮胎规格:235/55R19 / 载重速度级别:101V',
       (SELECT id FROM base_unit WHERE code = 'UNIT000016'),
       471.00, 607.00, 759.00, 20.00, 300.00, 1, 'SPU000028 轮胎商品', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_material) x WHERE x.`code` = 'MAT000086');

-- -----------------------------------------------------------------------------
-- 8. SKU 销售属性绑定（base_material_property_rel）
-- -----------------------------------------------------------------------------
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000024'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000005'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000005') AND name = '195/65R15'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000024')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000024'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000006'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000006') AND name = '91H'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000024')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000025'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000005'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000005') AND name = '205/55R16'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000025')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000025'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000006'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000006') AND name = '91V'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000025')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000026'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000005'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000005') AND name = '215/60R16'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000026')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000026'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000006'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000006') AND name = '95V'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000026')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000027'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000005'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000005') AND name = '225/50R17'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000027')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000027'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000006'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000006') AND name = '94V'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000027')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000028'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000005'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000005') AND name = '205/55R16'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000028')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000028'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000006'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000006') AND name = '91W'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000028')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000029'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000005'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000005') AND name = '225/45R17'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000029')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000029'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000006'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000006') AND name = '94W'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000029')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000030'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000005'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000005') AND name = '225/40R18'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000030')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000030'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000006'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000006') AND name = '92Y'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000030')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000031'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000005'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000005') AND name = '225/60R18'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000031')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000031'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000006'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000006') AND name = '100H'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000031')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000032'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000005'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000005') AND name = '235/55R19'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000032')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000032'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000006'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000006') AND name = '101V'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000032')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000033'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000005'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000005') AND name = '245/45R20'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000033')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000033'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000006'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000006') AND name = '103V'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000033')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000034'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000005'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000005') AND name = '195/65R15'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000034')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000034'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000006'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000006') AND name = '91H'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000034')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000035'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000005'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000005') AND name = '205/55R16'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000035')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000035'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000006'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000006') AND name = '91V'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000035')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000036'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000005'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000005') AND name = '215/60R16'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000036')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000036'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000006'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000006') AND name = '95V'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000036')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000037'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000005'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000005') AND name = '225/45R17'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000037')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000037'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000006'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000006') AND name = '91W'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000037')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000038'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000005'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000005') AND name = '195/65R15'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000038')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000038'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000006'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000006') AND name = '91H'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000038')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000039'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000005'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000005') AND name = '205/55R16'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000039')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000039'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000006'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000006') AND name = '91V'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000039')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000040'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000005'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000005') AND name = '215/60R16'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000040')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000040'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000006'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000006') AND name = '95H'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000040')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000041'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000005'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000005') AND name = '225/60R18'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000041')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000041'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000006'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000006') AND name = '100H'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000041')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000042'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000005'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000005') AND name = '235/55R19'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000042')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000042'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000006'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000006') AND name = '101V'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000042')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000043'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000005'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000005') AND name = '235/60R18'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000043')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000043'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000006'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000006') AND name = '103H'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000043')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000044'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000005'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000005') AND name = '195/65R15'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000044')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000044'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000006'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000006') AND name = '91H'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000044')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000045'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000005'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000005') AND name = '205/55R16'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000045')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000045'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000006'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000006') AND name = '91V'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000045')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000046'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000005'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000005') AND name = '215/60R16'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000046')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000046'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000006'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000006') AND name = '95H'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000046')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000047'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000005'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000005') AND name = '205/55R16'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000047')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000047'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000006'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000006') AND name = '91V'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000047')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000048'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000005'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000005') AND name = '215/55R17'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000048')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000048'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000006'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000006') AND name = '94V'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000048')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000049'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000005'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000005') AND name = '225/45R17'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000049')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000049'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000006'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000006') AND name = '91W'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000049')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000050'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000005'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000005') AND name = '225/60R18'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000050')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000050'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000006'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000006') AND name = '100H'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000050')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000051'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000005'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000005') AND name = '235/55R19'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000051')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000051'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000006'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000006') AND name = '101V'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000051')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000052'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000005'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000005') AND name = '245/45R20'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000052')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000052'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000006'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000006') AND name = '103V'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000052')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000053'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000005'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000005') AND name = '195/65R15'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000053')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000053'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000006'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000006') AND name = '91H'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000053')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000054'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000005'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000005') AND name = '205/55R16'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000054')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000054'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000006'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000006') AND name = '91V'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000054')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000055'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000005'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000005') AND name = '215/60R16'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000055')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000055'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000006'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000006') AND name = '95H'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000055')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000056'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000005'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000005') AND name = '225/45R17'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000056')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000056'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000006'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000006') AND name = '91W'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000056')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000057'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000005'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000005') AND name = '235/45R18'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000057')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000057'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000006'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000006') AND name = '98Y'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000057')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000058'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000005'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000005') AND name = '245/40R19'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000058')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000058'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000006'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000006') AND name = '98Y'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000058')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000059'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000005'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000005') AND name = '225/60R18'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000059')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000059'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000006'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000006') AND name = '100H'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000059')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000060'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000005'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000005') AND name = '235/55R19'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000060')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000060'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000006'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000006') AND name = '101V'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000060')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000061'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000005'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000005') AND name = '235/60R18'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000061')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000061'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000006'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000006') AND name = '103H'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000061')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000062'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000005'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000005') AND name = '205/55R16'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000062')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000062'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000006'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000006') AND name = '91V'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000062')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000063'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000005'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000005') AND name = '215/60R16'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000063')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000063'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000006'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000006') AND name = '95H'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000063')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000064'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000005'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000005') AND name = '225/45R17'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000064')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000064'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000006'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000006') AND name = '91W'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000064')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000065'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000005'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000005') AND name = '205/55R16'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000065')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000065'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000006'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000006') AND name = '91W'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000065')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000066'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000005'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000005') AND name = '225/45R17'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000066')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000066'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000006'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000006') AND name = '94W'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000066')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000067'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000005'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000005') AND name = '235/45R18'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000067')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000067'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000006'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000006') AND name = '98Y'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000067')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000068'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000005'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000005') AND name = '235/60R18'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000068')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000068'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000006'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000006') AND name = '103H'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000068')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000069'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000005'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000005') AND name = '255/55R19'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000069')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000069'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000006'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000006') AND name = '111H'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000069')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000070'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000005'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000005') AND name = '195/65R15'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000070')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000070'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000006'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000006') AND name = '91H'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000070')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000071'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000005'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000005') AND name = '205/55R16'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000071')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000071'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000006'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000006') AND name = '91V'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000071')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000072'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000005'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000005') AND name = '215/55R17'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000072')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000072'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000006'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000006') AND name = '94V'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000072')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000073'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000005'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000005') AND name = '225/60R18'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000073')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000073'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000006'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000006') AND name = '100H'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000073')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000074'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000005'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000005') AND name = '235/55R19'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000074')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000074'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000006'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000006') AND name = '101V'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000074')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000075'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000005'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000005') AND name = '175/65R15'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000075')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000075'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000006'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000006') AND name = '88H'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000075')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000076'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000005'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000005') AND name = '195/65R15'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000076')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000076'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000006'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000006') AND name = '91H'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000076')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000077'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000005'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000005') AND name = '205/55R16'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000077')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000077'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000006'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000006') AND name = '91V'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000077')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000078'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000005'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000005') AND name = '205/55R16'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000078')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000078'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000006'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000006') AND name = '91V'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000078')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000079'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000005'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000005') AND name = '215/55R18'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000079')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000079'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000006'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000006') AND name = '99V'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000079')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000080'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000005'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000005') AND name = '225/60R18'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000080')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000080'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000006'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000006') AND name = '100H'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000080')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000081'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000005'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000005') AND name = '255/60R18'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000081')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000081'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000006'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000006') AND name = '112V'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000081')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000082'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000005'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000005') AND name = '195/65R15'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000082')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000082'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000006'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000006') AND name = '91H'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000082')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000083'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000005'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000005') AND name = '205/55R16'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000083')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000083'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000006'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000006') AND name = '91V'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000083')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000084'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000005'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000005') AND name = '215/60R17'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000084')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000084'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000006'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000006') AND name = '96V'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000084')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000085'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000005'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000005') AND name = '225/60R18'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000085')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000085'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000006'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000006') AND name = '100H'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000085')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000086'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000005'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000005') AND name = '235/55R19'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000086')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000005'));
INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000086'),
       (SELECT id FROM base_sale_property WHERE code = 'SP000006'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = 'SP000006') AND name = '101V'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000086')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = 'SP000006'));

-- -----------------------------------------------------------------------------
-- 9. 一品多码：为每条 SKU 生成主编号
-- -----------------------------------------------------------------------------
INSERT INTO `base_material_code` (`material_id`, `code`, `code_type`, `is_main`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000024'), 'MAT000024', 1, 1, '轮胎初始化生成', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_code c WHERE c.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000024') AND c.`code` = 'MAT000024' AND c.`code_type` = 1);
INSERT INTO `base_material_code` (`material_id`, `code`, `code_type`, `is_main`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000025'), 'MAT000025', 1, 1, '轮胎初始化生成', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_code c WHERE c.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000025') AND c.`code` = 'MAT000025' AND c.`code_type` = 1);
INSERT INTO `base_material_code` (`material_id`, `code`, `code_type`, `is_main`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000026'), 'MAT000026', 1, 1, '轮胎初始化生成', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_code c WHERE c.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000026') AND c.`code` = 'MAT000026' AND c.`code_type` = 1);
INSERT INTO `base_material_code` (`material_id`, `code`, `code_type`, `is_main`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000027'), 'MAT000027', 1, 1, '轮胎初始化生成', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_code c WHERE c.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000027') AND c.`code` = 'MAT000027' AND c.`code_type` = 1);
INSERT INTO `base_material_code` (`material_id`, `code`, `code_type`, `is_main`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000028'), 'MAT000028', 1, 1, '轮胎初始化生成', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_code c WHERE c.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000028') AND c.`code` = 'MAT000028' AND c.`code_type` = 1);
INSERT INTO `base_material_code` (`material_id`, `code`, `code_type`, `is_main`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000029'), 'MAT000029', 1, 1, '轮胎初始化生成', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_code c WHERE c.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000029') AND c.`code` = 'MAT000029' AND c.`code_type` = 1);
INSERT INTO `base_material_code` (`material_id`, `code`, `code_type`, `is_main`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000030'), 'MAT000030', 1, 1, '轮胎初始化生成', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_code c WHERE c.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000030') AND c.`code` = 'MAT000030' AND c.`code_type` = 1);
INSERT INTO `base_material_code` (`material_id`, `code`, `code_type`, `is_main`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000031'), 'MAT000031', 1, 1, '轮胎初始化生成', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_code c WHERE c.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000031') AND c.`code` = 'MAT000031' AND c.`code_type` = 1);
INSERT INTO `base_material_code` (`material_id`, `code`, `code_type`, `is_main`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000032'), 'MAT000032', 1, 1, '轮胎初始化生成', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_code c WHERE c.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000032') AND c.`code` = 'MAT000032' AND c.`code_type` = 1);
INSERT INTO `base_material_code` (`material_id`, `code`, `code_type`, `is_main`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000033'), 'MAT000033', 1, 1, '轮胎初始化生成', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_code c WHERE c.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000033') AND c.`code` = 'MAT000033' AND c.`code_type` = 1);
INSERT INTO `base_material_code` (`material_id`, `code`, `code_type`, `is_main`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000034'), 'MAT000034', 1, 1, '轮胎初始化生成', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_code c WHERE c.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000034') AND c.`code` = 'MAT000034' AND c.`code_type` = 1);
INSERT INTO `base_material_code` (`material_id`, `code`, `code_type`, `is_main`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000035'), 'MAT000035', 1, 1, '轮胎初始化生成', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_code c WHERE c.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000035') AND c.`code` = 'MAT000035' AND c.`code_type` = 1);
INSERT INTO `base_material_code` (`material_id`, `code`, `code_type`, `is_main`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000036'), 'MAT000036', 1, 1, '轮胎初始化生成', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_code c WHERE c.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000036') AND c.`code` = 'MAT000036' AND c.`code_type` = 1);
INSERT INTO `base_material_code` (`material_id`, `code`, `code_type`, `is_main`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000037'), 'MAT000037', 1, 1, '轮胎初始化生成', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_code c WHERE c.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000037') AND c.`code` = 'MAT000037' AND c.`code_type` = 1);
INSERT INTO `base_material_code` (`material_id`, `code`, `code_type`, `is_main`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000038'), 'MAT000038', 1, 1, '轮胎初始化生成', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_code c WHERE c.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000038') AND c.`code` = 'MAT000038' AND c.`code_type` = 1);
INSERT INTO `base_material_code` (`material_id`, `code`, `code_type`, `is_main`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000039'), 'MAT000039', 1, 1, '轮胎初始化生成', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_code c WHERE c.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000039') AND c.`code` = 'MAT000039' AND c.`code_type` = 1);
INSERT INTO `base_material_code` (`material_id`, `code`, `code_type`, `is_main`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000040'), 'MAT000040', 1, 1, '轮胎初始化生成', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_code c WHERE c.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000040') AND c.`code` = 'MAT000040' AND c.`code_type` = 1);
INSERT INTO `base_material_code` (`material_id`, `code`, `code_type`, `is_main`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000041'), 'MAT000041', 1, 1, '轮胎初始化生成', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_code c WHERE c.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000041') AND c.`code` = 'MAT000041' AND c.`code_type` = 1);
INSERT INTO `base_material_code` (`material_id`, `code`, `code_type`, `is_main`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000042'), 'MAT000042', 1, 1, '轮胎初始化生成', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_code c WHERE c.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000042') AND c.`code` = 'MAT000042' AND c.`code_type` = 1);
INSERT INTO `base_material_code` (`material_id`, `code`, `code_type`, `is_main`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000043'), 'MAT000043', 1, 1, '轮胎初始化生成', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_code c WHERE c.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000043') AND c.`code` = 'MAT000043' AND c.`code_type` = 1);
INSERT INTO `base_material_code` (`material_id`, `code`, `code_type`, `is_main`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000044'), 'MAT000044', 1, 1, '轮胎初始化生成', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_code c WHERE c.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000044') AND c.`code` = 'MAT000044' AND c.`code_type` = 1);
INSERT INTO `base_material_code` (`material_id`, `code`, `code_type`, `is_main`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000045'), 'MAT000045', 1, 1, '轮胎初始化生成', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_code c WHERE c.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000045') AND c.`code` = 'MAT000045' AND c.`code_type` = 1);
INSERT INTO `base_material_code` (`material_id`, `code`, `code_type`, `is_main`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000046'), 'MAT000046', 1, 1, '轮胎初始化生成', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_code c WHERE c.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000046') AND c.`code` = 'MAT000046' AND c.`code_type` = 1);
INSERT INTO `base_material_code` (`material_id`, `code`, `code_type`, `is_main`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000047'), 'MAT000047', 1, 1, '轮胎初始化生成', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_code c WHERE c.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000047') AND c.`code` = 'MAT000047' AND c.`code_type` = 1);
INSERT INTO `base_material_code` (`material_id`, `code`, `code_type`, `is_main`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000048'), 'MAT000048', 1, 1, '轮胎初始化生成', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_code c WHERE c.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000048') AND c.`code` = 'MAT000048' AND c.`code_type` = 1);
INSERT INTO `base_material_code` (`material_id`, `code`, `code_type`, `is_main`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000049'), 'MAT000049', 1, 1, '轮胎初始化生成', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_code c WHERE c.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000049') AND c.`code` = 'MAT000049' AND c.`code_type` = 1);
INSERT INTO `base_material_code` (`material_id`, `code`, `code_type`, `is_main`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000050'), 'MAT000050', 1, 1, '轮胎初始化生成', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_code c WHERE c.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000050') AND c.`code` = 'MAT000050' AND c.`code_type` = 1);
INSERT INTO `base_material_code` (`material_id`, `code`, `code_type`, `is_main`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000051'), 'MAT000051', 1, 1, '轮胎初始化生成', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_code c WHERE c.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000051') AND c.`code` = 'MAT000051' AND c.`code_type` = 1);
INSERT INTO `base_material_code` (`material_id`, `code`, `code_type`, `is_main`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000052'), 'MAT000052', 1, 1, '轮胎初始化生成', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_code c WHERE c.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000052') AND c.`code` = 'MAT000052' AND c.`code_type` = 1);
INSERT INTO `base_material_code` (`material_id`, `code`, `code_type`, `is_main`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000053'), 'MAT000053', 1, 1, '轮胎初始化生成', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_code c WHERE c.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000053') AND c.`code` = 'MAT000053' AND c.`code_type` = 1);
INSERT INTO `base_material_code` (`material_id`, `code`, `code_type`, `is_main`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000054'), 'MAT000054', 1, 1, '轮胎初始化生成', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_code c WHERE c.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000054') AND c.`code` = 'MAT000054' AND c.`code_type` = 1);
INSERT INTO `base_material_code` (`material_id`, `code`, `code_type`, `is_main`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000055'), 'MAT000055', 1, 1, '轮胎初始化生成', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_code c WHERE c.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000055') AND c.`code` = 'MAT000055' AND c.`code_type` = 1);
INSERT INTO `base_material_code` (`material_id`, `code`, `code_type`, `is_main`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000056'), 'MAT000056', 1, 1, '轮胎初始化生成', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_code c WHERE c.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000056') AND c.`code` = 'MAT000056' AND c.`code_type` = 1);
INSERT INTO `base_material_code` (`material_id`, `code`, `code_type`, `is_main`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000057'), 'MAT000057', 1, 1, '轮胎初始化生成', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_code c WHERE c.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000057') AND c.`code` = 'MAT000057' AND c.`code_type` = 1);
INSERT INTO `base_material_code` (`material_id`, `code`, `code_type`, `is_main`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000058'), 'MAT000058', 1, 1, '轮胎初始化生成', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_code c WHERE c.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000058') AND c.`code` = 'MAT000058' AND c.`code_type` = 1);
INSERT INTO `base_material_code` (`material_id`, `code`, `code_type`, `is_main`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000059'), 'MAT000059', 1, 1, '轮胎初始化生成', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_code c WHERE c.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000059') AND c.`code` = 'MAT000059' AND c.`code_type` = 1);
INSERT INTO `base_material_code` (`material_id`, `code`, `code_type`, `is_main`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000060'), 'MAT000060', 1, 1, '轮胎初始化生成', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_code c WHERE c.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000060') AND c.`code` = 'MAT000060' AND c.`code_type` = 1);
INSERT INTO `base_material_code` (`material_id`, `code`, `code_type`, `is_main`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000061'), 'MAT000061', 1, 1, '轮胎初始化生成', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_code c WHERE c.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000061') AND c.`code` = 'MAT000061' AND c.`code_type` = 1);
INSERT INTO `base_material_code` (`material_id`, `code`, `code_type`, `is_main`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000062'), 'MAT000062', 1, 1, '轮胎初始化生成', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_code c WHERE c.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000062') AND c.`code` = 'MAT000062' AND c.`code_type` = 1);
INSERT INTO `base_material_code` (`material_id`, `code`, `code_type`, `is_main`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000063'), 'MAT000063', 1, 1, '轮胎初始化生成', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_code c WHERE c.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000063') AND c.`code` = 'MAT000063' AND c.`code_type` = 1);
INSERT INTO `base_material_code` (`material_id`, `code`, `code_type`, `is_main`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000064'), 'MAT000064', 1, 1, '轮胎初始化生成', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_code c WHERE c.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000064') AND c.`code` = 'MAT000064' AND c.`code_type` = 1);
INSERT INTO `base_material_code` (`material_id`, `code`, `code_type`, `is_main`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000065'), 'MAT000065', 1, 1, '轮胎初始化生成', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_code c WHERE c.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000065') AND c.`code` = 'MAT000065' AND c.`code_type` = 1);
INSERT INTO `base_material_code` (`material_id`, `code`, `code_type`, `is_main`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000066'), 'MAT000066', 1, 1, '轮胎初始化生成', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_code c WHERE c.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000066') AND c.`code` = 'MAT000066' AND c.`code_type` = 1);
INSERT INTO `base_material_code` (`material_id`, `code`, `code_type`, `is_main`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000067'), 'MAT000067', 1, 1, '轮胎初始化生成', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_code c WHERE c.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000067') AND c.`code` = 'MAT000067' AND c.`code_type` = 1);
INSERT INTO `base_material_code` (`material_id`, `code`, `code_type`, `is_main`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000068'), 'MAT000068', 1, 1, '轮胎初始化生成', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_code c WHERE c.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000068') AND c.`code` = 'MAT000068' AND c.`code_type` = 1);
INSERT INTO `base_material_code` (`material_id`, `code`, `code_type`, `is_main`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000069'), 'MAT000069', 1, 1, '轮胎初始化生成', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_code c WHERE c.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000069') AND c.`code` = 'MAT000069' AND c.`code_type` = 1);
INSERT INTO `base_material_code` (`material_id`, `code`, `code_type`, `is_main`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000070'), 'MAT000070', 1, 1, '轮胎初始化生成', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_code c WHERE c.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000070') AND c.`code` = 'MAT000070' AND c.`code_type` = 1);
INSERT INTO `base_material_code` (`material_id`, `code`, `code_type`, `is_main`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000071'), 'MAT000071', 1, 1, '轮胎初始化生成', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_code c WHERE c.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000071') AND c.`code` = 'MAT000071' AND c.`code_type` = 1);
INSERT INTO `base_material_code` (`material_id`, `code`, `code_type`, `is_main`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000072'), 'MAT000072', 1, 1, '轮胎初始化生成', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_code c WHERE c.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000072') AND c.`code` = 'MAT000072' AND c.`code_type` = 1);
INSERT INTO `base_material_code` (`material_id`, `code`, `code_type`, `is_main`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000073'), 'MAT000073', 1, 1, '轮胎初始化生成', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_code c WHERE c.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000073') AND c.`code` = 'MAT000073' AND c.`code_type` = 1);
INSERT INTO `base_material_code` (`material_id`, `code`, `code_type`, `is_main`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000074'), 'MAT000074', 1, 1, '轮胎初始化生成', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_code c WHERE c.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000074') AND c.`code` = 'MAT000074' AND c.`code_type` = 1);
INSERT INTO `base_material_code` (`material_id`, `code`, `code_type`, `is_main`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000075'), 'MAT000075', 1, 1, '轮胎初始化生成', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_code c WHERE c.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000075') AND c.`code` = 'MAT000075' AND c.`code_type` = 1);
INSERT INTO `base_material_code` (`material_id`, `code`, `code_type`, `is_main`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000076'), 'MAT000076', 1, 1, '轮胎初始化生成', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_code c WHERE c.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000076') AND c.`code` = 'MAT000076' AND c.`code_type` = 1);
INSERT INTO `base_material_code` (`material_id`, `code`, `code_type`, `is_main`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000077'), 'MAT000077', 1, 1, '轮胎初始化生成', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_code c WHERE c.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000077') AND c.`code` = 'MAT000077' AND c.`code_type` = 1);
INSERT INTO `base_material_code` (`material_id`, `code`, `code_type`, `is_main`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000078'), 'MAT000078', 1, 1, '轮胎初始化生成', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_code c WHERE c.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000078') AND c.`code` = 'MAT000078' AND c.`code_type` = 1);
INSERT INTO `base_material_code` (`material_id`, `code`, `code_type`, `is_main`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000079'), 'MAT000079', 1, 1, '轮胎初始化生成', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_code c WHERE c.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000079') AND c.`code` = 'MAT000079' AND c.`code_type` = 1);
INSERT INTO `base_material_code` (`material_id`, `code`, `code_type`, `is_main`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000080'), 'MAT000080', 1, 1, '轮胎初始化生成', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_code c WHERE c.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000080') AND c.`code` = 'MAT000080' AND c.`code_type` = 1);
INSERT INTO `base_material_code` (`material_id`, `code`, `code_type`, `is_main`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000081'), 'MAT000081', 1, 1, '轮胎初始化生成', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_code c WHERE c.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000081') AND c.`code` = 'MAT000081' AND c.`code_type` = 1);
INSERT INTO `base_material_code` (`material_id`, `code`, `code_type`, `is_main`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000082'), 'MAT000082', 1, 1, '轮胎初始化生成', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_code c WHERE c.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000082') AND c.`code` = 'MAT000082' AND c.`code_type` = 1);
INSERT INTO `base_material_code` (`material_id`, `code`, `code_type`, `is_main`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000083'), 'MAT000083', 1, 1, '轮胎初始化生成', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_code c WHERE c.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000083') AND c.`code` = 'MAT000083' AND c.`code_type` = 1);
INSERT INTO `base_material_code` (`material_id`, `code`, `code_type`, `is_main`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000084'), 'MAT000084', 1, 1, '轮胎初始化生成', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_code c WHERE c.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000084') AND c.`code` = 'MAT000084' AND c.`code_type` = 1);
INSERT INTO `base_material_code` (`material_id`, `code`, `code_type`, `is_main`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000085'), 'MAT000085', 1, 1, '轮胎初始化生成', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_code c WHERE c.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000085') AND c.`code` = 'MAT000085' AND c.`code_type` = 1);
INSERT INTO `base_material_code` (`material_id`, `code`, `code_type`, `is_main`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = 'MAT000086'), 'MAT000086', 1, 1, '轮胎初始化生成', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_code c WHERE c.`material_id` = (SELECT id FROM base_material WHERE code = 'MAT000086') AND c.`code` = 'MAT000086' AND c.`code_type` = 1);

-- -----------------------------------------------------------------------------
-- 10. 库存初始化（主仓库 WH001，按采购成本价计价）
-- -----------------------------------------------------------------------------
INSERT INTO `product_stock` (`sc_id`, `product_id`, `stock_num`, `tax_price`, `tax_amount`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`)
SELECT 1, (SELECT id FROM base_material WHERE code = 'MAT000024'), 85.0000, 278.00, 23630.00, 'system', NOW(), 'system', NOW(), 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM product_stock ps WHERE ps.`sc_id` = 1 AND ps.`product_id` = (SELECT id FROM base_material WHERE code = 'MAT000024'));
INSERT INTO `product_stock` (`sc_id`, `product_id`, `stock_num`, `tax_price`, `tax_amount`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`)
SELECT 1, (SELECT id FROM base_material WHERE code = 'MAT000025'), 98.0000, 328.00, 32144.00, 'system', NOW(), 'system', NOW(), 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM product_stock ps WHERE ps.`sc_id` = 1 AND ps.`product_id` = (SELECT id FROM base_material WHERE code = 'MAT000025'));
INSERT INTO `product_stock` (`sc_id`, `product_id`, `stock_num`, `tax_price`, `tax_amount`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`)
SELECT 1, (SELECT id FROM base_material WHERE code = 'MAT000026'), 111.0000, 409.00, 45399.00, 'system', NOW(), 'system', NOW(), 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM product_stock ps WHERE ps.`sc_id` = 1 AND ps.`product_id` = (SELECT id FROM base_material WHERE code = 'MAT000026'));
INSERT INTO `product_stock` (`sc_id`, `product_id`, `stock_num`, `tax_price`, `tax_amount`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`)
SELECT 1, (SELECT id FROM base_material WHERE code = 'MAT000027'), 34.0000, 514.00, 17476.00, 'system', NOW(), 'system', NOW(), 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM product_stock ps WHERE ps.`sc_id` = 1 AND ps.`product_id` = (SELECT id FROM base_material WHERE code = 'MAT000027'));
INSERT INTO `product_stock` (`sc_id`, `product_id`, `stock_num`, `tax_price`, `tax_amount`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`)
SELECT 1, (SELECT id FROM base_material WHERE code = 'MAT000028'), 47.0000, 359.00, 16873.00, 'system', NOW(), 'system', NOW(), 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM product_stock ps WHERE ps.`sc_id` = 1 AND ps.`product_id` = (SELECT id FROM base_material WHERE code = 'MAT000028'));
INSERT INTO `product_stock` (`sc_id`, `product_id`, `stock_num`, `tax_price`, `tax_amount`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`)
SELECT 1, (SELECT id FROM base_material WHERE code = 'MAT000029'), 60.0000, 533.00, 31980.00, 'system', NOW(), 'system', NOW(), 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM product_stock ps WHERE ps.`sc_id` = 1 AND ps.`product_id` = (SELECT id FROM base_material WHERE code = 'MAT000029'));
INSERT INTO `product_stock` (`sc_id`, `product_id`, `stock_num`, `tax_price`, `tax_amount`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`)
SELECT 1, (SELECT id FROM base_material WHERE code = 'MAT000030'), 73.0000, 719.00, 52487.00, 'system', NOW(), 'system', NOW(), 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM product_stock ps WHERE ps.`sc_id` = 1 AND ps.`product_id` = (SELECT id FROM base_material WHERE code = 'MAT000030'));
INSERT INTO `product_stock` (`sc_id`, `product_id`, `stock_num`, `tax_price`, `tax_amount`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`)
SELECT 1, (SELECT id FROM base_material WHERE code = 'MAT000031'), 86.0000, 681.00, 58566.00, 'system', NOW(), 'system', NOW(), 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM product_stock ps WHERE ps.`sc_id` = 1 AND ps.`product_id` = (SELECT id FROM base_material WHERE code = 'MAT000031'));
INSERT INTO `product_stock` (`sc_id`, `product_id`, `stock_num`, `tax_price`, `tax_amount`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`)
SELECT 1, (SELECT id FROM base_material WHERE code = 'MAT000032'), 99.0000, 867.00, 85833.00, 'system', NOW(), 'system', NOW(), 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM product_stock ps WHERE ps.`sc_id` = 1 AND ps.`product_id` = (SELECT id FROM base_material WHERE code = 'MAT000032'));
INSERT INTO `product_stock` (`sc_id`, `product_id`, `stock_num`, `tax_price`, `tax_amount`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`)
SELECT 1, (SELECT id FROM base_material WHERE code = 'MAT000033'), 112.0000, 1091.00, 122192.00, 'system', NOW(), 'system', NOW(), 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM product_stock ps WHERE ps.`sc_id` = 1 AND ps.`product_id` = (SELECT id FROM base_material WHERE code = 'MAT000033'));
INSERT INTO `product_stock` (`sc_id`, `product_id`, `stock_num`, `tax_price`, `tax_amount`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`)
SELECT 1, (SELECT id FROM base_material WHERE code = 'MAT000034'), 35.0000, 272.00, 9520.00, 'system', NOW(), 'system', NOW(), 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM product_stock ps WHERE ps.`sc_id` = 1 AND ps.`product_id` = (SELECT id FROM base_material WHERE code = 'MAT000034'));
INSERT INTO `product_stock` (`sc_id`, `product_id`, `stock_num`, `tax_price`, `tax_amount`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`)
SELECT 1, (SELECT id FROM base_material WHERE code = 'MAT000035'), 48.0000, 322.00, 15456.00, 'system', NOW(), 'system', NOW(), 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM product_stock ps WHERE ps.`sc_id` = 1 AND ps.`product_id` = (SELECT id FROM base_material WHERE code = 'MAT000035'));
INSERT INTO `product_stock` (`sc_id`, `product_id`, `stock_num`, `tax_price`, `tax_amount`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`)
SELECT 1, (SELECT id FROM base_material WHERE code = 'MAT000036'), 61.0000, 402.00, 24522.00, 'system', NOW(), 'system', NOW(), 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM product_stock ps WHERE ps.`sc_id` = 1 AND ps.`product_id` = (SELECT id FROM base_material WHERE code = 'MAT000036'));
INSERT INTO `product_stock` (`sc_id`, `product_id`, `stock_num`, `tax_price`, `tax_amount`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`)
SELECT 1, (SELECT id FROM base_material WHERE code = 'MAT000037'), 74.0000, 514.00, 38036.00, 'system', NOW(), 'system', NOW(), 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM product_stock ps WHERE ps.`sc_id` = 1 AND ps.`product_id` = (SELECT id FROM base_material WHERE code = 'MAT000037'));
INSERT INTO `product_stock` (`sc_id`, `product_id`, `stock_num`, `tax_price`, `tax_amount`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`)
SELECT 1, (SELECT id FROM base_material WHERE code = 'MAT000038'), 87.0000, 241.00, 20967.00, 'system', NOW(), 'system', NOW(), 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM product_stock ps WHERE ps.`sc_id` = 1 AND ps.`product_id` = (SELECT id FROM base_material WHERE code = 'MAT000038'));
INSERT INTO `product_stock` (`sc_id`, `product_id`, `stock_num`, `tax_price`, `tax_amount`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`)
SELECT 1, (SELECT id FROM base_material WHERE code = 'MAT000039'), 100.0000, 285.00, 28500.00, 'system', NOW(), 'system', NOW(), 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM product_stock ps WHERE ps.`sc_id` = 1 AND ps.`product_id` = (SELECT id FROM base_material WHERE code = 'MAT000039'));
INSERT INTO `product_stock` (`sc_id`, `product_id`, `stock_num`, `tax_price`, `tax_amount`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`)
SELECT 1, (SELECT id FROM base_material WHERE code = 'MAT000040'), 113.0000, 347.00, 39211.00, 'system', NOW(), 'system', NOW(), 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM product_stock ps WHERE ps.`sc_id` = 1 AND ps.`product_id` = (SELECT id FROM base_material WHERE code = 'MAT000040'));
INSERT INTO `product_stock` (`sc_id`, `product_id`, `stock_num`, `tax_price`, `tax_amount`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`)
SELECT 1, (SELECT id FROM base_material WHERE code = 'MAT000041'), 36.0000, 719.00, 25884.00, 'system', NOW(), 'system', NOW(), 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM product_stock ps WHERE ps.`sc_id` = 1 AND ps.`product_id` = (SELECT id FROM base_material WHERE code = 'MAT000041'));
INSERT INTO `product_stock` (`sc_id`, `product_id`, `stock_num`, `tax_price`, `tax_amount`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`)
SELECT 1, (SELECT id FROM base_material WHERE code = 'MAT000042'), 49.0000, 905.00, 44345.00, 'system', NOW(), 'system', NOW(), 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM product_stock ps WHERE ps.`sc_id` = 1 AND ps.`product_id` = (SELECT id FROM base_material WHERE code = 'MAT000042'));
INSERT INTO `product_stock` (`sc_id`, `product_id`, `stock_num`, `tax_price`, `tax_amount`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`)
SELECT 1, (SELECT id FROM base_material WHERE code = 'MAT000043'), 62.0000, 781.00, 48422.00, 'system', NOW(), 'system', NOW(), 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM product_stock ps WHERE ps.`sc_id` = 1 AND ps.`product_id` = (SELECT id FROM base_material WHERE code = 'MAT000043'));
INSERT INTO `product_stock` (`sc_id`, `product_id`, `stock_num`, `tax_price`, `tax_amount`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`)
SELECT 1, (SELECT id FROM base_material WHERE code = 'MAT000044'), 75.0000, 247.00, 18525.00, 'system', NOW(), 'system', NOW(), 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM product_stock ps WHERE ps.`sc_id` = 1 AND ps.`product_id` = (SELECT id FROM base_material WHERE code = 'MAT000044'));
INSERT INTO `product_stock` (`sc_id`, `product_id`, `stock_num`, `tax_price`, `tax_amount`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`)
SELECT 1, (SELECT id FROM base_material WHERE code = 'MAT000045'), 88.0000, 291.00, 25608.00, 'system', NOW(), 'system', NOW(), 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM product_stock ps WHERE ps.`sc_id` = 1 AND ps.`product_id` = (SELECT id FROM base_material WHERE code = 'MAT000045'));
INSERT INTO `product_stock` (`sc_id`, `product_id`, `stock_num`, `tax_price`, `tax_amount`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`)
SELECT 1, (SELECT id FROM base_material WHERE code = 'MAT000046'), 101.0000, 353.00, 35653.00, 'system', NOW(), 'system', NOW(), 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM product_stock ps WHERE ps.`sc_id` = 1 AND ps.`product_id` = (SELECT id FROM base_material WHERE code = 'MAT000046'));
INSERT INTO `product_stock` (`sc_id`, `product_id`, `stock_num`, `tax_price`, `tax_amount`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`)
SELECT 1, (SELECT id FROM base_material WHERE code = 'MAT000047'), 114.0000, 328.00, 37392.00, 'system', NOW(), 'system', NOW(), 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM product_stock ps WHERE ps.`sc_id` = 1 AND ps.`product_id` = (SELECT id FROM base_material WHERE code = 'MAT000047'));
INSERT INTO `product_stock` (`sc_id`, `product_id`, `stock_num`, `tax_price`, `tax_amount`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`)
SELECT 1, (SELECT id FROM base_material WHERE code = 'MAT000048'), 37.0000, 452.00, 16724.00, 'system', NOW(), 'system', NOW(), 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM product_stock ps WHERE ps.`sc_id` = 1 AND ps.`product_id` = (SELECT id FROM base_material WHERE code = 'MAT000048'));
INSERT INTO `product_stock` (`sc_id`, `product_id`, `stock_num`, `tax_price`, `tax_amount`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`)
SELECT 1, (SELECT id FROM base_material WHERE code = 'MAT000049'), 50.0000, 489.00, 24450.00, 'system', NOW(), 'system', NOW(), 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM product_stock ps WHERE ps.`sc_id` = 1 AND ps.`product_id` = (SELECT id FROM base_material WHERE code = 'MAT000049'));
INSERT INTO `product_stock` (`sc_id`, `product_id`, `stock_num`, `tax_price`, `tax_amount`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`)
SELECT 1, (SELECT id FROM base_material WHERE code = 'MAT000050'), 63.0000, 657.00, 41391.00, 'system', NOW(), 'system', NOW(), 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM product_stock ps WHERE ps.`sc_id` = 1 AND ps.`product_id` = (SELECT id FROM base_material WHERE code = 'MAT000050'));
INSERT INTO `product_stock` (`sc_id`, `product_id`, `stock_num`, `tax_price`, `tax_amount`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`)
SELECT 1, (SELECT id FROM base_material WHERE code = 'MAT000051'), 76.0000, 843.00, 64068.00, 'system', NOW(), 'system', NOW(), 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM product_stock ps WHERE ps.`sc_id` = 1 AND ps.`product_id` = (SELECT id FROM base_material WHERE code = 'MAT000051'));
INSERT INTO `product_stock` (`sc_id`, `product_id`, `stock_num`, `tax_price`, `tax_amount`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`)
SELECT 1, (SELECT id FROM base_material WHERE code = 'MAT000052'), 89.0000, 1047.00, 93183.00, 'system', NOW(), 'system', NOW(), 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM product_stock ps WHERE ps.`sc_id` = 1 AND ps.`product_id` = (SELECT id FROM base_material WHERE code = 'MAT000052'));
INSERT INTO `product_stock` (`sc_id`, `product_id`, `stock_num`, `tax_price`, `tax_amount`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`)
SELECT 1, (SELECT id FROM base_material WHERE code = 'MAT000053'), 102.0000, 229.00, 23358.00, 'system', NOW(), 'system', NOW(), 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM product_stock ps WHERE ps.`sc_id` = 1 AND ps.`product_id` = (SELECT id FROM base_material WHERE code = 'MAT000053'));
INSERT INTO `product_stock` (`sc_id`, `product_id`, `stock_num`, `tax_price`, `tax_amount`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`)
SELECT 1, (SELECT id FROM base_material WHERE code = 'MAT000054'), 115.0000, 266.00, 30590.00, 'system', NOW(), 'system', NOW(), 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM product_stock ps WHERE ps.`sc_id` = 1 AND ps.`product_id` = (SELECT id FROM base_material WHERE code = 'MAT000054'));
INSERT INTO `product_stock` (`sc_id`, `product_id`, `stock_num`, `tax_price`, `tax_amount`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`)
SELECT 1, (SELECT id FROM base_material WHERE code = 'MAT000055'), 38.0000, 328.00, 12464.00, 'system', NOW(), 'system', NOW(), 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM product_stock ps WHERE ps.`sc_id` = 1 AND ps.`product_id` = (SELECT id FROM base_material WHERE code = 'MAT000055'));
INSERT INTO `product_stock` (`sc_id`, `product_id`, `stock_num`, `tax_price`, `tax_amount`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`)
SELECT 1, (SELECT id FROM base_material WHERE code = 'MAT000056'), 51.0000, 464.00, 23664.00, 'system', NOW(), 'system', NOW(), 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM product_stock ps WHERE ps.`sc_id` = 1 AND ps.`product_id` = (SELECT id FROM base_material WHERE code = 'MAT000056'));
INSERT INTO `product_stock` (`sc_id`, `product_id`, `stock_num`, `tax_price`, `tax_amount`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`)
SELECT 1, (SELECT id FROM base_material WHERE code = 'MAT000057'), 64.0000, 681.00, 43584.00, 'system', NOW(), 'system', NOW(), 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM product_stock ps WHERE ps.`sc_id` = 1 AND ps.`product_id` = (SELECT id FROM base_material WHERE code = 'MAT000057'));
INSERT INTO `product_stock` (`sc_id`, `product_id`, `stock_num`, `tax_price`, `tax_amount`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`)
SELECT 1, (SELECT id FROM base_material WHERE code = 'MAT000058'), 77.0000, 867.00, 66759.00, 'system', NOW(), 'system', NOW(), 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM product_stock ps WHERE ps.`sc_id` = 1 AND ps.`product_id` = (SELECT id FROM base_material WHERE code = 'MAT000058'));
INSERT INTO `product_stock` (`sc_id`, `product_id`, `stock_num`, `tax_price`, `tax_amount`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`)
SELECT 1, (SELECT id FROM base_material WHERE code = 'MAT000059'), 90.0000, 595.00, 53550.00, 'system', NOW(), 'system', NOW(), 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM product_stock ps WHERE ps.`sc_id` = 1 AND ps.`product_id` = (SELECT id FROM base_material WHERE code = 'MAT000059'));
INSERT INTO `product_stock` (`sc_id`, `product_id`, `stock_num`, `tax_price`, `tax_amount`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`)
SELECT 1, (SELECT id FROM base_material WHERE code = 'MAT000060'), 103.0000, 743.00, 76529.00, 'system', NOW(), 'system', NOW(), 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM product_stock ps WHERE ps.`sc_id` = 1 AND ps.`product_id` = (SELECT id FROM base_material WHERE code = 'MAT000060'));
INSERT INTO `product_stock` (`sc_id`, `product_id`, `stock_num`, `tax_price`, `tax_amount`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`)
SELECT 1, (SELECT id FROM base_material WHERE code = 'MAT000061'), 116.0000, 657.00, 76212.00, 'system', NOW(), 'system', NOW(), 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM product_stock ps WHERE ps.`sc_id` = 1 AND ps.`product_id` = (SELECT id FROM base_material WHERE code = 'MAT000061'));
INSERT INTO `product_stock` (`sc_id`, `product_id`, `stock_num`, `tax_price`, `tax_amount`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`)
SELECT 1, (SELECT id FROM base_material WHERE code = 'MAT000062'), 39.0000, 272.00, 10608.00, 'system', NOW(), 'system', NOW(), 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM product_stock ps WHERE ps.`sc_id` = 1 AND ps.`product_id` = (SELECT id FROM base_material WHERE code = 'MAT000062'));
INSERT INTO `product_stock` (`sc_id`, `product_id`, `stock_num`, `tax_price`, `tax_amount`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`)
SELECT 1, (SELECT id FROM base_material WHERE code = 'MAT000063'), 52.0000, 340.00, 17680.00, 'system', NOW(), 'system', NOW(), 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM product_stock ps WHERE ps.`sc_id` = 1 AND ps.`product_id` = (SELECT id FROM base_material WHERE code = 'MAT000063'));
INSERT INTO `product_stock` (`sc_id`, `product_id`, `stock_num`, `tax_price`, `tax_amount`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`)
SELECT 1, (SELECT id FROM base_material WHERE code = 'MAT000064'), 65.0000, 433.00, 28145.00, 'system', NOW(), 'system', NOW(), 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM product_stock ps WHERE ps.`sc_id` = 1 AND ps.`product_id` = (SELECT id FROM base_material WHERE code = 'MAT000064'));
INSERT INTO `product_stock` (`sc_id`, `product_id`, `stock_num`, `tax_price`, `tax_amount`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`)
SELECT 1, (SELECT id FROM base_material WHERE code = 'MAT000065'), 78.0000, 291.00, 22698.00, 'system', NOW(), 'system', NOW(), 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM product_stock ps WHERE ps.`sc_id` = 1 AND ps.`product_id` = (SELECT id FROM base_material WHERE code = 'MAT000065'));
INSERT INTO `product_stock` (`sc_id`, `product_id`, `stock_num`, `tax_price`, `tax_amount`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`)
SELECT 1, (SELECT id FROM base_material WHERE code = 'MAT000066'), 91.0000, 452.00, 41132.00, 'system', NOW(), 'system', NOW(), 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM product_stock ps WHERE ps.`sc_id` = 1 AND ps.`product_id` = (SELECT id FROM base_material WHERE code = 'MAT000066'));
INSERT INTO `product_stock` (`sc_id`, `product_id`, `stock_num`, `tax_price`, `tax_amount`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`)
SELECT 1, (SELECT id FROM base_material WHERE code = 'MAT000067'), 104.0000, 619.00, 64376.00, 'system', NOW(), 'system', NOW(), 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM product_stock ps WHERE ps.`sc_id` = 1 AND ps.`product_id` = (SELECT id FROM base_material WHERE code = 'MAT000067'));
INSERT INTO `product_stock` (`sc_id`, `product_id`, `stock_num`, `tax_price`, `tax_amount`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`)
SELECT 1, (SELECT id FROM base_material WHERE code = 'MAT000068'), 117.0000, 557.00, 65169.00, 'system', NOW(), 'system', NOW(), 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM product_stock ps WHERE ps.`sc_id` = 1 AND ps.`product_id` = (SELECT id FROM base_material WHERE code = 'MAT000068'));
INSERT INTO `product_stock` (`sc_id`, `product_id`, `stock_num`, `tax_price`, `tax_amount`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`)
SELECT 1, (SELECT id FROM base_material WHERE code = 'MAT000069'), 40.0000, 719.00, 28760.00, 'system', NOW(), 'system', NOW(), 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM product_stock ps WHERE ps.`sc_id` = 1 AND ps.`product_id` = (SELECT id FROM base_material WHERE code = 'MAT000069'));
INSERT INTO `product_stock` (`sc_id`, `product_id`, `stock_num`, `tax_price`, `tax_amount`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`)
SELECT 1, (SELECT id FROM base_material WHERE code = 'MAT000070'), 53.0000, 204.00, 10812.00, 'system', NOW(), 'system', NOW(), 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM product_stock ps WHERE ps.`sc_id` = 1 AND ps.`product_id` = (SELECT id FROM base_material WHERE code = 'MAT000070'));
INSERT INTO `product_stock` (`sc_id`, `product_id`, `stock_num`, `tax_price`, `tax_amount`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`)
SELECT 1, (SELECT id FROM base_material WHERE code = 'MAT000071'), 66.0000, 235.00, 15510.00, 'system', NOW(), 'system', NOW(), 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM product_stock ps WHERE ps.`sc_id` = 1 AND ps.`product_id` = (SELECT id FROM base_material WHERE code = 'MAT000071'));
INSERT INTO `product_stock` (`sc_id`, `product_id`, `stock_num`, `tax_price`, `tax_amount`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`)
SELECT 1, (SELECT id FROM base_material WHERE code = 'MAT000072'), 79.0000, 309.00, 24411.00, 'system', NOW(), 'system', NOW(), 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM product_stock ps WHERE ps.`sc_id` = 1 AND ps.`product_id` = (SELECT id FROM base_material WHERE code = 'MAT000072'));
INSERT INTO `product_stock` (`sc_id`, `product_id`, `stock_num`, `tax_price`, `tax_amount`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`)
SELECT 1, (SELECT id FROM base_material WHERE code = 'MAT000073'), 92.0000, 409.00, 37628.00, 'system', NOW(), 'system', NOW(), 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM product_stock ps WHERE ps.`sc_id` = 1 AND ps.`product_id` = (SELECT id FROM base_material WHERE code = 'MAT000073'));
INSERT INTO `product_stock` (`sc_id`, `product_id`, `stock_num`, `tax_price`, `tax_amount`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`)
SELECT 1, (SELECT id FROM base_material WHERE code = 'MAT000074'), 105.0000, 495.00, 51975.00, 'system', NOW(), 'system', NOW(), 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM product_stock ps WHERE ps.`sc_id` = 1 AND ps.`product_id` = (SELECT id FROM base_material WHERE code = 'MAT000074'));
INSERT INTO `product_stock` (`sc_id`, `product_id`, `stock_num`, `tax_price`, `tax_amount`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`)
SELECT 1, (SELECT id FROM base_material WHERE code = 'MAT000075'), 118.0000, 235.00, 27730.00, 'system', NOW(), 'system', NOW(), 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM product_stock ps WHERE ps.`sc_id` = 1 AND ps.`product_id` = (SELECT id FROM base_material WHERE code = 'MAT000075'));
INSERT INTO `product_stock` (`sc_id`, `product_id`, `stock_num`, `tax_price`, `tax_amount`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`)
SELECT 1, (SELECT id FROM base_material WHERE code = 'MAT000076'), 41.0000, 266.00, 10906.00, 'system', NOW(), 'system', NOW(), 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM product_stock ps WHERE ps.`sc_id` = 1 AND ps.`product_id` = (SELECT id FROM base_material WHERE code = 'MAT000076'));
INSERT INTO `product_stock` (`sc_id`, `product_id`, `stock_num`, `tax_price`, `tax_amount`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`)
SELECT 1, (SELECT id FROM base_material WHERE code = 'MAT000077'), 54.0000, 303.00, 16362.00, 'system', NOW(), 'system', NOW(), 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM product_stock ps WHERE ps.`sc_id` = 1 AND ps.`product_id` = (SELECT id FROM base_material WHERE code = 'MAT000077'));
INSERT INTO `product_stock` (`sc_id`, `product_id`, `stock_num`, `tax_price`, `tax_amount`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`)
SELECT 1, (SELECT id FROM base_material WHERE code = 'MAT000078'), 67.0000, 272.00, 18224.00, 'system', NOW(), 'system', NOW(), 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM product_stock ps WHERE ps.`sc_id` = 1 AND ps.`product_id` = (SELECT id FROM base_material WHERE code = 'MAT000078'));
INSERT INTO `product_stock` (`sc_id`, `product_id`, `stock_num`, `tax_price`, `tax_amount`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`)
SELECT 1, (SELECT id FROM base_material WHERE code = 'MAT000079'), 80.0000, 303.00, 24240.00, 'system', NOW(), 'system', NOW(), 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM product_stock ps WHERE ps.`sc_id` = 1 AND ps.`product_id` = (SELECT id FROM base_material WHERE code = 'MAT000079'));
INSERT INTO `product_stock` (`sc_id`, `product_id`, `stock_num`, `tax_price`, `tax_amount`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`)
SELECT 1, (SELECT id FROM base_material WHERE code = 'MAT000080'), 93.0000, 433.00, 40269.00, 'system', NOW(), 'system', NOW(), 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM product_stock ps WHERE ps.`sc_id` = 1 AND ps.`product_id` = (SELECT id FROM base_material WHERE code = 'MAT000080'));
INSERT INTO `product_stock` (`sc_id`, `product_id`, `stock_num`, `tax_price`, `tax_amount`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`)
SELECT 1, (SELECT id FROM base_material WHERE code = 'MAT000081'), 106.0000, 508.00, 53848.00, 'system', NOW(), 'system', NOW(), 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM product_stock ps WHERE ps.`sc_id` = 1 AND ps.`product_id` = (SELECT id FROM base_material WHERE code = 'MAT000081'));
INSERT INTO `product_stock` (`sc_id`, `product_id`, `stock_num`, `tax_price`, `tax_amount`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`)
SELECT 1, (SELECT id FROM base_material WHERE code = 'MAT000082'), 119.0000, 223.00, 26537.00, 'system', NOW(), 'system', NOW(), 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM product_stock ps WHERE ps.`sc_id` = 1 AND ps.`product_id` = (SELECT id FROM base_material WHERE code = 'MAT000082'));
INSERT INTO `product_stock` (`sc_id`, `product_id`, `stock_num`, `tax_price`, `tax_amount`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`)
SELECT 1, (SELECT id FROM base_material WHERE code = 'MAT000083'), 42.0000, 260.00, 10920.00, 'system', NOW(), 'system', NOW(), 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM product_stock ps WHERE ps.`sc_id` = 1 AND ps.`product_id` = (SELECT id FROM base_material WHERE code = 'MAT000083'));
INSERT INTO `product_stock` (`sc_id`, `product_id`, `stock_num`, `tax_price`, `tax_amount`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`)
SELECT 1, (SELECT id FROM base_material WHERE code = 'MAT000084'), 55.0000, 322.00, 17710.00, 'system', NOW(), 'system', NOW(), 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM product_stock ps WHERE ps.`sc_id` = 1 AND ps.`product_id` = (SELECT id FROM base_material WHERE code = 'MAT000084'));
INSERT INTO `product_stock` (`sc_id`, `product_id`, `stock_num`, `tax_price`, `tax_amount`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`)
SELECT 1, (SELECT id FROM base_material WHERE code = 'MAT000085'), 68.0000, 390.00, 26520.00, 'system', NOW(), 'system', NOW(), 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM product_stock ps WHERE ps.`sc_id` = 1 AND ps.`product_id` = (SELECT id FROM base_material WHERE code = 'MAT000085'));
INSERT INTO `product_stock` (`sc_id`, `product_id`, `stock_num`, `tax_price`, `tax_amount`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`)
SELECT 1, (SELECT id FROM base_material WHERE code = 'MAT000086'), 81.0000, 471.00, 38151.00, 'system', NOW(), 'system', NOW(), 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM product_stock ps WHERE ps.`sc_id` = 1 AND ps.`product_id` = (SELECT id FROM base_material WHERE code = 'MAT000086'));

-- -----------------------------------------------------------------------------
-- 11. 编号流水校正（主数据编号必须取「含已逻辑删除记录」的全表最大值）
-- -----------------------------------------------------------------------------
UPDATE `sys_code_rule` SET `current_value` = (SELECT COALESCE(MAX(CAST(REGEXP_REPLACE(RIGHT(`code`, 6), '[^0-9]', '') AS UNSIGNED)), 0) FROM `base_material` WHERE `code` LIKE 'MAT%') WHERE `rule_code` = 'BASE_MATERIAL';
UPDATE `sys_code_rule` SET `current_value` = (SELECT COALESCE(MAX(CAST(REGEXP_REPLACE(RIGHT(`code`, 6), '[^0-9]', '') AS UNSIGNED)), 0) FROM `base_material_category` WHERE `code` LIKE 'MC%') WHERE `rule_code` = 'BASE_MATERIAL_CATEGORY';
UPDATE `sys_code_rule` SET `current_value` = (SELECT COALESCE(MAX(CAST(REGEXP_REPLACE(RIGHT(`code`, 6), '[^0-9]', '') AS UNSIGNED)), 0) FROM `base_brand` WHERE `code` LIKE 'BRD%') WHERE `rule_code` = 'BASE_BRAND';
UPDATE `sys_code_rule` SET `current_value` = (SELECT COALESCE(MAX(CAST(REGEXP_REPLACE(RIGHT(`code`, 6), '[^0-9]', '') AS UNSIGNED)), 0) FROM `base_unit` WHERE `code` LIKE 'UNIT%') WHERE `rule_code` = 'BASE_UNIT';
UPDATE `sys_code_rule` SET `current_value` = (SELECT COALESCE(MAX(CAST(REGEXP_REPLACE(RIGHT(`code`, 6), '[^0-9]', '') AS UNSIGNED)), 0) FROM `base_spu` WHERE `code` LIKE 'SPU%') WHERE `rule_code` = 'SPU';
UPDATE `sys_code_rule` SET `current_value` = (SELECT COALESCE(MAX(CAST(REGEXP_REPLACE(RIGHT(`code`, 6), '[^0-9]', '') AS UNSIGNED)), 0) FROM `base_sale_property` WHERE `code` LIKE 'SP%') WHERE `rule_code` = 'SALE_PROPERTY';
UPDATE `sys_code_rule` SET `current_value` = (SELECT COALESCE(MAX(CAST(REGEXP_REPLACE(RIGHT(`code`, 6), '[^0-9]', '') AS UNSIGNED)), 0) FROM `base_sale_property_item` WHERE `code` LIKE 'SI%') WHERE `rule_code` = 'SALE_PROPERTY_ITEM';

-- -----------------------------------------------------------------------------
-- 12. 执行结果核对
-- -----------------------------------------------------------------------------
SELECT '轮胎分类'   AS `检查项`, COUNT(*) AS `值` FROM `base_material_category` WHERE `code` IN ('MC000006','MC000007','MC000008','MC000009')
UNION ALL SELECT '轮胎品牌', COUNT(*) FROM `base_brand` WHERE `code` LIKE 'BRD%' AND `code` >= 'BRD000004'
UNION ALL SELECT '轮胎SPU',  COUNT(*) FROM `base_spu` WHERE `code` BETWEEN 'SPU000007' AND 'SPU000028'
UNION ALL SELECT '轮胎SKU',  COUNT(*) FROM `base_material` m INNER JOIN `base_spu` s ON s.`id` = m.`spu_id` WHERE s.`code` BETWEEN 'SPU000007' AND 'SPU000028' AND m.`deleted` = 0
UNION ALL SELECT 'SKU属性绑定', COUNT(*) FROM `base_material_property_rel` r INNER JOIN `base_material` m ON m.`id` = r.`material_id` INNER JOIN `base_spu` s ON s.`id` = m.`spu_id` WHERE s.`code` BETWEEN 'SPU000007' AND 'SPU000028' AND r.`deleted` = 0
UNION ALL SELECT '一品多码', COUNT(*) FROM `base_material_code` c INNER JOIN `base_material` m ON m.`id` = c.`material_id` INNER JOIN `base_spu` s ON s.`id` = m.`spu_id` WHERE s.`code` BETWEEN 'SPU000007' AND 'SPU000028'
UNION ALL SELECT '库存记录', COUNT(*) FROM `product_stock` ps INNER JOIN `base_material` m ON m.`id` = ps.`product_id` INNER JOIN `base_spu` s ON s.`id` = m.`spu_id` WHERE s.`code` BETWEEN 'SPU000007' AND 'SPU000028' AND ps.`deleted` = 0;

SELECT b.`name` AS `品牌`, s.`code` AS `SPU编号`, s.`name` AS `花纹系列`, COUNT(m.`id`) AS `SKU数`
FROM `base_spu` s
LEFT JOIN `base_brand` b ON b.`id` = s.`brand_id`
LEFT JOIN `base_material` m ON m.`spu_id` = s.`id` AND m.`deleted` = 0
WHERE s.`code` BETWEEN 'SPU000007' AND 'SPU000028'
GROUP BY s.`id`, b.`name`, s.`code`, s.`name`
ORDER BY s.`code`;

SELECT `rule_code`, `prefix`, `current_value` FROM `sys_code_rule` WHERE `reset_by_date` = 0 ORDER BY `rule_code`;

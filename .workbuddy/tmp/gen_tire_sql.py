# -*- coding: utf-8 -*-
"""生成「轮胎商品」初始化 SQL（SPU + SKU + 销售属性 + 库存）。
输出：D:/code/java/java_study/erp-system/sql/init_tire_products.sql
"""
from io import StringIO
from collections import OrderedDict

OUT = r"D:/code/java/java_study/erp-system/sql/init_tire_products.sql"

# ---------------------------------------------------------------- 品牌
# (code, name, short_name, remark, sort)
BRANDS = [
    ("BRD000004", "德国马牌",   "Continental", "德国大陆集团（Continental AG）旗下轮胎品牌，1871年创立于汉诺威", 4),
    ("BRD000005", "普利司通",   "Bridgestone", "日本株式会社普利司通，1931年创立于福冈县久留米市", 5),
    ("BRD000006", "优科豪马",   "Yokohama",   "日本横滨橡胶株式会社，1917年创立；2017年起中文品牌名由「横滨」改为「优科豪马」", 6),
    ("BRD000007", "韩泰轮胎",   "Hankook",    "韩国韩泰轮胎株式会社，1941年创立，总部首尔", 7),
    ("BRD000008", "美国固铂",   "Cooper",     "美国固铂轮胎橡胶公司（Cooper Tire），1914年创立，总部俄亥俄州芬德利", 8),
    ("BRD000009", "沃凯途轮胎", "VOKATU",     "德系技术背景轮胎品牌，主打静音舒适与中高端替换市场", 9),
    ("BRD000010", "赛轮路极",   "ROADX",      "赛轮集团（SAILUN）旗下路极 ROADX 品牌，轿车胎/SUV胎/营运胎", 10),
    ("BRD000011", "达迈轮胎",   "DAMAI",      "青岛胜驰轮胎旗下品牌，山东广饶生产基地，主打高性价比静音耐磨胎", 11),
]

# ---------------------------------------------------------------- 分类
# (code, name, parent_code, sort, remark)
CATEGORIES = [
    ("MC000006", "轮胎",            None,      6, "轮胎商品总分类"),
    ("MC000007", "轿车轮胎(PCR)",   "MC000006", 7, "乘用车轿车轮胎"),
    ("MC000008", "SUV越野轮胎",     "MC000006", 8, "城市SUV/越野轮胎"),
    ("MC000009", "卡客车轮胎(TBR)", "MC000006", 9, "卡客车/商用车轮胎（本期暂无商品）"),
]

# ---------------------------------------------------------------- 计量单位
UNITS = [("UNIT000016", "条", 16, "轮胎计量单位")]

# ---------------------------------------------------------------- SPU / SKU
# spu: (code, brand_code, cat_code, name, short_name, spec, remark,
#       [(规格, 载重速度级别, 零售价)])
SPUS = [
    # ===== 德国马牌 =====
    ("SPU000007", "BRD000004", "MC000007",
     "德国马牌 ComfortContact CC7", "马牌CC7", "轿车轮胎 舒适静音 15-17寸",
     "马牌舒适静音系列主力花纹，主打降噪与湿地制动",
     [("195/65R15", "91H", 449), ("205/55R16", "91V", 529),
      ("215/60R16", "95V", 659), ("225/50R17", "94V", 829)]),
    ("SPU000008", "BRD000004", "MC000007",
     "德国马牌 PremiumContact 7", "马牌PC7", "轿车轮胎 运动操控 16-18寸",
     "马牌第七代运动操控花纹，干湿地抓地与转向响应为主",
     [("205/55R16", "91W", 579), ("225/45R17", "94W", 859), ("225/40R18", "92Y", 1159)]),
    ("SPU000009", "BRD000004", "MC000008",
     "德国马牌 UltraContact UC7 SUV", "马牌UC7 SUV", "SUV轮胎 均衡操控 18-20寸",
     "马牌城市SUV花纹，兼顾舒适、湿地安全与操控",
     [("225/60R18", "100H", 1099), ("235/55R19", "101V", 1399), ("245/45R20", "103V", 1759)]),

    # ===== 普利司通 =====
    ("SPU000010", "BRD000005", "MC000007",
     "普利司通 TURANZA T005 泰然者", "普利司通T005", "轿车轮胎 舒适静逸 15-17寸",
     "泰然者T005，舒适静音与湿地性能均衡",
     [("195/65R15", "91H", 439), ("205/55R16", "91V", 519),
      ("215/60R16", "95V", 649), ("225/45R17", "91W", 829)]),
    ("SPU000011", "BRD000005", "MC000007",
     "普利司通 ECOPIA EP150 御乘", "普利司通EP150", "轿车轮胎 节能环保 15-16寸",
     "ECOPIA 环保系列，低滚阻节油",
     [("195/65R15", "91H", 389), ("205/55R16", "91V", 459), ("215/60R16", "95H", 559)]),
    ("SPU000012", "BRD000005", "MC000008",
     "普利司通 ALENZA 001", "普利司通ALENZA001", "SUV轮胎 豪华舒适 18-19寸",
     "ALENZA 001 豪华SUV公路胎，静音舒适取向",
     [("225/60R18", "100H", 1159), ("235/55R19", "101V", 1459), ("235/60R18", "103H", 1259)]),

    # ===== 优科豪马 =====
    ("SPU000013", "BRD000006", "MC000007",
     "优科豪马 BluEarth AE50 蔚弛", "优科豪马AE50", "轿车轮胎 节能环保 15-16寸",
     "BluEarth 蔚弛系列，低滚阻 + 湿地安全",
     [("195/65R15", "91H", 399), ("205/55R16", "91V", 469), ("215/60R16", "95H", 569)]),
    ("SPU000014", "BRD000006", "MC000007",
     "优科豪马 ADVAN dB V551", "优科豪马V551", "轿车轮胎 静音舒适 16-17寸",
     "ADVAN dB 静音系列，主打低噪与舒适",
     [("205/55R16", "91V", 529), ("215/55R17", "94V", 729), ("225/45R17", "91W", 789)]),
    ("SPU000015", "BRD000006", "MC000008",
     "优科豪马 GEOLANDAR X-CV", "优科豪马X-CV", "SUV轮胎 公路操控 18-20寸",
     "GEOLANDAR 城市SUV公路胎，操控与湿地制动见长",
     [("225/60R18", "100H", 1059), ("235/55R19", "101V", 1359), ("245/45R20", "103V", 1689)]),

    # ===== 韩泰 =====
    ("SPU000016", "BRD000007", "MC000007",
     "韩泰 KINERGY ECO K435 傲特马", "韩泰K435", "轿车轮胎 节能环保 15-16寸",
     "Kinergy Eco 系列，耐磨节油，A级湿地",
     [("195/65R15", "91H", 369), ("205/55R16", "91V", 429), ("215/60R16", "95H", 529)]),
    ("SPU000017", "BRD000007", "MC000007",
     "韩泰 VENTUS S1 evo3 K127 万途仕", "韩泰K127", "轿车轮胎 运动操控 17-19寸",
     "Ventus S1 evo3 高性能运动胎，干湿地操控",
     [("225/45R17", "91W", 749), ("235/45R18", "98Y", 1099), ("245/40R19", "98Y", 1399)]),
    ("SPU000018", "BRD000007", "MC000008",
     "韩泰 DynaPro HP2 RA33", "韩泰RA33", "SUV轮胎 舒适静音 18-19寸",
     "Dynapro HP2 城市SUV公路胎，舒适静音取向",
     [("225/60R18", "100H", 959), ("235/55R19", "101V", 1199), ("235/60R18", "103H", 1059)]),

    # ===== 美国固铂 =====
    ("SPU000019", "BRD000008", "MC000007",
     "美国固铂 ZEON CS8 泽昂", "固铂CS8", "轿车轮胎 静音舒适 16-17寸",
     "Zeon CS8 静音舒适轿车胎",
     [("205/55R16", "91V", 439), ("215/60R16", "95H", 549), ("225/45R17", "91W", 699)]),
    ("SPU000020", "BRD000008", "MC000007",
     "美国固铂 EVOLUTION CTT 纵横者", "固铂CTT", "轿车轮胎 操控运动 16-18寸",
     "Evolution CTT 操控型轿车胎",
     [("205/55R16", "91W", 469), ("225/45R17", "94W", 729), ("235/45R18", "98Y", 999)]),
    ("SPU000021", "BRD000008", "MC000008",
     "美国固铂 DISCOVERER HTS", "固铂HTS", "SUV轮胎 公路舒适 18-19寸",
     "Discoverer HTS 城市SUV公路胎，耐磨与湿地主打",
     [("235/60R18", "103H", 899), ("255/55R19", "111H", 1159)]),

    # ===== 沃凯途 =====
    ("SPU000022", "BRD000009", "MC000007",
     "沃凯途 静音舒适系列 VK5", "沃凯途VK5", "轿车轮胎 静音舒适 15-17寸",
     "沃凯途轿车胎主力花纹，静音舒适 + 耐磨",
     [("195/65R15", "91H", 329), ("205/55R16", "91V", 379), ("215/55R17", "94V", 499)]),
    ("SPU000023", "BRD000009", "MC000008",
     "沃凯途 城市SUV系列 VK8", "沃凯途VK8", "SUV轮胎 城市公路 18-19寸",
     "沃凯途城市SUV胎，舒适静音与承载兼顾",
     [("225/60R18", "100H", 659), ("235/55R19", "101V", 799)]),

    # ===== 赛轮路极 ROADX =====
    ("SPU000024", "BRD000010", "MC000007",
     "赛轮路极 ROADX DH12", "路极DH12", "轿车轮胎 结实耐磨 15-16寸",
     "ROADX DH12，结实耐磨取向的经济型轿车胎",
     [("175/65R15", "88H", 379), ("195/65R15", "91H", 429), ("205/55R16", "91V", 489)]),
    ("SPU000025", "BRD000010", "MC000007",
     "赛轮路极 ROADX DT21 营运胎", "路极DT21", "轿车轮胎 营运耐磨 16-18寸",
     "ROADX DT21 营运胎，高里程耐磨，适配网约车/营运车辆",
     [("205/55R16", "91V", 439), ("215/55R18", "99V", 489)]),
    ("SPU000026", "BRD000010", "MC000008",
     "赛轮路极 ROADX DSU01", "路极DSU01", "SUV轮胎 舒适静音耐磨 18寸",
     "ROADX DSU01 城市SUV胎，舒适静音 + 耐磨",
     [("225/60R18", "100H", 699), ("255/60R18", "112V", 819)]),

    # ===== 达迈 =====
    ("SPU000027", "BRD000011", "MC000007",
     "达迈 GLEDE 静音系列", "达迈GLEDE", "轿车轮胎 静音舒适 15-17寸",
     "达迈 GLEDE 系列，静音舒适，对标国产主流价位",
     [("195/65R15", "91H", 359), ("205/55R16", "91V", 419), ("215/60R17", "96V", 519)]),
    ("SPU000028", "BRD000011", "MC000008",
     "达迈 宝石 耐磨系列", "达迈宝石", "SUV轮胎 耐磨承载 18-19寸",
     "达迈宝石系列，双层帘线结构，耐磨防鼓包",
     [("225/60R18", "100H", 629), ("235/55R19", "101V", 759)]),
]

PROP_SPEC = ("SP000005", "轮胎规格")       # 规格尺寸
PROP_LOAD = ("SP000006", "载重速度级别")    # 载重指数+速度级别

MAT_START = 24      # 现有最大 MAT000023
MAT_SEQ_LEN = 6
SI_START = 13       # 现有最大 SI000012
SPU_START = 7       # 现有最大 SPU000006


def money(x):
    return f"{x:.2f}"


o = StringIO()
w = o.write

w("""-- =============================================================================
-- 轮胎商品初始化数据（SPU + SKU + 销售属性 + 库存）
-- -----------------------------------------------------------------------------
-- 执行方式（必须指定库，脚本内不写 USE）：
--   mysql -h192.168.31.199 -uroot -p123456 --default-character-set=utf8mb4 \\
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
""")

for code, name, parent, sort, remark in CATEGORIES:
    if parent is None:
        parent_expr = "0"
    else:
        parent_expr = f"(SELECT id FROM base_material_category WHERE code = '{parent}')"
    w(f"""INSERT INTO `base_material_category` (`parent_id`, `code`, `name`, `sort`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT {parent_expr}, '{code}', '{name}', {sort}, 1, '{remark}', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_material_category) x WHERE x.`code` = '{code}');
""")

w("""
-- -----------------------------------------------------------------------------
-- 2. 计量单位：条
-- -----------------------------------------------------------------------------
""")
for code, name, sort, remark in UNITS:
    w(f"""INSERT INTO `base_unit` (`code`, `name`, `sort`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT '{code}', '{name}', {sort}, 1, '{remark}', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_unit) x WHERE x.`code` = '{code}');
""")

w("""
-- -----------------------------------------------------------------------------
-- 3. 轮胎品牌（8 个）
-- -----------------------------------------------------------------------------
""")
for code, name, short, remark, sort in BRANDS:
    w(f"""INSERT INTO `base_brand` (`code`, `name`, `short_name`, `status`, `sort`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT '{code}', '{name}', '{short}', 1, {sort}, '{remark}', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_brand) x WHERE x.`code` = '{code}');
""")

# ---- 收集销售属性值（按首次出现顺序编号）
sizes, loads = OrderedDict(), OrderedDict()
for _, _, _, _, _, _, _, skus in SPUS:
    for size, load, _ in skus:
        sizes.setdefault(size, None)
        loads.setdefault(load, None)
si = SI_START
for s in sizes:
    sizes[s] = f"SI{si:06d}"
    si += 1
for l in loads:
    loads[l] = f"SI{si:06d}"
    si += 1

w(f"""
-- -----------------------------------------------------------------------------
-- 4. 销售属性组与属性值
--    轮胎规格（{len(sizes)} 个值）+ 载重速度级别（{len(loads)} 个值）
-- -----------------------------------------------------------------------------
""")
for code, name in (PROP_SPEC, PROP_LOAD):
    w(f"""INSERT INTO `base_sale_property` (`code`, `name`, `sort`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT '{code}', '{name}', 0, 1, '轮胎商品销售属性', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_sale_property) x WHERE x.`code` = '{code}');
""")

w("")
for i, (val, vcode) in enumerate(sizes.items(), start=1):
    w(f"""INSERT INTO `base_sale_property_item` (`property_id`, `code`, `name`, `sort`, `status`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_sale_property WHERE code = '{PROP_SPEC[0]}'), '{vcode}', '{val}', {i}, 1, 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_sale_property_item i2 WHERE i2.`property_id` = (SELECT id FROM base_sale_property WHERE code = '{PROP_SPEC[0]}') AND i2.`name` = '{val}');
""")

w("")
for i, (val, vcode) in enumerate(loads.items(), start=1):
    w(f"""INSERT INTO `base_sale_property_item` (`property_id`, `code`, `name`, `sort`, `status`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_sale_property WHERE code = '{PROP_LOAD[0]}'), '{vcode}', '{val}', {i}, 1, 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_sale_property_item i2 WHERE i2.`property_id` = (SELECT id FROM base_sale_property WHERE code = '{PROP_LOAD[0]}') AND i2.`name` = '{val}');
""")

w(f"""
-- -----------------------------------------------------------------------------
-- 5. SPU（{len(SPUS)} 个花纹系列）
-- -----------------------------------------------------------------------------
""")
for spu_code, brand_code, cat_code, name, short, spec, remark, skus in SPUS:
    size_txt = "/".join(sorted({s for s, _, _ in skus}))
    w(f"""INSERT INTO `base_spu` (`category_id`, `brand_id`, `unit_id`, `code`, `name`, `short_name`, `specification`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = '{cat_code}'),
       (SELECT id FROM base_brand            WHERE code = '{brand_code}'),
       (SELECT id FROM base_unit             WHERE code = 'UNIT000016'),
       '{spu_code}', '{name}', '{short}', '{spec}', 1, '{remark}', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_spu) x WHERE x.`code` = '{spu_code}');
""")

w(f"""
-- -----------------------------------------------------------------------------
-- 6. SPU 绑定两条销售属性维度（轮胎规格 / 载重速度级别）
-- -----------------------------------------------------------------------------
""")
for spu_code, *_ in SPUS:
    for prop_code in (PROP_SPEC[0], PROP_LOAD[0]):
        w(f"""INSERT INTO `base_spu_sale_property` (`spu_id`, `property_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_spu             WHERE code = '{spu_code}'),
       (SELECT id FROM base_sale_property   WHERE code = '{prop_code}'), 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_spu_sale_property p
                            WHERE p.`spu_id` = (SELECT id FROM base_spu WHERE code = '{spu_code}')
                              AND p.`property_id` = (SELECT id FROM base_sale_property WHERE code = '{prop_code}'));
""")

w("""
-- -----------------------------------------------------------------------------
-- 7. SKU（base_material，即商品SKU）
--    命名规则：SPU名称 + 规格 + 载重速度级别
-- -----------------------------------------------------------------------------
""")

mat_seq = MAT_START
sku_rows = []  # (mat_code, spu_code, cat_code, name, spec, prop_text, size, load, purchase, sale, retail, stock)
for spu_code, brand_code, cat_code, name, short, spec, remark, skus in SPUS:
    for size, load, retail in skus:
        mat_code = f"MAT{mat_seq:06d}"
        mat_seq += 1
        sku_name = f"{name} {size} {load}"
        prop_text = f"轮胎规格:{size} / 载重速度级别:{load}"
        purchase = round(retail * 0.62)
        sale = round(retail * 0.80)
        stock = 30 + (mat_seq * 13) % 90
        sku_rows.append((mat_code, spu_code, cat_code, sku_name, f"{size} {load}", prop_text,
                         size, load, purchase, sale, retail, stock))

for (mat_code, spu_code, cat_code, sku_name, spec, prop_text,
     size, load, purchase, sale, retail, stock) in sku_rows:
    w(f"""INSERT INTO `base_material` (`category_id`, `spu_id`, `code`, `name`, `short_name`, `specification`, `sale_property_text`, `unit_id`, `purchase_price`, `sale_price`, `retail_price`, `min_stock`, `max_stock`, `status`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material_category WHERE code = '{cat_code}'),
       (SELECT id FROM base_spu WHERE code = '{spu_code}'),
       '{mat_code}', '{sku_name}', '{sku_name}', '{spec}', '{prop_text}',
       (SELECT id FROM base_unit WHERE code = 'UNIT000016'),
       {money(purchase)}, {money(sale)}, {money(retail)}, 20.00, 300.00, 1, '{spu_code} 轮胎商品', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT code FROM base_material) x WHERE x.`code` = '{mat_code}');
""")

w("""
-- -----------------------------------------------------------------------------
-- 8. SKU 销售属性绑定（base_material_property_rel）
-- -----------------------------------------------------------------------------
""")
for (mat_code, spu_code, cat_code, sku_name, spec, prop_text,
     size, load, purchase, sale, retail, stock) in sku_rows:
    for prop_code, val in ((PROP_SPEC[0], size), (PROP_LOAD[0], load)):
        w(f"""INSERT INTO `base_material_property_rel` (`material_id`, `property_id`, `property_item_id`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = '{mat_code}'),
       (SELECT id FROM base_sale_property WHERE code = '{prop_code}'),
       (SELECT id FROM base_sale_property_item WHERE property_id = (SELECT id FROM base_sale_property WHERE code = '{prop_code}') AND name = '{val}'),
       0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_property_rel r
                            WHERE r.`material_id` = (SELECT id FROM base_material WHERE code = '{mat_code}')
                              AND r.`property_id` = (SELECT id FROM base_sale_property WHERE code = '{prop_code}'));
""")

w("""
-- -----------------------------------------------------------------------------
-- 9. 一品多码：为每条 SKU 生成主编号
-- -----------------------------------------------------------------------------
""")
for (mat_code, *_rest) in sku_rows:
    w(f"""INSERT INTO `base_material_code` (`material_id`, `code`, `code_type`, `is_main`, `remark`, `deleted`, `create_time`, `update_time`)
SELECT (SELECT id FROM base_material WHERE code = '{mat_code}'), '{mat_code}', 1, 1, '轮胎初始化生成', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM base_material_code c WHERE c.`material_id` = (SELECT id FROM base_material WHERE code = '{mat_code}') AND c.`code` = '{mat_code}' AND c.`code_type` = 1);
""")

w("""
-- -----------------------------------------------------------------------------
-- 10. 库存初始化（主仓库 WH001，按采购成本价计价）
-- -----------------------------------------------------------------------------
""")
for (mat_code, spu_code, cat_code, sku_name, spec, prop_text,
     size, load, purchase, sale, retail, stock) in sku_rows:
    amount = purchase * stock
    w(f"""INSERT INTO `product_stock` (`sc_id`, `product_id`, `stock_num`, `tax_price`, `tax_amount`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`)
SELECT 1, (SELECT id FROM base_material WHERE code = '{mat_code}'), {stock}.0000, {money(purchase)}, {money(amount)}, 'system', NOW(), 'system', NOW(), 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM product_stock ps WHERE ps.`sc_id` = 1 AND ps.`product_id` = (SELECT id FROM base_material WHERE code = '{mat_code}'));
""")

w("""
-- -----------------------------------------------------------------------------
-- 11. 编号流水校正（主数据编号必须取「含已逻辑删除记录」的全表最大值）
-- -----------------------------------------------------------------------------
""")
for rule, table, prefix in (
        ("BASE_MATERIAL", "base_material", "MAT"),
        ("BASE_MATERIAL_CATEGORY", "base_material_category", "MC"),
        ("BASE_BRAND", "base_brand", "BRD"),
        ("BASE_UNIT", "base_unit", "UNIT"),
        ("SPU", "base_spu", "SPU"),
        ("SALE_PROPERTY", "base_sale_property", "SP"),
        ("SALE_PROPERTY_ITEM", "base_sale_property_item", "SI")):
    w(f"""UPDATE `sys_code_rule` SET `current_value` = (SELECT COALESCE(MAX(CAST(REGEXP_REPLACE(RIGHT(`code`, 6), '[^0-9]', '') AS UNSIGNED)), 0) FROM `{table}` WHERE `code` LIKE '{prefix}%') WHERE `rule_code` = '{rule}';
""")

w("""
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
""")

with open(OUT, "w", encoding="utf-8", newline="\n") as f:
    f.write(o.getvalue())

print(f"OK -> {OUT}")
print(f"SPU={len(SPUS)} SKU={len(sku_rows)} 规格值={len(sizes)} 载重速度值={len(loads)}")
print(f"MAT 区间: MAT{MAT_START:06d} ~ MAT{mat_seq - 1:06d}")

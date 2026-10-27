# 商品中心 SKU 改造方案

> 编制日期：2026-10-03　｜　状态：**待评审**（评审通过后执行）
> 参照项目：`xingyun`（星云ERP）`V1.23__product_sku_refactor.sql` + `ProductSku*` 系列实体

---

## 一、结论先行

**推荐方案 A：SPU 作为新增维度层，现有 `base_material` 即 SKU。**

一句话概括：**新建 SPU 与销售属性相关表，给 `base_material` 补 4 个字段，单据与库存的 13 张表、58 处 Java 引用、23 个前端文件全部零改动。**

理由如下表。

| 维度 | 方案 A（推荐） | 方案 B（完全对齐 xingyun） |
|---|---|---|
| 表结构变动 | 新增 6 张表 + `base_material` 补 4 列 + 迁移脚本 | **13 张表加 `sku_id`** + 全部明细表改索引 + 改唯一键 |
| Java 改动 | 新增 ~20 个类，老代码 0 改动 | 30 文件 58 处 `productId` 全改，明细 DTO/Entity/Service 全动 |
| 前端改动 | 物料页升级 + 新增 2 个页面 | 23 个文件全改（含 11 个单据 Form 的选品逻辑） |
| 回归风险 | 低（老链路完全不碰） | 高（采购/销售/零售/库存全链路重测） |
| 落地周期 | 6 步，可分批验证 | 10+ 步，必须一次性切换 |
| 与 xingyun 对齐度 | 模型语义一致（SPU↔SKU↔属性↔码），仅列名沿用 `product_id` | 完全一致 |

方案 A 之所以成立，是因为**现有 `base_material` 的粒度本身就已经是 SKU**——它带着 `specification`（规格型号）、独立价格三档、独立库存上下限。缺的只是上层 SPU 聚合与销售属性维度。

---

## 二、现状实测（影响面）

以下数据均为 2026-10-03 实际查询结果，非估算。

### 2.1 数据量 —— 迁移成本近乎为零

| 表 | 行数 |
|---|---|
| `base_material` | **5** |
| `product_stock` | **1** |
| `product_stock_log` | **2** |
| `purchase_order_detail` / `purchase_receipt_detail` / `purchase_return_detail` | 各 **1** |
| 其余 8 张明细表（销售/零售/库存） | **全部 0** |

结论：**这是一次"空库重构"**。迁移脚本不会遇到任何脏数据边界情况，也不需要对存量单据做语义兼容。这正是推荐方案 A 的前提。

### 2.2 含 `product_id` 的表（共 13 张）

```
product_stock              商品库存表
product_stock_log          商品库存日志表
purchase_order_detail      采购订单明细表
purchase_receipt_detail    采购收货明细表
purchase_return_detail     采购退货明细表
sale_order_detail          销售订单明细表
sale_delivery_detail       销售出库明细表
sale_return_detail         销售退货明细表
retail_out_sheet_detail    零售出库单明细
retail_return_detail       零售退货单明细
stock_adjust_sheet_detail  库存调整单明细
stock_check_detail         库存盘点明细表
stock_transfer_detail      库存调拨明细表
```

### 2.3 代码引用面

| 层 | 规模 |
|---|---|
| Java | 30 个文件 / 58 处 `productId`（11 个明细 Entity + 8 个 DTO + `ProductStock*` + `ChartServiceImpl` + 1 Controller） |
| 前端 | 23 个文件（11 个单据的 `Form` + `Detail`，加 2 个 api 文件） |
| 库存服务入口 | `IProductStockService` 4 个方法，签名均以 `Long productId` 为第二个参数 |

### 2.4 现有菜单现状

| id | parent_id | code | title | menu_type | permission |
|---|---|---|---|---|---|
| 5 | 9 | `M000102` | 物料管理 | 0（目录） | — |
| 6 | 5 | `M000103` | 物料分类 | 1 | `basedata:material:category` |
| 8 | 5 | `M000105` | 物料信息 | 1 | `basedata:material:info` |

---

## 三、目标数据模型

### 3.1 分层结构

```
base_spu（SPU·商品）                 ← 新增：聚合层，描述"这个商品是什么"
   │  1:N
   ├── base_material（SKU·物料）      ← 已存在，补 spu_id / sale_property_text
   │        │  N:M（通过关联表）
   │        └── base_sale_property_item（属性值，如"红色"/"XL"）
   │                    │ N:1
   │                    └── base_sale_property（属性组，如"颜色"/"尺码"）
   │
   ├── base_spu_sale_property         ← 新增：SPU 选用哪几个属性组
   ├── base_material_property_rel     ← 新增：SKU 选了哪几个属性值
   └── base_material_code             ← 新增：一品多码（主码/条码）
```

**库存与单据锚定在 SKU（`base_material.id`）**，与现状完全一致。

### 3.2 新增表 DDL（6 张）

> 命名沿用项目既有风格：`base_` 前缀、`BIGINT AUTO_INCREMENT` 主键、`utf8mb4`、
> 继承 `BaseDataEntity` 的公共列（`deleted/create_by/create_time/update_by/update_time`）。

```sql
-- ① SPU 商品主表
CREATE TABLE `base_spu` (
  `id`           BIGINT NOT NULL AUTO_INCREMENT COMMENT 'SPU ID',
  `category_id`  BIGINT NOT NULL COMMENT '分类ID',
  `brand_id`     BIGINT DEFAULT NULL COMMENT '品牌ID',
  `unit_id`      BIGINT NOT NULL COMMENT '基本计量单位ID',
  `code`         VARCHAR(50) NOT NULL COMMENT 'SPU编号',
  `name`         VARCHAR(200) NOT NULL COMMENT '商品名称',
  `short_name`   VARCHAR(100) DEFAULT NULL COMMENT '商品简称',
  `specification` VARCHAR(200) DEFAULT NULL COMMENT '主体规格描述',
  `main_image`   VARCHAR(500) DEFAULT NULL COMMENT '商品主图',
  `status`       TINYINT DEFAULT 1 COMMENT '状态(0=禁用,1=启用)',
  `remark`       VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `deleted`      TINYINT DEFAULT 0 COMMENT '删除标记(0=未删除,1=已删除)',
  `create_by`    VARCHAR(50) DEFAULT NULL COMMENT '创建人',
  `create_time`  DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`    VARCHAR(50) DEFAULT NULL COMMENT '更新人',
  `update_time`  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_brand_id` (`brand_id`),
  KEY `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SPU商品主表';

-- ② 销售属性组（如"颜色"、"尺码"）
CREATE TABLE `base_sale_property` (
  `id`          BIGINT NOT NULL AUTO_INCREMENT COMMENT '属性组ID',
  `code`        VARCHAR(50) NOT NULL COMMENT '属性组编码',
  `name`        VARCHAR(50) NOT NULL COMMENT '属性组名称',
  `sort`        INT DEFAULT 0 COMMENT '排序',
  `status`      TINYINT DEFAULT 1 COMMENT '状态(0=禁用,1=启用)',
  `remark`      VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `deleted`     TINYINT DEFAULT 0 COMMENT '删除标记',
  `create_by`   VARCHAR(50) DEFAULT NULL, `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_by`   VARCHAR(50) DEFAULT NULL, `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品销售属性组';

-- ③ 销售属性值（如"红色"、"XL"）
CREATE TABLE `base_sale_property_item` (
  `id`          BIGINT NOT NULL AUTO_INCREMENT COMMENT '属性值ID',
  `property_id` BIGINT NOT NULL COMMENT '属性组ID',
  `code`        VARCHAR(50) NOT NULL COMMENT '属性值编码',
  `name`        VARCHAR(50) NOT NULL COMMENT '属性值名称',
  `sort`        INT DEFAULT 0 COMMENT '排序',
  `status`      TINYINT DEFAULT 1 COMMENT '状态(0=禁用,1=启用)',
  `remark`      VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `deleted`     TINYINT DEFAULT 0 COMMENT '删除标记',
  `create_by`   VARCHAR(50) DEFAULT NULL, `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_by`   VARCHAR(50) DEFAULT NULL, `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_prop_code` (`property_id`, `code`),
  KEY `idx_property_id` (`property_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品销售属性值';

-- ④ SPU 与属性组的绑定（决定该商品用哪几个维度生成 SKU 矩阵）
CREATE TABLE `base_spu_sale_property` (
  `id`          BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `spu_id`      BIGINT NOT NULL COMMENT 'SPU ID',
  `property_id` BIGINT NOT NULL COMMENT '属性组ID',
  `deleted`     TINYINT DEFAULT 0 COMMENT '删除标记',
  `create_by`   VARCHAR(50) DEFAULT NULL, `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_by`   VARCHAR(50) DEFAULT NULL, `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_spu_property` (`spu_id`, `property_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SPU销售属性绑定';

-- ⑤ SKU 与属性值的绑定（拼出 sale_property_text）
CREATE TABLE `base_material_property_rel` (
  `id`               BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `material_id`      BIGINT NOT NULL COMMENT 'SKU(=base_material.id)',
  `property_id`      BIGINT NOT NULL COMMENT '属性组ID',
  `property_item_id` BIGINT NOT NULL COMMENT '属性值ID',
  `deleted`          TINYINT DEFAULT 0 COMMENT '删除标记',
  `create_by`        VARCHAR(50) DEFAULT NULL, `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_by`        VARCHAR(50) DEFAULT NULL, `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_mat_property` (`material_id`, `property_id`),
  KEY `idx_item` (`property_item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SKU销售属性关系';

-- ⑥ 一品多码
CREATE TABLE `base_material_code` (
  `id`          BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `material_id` BIGINT NOT NULL COMMENT 'SKU ID',
  `code`        VARCHAR(50) NOT NULL COMMENT '编码/条码',
  `code_type`   TINYINT NOT NULL DEFAULT 1 COMMENT '类型(1=主编号,2=条码,3=其他)',
  `is_main`     TINYINT DEFAULT 0 COMMENT '是否主码(0=否,1=是)',
  `remark`      VARCHAR(200) DEFAULT NULL COMMENT '备注',
  `deleted`     TINYINT DEFAULT 0 COMMENT '删除标记',
  `create_by`   VARCHAR(50) DEFAULT NULL, `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_by`   VARCHAR(50) DEFAULT NULL, `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code_type` (`code`, `code_type`),
  KEY `idx_material_id` (`material_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SKU一品多码';
```

### 3.3 `base_material` 增量（4 列，全部可空，向下兼容）

```sql
ALTER TABLE `base_material`
  ADD COLUMN `spu_id`             BIGINT       DEFAULT NULL COMMENT 'SPU ID' AFTER `category_id`,
  ADD COLUMN `sale_property_text` VARCHAR(500) DEFAULT ''   COMMENT '销售属性文本(如:颜色:红 / 尺码:XL)' AFTER `specification`,
  ADD COLUMN `main_image`         VARCHAR(500) DEFAULT NULL COMMENT 'SKU图片' AFTER `sale_property_text`,
  ADD COLUMN `is_multi_code`      TINYINT      DEFAULT 0    COMMENT '是否一品多码(0=否,1=是)' AFTER `main_image`,
  ADD KEY `idx_spu_id` (`spu_id`);
```

> 全部字段可空且有默认值 ⇒ **老代码即使不认识这些列也能正常跑**，可先发后端、再发前端。

### 3.4 数据迁移（脚本已按空库场景简化）

```sql
-- ① 每个存量物料生成一个 1:1 的 SPU，编号沿用物料编号
INSERT INTO base_spu (category_id, brand_id, unit_id, code, name, short_name,
                      specification, status, remark, deleted, create_time, update_time)
SELECT category_id, NULL, unit_id, code, name, short_name,
       specification, status, remark, 0, NOW(), NOW()
FROM base_material WHERE deleted = 0;

-- ② 回填 spu_id
UPDATE base_material m
  JOIN base_spu s ON s.code = m.code
SET m.spu_id = s.id
WHERE m.spu_id IS NULL;

-- ③ 生成主码记录
INSERT INTO base_material_code (material_id, code, code_type, is_main, deleted, create_time, update_time)
SELECT id, code, 1, 1, 0, NOW(), NOW() FROM base_material WHERE deleted = 0;
```

**说明**：1:1 迁移后，运营可在「商品中心」把同款不同规格的物料归并到同一个 SPU 下（改 `spu_id` 即可），
无需一次性人工整理。

---

## 四、Java 改造清单

### 4.1 erp-basedata（新增 ~18 个类）

| 类型 | 文件 |
|---|---|
| Entity | `BaseSpu`、`BaseSaleProperty`、`BaseSalePropertyItem`、`BaseSpuSaleProperty`、`BaseMaterialPropertyRel`、`BaseMaterialCode`（均 `extends BaseDataEntity`） |
| Mapper | 对应 6 个（`extends BaseMapper<T>`） |
| Service + Impl | `IBaseSpuService`、`ISalePropertyService`、`IMaterialCodeService` 三组 |

**关键业务逻辑**（`BaseSpuServiceImpl`）：

1. `saveSpuWithSkus(dto)` —— 事务内：保存 SPU → 按勾选的属性组求**笛卡尔积**生成 SKU 矩阵 →
   每个 SKU 生成 `sale_property_text`（`颜色:红 / 尺码:XL`）+ 明细关联记录 → 自动编号
   （走 `codeGenerateService.generate("MAT")`，SPU 走 `generate("SPU")`）。
2. `buildSkuMatrix(spuId, propertyItemIds)` —— 矩阵生成，**幂等**：已存在的属性组合跳过，只补新增组合。
3. `updateSpu` —— 属性组变更时**只增不删**（已有 SKU 保留，避免破坏库存引用）。

`IMaterialCodeService`：`listByMaterial` / `saveBatch` / 扫码查询 `getByCode(code, codeType)`。

### 4.2 erp-web（新增 3 个 Controller）

| Controller | 端点前缀 | 权限串 |
|---|---|---|
| `SpuController` | `/basedata/spu` | `basedata:spu:*` |
| `SalePropertyController` | `/basedata/sale-property` | `basedata:sale-property:*` |
| `MaterialCodeController` | `/basedata/material-code` | `basedata:material:code:*` |

> 🔴 **铁律**：权限串必须**先写入 `sys_menu.permission` 再写注解**，否则非超管永远 403 且授权页勾不到。
> 顺序 = 先跑菜单 SQL → 清 `erp:auth:*` → 再发后端。

**下拉豁免**：`/basedata/spu/list`、`/basedata/sale-property/list` 作为跨模块下拉数据源，
**不加 `@SaCheckPermission`**（只要求登录），与既有约定一致。

### 4.3 零改动清单（明确列出以免误伤）

- `IProductStockService` 4 个方法签名 —— **不动**
- 11 个明细 Entity、8 个 DTO —— **不动**
- `ProductStockServiceImpl` / `ProductStockLogServiceImpl` —— **不动**
- `MyMetaObjectHandler`、`LogAspect`、`GlobalExceptionHandler` —— **不动**

---

## 五、前端改造清单

### 5.1 新增页面

```
src/pages/Basedata/Product/
├── Spu/
│   ├── index.tsx         SPU 列表（分类/品牌/状态筛选、Excel 导出可后置）
│   └── Form/index.tsx    新增/编辑 + SKU 矩阵生成（勾属性组 → 预览生成 SKU 列表）
├── SaleProperty/
│   └── index.tsx         属性组 + 属性值左右联动维护
└── api 层
    └── src/api/basedata/{spu,saleProperty,materialCode}.ts
```

### 5.2 改造「物料信息」页 → 升级为 SKU 管理

- 新增列：`所属SPU`、`销售属性`（`sale_property_text`）
- 新增筛选：按 SPU 过滤
- 表单新增：SPU 选择（可空）、销售属性值多选、一品多码子表编辑
- ⚠️ **菜单只改 `title`（`物料信息` → `商品SKU`），`code` / `path` / `permission` 一律不动**
  —— 动了权限串就要重授角色，且会撞"注解串必须已存在"的铁律

### 5.3 可选优化：抽取公共选品组件

现状是 11 个单据 `Form` **各自内嵌一个 `<Select>` 加载全量物料**（`getMaterialPage` 拉全量），
代码重复度高且物料变多后会拉爆。建议借本次改造抽出：

```
src/components/SkuSelector/index.tsx
   props: value / onChange / multiple / allowSearch
   内部：按 SPU 二级联动（先选商品 → 再选 SKU），支持条码搜索
```

替换 11 个 Form 的重复逻辑。**此项可独立成一步，失败不影响 SKU 主流程。**

---

## 六、执行计划（6 步，每步可独立验证）

| 步骤 | 内容 | 验收方式 | 可回滚 |
|---|---|---|---|
| **S1** | 执行 DDL + 迁移脚本（新增 6 表、`base_material` 补 4 列、回填 `spu_id`） | 查 `base_spu` 有 5 条、`base_material.spu_id` 无 NULL、`base_material_code` 有 5 条 | 备份后 drop 新表 + drop 列 |
| **S2** | 后端 Entity/Mapper/Service/Controller（SPU + 属性组 CRUD） | 新端点 CRUD 全通；老接口回归无损 | 删新增类 |
| **S3** | SKU 矩阵生成逻辑 + 属性文本回写 | 选"颜色×尺码"两属性，各 2 值 → 生成 4 个 SKU，属性文本正确 | 删矩阵方法 |
| **S4** | 一品多码 | 一个 SKU 挂 3 个码，条码查询命中；重复码被唯一键拦截 | 删表 |
| **S5** | 菜单 SQL + 权限串 + 前端页面（SPU / 属性 / SKU 页升级） | 用采购经理账号验证 403/放行符合预期 | 删菜单 |
| **S6** | 全链路回归（11 个单据选品 + 库存变动） | 见第八节用例 | — |

**每步结束都要 `mvn clean package` 并重启 8082 实例实测**（Java 改动必须重新打包）。

---

## 七、风险与应对

| 风险 | 等级 | 应对 |
|---|---|---|
| 权限串未入库就写注解 → 非超管 403 | **高** | 严格"先 SQL 后代码"，S5 前先执行菜单脚本并清 `erp:auth:*` |
| 菜单 `code`/`permission` 被改动 → 角色授权失效 | **高** | 只允许改 `title`，`code/path/permission` 冻结 |
| SKU 矩阵生成破坏已有库存引用 | 中 | 属性组变更**只增不删**；删 SKU 前校验 `product_stock` 与明细引用 |
| `base_material.code` 唯一键与 SPU 编号冲突 | 低 | SPU 独立编号空间（`SPU` 前缀），与 `MAT` 不重叠 |
| 老数据 `spu_id` 为 NULL 导致页面报错 | 低 | 前端对 `spuId` 为 null 显示"—"，列表不做强制关联 |
| 物料页改 `title` 后用户找不到入口 | 低 | 侧边栏搜索仍命中；必要时保留"物料"作为别名提示 |
| 一次性改动面大 | 中 | 6 步分批，S1/S2 完成后系统即可用（老功能不受影响） |

**回滚方案**：S1 前用 `mysqldump` 全量备份 `base_material` + `product_stock` + 三张有数据的明细表到
`.workbuddy/backup/`。新增表与新增列**不影响老代码**，因此回滚只需 `DROP` 新表/新列，无需回滚数据。

---

## 八、验收用例（S6 全链路回归）

| # | 场景 | 预期 |
|---|---|---|
| 1 | 新建 SPU「螺丝 M8」+ 属性组「材质(不锈钢/碳钢)」「长度(20/30)」 | 生成 4 个 SKU，`sale_property_text` 分别为 `材质:不锈钢 / 长度:20` 等 |
| 2 | 再勾一个属性值「长度 40」 | **只新增 2 个 SKU**，已有 4 个不被重建、库存不受影响 |
| 3 | 编辑某 SKU 的采购价 | 只影响该 SKU，同 SPU 下其他 SKU 价格不变 |
| 4 | 采购订单选择 SPU → SKU 二级联动 | 下拉只显示该 SPU 下的 SKU |
| 5 | 采购收货入库某 SKU | `product_stock` 只新增该 SKU 的行，成本价正确 |
| 6 | 给 SKU 挂 3 个条码，用第 2 个条码查询 | 命中该 SKU |
| 7 | 用重复条码新增 | 被 `uk_code_type` 拦截，提示友好文案 |
| 8 | 采购经理账号访问 `/basedata/spu/page` | 按角色授权结果放行或 403；下拉 `/basedata/spu/list` 始终可用 |
| 9 | 回归：不关联 SPU 的老物料走完 采购订单→收货→库存→销售出库 | 与改造前行为完全一致 |

---

## 九、待确认事项

1. **是否保留 `base_material` 命名？** 方案 A 保留（零改动前提）；若坚持改名 `base_product_sku`，
   则 13 张表的 `product_id` 注释与 Java 实体全部要动，退化为方案 B 的工作量。
2. **仓位商品（库位）是否纳入本批？** 现系统仓库只有单层（`sc_id`），没有库位模型。
   建议**归入下一批「库存体系补齐」**，本批不做。
3. **组合商品（bundle）是否需要？** xingyun 有 `product_sku_bundle`。
   现系统无此概念，建议本批不做，后续按需增补。
4. **是否同步抽取 `SkuSelector` 公共组件？** 建议做，但排在 S6 之后作为独立优化。

---

## 十、实施记录（2026-10-03 已完成）

用户确认采用方案 A 后，S1–S6 已一次性完成并实测通过。以下是落地时对原方案的**修正与补充**。

### 10.1 与原方案的差异（4 处）

| # | 原方案 | 实际落地 | 原因 |
|---|---|---|---|
| 1 | 6 张新表实体均 `extends BaseDataEntity` | **关联表不继承基类、改物理删除**（`BaseSpuSaleProperty` / `BaseMaterialPropertyRel` / `BaseMaterialCode` / `BaseSalePropertyItem` 直接 `implements Serializable`）；`base_spu` / `base_sale_property` 保留逻辑删除 | 这些表的唯一键（如 `uk_spu_property(spu_id, property_id)`）**不含 `deleted` 列**，逻辑删除后重新绑定必然撞唯一键 |
| 2 | 一品多码权限串 `basedata:material:code:*` | **复用 `basedata:material:info`** | 它是物料编辑表单的附属能力，少新增一个权限串就少一份"串未入库导致 403"的风险 |
| 3 | 物料信息菜单 → 改名为「商品SKU」 | 同左，并**额外明确：`code` / `path` / `permission` 全部冻结** | 动权限串要重授所有角色，且会撞"注解串必须已存在"的铁律 |
| 4 | 属性组/属性值与 SKU 编号生成未提及 | 新增编号规则 `SALE_PROPERTY`(SP) / `SALE_PROPERTY_ITEM`(SI) | 走项目既定的"表驱动编号"约定，而非在代码里拼字符串 |

### 10.2 新增的两条实现约束（原方案未覆盖，务必遵守）

1. **属性组变更时，属性值必须差异更新而非重建。**
   如果按"整体删除再插入"，属性值 ID 会变，`base_material_property_rel.property_item_id` 立刻失效，
   所有 SKU 的属性绑定会被静默切断。实现上按**名称**匹配复用旧记录 ID。
2. **主数据编号流水必须取「包含已逻辑删除记录」的最大值。**
   实测踩到：库里存在一条 `deleted=1` 的 `MAT000005`，而 `current_value` 停在 4，
   下一个编号仍然生成 `MAT000005` → `Duplicate entry 'MAT000005' for key 'base_material.uk_code'`，
   新增 SKU 直接 500。`sql/add_sku_center.sql` 第 11 节已统一校正 10 条主数据规则，幂等可重跑。

### 10.3 验收结果

| 测试 | 用例数 | 结果 |
|---|---|---|
| `test_sku_api.py`（SKU 接口） | 29 | **全过** |
| `test_smoke_regression.py`（老链路回归） | 27 | **全过** |
| `vite build`（前端） | — | 通过（3314 模块） |
| `mvn clean package` | — | BUILD SUCCESS |

覆盖的关键场景：矩阵生成 4 个 SKU 且属性文本为「测试颜色:红 / 测试尺码:M」；
增量勾选只新增 2 个、**原有 4 个 SKU 的 ID 不变**；一品多码挂 3 个码并按条码扫码命中；
重复条码被拦截；SPU / 属性组 / 属性值三级删除保护；物料页回填 `spuName` 与 `salePropertyText`；
库存成本价在改造后仍为 110（未被污染）。

### 10.4 落地清单

**SQL**（均已执行）：`sql/add_sku_center.sql`、`sql/add_product_center_menu.sql`

**后端新增 18 个类**：Entity 6（`BaseSpu`、`BaseSaleProperty`、`BaseSalePropertyItem`、
`BaseSpuSaleProperty`、`BaseMaterialPropertyRel`、`BaseMaterialCode`）、Mapper 6、Service+Impl 3 组
（`IBaseSpuService`、`ISalePropertyService`、`IMaterialCodeService`）、Controller 3
（`SpuController`、`SalePropertyController`、`MaterialCodeController`）

**后端改动**：`BaseMaterial`（+5 字段）、`BaseMaterialServiceImpl`（回填 `spuName`、支持 `spuId` 筛选）、
`MaterialPageParam`（+`spuId`）、`BaseSpuMapper` / `BaseSalePropertyMapper`（+`countByCodeIncludeDeleted`）

**前端**：`api/basedata/spu.ts`、`api/basedata/saleProperty.ts`、`api/material/materialCode.ts`、
`pages/Basedata/Product/Spu/index.tsx`、`pages/Basedata/Product/Spu/SkuDrawer.tsx`、
`pages/Basedata/Product/SaleProperty/index.tsx`、`router/index.tsx`（+2 路由）、
`pages/Basedata/Material/Info/index.tsx`（+2 列、+SPU 筛选、+表单 SPU 选择）

### 10.5 后续待办

- **`SkuSelector` 公共选品组件**：11 个单据 Form 仍各自内嵌 `<Select>` 拉全量物料，
  物料变多后会拉爆，建议单独立项做二级联动（选 SPU → 选 SKU + 条码搜索）。
- **仓位商品（库位）**：依赖库位模型，归入下一批「库存体系补齐」。
- **组合商品 bundle**：现系统无此概念，按需再评估。

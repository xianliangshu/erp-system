# ERP 系统功能分析与后台菜单设计

> 分析对象：`D:/code/java/java_study/erp-system`（自研 ERP，参照 `xingyun` 星云 ERP 开发）
> 分析依据：5 个 Maven 模块源码（37 个 Controller / 27 个业务 Service / 约 130 个实体）、`sql/` 下 41 个脚本中的 `sys_menu` 插入语句、`erp-adminPro` 前端页面树与路由表、`NEXT_STEPS.md` 开发计划，以及 `xingyun/` 下 4 份源码级分析报告（星云 1213 个 Java 文件 / 190 张表 / 253 条菜单项）。
> 日期：2026-10-01

---

## 一、结论速览

| 维度 | 现状 |
|---|---|
| 项目形态 | Spring Boot 3.5.0 单体多模块（Maven 5 模块），JDK 21，MyBatis-Plus 3.5.7，Knife4j 4.5.0 |
| 后端接口 | 37 个 Controller，覆盖系统管理 / 基础数据 / 采购 / 销售 / 库存 / 零售 / 结算 / 报表 8 大域 |
| 前端 | `erp-adminPro`（React + Vite + TS），页面树与后端接口基本一一对应 |
| 菜单模型 | `sys_menu` 单表树形（`parent_id`），`menu_type` 0=目录 / 1=菜单 / 2=按钮 |
| 对照 xingyun | 功能覆盖约 **60%**；缺失 **物流管理、商品中心(SKU体系)、流程中心、消息中心、开发管理、便携办公** 6 大块 |
| ⚠️ 关键风险 | **后端没有任何权限校验**（无拦截器 / 无 `@PreAuthorize` / 无 `@SaCheckPermission`），`sys_menu.permission` 目前只驱动前端按钮显隐，不构成安全边界 |
| ⚠️ 菜单数据 | 现有菜单 SQL 存在 **编码重复（M000105）、编码位数不统一、零售标题乱码、采购收货菜单缺失** 等问题，需先修数据再做扩展 |

---

## 二、项目现状

### 2.1 工程结构

```
erp-system (erp-parent, pom)
├── erp-common        通用：Result / PageResult / BusinessException / GlobalExceptionHandler / PasswordUtil
├── erp-system        系统管理：组织-角色-用户-菜单-字典 + MyBatis-Plus 配置
├── erp-basedata      基础数据：仓库 / 供应商 / 客户 / 品牌 / 物料分类 / 计量单位 / 物料
├── erp-business      业务中台：采购 / 销售 / 零售 / 库存 / 结算 / 报表（约 130 个实体）
├── erp-web           启动模块 + 全部 37 个 Controller + Knife4j / CORS 配置
├── erp-adminPro      前端（React 18 + Vite + TypeScript + Ant Design 风格）
└── sql/              41 个建表 / 种子 / 菜单脚本
```

### 2.2 已实现功能域（后端 Controller ↔ 前端页面）

| 功能域 | Controller（路由前缀） | 前端页面 |
|---|---|---|
| 认证 | `/auth`（login / logout） | `Login` |
| 系统管理 | `/system/user`、`/system/role`、`/system/dept`、`/system/menu`、`/system/dict` | `System/User|Role|Dept|Menu`（**字典无页面**） |
| 基础数据 | `/basedata/warehouse`、`/supplier`、`/customer`、`/brand`、`/material`、`/material-category`、`/unit` | `Basedata/*` |
| 采购 | `/business/purchase/order`、`/receipt`、`/return` | `Business/Purchase/{Order,Receipt,Return}/{List,Form,Detail}` |
| 销售 | `/business/sale/order`、`/delivery`、`/return` | `Business/Sale/{Order,Delivery,Return}/{List,Form,Detail}` |
| 库存 | `/business/stock`、`/business/stock/check`、`/business/stock/transfer`、`/stock/adjust/sheet`、`/stock/adjust/reason` | `Business/Stock/{Query,Check,Transfer,Adjust}` |
| 零售 | `/retail/out`、`/retail/return`、`/retail/config` | `Business/Retail/{Config,Outbound,Return}` |
| 结算 | `/business/settle/{item,fee,pre,check,sheet}` + `/business/settle/customer/{fee,pre,check,sheet}` | `Business/Settle/*` |
| 报表 | `/chart/{dashboard,purchase,sales,stock,summary}` | `Business/Chart/*` |

### 2.3 权限与菜单模型（**重要**）

`sys_menu` 表字段：`parent_id / code / name / title / menu_type / path / component / permission / icon / sort / visible / status / remark`。

- `menu_type`：**0 = 目录**，**1 = 菜单（页面）**，**2 = 按钮权限**
- 授权链：`sys_user_role` → `sys_role_menu` → `sys_menu`
- 前端拿 `GET /system/menu` 渲染侧边栏，并用 `permission` 控制按钮显隐

> **实测结论**：全仓 grep `@PreAuthorize` / `@SaCheckPermission` / `RequiresPermission` / `hasPermission` **零命中**；`erp-web` 下只有 `CorsFilter`，**没有登录拦截器、没有 JWT 解析过滤器**；`AuthController` 只发 token 不校验。`erp-common` 中 `LoginResult` 有 `token` 字段但无 `JwtUtil`。
> 即：**当前权限体系只到"前端按钮隐藏"这一层，任何登录用户都可直接调任意接口。** 这是后续菜单权限设计必须先堵上的洞。

---

## 三、参照项目 xingyun 功能全景（12 大域）

| # | 域 | 主要功能 | ERP 是否具备 |
|---|---|---|---|
| 1 | **系统与平台管理** | 菜单(253项) / 部门 / 岗位 / 角色+角色分类 / 用户+用户分组+多部门 / 操作日志 / 系统参数 / 通知公告 / 数据字典(分类-字典-字典值三级) / 编号规则 / 租户(独立物理库) / 模块授权 / 开放域 / 定时器(Quartz) / 数据权限 | 部分（缺岗位、操作日志、参数、编号规则、数据权限） |
| 2 | **基础资料** | 仓库 / 仓位(库位) / 仓位商品 / 客户 / 供应商 / 会员 / 门店 / 地址库 / 物流公司 / 支付方式 / 打印模板 | 部分（缺仓位、会员、门店、地址库、物流公司、支付方式、打印模板） |
| 3 | **商品中心** | SPU+SKU / 一品多码 / 三套价格(采购·销售·零售) / 销售属性 / 类目属性 / 组合商品(BUNDLE) / 导入 / SKU选择器 | ❌（现有"物料"是扁平模型） |
| 4 | **采购管理** | 采购订单 → 采购收货单 → 采购退货单 + 采购参数设置 + BPM审批(唯一接入) | ✅（缺参数设置） |
| 5 | **销售管理** | 销售订单 → 销售出库单 → 销售退货单 + 销售参数设置 + 成本分层(lot) | ✅（缺参数设置、成本分层） |
| 6 | **零售管理** | 零售出库单 → 零售退货单 + 零售参数设置（⚠️星云零售不进结算链路） | ✅ |
| 7 | **库存管理** | 库存查询 / 库存变动记录 / 库存预警(上下限+通知组) / 库存调整(原因+单据) / 仓库调拨(支持部分收货) / 盘点四件套(预先盘点单·盘点任务·盘点单·盘点参数) + 移动加权平均成本 | 部分（缺预警、变动记录、盘点任务体系） |
| 8 | **结算管理** | 收支项目 + 供应商侧4单(费用·预付款·对账单·结算单) + 客户侧4单(费用·预收款·对账单·结算单)，CAS乐观锁核销、余额不变号 | ✅（结构已对齐） |
| 9 | **物流管理** | 物流单(一单绑多张出库单) / 批量发货 / 重量体积计算 / 腾讯地图地址解析 | ❌ |
| 10 | **报表** | 数据看板 + 各类统计（星云仅"业务类型+时间"2维度，能力很弱） | ✅（ERP 的报表反而更细） |
| 11 | **流程中心 / 消息中心** | BPM(Warm-Flow 流程设计·待办·我的发起) / 公告·站内信·邮件·消息通知组 | ❌ |
| 12 | **开发管理 / 便捷办公** | 定时器·数据实体·自定义列表/选择器/表单/页面 / 文件收纳箱·在线Excel | ❌ |

---

## 四、后台菜单设计

### 4.1 设计原则

1. **编码规则**：统一 `M` + 6 位数字。
   - 一级目录 `M{域}00000`，如 `M100000`；
   - 二级菜单 `M{域}{序}000`，如 `M101000`；
   - 三级菜单 `M{域}{序}{子}00`，如 `M101100`；
   - 按钮权限 `B{对应菜单码}` 或 `M...+1` 递增（推荐 `B` 前缀便于区分）。
   > 现有数据用的是 `M000001 / M000101 / M000201` 混合位数体系，见 §5 的修复建议；下表**保留现有编码不动**，只补齐缺号，降低迁移成本。
2. **权限串规则**：统一 `域:模块:动作`，全小写英短横线。如 `system:user:list`、`basedata:warehouse:add`、`purchase:order:approve`。
   > 现状三种风格混用（`basedata:*` / `business:*` / `stock:*`），且 `M000011` 写成 `system:role:授权`（**中文混入**），必须统一。
3. **按钮权限不建独立菜单行渲染**：`menu_type=2` 只做权限标识，前端不出现在侧边栏。
4. **路由与组件**：`path` 与前端 `router/index.tsx` 一致，`component` 与 `pages/` 目录结构一致（当前项目两者已基本对齐）。

### 4.2 完整菜单树

图例：✅ 已实现（菜单+接口+页面） · 🟡 部分（接口/实体在，菜单或页面缺） · ⬜ 待开发

#### 0. 工作台

| 编码 | 名称 | 类型 | 路由 | 组件 | 权限串 | 状态 |
|---|---|---|---|---|---|---|
| M000000 | 工作台 | 菜单 | `/dashboard` | `Dashboard/index` | — | ✅ |

#### 1. 系统管理 `M000001` (`/system`)

| 编码 | 名称 | 类型 | 路由 | 组件 | 权限串 | 状态 |
|---|---|---|---|---|---|---|
| M000002 | 用户管理 | 菜单 | `/system/user` | `System/User` | `system:user:list` | ✅ |
| M000003~006 | 新增/修改/删除/重置密码 | 按钮 | — | — | `system:user:add\|edit\|delete\|resetPwd` | ⬜ 未建 |
| M000007 | 角色管理 | 菜单 | `/system/role` | `System/Role` | `system:role:list` | ✅ |
| M000008~011 | 新增/修改/删除/分配权限 | 按钮 | — | — | `system:role:add\|edit\|delete\|assign` | ⬜ 未建（且现有权限串含中文） |
| M000012 | 部门管理 | 菜单 | `/system/dept` | `System/Dept` | `system:dept:list` | ✅ |
| M000013~015 | 新增/修改/删除 | 按钮 | — | — | `system:dept:add\|edit\|delete` | ⬜ 未建 |
| M000016 | 菜单管理 | 菜单 | `/system/menu` | `System/Menu` | `system:menu:list` | ✅ |
| M000017~019 | 新增/修改/删除 | 按钮 | — | — | `system:menu:add\|edit\|delete` | ⬜ 未建 |
| **M000020** | **字典管理** | 菜单 | `/system/dict` | `System/Dict` | `system:dict:list` | 🟡 后端 `SysDictController` 已实现，**菜单与前端页面缺** |
| **M000021** | **操作日志** | 菜单 | `/system/operation-log` | `System/OperationLog` | `system:log:operation:list` | 🟡 `SysOperationLog` 实体在，**Controller 与页面缺** |
| **M000022** | **登录日志** | 菜单 | `/system/login-log` | `System/LoginLog` | `system:log:login:list` | 🟡 `SysLoginLog` 实体在，**Controller 与页面缺** |
| M000023 | 系统参数 | 菜单 | `/system/param` | `System/Param` | `system:param:list` | ⬜ |
| M000024 | 编号规则 | 菜单 | `/system/code-rule` | `System/CodeRule` | `system:code-rule:list` | ⬜ |
| M000025 | 消息通知 | 菜单 | `/system/notice` | `System/Notice` | `system:notice:list` | ⬜ |

#### 2. 基础数据 `M000100` (`/basedata`)

| 编码 | 名称 | 类型 | 路由 | 组件 | 权限串 | 状态 |
|---|---|---|---|---|---|---|
| M000101 | 仓库管理 | 菜单 | `/basedata/warehouse` | `Basedata/Warehouse` | `basedata:warehouse:list` | ✅ 含 4 个按钮权限 |
| M000102 | 物料管理 | 目录 | `/basedata/material` | — | — | ✅ |
| M000103 | 物料分类 | 菜单 | `/basedata/material/category` | `Basedata/Material/Category` | `basedata:material:category` | ✅ |
| M000104 | 计量单位 | 菜单 | `/basedata/material/unit` | `Basedata/Material/Unit` | `basedata:material:unit` | ✅ |
| M000105 | 物料信息 | 菜单 | `/basedata/material/info` | `Basedata/Material/Info` | `basedata:material:info` | ✅ ⚠️编码与供应商冲突 |
| M000106 | 供应商管理 | 菜单 | `/basedata/supplier` | `Basedata/Supplier` | `basedata:supplier:list` | ✅ ⚠️现脚本误用 M000105 |
| M000107 | 客户管理 | 菜单 | `/basedata/customer` | `Basedata/Customer` | `basedata:customer:list` | ✅ |
| M000108 | 品牌管理 | 菜单 | `/basedata/brand` | `Basedata/Brand` | `basedata:brand:list` | ✅ |
| M000109 | 支付方式 | 菜单 | `/basedata/pay-type` | `Basedata/PayType` | `basedata:pay-type:list` | ⬜ |
| M000110 | 物流公司 | 菜单 | `/basedata/logistics-company` | `Basedata/LogisticsCompany` | `basedata:logistics:list` | ⬜ |
| M000111 | 会员管理 | 菜单 | `/basedata/member` | `Basedata/Member` | `basedata:member:list` | ⬜（零售场景需要） |
| M000112 | 门店管理 | 菜单 | `/basedata/shop` | `Basedata/Shop` | `basedata:shop:list` | ⬜（零售场景需要） |
| M000113 | 地址库 | 菜单 | `/basedata/address` | `Basedata/Address` | `basedata:address:list` | ⬜（物流场景需要） |
| M000114 | 打印模板 | 菜单 | `/basedata/print-template` | `Basedata/PrintTemplate` | `basedata:print:list` | ⬜ |

#### 3. 商品中心 `M001000` (`/product`) — 建议新增

> 现有"物料"是**扁平模型**（无 SKU、无一品多码、无销售属性、无组合商品）。星云的商品中心是 ERP 的差异化能力，建议独立成域而非塞进基础数据。

| 编码 | 名称 | 类型 | 路由 | 权限串 | 状态 |
|---|---|---|---|---|---|
| M001001 | 商品档案(SPU) | 菜单 | `/product/spu` | `product:spu:list` | ⬜ |
| M001002 | 商品规格(SKU) | 菜单 | `/product/sku` | `product:sku:list` | ⬜ |
| M001003 | 商品分类 | 菜单 | `/product/category` | `product:category:list` | ⬜ |
| M001004 | 销售属性 | 菜单 | `/product/sale-property` | `product:property:sale:list` | ⬜ |
| M001005 | 类目属性 | 菜单 | `/product/category-property` | `product:property:category:list` | ⬜ |
| M001006 | 组合商品 | 菜单 | `/product/bundle` | `product:bundle:list` | ⬜ |
| M001007 | 商品导入 | 菜单 | `/product/import` | `product:import` | ⬜ |

#### 4. 采购管理 `M000300` (`/purchase`)

| 编码 | 名称 | 类型 | 路由 | 组件 | 权限串 | 状态 |
|---|---|---|---|---|---|---|
| M000301 | 采购订单 | 菜单 | `/business/purchase/order` | `Business/Purchase/Order/List` | `business:purchase:order` | ✅ |
| **M000302** | **采购收货单** | 菜单 | `/business/purchase/receipt` | `Business/Purchase/Receipt/List` | `business:purchase:receipt` | 🟡 **功能已实现，菜单脚本缺失** |
| M000303 | 采购退货单 | 菜单 | `/business/purchase/return` | `Business/Purchase/Return/List` | `business:purchase:return` | ✅ |
| M000304 | 采购参数设置 | 菜单 | `/purchase/config` | `Purchase/Config` | `purchase:config:query` | ⬜ |

#### 5. 销售管理 `M000400` (`/sale`)

| 编码 | 名称 | 类型 | 路由 | 组件 | 权限串 | 状态 |
|---|---|---|---|---|---|---|
| M000401 | 销售订单 | 菜单 | `/business/sale/order` | `Business/Sale/Order/List` | `business:sale:order` | ✅ |
| M000402 | 销售出库单 | 菜单 | `/business/sale/delivery` | `Business/Sale/Delivery/List` | `business:sale:delivery` | ✅ |
| M000403 | 销售退货单 | 菜单 | `/business/sale/return` | `Business/Sale/Return/List` | `business:sale:return` | ✅ |
| M000404 | 销售参数设置 | 菜单 | `/sale/config` | `Sale/Config` | `sale:config:query` | ⬜ |

#### 6. 零售管理 `M000600` (`/retail`)

| 编码 | 名称 | 类型 | 路由 | 组件 | 权限串 | 状态 |
|---|---|---|---|---|---|---|
| M000601 | 零售配置 | 菜单 | `/business/retail/config` | `Business/Retail/Config` | `business:retail:config` | ✅ |
| M000602 | 零售出库单 | 菜单 | `/business/retail/out` | `Business/Retail/Outbound/List` | `business:retail:out` | ✅ |
| M000603 | 零售退货单 | 菜单 | `/business/retail/return` | `Business/Retail/Return/List` | `business:retail:return` | ✅ ⚠️标题乱码 |

#### 7. 库存管理 `M000200` (`/inventory`)

| 编码 | 名称 | 类型 | 路由 | 组件 | 权限串 | 状态 |
|---|---|---|---|---|---|---|
| M000201 | 库存查询 | 菜单 | `/business/stock/query` | `Business/Stock/Query` | `business:stock:query` | ✅ |
| M000202 | 库存变动记录 | 菜单 | `/business/stock/log` | `Business/Stock/Log` | `business:stock:log` | 🟡 `ProductStockLog` 实体在 |
| M000203 | 库存调整 | 菜单 | `/stock/adjust/sheet` | `Business/Stock/Adjust/Sheet/List` | `stock:adjust:query` | ✅ |
| M000204 | 库存调整原因 | 菜单 | `/stock/adjust/reason` | `Business/Stock/Adjust/Reason` | `stock:adjust:reason:query` | ✅ |
| **M000502** | **库存盘点** | 菜单 | `/business/stock/check` | `Business/Stock/Check/List` | `business:stock:check` | ✅ ⚠️编码应归入 M0002 段 |
| **M000503** | **库存调拨** | 菜单 | `/business/stock/transfer` | `Business/Stock/Transfer/List` | `business:stock:transfer` | ✅ ⚠️编码应归入 M0002 段 |
| M000504 | 库存预警 | 菜单 | `/business/stock/warning` | `Business/Stock/Warning` | `stock:warning:query` | ⬜ |
| M000505 | 盘点任务 | 菜单 | `/business/stock/take/plan` | `Business/Stock/Take/Plan` | `stock:take:plan:query` | ⬜（星云有此能力，ERP 只有单层盘点） |
| M000506 | 盘点参数 | 菜单 | `/business/stock/take/config` | `Business/Stock/Take/Config` | `stock:take:config:query` | ⬜ |

#### 8. 结算管理 `M000700` (`/settle`)

| 编码 | 名称 | 类型 | 路由 | 权限串 | 状态 |
|---|---|---|---|---|---|
| M000701 | 收支项目 | 菜单 | `/business/settle/item` | `business:settle:item` | ✅ |
| M000702 | 供应商费用单 | 菜单 | `/business/settle/fee` | `business:settle:fee` | ✅ |
| M000703 | 供应商预付款单 | 菜单 | `/business/settle/pre` | `business:settle:pre` | ✅ |
| M000704 | 供应商对账单 | 菜单 | `/business/settle/check` | `business:settle:check` | ✅ |
| M000705 | 供应商结算单 | 菜单 | `/business/settle/sheet` | `business:settle:sheet` | ✅ |
| M000706 | 客户费用单 | 菜单 | `/business/settle/customer/fee` | `business:settle:customer:fee` | ✅ |
| M000707 | 客户预收款单 | 菜单 | `/business/settle/customer/pre` | `business:settle:customer:pre` | ✅ |
| M000708 | 客户对账单 | 菜单 | `/business/settle/customer/check` | `business:settle:customer:check` | ✅ |
| M000709 | 客户结算单 | 菜单 | `/business/settle/customer/sheet` | `business:settle:customer:sheet` | ✅ |

#### 9. 物流管理 `M000900` (`/logistics`) — 建议新增

| 编码 | 名称 | 类型 | 路由 | 权限串 | 状态 |
|---|---|---|---|---|---|
| M000901 | 物流单 | 菜单 | `/business/logistics/sheet` | `logistics:sheet:query` | ⬜ |
| M000902 | 批量发货 | 菜单 | `/business/logistics/delivery` | `logistics:delivery:import` | ⬜ |

#### 10. 报表统计 `M000800` (`/chart`)

| 编码 | 名称 | 类型 | 路由 | 权限串 | 状态 |
|---|---|---|---|---|---|
| M000801 | 数据看板 | 菜单 | `/chart/dashboard` | `chart:dashboard` | ✅ |
| M000802 | 采购统计 | 菜单 | `/chart/purchase` | `chart:purchase` | ✅ |
| M000803 | 销售统计 | 菜单 | `/chart/sales` | `chart:sales` | ✅ |
| M000804 | 库存报表 | 菜单 | `/chart/stock` | `chart:stock` | ✅ |
| M000805 | 进销存汇总 | 菜单 | `/chart/summary` | `chart:summary` | ✅ |
| M000806 | 往来对账汇总 | 菜单 | `/chart/settle` | `chart:settle` | ⬜ |

#### 11. 消息中心 `M001100` — 建议新增

| 编码 | 名称 | 类型 | 路由 | 权限串 | 状态 |
|---|---|---|---|---|---|
| M001101 | 通知公告 | 菜单 | `/message/notice` | `message:notice:list` | ⬜ |
| M001102 | 站内信 | 菜单 | `/message/site` | `message:site:list` | ⬜ |
| M001103 | 消息通知组 | 菜单 | `/message/group` | `message:group:list` | ⬜ |

#### 12. 流程中心 `M001200` — 建议新增（可选，工作量大）

| 编码 | 名称 | 类型 | 路由 | 权限串 | 状态 |
|---|---|---|---|---|---|
| M001201 | 流程设计 | 菜单 | `/bpm/definition` | `bpm:definition:list` | ⬜ |
| M001202 | 我的发起 | 菜单 | `/bpm/my-start` | `bpm:instance:mine` | ⬜ |
| M001203 | 待办任务 | 菜单 | `/bpm/todo` | `bpm:task:todo` | ⬜ |

---

## 五、现有菜单数据的问题（扩展前必须先修）

| # | 严重度 | 问题 | 位置 | 影响 |
|---|---|---|---|---|
| 1 | 🔴 高 | **编码重复**：`M000105` 同时用于「物料信息」和「供应商管理」 | `material_menu.sql` vs `add_supplier_menu_final.sql` | 若 `code` 有唯一索引则第二个脚本直接失败；无索引则菜单树出现重复/错位 |
| 2 | 🔴 高 | **零售菜单中文乱码**：`'闆跺敭绠＄悊'`（UTF-8 被按 GBK 解读的"零售管理"），且 `'闆跺敭鍑哄簱鍗?` **单引号未闭合** | `retail_module_utf8.sql` L84-104 | 直接执行会**语法报错**；即使执行成功侧边栏显示乱码 |
| 3 | 🔴 高 | **采购收货菜单缺失**：`M000302` 全仓不存在，`purchase_receipt.sql` 无 `sys_menu` 插入 | `purchase_receipt.sql` | 采购收货功能完整实现但**侧边栏进不去** |
| 4 | 🟠 中 | **编码位数不统一**：`M0002 / M0003 / M0004 / M0006 / M0007`（4 位）与 `M000001 / M000100 / M000201`（6 位）混用 | `add_inventory_menu.sql` 等 | 排序混乱，与 `M0002xx`（库存子菜单）前缀冲突，无法按编码推导层级 |
| 5 | 🟠 中 | **库存子菜单编码跨段**：库存管理下用 `M0002xx`（查询/调整）和 `M0005xx`（盘点/调拨）两段 | `stock_check.sql`、`stock_transfer.sql` | 语义与编码不符；`M0005` 段其实是空的（无 M0005 目录） |
| 6 | 🟠 中 | **父级取法不一致**：`add_inventory_menu.sql` 建了「库存管理」目录，但 `stock_adjust.sql` 用 `@parent_id`，`stock_check.sql`/`stock_transfer.sql` 用 `parent_id of stock-query` | 多个脚本 | 依赖执行顺序，先跑后跑结果不同 |
| 7 | 🟠 中 | **权限串混入中文**：`M000011` 权限串写作 `'system:role:授权'` | `user-management-database-design.sql` | 权限串失效 |
| 8 | 🟡 低 | **权限串前缀三种风格**：`basedata:*` / `business:*` / `stock:*` / `system:*` / `chart:*` | 全量 | 后期做权限拦截时难以统一 `if` 判断 |
| 9 | 🟡 低 | **按钮权限只有仓库有**（`B000101~B000104`），其余模块全缺 | `menu_warehouse.sql` | `menu_type=2` 形同虚设，角色分配只能到页面级 |
| 10 | 🟡 低 | **字典管理无菜单**：`SysDictController` 已实现 `/system/dict`，前端无页面、菜单无记录 | — | 功能存在但不可达 |

---

## 六、落地建议（按优先级）

### P0 — 菜单数据修复（1 天）
1. 先备份 `sys_menu` / `sys_role_menu`：`CREATE TABLE sys_menu_bak_20261001 AS SELECT * FROM sys_menu;`
2. 修 `retail_module_utf8.sql` 乱码与未闭合引号，或改用 `retail_module_simple.sql` 重建。
3. 补 `M000302 采购收货单` 菜单。
4. 重编 `M000105` 冲突（建议供应商保留 `M000106`，物料信息保留 `M000105`，删除重复插入）。
5. 统一权限串前缀 + 清掉 `system:role:授权`。

### P1 — 补起"有代码无菜单"的三处
`字典管理`、`采购收货单`、`库存变动记录`；再补 `操作日志`/`登录日志`（实体已在 `erp-system/entity`，只差 Controller + 页面）。

### P2 — 权限真正生效（**最高技术优先级**）
当前后端零校验。建议：
- 引入 Sa-Token（项目已有 `sys_role_menu` 授权链，接入成本低）或自写 `HandlerInterceptor` + JWT 解析；
- 在 `erp-web` 加 `AuthInterceptor`，白名单 `/auth/login`、Knife4j 路径；
- 给 Controller 方法加 `@SaCheckPermission("basedata:warehouse:add")`，权限串与 `sys_menu.permission` 对齐；
- 前端按钮显隐继续用同一套权限串，做到"前后端同源"。

### P3 — 按 §4 目录补功能域
顺序建议：**物流管理** → **库存预警/盘点任务** → **基础数据补全（会员/门店/地址/支付方式/物流公司/打印模板）** → **商品中心(SKU)** → **消息中心** → **流程中心**。

---

## 附：快速自查命令

```bash
# 查看当前菜单树（按父级分组）
mysql -e "SELECT m.id, m.parent_id, p.title AS parent, m.code, m.title, m.menu_type, m.path, m.permission
          FROM sys_menu m LEFT JOIN sys_menu p ON m.parent_id=p.id
          ORDER BY m.parent_id, m.sort;"

# 查编码重复
mysql -e "SELECT code, COUNT(*) c FROM sys_menu GROUP BY code HAVING c>1;"

# 查孤儿节点（parent_id 指向不存在的菜单）
mysql -e "SELECT m.* FROM sys_menu m LEFT JOIN sys_menu p ON m.parent_id=p.id
          WHERE m.parent_id>0 AND p.id IS NULL;"

# 查 admin 未被授权的菜单
mysql -e "SELECT m.id,m.code,m.title FROM sys_menu m
          WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id=1 AND rm.menu_id=m.id);"
```

---

## 附录 B：执行记录（2026-10-01）

> 本节记录 P0 + P1 前半部分的**实际执行结果**，其余章节为分析内容，保持原文。

### B.1 执行前的真实数据库状态（与 SQL 脚本严重脱节）

对 `erp-data` 实测，`sys_menu` **只有 16 条**，而前端路由有 100 条、后端 Controller 37 个。具体缺口：

| 类别 | 缺失内容 |
|---|---|
| 整棵树缺失 | **系统管理**（用户/角色/部门/菜单）、**销售管理**、**零售管理**、**结算管理（9项）**、**统计报表（5项）** |
| 部分缺失 | 基础数据的 客户管理 / 品牌管理；库存的 库存调整 / 调整原因 / 库存盘点 / 库存调拨；采购的 采购收货 |
| **孤儿节点** | `M000102 物料管理` parent_id=20、`M000106 供应商管理` parent_id=27，而 id 20/27 **不存在** → 侧边栏不显示 |
| **授权断裂** | `sys_role_menu` 中 role_id=1 **只绑定了子菜单**（id 5,6,7,8,2,3,4,15,16），**没有绑定任何目录**（id 1、9） → 菜单树无法正确组装 |

**结论：script 里"写了"不等于库里"有"。** `sql/` 下 30+ 个菜单脚本互相独立、没有执行顺序约定，实际只跑了其中一小部分。

### B.2 修复动作与结果

| 步骤 | 动作 | 结果 |
|---|---|---|
| 备份 | `mysqldump sys_menu sys_role_menu` → `.workbuddy/backup/sys_menu_before_20261001.sql` | 5260 B，含全量数据 |
| 编码规范化 | `M0002→M000200`、`M0003→M000300`（对齐 `SysMenuServiceImpl.generateMenuCode()` 的 `M%06d` 约定） | 无重复编码 |
| 菜单 UPSERT | 按 `uk_code` 唯一键 upsert 全量 71 条菜单 | `sys_menu` **16 → 71** |
| parent 回填 | 临时映射表按 `code→parent_code` 统一回填 `parent_id` | **孤儿节点 0** |
| 权限串修正 | `system:role:授权` → `system:role:assign` | 已修正 |
| 授权补全 | 给 role_id=1 upsert 全部菜单（目录+菜单+按钮） | **未授权菜单 0** |

**新增/补齐的菜单**（重点三项已标注）：
- 系统管理：用户/角色/部门/菜单/**字典管理(M000020)** + 21 个按钮权限
- 基础数据：客户管理(M000107)、品牌管理(M000108)
- 库存管理：**库存流水(M000202)**、库存调整(M000203)、调整原因(M000204)、库存盘点(M000205)、库存调拨(M000206)
- 采购管理：**采购收货(M000302)**
- 销售管理：订单/出库/退货（M000401~403）
- 零售管理：配置/出库/退货（M000601~603）
- 结算管理：收支项目 + 供应商 4 单 + 客户 4 单（M000701~709）
- 统计报表：看板/采购/销售/库存/汇总（M000801~805）

统一脚本：**`sql/fix_menu_full.sql`**（幂等三段式：UPSERT → 映射回填 parent_id → 全量授权）。
⚠️ `sql/patch_menu_design.sql` 已被它取代，文件头已加废弃标记；`sql/` 下 17 个历史菜单脚本同样作废。

### B.3 "有代码无菜单"三处的处理

| 功能 | 后端 | 前端 | 菜单 | 说明 |
|---|---|---|---|---|
| **字典管理** | 已有 `SysDictController`（8 个端点） | **新建** `pages/System/Dict`（左类型右数据双栏）+ `api/system/dict.ts` | ✅ M000020 | 此前功能存在但**完全不可达** |
| **采购收货** | 已有 `PurchaseReceiptController` | 已有页面 + 路由 | ✅ M000302 | 仅补菜单 |
| **库存流水** | **新建** `IProductStockLogService` + `Impl` + `ProductStockLogVO` + `ProductStockLogController` | **新建** `pages/Business/Stock/Log` + `api/business/stockLog.ts` | ✅ M000202 | 此前只有实体和 Mapper，缺口最大；前端 `api/business/stock.ts` 里甚至已预留了 `getProductStockLogPage` 指向不存在的接口 |

新增后端接口：`GET /business/stock/log/page`（仓库/商品名/业务类型/时间区间筛选）、`GET /business/stock/log/biz-types`。
实现细节：关联字段（仓库名、商品编号/名称/规格/单位）用 `selectBatchIds` **批量装配**而非逐行查询，避免 N+1；关键字查不到商品时提前返回空页，规避 `IN ()` 语法问题。

### B.4 验证结果（真实运行）

后端 `mvn package` **BUILD SUCCESS**；前端 `vite build` **3303 modules transformed ✓**；应用在 8080 启动后实测：

| 验证项 | 结果 |
|---|---|
| `GET /api/system/menu/user-menus?userId=1` | ✅ 返回 **8 个顶级目录 / 41 个菜单级子项**，层级正确，无孤儿 |
| `GET /api/business/stock/log/biz-types` | ✅ 返回 12 个业务类型 |
| `GET /api/business/stock/log/page` | ✅ 关联字段全部正确（`scName=主仓库`、`productCode=MAT000001`、`productName=钢板Q235`、`unitName=千克`、`bizTypeDesc=采购收货`）；筛选 `scId` / `bizType` / `productName` / 日期区间 / 组合条件均正确；无匹配关键字返回空页不报错 |
| `GET /api/system/dict/type/page` | ✅ 返回 4 个字典类型 |

> 库存流水验证期间插入的 2 条测试数据（`biz_code='TEST-VERIFY-20261001'`）**已删除**，表恢复为空。

### B.5 ⚠️ 过程中发现的两个新问题（未修，待决策）

1. **🏴 `SERVER__PORT` 环境变量劫持端口** —— 本机环境注入了 `SERVER__PORT=51731` 与 `SERVER__HOST=127.0.0.1`，Spring 宽松绑定会把它当成 `server.port`，**覆盖 `application.yml` 里的 8080**，导致启动报 `Port 51731 was already in use`（51731 被 WorkBuddy.exe 占用）。
   绕过方式：`env -u SERVER__PORT -u SERVER__HOST java -jar ... --server.port=8080`。
   若在 IDE 里直接跑启动类也会中招，建议排查是谁注入的、或显式加 `--server.port`。

2. **🔴 `npm run build` 在改动前就是坏的** —— `tsc --noEmit` 有 **45 个错误**，分布在 **24 个文件**（`Basedata/Material/*`、`System/User`、`System/Role`、`Business/Stock/Query`、`store/menuStore.ts` 等，**均非本次新增**）。根因是 `utils/request.ts` 的响应拦截器**运行时返回 `data.data`，但类型仍是 `AxiosResponse`**，于是所有 `await getXxx()` 拿到的类型都对不上。
   本次新增的 `System/Dict`、`Stock/Log`、`api/system/dict.ts`、`api/business/stockLog.ts` **已用显式返回类型规避，0 报错**。
   建议统一在 `request.ts` 里把拦截器返回值断言成 `Promise<any>` / 重载 `request` 的类型，一次性清掉这 45 个错误。

3. **🟡 角色 2/3/4（采购经理/销售经理/仓库管理员）当前无任何菜单授权** —— 本次只补了超级管理员。**已于附录 C 处理完毕。**

---

*本报告正文基于静态代码与脚本分析；附录 B 为 2026-10-01 的实际执行与运行验证记录。所有结论均可通过文中给出的表名、Controller 路径、脚本文件名与菜单编码在仓库中定位。*

---

## 附录 C：执行记录（2026-10-01，第二轮 —— 角色授权与授权链路修复）

本轮接续附录 B，处理 B.5 遗留的第 3 项，并在过程中挖出**授权链路上 3 个更底层的缺陷**。

### C.1 角色菜单授权

执行脚本：`sql/grant_role_menus.sql`（幂等，已连跑两次结果一致，中间表自动清理）。

脚本不用自增 id，而是按 **`role.code` + `menu.code`** 映射授权，并用**递归 CTE 做祖先闭包** —— 只要授了子节点，其所有上级目录自动补上。这一步不是锦上添花：`SysMenuServiceImpl.buildTreeFromList()` 只从授权集合里挑 `parent_id = 0` 的节点当根，**目录漏授会让整棵子树从侧边栏消失**（附录 B 里 role 1 就是这么坏的）。

| 角色 | 编码 | 授权菜单数 | 覆盖范围 |
|---|---|---|---|
| 超级管理员 | `R000001` | 71 | 全部 |
| 采购经理 | `R000002` | 23 | 采购管理(3) + 库存查询 + 基础数据(仓库/物料体系/供应商) + 结算管理(收支项目+供应商四单) + 报表(采购统计/库存报表/进销存汇总) |
| 销售经理 | `R000003` | 27 | 销售管理(3) + 零售管理(3) + 库存查询 + 基础数据(物料体系/客户/品牌) + 结算管理(收支项目+客户四单) + 报表(销售统计/库存报表/进销存汇总) |
| 仓库管理员 | `R000004` | 26 | 库存管理全 6 项 + 仓库管理(含 4 个按钮) + 物料体系 + 采购收货 + 销售出库 + 零售出库 + 报表(库存报表/进销存汇总) |

校验结果：**缺失父级 0 行**、**无任何菜单处于"所有角色都未授权"状态**。

### C.2 修掉的 3 个授权链路缺陷

| # | 位置 | 问题 | 后果 | 修法 |
|---|---|---|---|---|
| 1 | `SysMenuServiceImpl.buildTree()` | 过滤条件写成 `parentId == null`，但库中顶级菜单一律是 `parent_id = 0`；且该方法**从不给 `children` 赋值** | `GET /system/menu/tree` **恒返回 `[]`** → 菜单管理页表格全空；`MenuForm` 的"上级菜单"选择器也无数据 | 改为委托 `buildTreeFromList()`：它同时兼容 `null`/`0` 两种根标记，并递归挂载 `children` |
| 2 | `SysRoleServiceImpl.assignMenus()` | 「先删后插」，但**不补祖先节点**；且未去重（`uk_role_menu` 唯一键会直接抛异常） | 前端一旦只提交叶子节点，目录就被写没了 | 用 `LinkedHashSet` 去重 + `fillAncestors()` 向上补齐全部祖先（带 50 层深度上限防成环） |
| 3 | `AssignMenus.tsx` | ① 用 `/system/menu/user-menus`（按**当前登录用户**过滤）当"全量菜单树"；② 加载时把角色已有的**父级 ID 直接塞进受控 `checkedKeys`** | ① 非超管打开时只能看到自己已有的菜单，隐藏菜单永远无法授权；② antd 非严格勾选模式会对受控值**向下传导**，父级一旦入列，其下**所有**子节点被自动勾上并随提交落库 → **静默越权** | 改用 `/system/menu/tree`；回填时只给叶子节点（父级交给 Tree 推导为半选/全选）；提交前用 `withAncestors()` 做客户端闭包 |

> 缺陷 3 的第 ② 条是**静默越权**，危害大于缺陷 2：它不会报错，只会让角色悄悄多出一批没勾过的菜单。两端各做一次闭包后，无论客户端怎么传都不会写坏数据。

### C.3 验证结果（真实运行）

后端 `mvn package` **BUILD SUCCESS**；前端 `vite build` **3303 modules ✓**；本轮改动的文件 `tsc --noEmit` **0 报错**（全仓错误数 45 → 44，仅减少未新增）。应用在 **8081** 起后实测：

| 验证项 | 结果 |
|---|---|
| `GET /api/system/menu/tree` | ✅ 修复前**恒为空**，修复后 **8 个顶级目录 / 71 个节点**，层级正确（`物料管理` 为嵌套目录、`仓库管理` 挂 4 个按钮） |
| `GET /api/system/menu/user-menus?userId=2`（采购经理） | ✅ **5 个顶级目录 / 23 个节点**，目录与菜单层级完整 |
| `GET /api/system/menu/user-menus?userId=3`（仓库管理员） | ✅ **6 个顶级目录 / 26 个节点**，含 4 个仓库按钮 |
| `POST /api/system/role/4/menus`（**只提交 18 个叶子节点**） | ✅ 回读仍为 **26 个**，8 个目录（库存管理/采购管理/物料管理/基础数据/仓库管理/销售管理/零售管理/统计报表）**全部被服务端自动补回**，结果与提交前完全一致 |

验证脚本：`.workbuddy/backup/verify_assign_closure.py`。为验证 `user-menus` 临时创建的用户 `tmp_buyer` / `tmp_wh`（id 2、3）**已删除**，`sys_user` 恢复为仅剩 `admin`；角色 4 授权经往返提交后仍为 26 条。

### C.4 本轮之后仍然存在的最大问题

**P0 未动：后端依旧零权限校验。** 全仓 grep `@PreAuthorize / @SaCheckPermission / hasPermission` **0 命中**，`erp-web` 只有 `CorsFilter`，没有登录拦截器、没有 JWT 解析过滤器，`AuthController` 只签发 token。也就是说：

- `sys_menu.permission`（如 `business:purchase:order`）目前**只是前端按钮显隐的装饰**；
- `GET /system/menu/user-menus?userId=2` 之所以能被任意调用，正是因为**接口压根不校验身份**——本轮验证也依赖了这一点；
- 菜单授权做好之后，**侧边栏会正确按角色收窄，但直调接口依然畅通**。

菜单/授权这一层现在已经是干净、自洽的；下一步真正让权限生效（登录拦截 + 权限校验），建议单独立项，因为需要给约 37 个 Controller 的 ~200 个端点标注权限，并新增「HTTP 端点 → 权限串」的映射约定。

---

## 附录 D：执行记录（2026-10-01，第三轮 —— 零售模块表结构修复）

### D.1 报错现象

零售管理下三个接口全部返回 `code:500`：

| 接口 | 报错 |
|---|---|
| `GET /api/retail/config` | `Unknown column 'create_by' in 'field list'` |
| `GET /api/retail/out/page` | `Unknown column 'deleted' in 'where clause'` |
| `GET /api/retail/return/page` | `Unknown column 'deleted' in 'where clause'` |

### D.2 根因

`RetailConfig` / `RetailOutSheet` / `RetailReturn` 三个实体均 `extends BaseBusinessEntity`，该基类声明了 6 个公共字段（`id` / `create_time` / `update_time` / `create_by` / `update_by` / `deleted`，其中 `deleted` 带 `@TableLogic`），而 `sql/retail_module*.sql` 建表时把这些列**漏掉了**。MyBatis-Plus 按实体生成 SQL，落库即失败——**典型的「实体与建表脚本各写各的」问题**。

全库交叉校验（65 个实体 × 数据库实际列，脚本 `.workbuddy/backup/check_entity_columns.py`）后确认**只有这 3 张表**存在此问题：

| 表 | 缺失列 |
|---|---|
| `retail_config` | `create_by`、`update_by`、`deleted` |
| `retail_out_sheet` | `deleted` |
| `retail_return` | `deleted` |

> 零售明细表（`retail_out_sheet_detail` / `retail_return_detail`）的实体是独立实现 `Serializable`、**不继承基类**，因此明细表无需这些列，本次未改动——这也解释了为何全库另有 20 余张 `*_detail` 表同样没有 `deleted` 列却一直正常。

### D.3 修复

`sql/fix_retail_table_columns.sql`：**幂等**（通过 `information_schema` 判断列是否存在再 `PREPARE/EXECUTE` 动态 DDL，可重复执行），只做 `ADD COLUMN`，列顺序与 `base_warehouse` 等既有表对齐，`deleted` 用 `NOT NULL DEFAULT 0`。三张表修复前均为 0 行，无数据风险。

### D.4 顺带发现并修复的两个缺陷

| # | 位置 | 问题 | 后果 |
|---|---|---|---|
| 1 | `pages/Business/Retail/Config/index.tsx` | 表单只声明了两个开关字段，`onFinish` 提交的 `values` **不含 `id`** | 后端 `updateById` 拿到 null 主键 |
| 2 | `RetailConfigServiceImpl.updateRetailConfig()` | `updateById` 在 id 查不到记录时**静默返回 false**，代码未判断 | 前端弹「保存成功」，**数据库实际没变**（实测：提交 `outStockUnApprove:true` 后库里仍是 0） |

修复：前端单独保存 `configId` 并在提交时回传；后端 `retail_config` 是全表唯一一行，`id` 为空时**兜底定位已有记录**，并在 `updateById` 返回 false 时**显式抛错**（`零售配置保存失败：未找到 id=xxx 的配置记录`），杜绝静默失败。同时 `getRetailConfig()` 在 `save()` 后回查一次，避免返回 `deleted = null` 的脏对象。

### D.5 验证（真实运行，18 个端点全部通过）

后端 `mvn package` **BUILD SUCCESS**，前端 `vite build` **3303 modules ✓**，改动文件 `tsc` **0 报错**。在 8081 独立实例上（不动用户 8080 的 IDEA 实例）实测：

- **读**：`retail/config`、`retail/out/page`、`retail/return/page` 三个报错接口全部恢复 200
- **写**：新增（编号 `LS202610010001`，金额 11010 = 10×1 + 2×5500 正确）→ 详情 → 修改 → 审核 / 拒绝 / 删除（逻辑删除 `deleted=1` 落库正确）；退货同系列全部通过，中文备注无乱码
- **审核联动**：出库审核后 `product_stock` 扣减、`product_stock_log` 写入 `bizType=11 零售出库`；退货审核 `bizType=12 零售退货` 且 `-10 + 4 = -6` 计算正确
- **配置兜底**：不带 id 提交 → 库中值真实变更；带不存在的 id=999 → 明确报错而非假成功
- 测试数据（3 张出库单 + 3 张退货单 + 明细 + 库存/流水）**已全部清理**，`retail_config` 保留 1 行并恢复为 `0 / 0`

### D.6 遗留：零售模块的三个业务逻辑缺陷（未改，需业务决策）

1. **🔴「自动审核」配置项是摆设** —— 前端文案承诺「开启后，零售出库单提交后将自动审核通过并扣减库存」，但全仓 grep `outStockUnApprove` 确认**除配置自身外无任何业务代码引用**，`RetailOutSheetServiceImpl.add()` 里硬编码 `setStatus(0)` 待审核。开关开与不开，行为完全一致。
2. **🟠 出库不校验库存充足性，直接产生负库存** —— `approve()` 调用 `addStock(出库数量取负)`，库存记录不存在时会新建，实测可把库存扣成 `-2` / `-10`。零售出库按常理应校验可用量并拒绝或告警。
3. **🟠 出库会污染成本价** —— `approve()` 走的是 `addStock`（内部 `isAdd = true`），而 `updateStock` 里 `if (isAdd && taxPrice != null) stock.setTaxPrice(taxPrice)`，于是**售价被写进成本价**。出库本应走 `subStock`，且无论如何不应更新成本价。

> 以上三条已于**附录 E** 全部修复。

---

## 附录 E：执行记录（2026-10-01，第四轮 —— 零售模块三个业务缺陷修复）

对应附录 D.6 遗留的三条。全部为代码改动，**无表结构变更**。

### E.1 修复 1：「自动审核」开关真正生效

- `IRetailConfigService` 注入 `RetailOutSheetServiceImpl` / `RetailReturnServiceImpl`，新增私有方法 `isAutoApproveEnabled()` 读取配置。
- `add()` 保存明细后，若开关为 `true` 则直接调用 `approve(sheet.getId())`：
  - 出库看 `retail_config.out_stock_un_approve`
  - 退货看 `retail_config.return_stock_un_approve`
- **事务边界**：`add()` 本身带 `@Transactional`，`approve()` 抛异常会连带回滚整张单据（含明细），不会留下"单据建了、库存没动"的中间态。实测库存不足时单据与明细均未落库。
- `add()` 返回体由 `void` 改为 `Map<String, Object>`，带 `id / code / status / autoApproved`；两个 Controller 的 `add` 同步改为 `Result<Map<String, Object>>`；前端表单据此提示「已按零售配置自动审核并扣减库存」或「待审核」。
- **修改（update）不触发自动审核** —— 配置语义是"提交（新建）后自动审核"，且已自动审核的单据状态为 1、前端不提供编辑入口。

### E.2 修复 2：出库前校验库存，杜绝负库存

- `IProductStockService` 新增 `checkStockEnough(scId, productId, requireNum)`：库存不足时抛 `BusinessException`，错误信息带**仓库名 + 商品编号与名称 + 当前库存 + 本次需求**，例如：
  > 库存不足：主仓库 的「MAT000003 螺栓M8*20」当前库存 90，本次需要 9999
- `RetailOutSheetServiceImpl.approve()` 在扣减前**按商品汇总**本次出库量再逐商品校验。
  > ⚠️ 按商品汇总这一步是必要的：同一次出库中同一商品可能落在多条明细上，逐条校验会漏掉累计超出（库存 88、两条明细各要 60，逐条都"合法"，累计 120 才会扣成负数）。
- 退货是入库方向，不做该校验。

### E.3 修复 3：出库不再污染成本价

- `ProductStockServiceImpl.updateStock()` 增加 `updateCost` 开关，现有三个入口的语义变为：

| 方法 | 方向 | 是否更新成本价 | 典型调用方 |
|---|---|---|---|
| `addStock` | 入库 | ✅ 是 | 采购收货、盘盈、调拨入库 |
| `subStock` | 出库 | ❌ 否 | 销售出库、采购退货、调拨出库、盘亏、**零售出库（本轮改）** |
| `addStockKeepCost`（新增） | 入库 | ❌ 否 | **零售退货（本轮改）** |

- `RetailOutSheetServiceImpl.approve()` 由 `addStock(outNum.negate())` 改为 `subStock(outNum)` —— 数量取负的方向虽然一样，但 `addStock` 的 `isAdd=true` 会覆盖成本价。
- `RetailReturnServiceImpl.approve()` 改用新增的 `addStockKeepCost`：数量加回去，但传入的是**售价**，不能用来覆盖成本价；仅在该仓库+商品尚无库存记录时作为初始成本价。
- 顺带修掉一个潜在 NPE：`updateStock` 中 `stock.getTaxPrice().multiply(...)` 在新建库存且 `taxPrice` 传 `null` 时会抛空指针，现增加兜底。

### E.4 顺带做的健壮性修正

- 两个服务中 `sheet.getStatus() != 0` / `== 1` 这类 **Integer 与字面量比较**改为 `Integer.valueOf(0).equals(...)`，避免 `status` 为 `null` 时拆箱 NPE。
- `refuse()` 拼接拒绝原因前先判断 `description` 是否为空，此前描述为 `null` 时会拼出字符串 `"null [拒绝原因: ...]"`。
- `approve()` / `update()` / `deleteById()` 统一补上"单据不存在"的明确报错。
- 审核前校验明细非空。
- 清理 `Retail/Outbound/Form`、`Retail/Return/Form` 中未使用的 `Modal`、`ExclamationCircleOutlined` 导入（原本就有，`tsc` 报 TS6133）。

### E.5 验证（8081 独立实例，真实运行）

后端 `mvn package` **BUILD SUCCESS**；前端 `vite build` **✓ built in 54.19s**；改动文件 `tsc` **0 报错**，全仓类型错误由 **44 降至 40**（只减不增）。

初始态：`product_stock` 一条记录（商品 `MAT000003 螺栓M8*20`，数量 100，**成本价 1.0000**），`retail_config` 为 `1 / 1`。

| # | 场景 | 结果 |
|---|---|---|
| 1 | 开关开 → 提交出库 10 件（售价 20） | `autoApproved:true, status:1`；库存 100→90；**成本价仍 1.0000**；流水 `bizType=11` |
| 2 | 开关开 → 提交 9999 件 | `库存不足：主仓库 的「MAT000003 螺栓M8*20」当前库存 90，本次需要 9999`；**单据数不变、明细数不变**（整单回滚） |
| 3 | 关闭开关 → 提交 5 件 | `autoApproved:false, status:0`；库存不动 |
| 4 | 手动审核该单 | 库存 90→85 |
| 5 | 退货开关开 → 提交 3 件 | `autoApproved:true, status:1`；库存 85→88；**成本价仍 1.0000**；流水 `bizType=12` |
| 6 | 同一商品两条明细各 60 件（库存 88） | `当前库存 88，本次需要 120` —— 按商品汇总校验生效，单据未创建 |
| 7 | 恰好出库 88 件 | 成功，库存 0，成本价仍 1.0000 |
| 8 | 库存 0 时出库 1 件 | `当前库存 0，本次需要 1` |

流水链 `100 → 90 → 85 → 88 → 0` 完全自洽，成本价全程保持 1.0000（修复前会被售价 20 覆盖）。测试数据已全部清理，`retail_config` 恢复为测试前的 `1 / 1`。

### E.6 同类问题（本次未动，需决策）

修复 2、3 的方法是通用的，但**只应用在了零售模块**。以下模块存在同样的隐患，建议按同样方式处理：

| 模块 | 现状 | 风险 |
|---|---|---|
| 销售出库 `SaleDeliveryServiceImpl` | 已用 `subStock`（方向正确），但**未做库存校验** | 可扣成负库存 |
| 采购退货 `PurchaseReturnServiceImpl` | 同上 | 可扣成负库存 |
| 调拨出库 `StockTransferServiceImpl` | 同上 | 可扣成负库存 |
| 销售退货 `SaleReturnServiceImpl` | 用 `addStock` 传售价 | **售价污染成本价** |
| 采购收货 `PurchaseReceiptServiceImpl` | 用 `addStock` 传采购价 | 合理，无需改 |

> 注意：盘亏（`StockCheckServiceImpl`）与库存调整（`StockAdjustSheetServiceImpl`）的扣减是"按实际盘点/调整结果修正"，允许把库存改为 0 或负值，**不应**加库存充足性校验。

### E.7 尚未解决的并发窗口

`checkStockEnough` 是"先查再扣"的两步操作，同一商品并发出库时两次查询可能都通过校验，仍会扣成负数。彻底解决需要把扣减改成**数据库层原子条件更新**（`UPDATE product_stock SET stock_num = stock_num - ? WHERE sc_id = ? AND product_id = ? AND stock_num >= ?`，按影响行数判断成功），或在库存行上加悲观锁。当前单机学习环境暂不处理。


---

## 附录 F：与 xingyun 的功能差距量化（2026-10-01）

> 目的：回答"当前项目相比 xingyun 还差多少功能没有开发"。
> 口径：**以 xingyun 自身 `sys_menu` 数据为功能全集基准**，与当前项目真实库
> （`erp-data.sys_menu`）逐项对齐。不依赖任何历史文档的转述。

### F.1 数据来源（可复现）

| 侧 | 来源 |
|----|------|
| xingyun | `xingyun/xingyun-api/src/main/resources/db/migration/tenant/V*.sql` 中全部 `INSERT INTO \`sys_menu\`` |
| 当前项目 | `erp-data.sys_menu` 真实数据（`WITH RECURSIVE` 递归 CTE 导出树） |

⚠️ 解析 xingyun 菜单**必须按列名匹配**。`V1.0__init.sql` 与 `v1.20__baseinfo_adjust.sql`
等后迁移脚本的列顺序不一致，按固定下标取会把 `display` 读成 `path`，导致按钮被误判成菜单。

### F.2 总量对比

| 指标 | xingyun | 当前项目 | 差距 |
|------|:---:|:---:|:---:|
| 菜单节点总数（目录+菜单+按钮） | 347 | 71 | −276 |
| 一级目录 | 17 | 8 | −9 |
| 目录级节点 | 19 | 9 | −10 |
| **功能页（菜单）** | **82** | **41** | **−41** |
| 按钮权限点 | 246 | 21 | −225 |
| 后端 Controller | 60 | 38 | −22 |
| 实体类（不含基类） | 103 | 62 | −41 |

**核心结论：功能页维度覆盖 36/82（含半成品 37/82，约 45%），完全缺失 45 项。**

### F.3 逐模块差距明细（口径 = 功能页）

| 模块 | xingyun | 已有 | 缺失 | 缺失的具体功能 |
|------|:---:|:---:|:---:|------|
| 系统管理 | 15 | 5 | **10** | 岗位管理、操作日志△、系统参数、发布系统通知、系统通知、租户管理、开放域、编号规则、消息通知组、用户分组 |
| 平台管理 | 1 | 0 | **1** | 在线开发 |
| 基础信息管理 | 9 | 3 | **6** | 会员信息、门店信息、支付方式、地址库、物流公司、打印模板 |
| 商品中心 | 7 | 3 | **4** | 商品销售属性组、商品属性、商品销售属性、仓位商品管理 |
| 采购管理 | 4 | 3 | **1** | 采购参数设置 |
| 销售管理 | 4 | 3 | **1** | 销售参数设置 |
| 零售管理 | 3 | 3 | 0 | — |
| 库存管理 | 4 | 3 | **1** | 库存预警 |
| 库存盘点 | 4 | 1 | **3** | 盘点参数设置、预先盘点单管理、盘点任务管理 |
| 库存调整 | 3 | 2 | **1** | 库存成本调整 |
| 结算管理 | 10 | 10 | 0 | — |
| 物流管理 | 1 | 0 | **1** | 物流单管理 |
| 开发管理 | 8 | 0 | **8** | 定时器管理、数据实体、数据对象、自定义列表、自定义选择器、表单生成器、自定义表单、自定义页面 |
| 便捷办公 | 2 | 0 | **2** | 文件收纳箱、在线Excel |
| 消息中心 | 3 | 0 | **3** | 站内信（管理端）、站内信（消息端）、邮件消息 |
| 流程中心 | 4 | 0 | **4** | 流程设计、我的发起、待办任务、流程实例管理 |
| **合计** | **82** | **36** | **45** | 另 1 项半成品：操作日志 |

△ 操作日志：`SysOperationLog` / `SysLoginLog` **实体与 Mapper 已存在，但无 Service、
无 Controller、无前端页面**，属于"建了表没做功能"的半成品。

### F.4 已 100% 对齐的主线（好消息）

**核心进销存链路已完整覆盖 xingyun**，且部分更强：

- 采购管理：订单 / 收货 / 退货（仅缺"参数设置"）
- 销售管理：订单 / 出库 / 退货（仅缺"参数设置"）
- 零售管理：配置 / 出库 / 退货 —— **完全对齐**
- 结算管理：收支项目 + 供应商四单 + 客户四单 —— **完全对齐**
- 统计报表：数据看板 / 采购统计 / 销售统计 / 库存报表 / 进销存汇总 ——
  **当前项目独有**，xingyun 没有独立报表目录（只有 `tbl_order_chart` + `ChartController` 服务端接口）

### F.5 非菜单类能力缺口（比菜单缺口更关键）

以下能力 xingyun 有、当前项目没有，且**不体现在菜单数量里**：

| 能力 | xingyun 实现 | 当前项目 | 影响 |
|------|------|------|------|
| **后端权限校验** | 注解 + 拦截器逐接口鉴权 | **零校验**（全仓 0 命中 `@PreAuthorize`/`@SaCheckPermission`） | 🔴 任何登录用户可直调任意接口 |
| 多租户 | `tenant.sql` + `V1.7__tenant_server_name.sql` | 无（grep `Tenant` = 0） | 无法 SaaS 化 |
| 商品 SKU 体系 | `ProductSku`/`ProductSkuCode`/`ProductBundle`/`ProductCode`（一品多码/组合商品/多规格） | `BaseMaterial` 扁平字段，仅一个 `specification` 字符串 | 无法做多规格、条码 |
| 库位（仓位） | `StockCell` / `StockCellProduct` | 无 | 仓库只能到"仓"不到"位" |
| 单据编号规则 | `gen_create_column_config` + `V1.10/V1.13` 编号规则 | 无 | 单号生成无法配置化 |
| 打印模板 | `PrintTemplate` + `V1.14__print_template.sql` | 无 | 单据无法打印 |
| Excel 导入导出 | `OnlineExcel` + 各模块导入/导出按钮 | **无（grep `Excel`/`Export` = 0）** | 批量数据只能手工录 |
| 登录验证码 | `V1.9__login_captcha_require.sql` | 无（grep `captcha` = 0） | 登录无防爆破 |
| 工作流 BPM | `V1.6/V1.17__bpm.sql`，4 个流程页 | 无 | 审批流硬编码在 `approve()` |
| 定时任务 | Quartz（`V1.3__update_qrtz_details.sql`） | 无（grep `Quartz`/`Job` = 0） | 无调度能力 |
| 低代码平台 | `gen_*` 共 20+ 张表 + 开发管理 8 个页 | 无 | 无法配置化扩展 |
| 消息通知 | 站内信 / 邮件 / 通知组 / `sys_notice` | 无 | 无任何通知触达 |
| 登录日志 | `op_logs` 表 + 操作日志页 | 实体 `SysLoginLog` 存在但无功能 | 审计缺失 |

### F.6 缺口归类（按投入产出比排序）

| 优先级 | 缺口簇 | 功能页数 | 说明 |
|:---:|------|:---:|------|
| **P0** | 后端权限校验落地 | 0（横切） | 不增加菜单，但决定系统能否交付。**最高技术优先级** |
| **P0** | 商品中心深化（SKU/属性/一品多码/组合商品/仓位） | 4 | 是采购/销售/零售/库存所有单据的数据基础，越晚做改造面越大 |
| **P1** | 系统增强：操作日志、登录日志、编号规则、打印模板、Excel 导入导出、系统参数、验证码 | 约 6（含半成品 1） | 交付一个"能用"的 ERP 的必要条件 |
| **P1** | 库存盘点体系补齐 + 库存预警 + 库存成本调整 | 5 | 直接补齐现有"库存管理"目录的完整性 |
| **P2** | 物流管理 + 基础信息补齐（会员/门店/支付方式/地址库/物流公司） | 7 | 支撑零售与线上业务 |
| **P3** | 消息中心 + 通知组 | 5 | 与其他模块解耦，可后置 |
| **P3** | 工作流 BPM | 5 | 当前 `approve()` 硬编码流程，量大后需重构 |
| **P3** | 低代码/开发管理 + 便携办公 | 11 | 平台化能力，非业务刚需 |
| **P3** | 多租户 + 租户管理 | 1 | 仅在做 SaaS 时才需要 |

### F.7 一句话回答

> **业务主线（采购/销售/零售/结算）已经和 xingyun 持平甚至更好（多了 5 个报表页）；
> 真正的差距不在"业务流程"，而在"平台能力与系统基础"** ——
> 缺 45 个功能页，其中约 30 个属于系统增强、商品中心深化、库存盘点补齐、
> 物流与消息这几类"让系统变得完整好用"的部分；
> 另有 11 个属于低代码/开发管理这类"xingyun 有但业务上用不到"的能力。
> 最关键的一项缺口不体现在菜单上：**后端权限校验目前完全没有落地**。



---

## 附录 G：权限落地实录 —— 接入 Sa-Token（2026-10-01）

> 背景：附录 F 指出最大缺口是"后端零权限校验"。本附录记录第 1 步（登录拦截）的完整落地过程与
> 踩到的坑，供后续补权限注解时参照。

### G.1 选型结论

**Sa-Token 1.46.0**（2026-08-18 发布）+ `sa-token-spring-boot3-starter`。

选它的五条硬理由：

| 证据 | 实际情况 |
|---|---|
| 没有迁移包袱 | 原 `generateToken()` 返回 `"Bearer_" + UUID`，**签完即丢**，服务端不存任何会话；`logout()` 是空 TODO。等于从零补，不用兼容历史 token |
| 没有冲突 | `erp-web/pom.xml` 里 `spring-boot-starter-security` 是**注释掉的**，classpath 干净，不会双过滤链打架 |
| 权限数据零改动 | `sys_menu.permission` 已是 `system:user:add` 这类 `域:模块:动作` 标准串，与 `StpUtil.hasPermission()` 同一套约定 |
| 数据链路已就绪 | `sys_user_role → sys_role_menu → sys_menu` 俱全，`StpInterface` 只包一层 |
| 存储已就绪 | Redis 已配好，`erp-system` 已依赖 `spring-boot-starter-data-redis` |

⚠️ 版本要点：Spring Boot 3.x 必须用 `sa-token-spring-boot3-starter`（jakarta）；
用 `sa-token-spring-boot-starter` 会 `ClassNotFoundException: javax.servlet.Filter`。
另：**Sa-Token 1.36.0 及更早存在 CVSS 9.8 严重漏洞**，1.37.0 起才干净，新项目直接用 1.46.0。

### G.2 改动清单

| 层 | 文件 | 改动 |
|---|---|---|
| 依赖 | `pom.xml`（父） | `sa-token.version=1.46.0` + 3 条 dependencyManagement |
| 依赖 | `erp-system/pom.xml` | starter + `sa-token-redis-jackson` + `commons-pool2` |
| 依赖 | `erp-common/pom.xml` | `sa-token-core`（仅用于异常映射，不引 starter） |
| 配置 | `erp-web/.../application.yml` | 新增 `sa-token` 段 |
| **新增** | `erp-web/.../config/SaTokenConfigure.java` | `SaInterceptor` + 白名单 |
| **新增** | `erp-system/.../satoken/StpInterfaceImpl.java` | 权限/角色加载，超管短路 |
| **新增** | `erp-system/.../satoken/SaPermissionCache.java` | 鉴权结果 Redis 缓存 |
| Mapper | `SysMenuMapper`(+xml) / `SysRoleMapper`(+xml) | 新增两条权限查询 |
| 异常 | `erp-common/.../GlobalExceptionHandler.java` | 401 / 403 映射 |
| 业务 | `AuthServiceImpl` | 签发真 token、退出真正注销 |
| 业务 | `SysRoleServiceImpl` / `SysUserServiceImpl` / `SysMenuServiceImpl` | 授权变更清缓存 |
| 试点 | `SysMenuController` | 4 个 `@SaCheckPermission` |

### G.3 四条硬约定（照做，否则会出事故）

1. **`token-name: Authorization` + `token-prefix: Bearer`** —— 前端 `request.ts` 发的是
   `Authorization: Bearer xxx`，配这两个值即可**前端零改动**。改了它们必须同步改前端。
2. **鉴权异常回真实 HTTP 状态码**，不能像业务异常那样回 HTTP 200 + code。
   前端靠 `error.response.status` 判断 401 跳登录页，回 200 的话"接口 401 了但页面不跳登录"。
3. **白名单路径不带 context-path**。`context-path=/api`，但写 `/auth/login` 而非 `/api/auth/login`；
   不写对的话登录接口自己会被拦，永远登不进去。
4. **`StpInterface` 每次鉴权都会被回调**，Sa-Token 不缓存其返回值 → 必须走 `SaPermissionCache`。
   缓存内部 Redis 异常时降级查库（缓存挂了不能让全站 401）。

### G.4 实测结果（8081 独立实例，11 项全过）

| # | 场景 | 期望 | 实测 |
|---|---|---|---|
| 1 | 未登录访问 `/system/menu/list`、`/system/user/list`、`/business/stock/list` | 401 | 401 ✓ |
| 2 | 白名单 `/doc.html`、`/v3/api-docs` 匿名访问 | 200 | 200 ✓ |
| 3 | 匿名调 `/auth/login`（admin/123456） | 200 + 真 token | 200，`2d7b7717-…` ✓ |
| 4 | 带 `Authorization: Bearer <token>` 访问 | 200 | 200 ✓ |
| 5 | 去掉 `Bearer` 前缀 | 401 | 401 ✓（证明 `token-prefix` 生效） |
| 6 | 伪造 token | 401 | 401 ✓ |
| 7 | `/auth/logout` 后原 token 再用 | 401 | 401 ✓（退出真的生效） |
| 8 | 仅"采购经理"角色访问 `/system/menu/list`、`GET /{id}`、`POST` | 403 | 403 +「没有该功能的操作权限: system:menu:list」✓ |
| 9 | 同一受限用户访问 `/system/menu/tree`、`/user-menus`（故意不注解） | 200 | 200 ✓ |
| 10 | 错误密码 | 业务错误 | code 500「用户名或密码错误」✓ |
| 11 | 调 `assignRoles` 后看缓存 | 精确失效 | `perms:4`/`roles:4` 被清、`roles:1` 保留 ✓ |

`mvn clean package` BUILD SUCCESS；fat jar 56 MB，含全部 8 个 sa-token 组件与 `commons-pool2`。

### G.5 三个值得记的坑

1. **Redis 里的 key 前缀不是 `satoken`**。Sa-Token 的 key 前缀取自 `token-name`，
   本项目下实际是 `Authorization:login:token:{token}` 和 `Authorization:login:session:{userId}`。
   一开始用 `KEYS satoken*` 查是空的，差点误判成"Redis 插件没生效"。
2. **`NotLoginException` 的常量不要用在 `switch` 的 case 上**（case 标签要求编译期常量，
   不保证成立），改用 if-else + 常量 `equals` 比较，零编译风险。
3. **超管短路返回 `["*"]` 是有效的**：Sa-Token 把"用户拥有的权限列表"当**模式串**去匹配，
   `vagueMatch("*", "system:menu:list")` 为 true。这样以后新增菜单不用给超管重新授权。

### G.6 哪些接口不该加权限注解

`/system/menu/user-menus`（每个登录用户构建自己的侧边栏）与 `/system/menu/tree`
（被"角色授权"页复用）**刻意不加**。加了会导致非管理员登录后菜单加载失败，
或点开角色授权直接 403 —— 这类"公共读取"接口只做登录校验即可。

### G.7 遗留

- 其余 36 个 Controller 约 190 个端点**仍只要登录即可访问**，`@SaCheckPermission` 需按模块分批补。
  → **已于附录 H 完成（37 个 Controller 注入 224 个注解，99 项实测全过）。**
- 密码仍是**明文**（`PasswordUtil` 明文直传；BCrypt 依赖已就绪但换密文要先刷存量密码）。
- `last_login_time/ip` 未回写；登录日志/操作日志仍无 Service/页面。
- `checkStockEnough` 的并发窗口（见 E.7）与本次无关，仍未解决。
---

## 附录 H：按模块补权限注解（2026-10-01，第 2 步）

> 接附录 G。第 1 步只做到"未登录不可访问"，本次把 243 个业务端点全部标注 `@SaCheckPermission`，
> 让 `sys_menu.permission` 从前端按钮显隐的"装饰"变成后端强制校验。

### H.1 成果

| 指标 | 数值 |
|---|---|
| 注入注解数 | **224 个** |
| 覆盖 Controller | 37 个（`AuthController` 除外） |
| 业务端点数 | 243 个（未加注解的 23 个为刻意排除，见 H.3） |
| 使用的权限串 | **63 个，全部取自既有 `sys_menu.permission`** |
| 新增权限串 / 数据库变更 | **0** |
| 改动文件 | 37 个 Controller；`36 files changed, 269 insertions(+)` |

**关键约束**：注解里写的权限串**必须已存在于 `sys_menu.permission`**。
否则非超管用户拿到 403 且"查不到该权限从哪来"——因为权限字典里根本没有这一条，
进角色授权页也无法勾选。本次 224 个注解全部落在这 63 条之内，**零新增**。

### H.2 权限串来源（63 条）

```
system:    user:{list,add,edit,delete,resetPwd} role:{list,add,edit,delete,assign}
           dept:{list,add,edit,delete}        dict:{list,add,edit,delete}
           menu:{list,add,edit,delete}
basedata:  warehouse:{list,add,edit,delete,setDefault}
           material:{info,category,unit}  brand:list  customer:list  supplier:list
business:  purchase:{order,receipt,return}   sale:{order,delivery,return}
           retail:{config,out,return}        stock:{query,check,transfer,log}
           settle:{item,sheet,check,fee,pre}
           settle:customer:{sheet,check,fee,pre}
chart:     {dashboard,purchase,sales,stock,summary}
stock:     adjust:query  adjust:reason:query
```

粒度是**不均匀**的：`system:*` 与 `basedata:warehouse:*` 是 CRUD 细粒度；
其余多为**菜单级**（一个串覆盖该页面全部操作）。
本次按现状映射（不新造权限串），所以 `basedata:brand:list` 也会用在 brand 的
新增/删除上——语义上略宽，但保证了零回归。要细化见 H.6。

### H.3 映射规则（三条）

| # | 规则 | 理由 |
|---|---|---|
| 1 | 菜单页面的主查询（`page`/`tree`/`getById`/`getInfo`）+ **全部写操作** → 该菜单的权限串 | 与前端"进得了页面"完全对齐；写操作是真正的越权风险点 |
| 2 | **跨模块复用的下拉数据源 → 不加**（只要求登录） | 见下，这是本次最关键的设计 |
| 3 | 细粒度模块（`system:*`、`basedata:warehouse:*`）按动作精确映射 | 库里本来就有 CRUD 串，没理由浪费 |

**规则 2 为什么必须是硬规则**：采购经理没有 `basedata:warehouse:list`、销售经理也没有；
仓库管理员没有 `basedata:supplier:list`。如果给这些下拉接口加权限，
**采购单表单里"选仓库/选供应商"会直接 403，页面当场崩**。
下拉属"为了完成本模块操作而必须读取它模块的数据"，不是越权。

### H.4 刻意不加权限注解的 23 个端点

| 端点 | 原因 |
|---|---|
| `POST /auth/login`、`POST /auth/logout` | 白名单，登录前就要能调 |
| `GET /system/menu/user-menus` | 每个登录用户构建**自己的**侧边栏 |
| `GET /system/menu/tree` | 被"角色授权"页复用，加 `system:menu:list` 会让非管理员点开即 403 |
| `POST /system/user/change-password` | 用户改**自己**的密码 |
| `GET /basedata/{brand,customer,supplier,unit}/list`、`/basedata/material/list`、`/basedata/material/by-category/{id}`、`/basedata/warehouse/{all,list}` | 业务单据表单的**下拉数据源** |
| `GET /business/settle/item/{list,list-enabled}` | 结算项目下拉 |
| `GET /business/purchase/{receipt/pending-orders,return/pending-receipts}`、`/business/sale/{delivery/pending-orders,return/pending-deliveries}` | 生成下游单据时的"待处理单"列表 |
| `GET /business/stock/log/biz-types` | 业务类型字典 |
| `GET /stock/adjust/reason/list` | 调整原因下拉 |

### H.5 实测结果（8081 独立实例，**99 项全过**）

构造三个受限账号（临时用户 id 5/6/7，测完已物理删除）：

| 维度 | 用例数 | 结果 |
|---|:--:|---|
| admin（走 `*` 通配）访问 38 个模块主列表 | 38 | 全部 200 ✓ |
| 采购经理有权限的接口（含 6 个跨模块下拉） | 11 | 全部 200 ✓ |
| 采购经理越权（菜单/用户/角色/销售/零售/品牌 + 3 个写） | 9 | 全部 403 ✓ |
| 销售经理有权/越权 | 9 | 200 / 403 均符合 ✓ |
| 仓库管理员有权/越权 | 9 | 200 / 403 均符合 ✓ |
| 零售模块（真实路径 `/retail/*`） | 11 | 全部符合 ✓ |
| 未登录 401 / 白名单 200 | 3 | 全部符合 ✓ |
| **合计** | **99** | **PASS=99 FAIL=0** |

`mvn clean package` BUILD SUCCESS（6 模块全绿）。

### H.6 本次踩到 / 值得记的点

1. **测试用例的路径要照前端抄，别照 Controller 猜**。第一轮我把零售写成了
   `/business/retail/out/page`（看着像、其实是错的，真实路径是 `/retail/out/page`），
   结果断言失败。而**未匹配的 URL 被 `GlobalExceptionHandler` 兜底成 HTTP 200 + code 500**，
   没报 404 —— 所以它"看起来像通过"。
2. **`/system/menu/tree` 这类"公共读取"要主动识别**。判据是"是不是换个角色登录也要调"。
   判错方向的代价不对称：少加一个注解只是不够严；多加一个会让**功能直接不可用**。
3. **粒度不均匀是现状，不是缺陷**。业务模块只有菜单级权限串，就等于"能进页面就能改"。
   想要"能看不能改"，得先给 `sys_menu` 补按钮记录（`menu_type=2`）再授权——
   这是**数据工作**，不是注解工作。
4. 批量注入用脚本（按"mapping 注解上一行"定位 + 已存在则跳过 + 自动补 import），
   219 个注解一次成型，避免手改 37 个文件。

### H.7 遗留

- **按钮级权限**：业务模块若需"只读角色"，要补 `sys_menu` 按钮记录并给角色授权。
- **前端按钮显隐**：后端已强制，前端按 `permission` 隐藏按钮是体验优化，优先级已降低。
- 密码仍是明文；`last_login_time/ip` 未回写；登录/操作日志仍无 Service 与页面。
- `GlobalExceptionHandler` 对未匹配 URL 返回 200 + code 500（而非 404），语义不严谨。

# ERP 项目 —— 详细技术档案（MEMORY.md 的展开版）

> MEMORY.md 只保留铁律与索引；这里是完整细节，按需查阅。
> 执行流水见 `YYYY-MM-DD.md`，功能设计见 `docs/`。

## 一、结构与环境

- `erp-parent` 多模块：`erp-common`（Result/异常/分页/Excel/编号）→ `erp-basedata`（仓库/供应商/客户/品牌/分类/单位/物料）、
  `erp-business`（~130 实体：采购/销售/零售/库存/结算）、`erp-system`（组织-角色-用户-菜单-字典-日志）、
  `erp-web`（**全部 Controller 在此层**，37 个）。
- 前端 `erp-adminPro`（React + Vite + TS + antd5）；参照项目 `xingyun`（星云ERP）。
- MySQL `erp-data`@192.168.31.199 root/123456，客户端 `/d/soft/mysql-8.4.10-winx64/bin/mysql.exe`，
  **必带 `--default-character-set=utf8mb4`**。Redis 同机 6379 / 密码 123456 / db0。
- 配置文件 `erp-web/src/main/resources/application.yml`，**context-path `/api`**。

## 二、菜单与权限

- `sys_menu` 单表树：`parent_id/code/name/menu_type/path/component/permission/icon/sort/visible/status`，
  **0=目录 1=菜单 2=按钮**。菜单编码 `M%06d` / `B%06d`。
- **顶级 `parent_id` 是 `0` 不是 `NULL`**，判根必须兼容两者（曾致 `/system/menu/tree` 恒返回空）。
- 授权链 `sys_user_role → sys_role_menu → sys_menu`。**授权必须含所有上级目录**，
  否则 `buildTreeFromList()` 找不到根、整棵子树从侧边栏消失；前后端各做一次祖先闭包
  （`SysMenuServiceImpl.assignMenus()` + 前端 `AssignMenus.tsx`）。
- 角色种子 `R000001` 超管 / `R000002` 采购经理 / `R000003` 销售经理 / `R000004` 仓库管理员。

## 三、Sa-Token（2026-10-01 落地）

Sa-Token **1.46.0** + `sa-token-spring-boot3-starter`（Boot 3.5.0 / JDK21 / jakarta）。
**不要用 `sa-token-spring-boot-starter`**（Boot2.x，会 `ClassNotFoundException: javax.servlet.Filter`）。

依赖归属：

| 依赖 | 模块 | 原因 |
|---|---|---|
| `sa-token-spring-boot3-starter` + `sa-token-redis-jackson` + `commons-pool2` | erp-system | `StpInterfaceImpl` 在此模块，要读 sys_menu/sys_role；erp-web 传递获得 |
| `sa-token-core` | erp-common | 只为在 `GlobalExceptionHandler` 里映射 401/403 |

代码落点：

| 文件 | 职责 |
|---|---|
| `erp-web/.../config/SaTokenConfigure.java` | 注册 `SaInterceptor`，全局只做 `StpUtil.checkLogin()` + 白名单 |
| `erp-system/.../satoken/StpInterfaceImpl.java` | `getPermissionList` / `getRoleList` |
| `erp-system/.../satoken/SaPermissionCache.java` | 鉴权结果 Redis 缓存 `erp:auth:perms:{userId}` / `erp:auth:roles:{userId}` |
| `erp-common/.../GlobalExceptionHandler.java` | `NotLoginException`→401，`NotPermission/NotRoleException`→403 |

**四条硬约定**

1. `token-name: Authorization` + `token-prefix: Bearer`（yml），改则前端 `request.ts` 同步改。
2. **鉴权异常必须回真实 HTTP 状态码**（401/403），不能回 200+code；前端靠 `error.response.status` 跳登录。
   `NotLoginException` 用 **if-else 比较，不要 switch**（case 需编译期常量）。
3. **白名单路径不带 context-path**：写 `/auth/login`，不是 `/api/auth/login`。
4. `StpInterface` 两方法**每次鉴权都调用**、Sa-Token 自身不缓存 → 必须走 `SaPermissionCache`，
   其内部 Redis 异常时降级查库（不能因缓存挂了导致全站 401）。

**授权变更必须清缓存**：`SysRoleServiceImpl.assignMenus/removeById`、`SysUserServiceImpl.assignRoles`、
`SysMenuServiceImpl.save/updateById/removeById` → `evictAll()/evictUser()`。

**Redis key 前缀不是 `satoken`**，取自 `token-name`：`Authorization:login:token:{token}` /
`Authorization:login:session:{userId}`。用 `KEYS satoken*` 查不到属正常。

**权限注解**：37 Controller / **224 个 `@SaCheckPermission`**，覆盖 243 个业务端点；
权限串**全部取自既有 `sys_menu.permission`（63 条），零新增**。

- 🔴 **铁律：注解里的串必须已存在于 `sys_menu.permission`**，否则非超管永远 403 且授权页勾不到。
- 三条映射规则：① 菜单页主查询（`page`/`tree`/`getById`）+ **全部写操作** → 该菜单权限串；
  ② **跨模块复用的下拉数据源不加注解**（只要求登录）；③ `system:*`、`basedata:warehouse:*` 按动作精确映射。
- **规则②不能松**：采购经理没有 `basedata:warehouse:list`、销售经理也没有 ⇒
  给下拉加权限会让采购单表单「选仓库/选供应商」直接 403。
- 刻意不加注解的端点：`/auth/login|logout`、`/system/menu/{user-menus,tree}`、`/system/user/change-password`，
  以及全部下拉：`/basedata/{brand,customer,supplier,unit}/list`、`/basedata/material/list`、
  `/basedata/material/by-category/{id}`、`/basedata/warehouse/{all,list}`、
  `/business/settle/item/{list,list-enabled}`、各 `pending-*`、`/business/stock/log/biz-types`、
  `/stock/adjust/reason/list`。
- 粒度现状：`business:*` 多为**菜单级**权限串（进页面即可增删改）。要「能看不能改」须先给
  `sys_menu` 补按钮记录（`menu_type=2`）并授权——是数据工作，不是注解工作。
- 批量注入用脚本（定位 mapping 注解上一行插入 + 已存在则跳过 + 自动补 import）。

## 四、编号 / Excel / 日志（2026-10-03）

### 编号规则

- 规则表 `sys_code_rule`：**列名是 `seq_date` 不是 `current_date`**（后者是 MySQL 保留字，建列直接语法报错）。
  `reset_by_date=1` 时按 `date_format` 的日期段日切重置。
- 统一入口 `ICodeGenerateService.generate(ruleCode)`（erp-common），内部
  `SELECT ... FOR UPDATE` + `@Transactional` 串行化，**不会并发重号**。
- 原来 28 个 `*ServiceImpl.generateXxxCode()` + 28 个 Mapper 的 `selectMaxCode()` **已全部删除**，
  新增单据一律走 `codeGenerateService.generate("XXX")`。
- 动态前缀示例：`codeGenerateService.generate(sheetType == 1 ? "CUSTOMER_FEE_IN" : "CUSTOMER_FEE_OUT")`。
- 规则清单：20 个单据（PO/PR/PTH/SO/SD/SR/LS/LT/SC/ST/SA/JS/DZ/FY/YF/KJS/KDZ/KSK/KKK/KYS）
  + `SETTLE_IN_OUT_ITEM`(SZ 无日期) + `STOCK_ADJUST_REASON`(3 位纯流水)
  + 7 个基础资料（MAT/CUS/SUP/WH/BRD/UNIT/MC，6 位不重置）+ SPU/SP/SI + PD/YPD/CB。
  DDL/种子见 `sql/fix_code_rule.sql`。

### Excel 导入导出

- `erp-common/.../util/ExcelUtils`：`export(...)` / `read(...)` / `statusText(...)` / `parseStatus(...)`。
  **模板下载与导出共用同一方法**——传空列表就只输出表头。
- 依赖 EasyExcel **3.3.4**（`com.alibaba:easyexcel` 是聚合 POM；FastExcel `cn.idev.excel` 1.2.0
  的聚合包里只有 `Empty.class`，别换）。
- 导入模型 VO 放各模块 `vo/` 包（`MaterialExcelVO`/`CustomerExcelVO`/`SupplierExcelVO`），
  用 `@ExcelProperty("中文列名")`，分类/单位按**名称**匹配，状态接受「启用/禁用」或「1/0」。
- **策略：逐行校验、任一行不合法则整体不落库**，实现为两趟：pass1 只校验收集错误（不写库），
  pass2 零错误才统一写入。返回 `ExcelImportResult{total,success,failed,errors}`，errors 带 Excel 行号。
- 端点固定三个：`GET /export`（按当前筛选条件不分页）、`GET /template`、`POST /import`；
  Controller 只负责转发 + 调 `ExcelUtils.export`，**Service 层不碰 `HttpServletResponse`**（收 `InputStream`）。
- 前端下载**不能走 `utils/request`**（拦截器会把二进制流当业务错误），用 `src/utils/download.ts`
  的 `downloadFile(url, params, fallbackName)`；上传用 `FormData` 走正常 request。
- 通用导入弹窗：`src/components/ExcelImport/ExcelImportButton.tsx`。
- ⚠️ `spring.servlet.multipart.max-file-size` 默认只有 **1MB**，已放宽到 10MB / 20MB。

### 日志体系

- **登录日志**：`AuthServiceImpl.login()` 在成功/失败/禁用三条分支都写 `sys_login_log`（含 IP、浏览器、OS），
  成功时同时回写 `sys_user.last_login_time/last_login_ip`。
- **操作日志**：`erp-web/.../aspect/LogAspect` 切 `com.erp.web.controller..*`。
  **不需要逐个接口标注 `@Log`**——自动记「所有非 GET 请求 + GET 的 `/export`」，`/auth/**` 跳过，
  GET 查询一律不记（否则列表翻页会把日志表刷爆）。`@Log` 只是**覆盖项**：

  | 场景 | 用法 |
  |---|---|
  | 标题要更精确 | `@Log(title="分配角色菜单权限", businessType=UPDATE)` |
  | 参数含密码 | `@Log(..., saveParam = false)` |
  | 要完整返回体 | `@Log(..., saveResult = true)` |

- 标题默认按 Controller 类名查 `LogAspect.MODULE_NAMES` 字典；**新增 Controller 记得登记**，
  否则标题退化成类名（不影响功能，只是不好看）。
- 参数入库前统一脱敏（键名含 password/passwd/pwd/secret/token 的值替换成 `***`）并截断 2000 字符；
  `json_result` 默认只存 `Result` 的 code+message。
- 操作人从 **Sa-Token 会话**取（`SessionKeys.USERNAME`），避免每条日志回查数据库；
  因此 `AuthServiceImpl` 登录时必须 `StpUtil.getSession().set(SessionKeys.USERNAME, ...)`。
- 两张日志表都是 `ServiceImpl` + `TRUNCATE` 清空（`truncateTable()` 定义在 Mapper 上）。
  `record()` 用 `Propagation.REQUIRES_NEW` 且**自己吞异常**——审计日志不能因业务回滚丢失，
  更不能因为写日志失败把业务带崩。
- 菜单 `sql/add_log_menu.sql`（幂等 `INSERT IGNORE`），权限串 `system:loginlog:list` /
  `system:operationlog:list`，已授权 R000001（71→73）。**执行后清 `erp:auth:*` 缓存**。
- `erp-web` 为此新增 `spring-boot-starter-aop`（`spring-aop` 本身不含 `@Aspect` 必要注解）。

## 五、实体基类与建表脚本

**建表脚本必须补齐对应基类的全部公共列**，否则 MyBatis-Plus 按实体拼 SQL 直接 `Unknown column`：

| 基类 | 模块 | 公共列 |
|---|---|---|
| `BaseBusinessEntity` | erp-business | `id, create_time, update_time, create_by, update_by, deleted(@TableLogic)` |
| `BaseDataEntity` | erp-basedata | 同上 |
| `BaseSystemEntity` | erp-system | 同上，但逻辑删除字段叫 **`is_deleted`** |
| `BaseAuditEntity` | erp-system | 前 5 个，**无逻辑删除列** |

- **`*_detail` 明细表实体独立实现 `Serializable`、不继承基类** ⇒ 明细表**不需要** `deleted` 等列。
  全库 20 余张明细表都没有该列属正常，排查时不要误判。
- 自查脚本 `.workbuddy/backup/check_entity_columns.py`
  （解析 `entity/*.java` 的 `extends` + `@TableName`，对照 `all_columns.tsv` 找缺列）。
  **遇到 `Unknown column` 报错先跑它。**
- MySQL 8 **不支持** `ADD COLUMN IF NOT EXISTS`，幂等补列只能走
  `information_schema` 判断 + `PREPARE/EXECUTE` 动态 DDL（参考 `sql/fix_retail_table_columns.sql`）。

## 六、库存变动

`IProductStockService` 三入口，**方向**与**是否更新成本价**是两个独立维度：

| 方法 | 方向 | 更新成本价 | 场景 |
|---|---|---|---|
| `addStock` | 入库 | ✅ | 采购收货、盘盈、调拨入库（传**成本价**） |
| `subStock` | 出库 | ❌ | 销售出库、采购退货、调拨出库、盘亏、零售出库 |
| `addStockKeepCost` | 入库 | ❌ | 销售/零售退货（传**售价**） |

- ⛔ **出库不要写 `addStock(数量.negate())`**——方向虽对，但 `isAdd=true` 会用售价覆盖成本价。
- 成本价是否被覆盖只看 `ProductStockServiceImpl.updateStock` 的
  `updateCost && isAdd && taxPrice != null`。只要传的不是成本价，就必须走
  `addStockKeepCost`（入库）或 `subStock`（出库）。
- 扣减前 `checkStockEnough(scId, productId, requireNum)`（不足抛 `BusinessException`）。
  **同一商品可能出现在多条明细，必须按商品汇总后再校验**，逐条校验会漏掉累计超出。
- 盘亏（`StockCheckServiceImpl`）与库存调整（`StockAdjustSheetServiceImpl`）是「按盘点/调整结果修正」，
  **不应**加充足性校验。
- ⚠️「先查再扣」有并发窗口，高并发下仍可能扣成负数；彻底解决需改成数据库层原子条件更新。

## 七、已知欠账

**业务正确性 ✅ 2026-10-03 已全部修复**（出库缺汇总校验 / 退货与调整污染成本价 / 状态字段拆箱 NPE，均已实测）。

**体验与安全（未修）**

1. 前端按钮显隐未按 `permission` 补齐（后端已强制，属体验优化）。
2. 密码仍**明文**：`PasswordUtil.encode/matches` 是明文直传（`erp-common` 已备好 BCrypt 依赖
   `spring-security-crypto`，换密文要先刷存量密码，别直接切）。**用户已明确暂不处理**。
3. `MyMetaObjectHandler` 的 `createBy/updateBy` 硬编码 `"system"`。
4. `GET /system/menu/user-menus` **信任客户端 `userId`**，可读他人菜单。
5. 无验证码、token 续期、踢人下线的管理界面；日志表无归档/清理策略。
6. ⚠️ **未匹配的 URL 被 `GlobalExceptionHandler` 兜底成 HTTP 200 + code 500**（不是 404），
   写测试断言时容易误判为「通过」。
7. `SaleDeliveryServiceImpl.checkAndUpdateOrderStatus` 用 `allMatch` 判「全部发货」，
   **空明细真空真** ⇒ 订单被误判完成。加 `!details.isEmpty()` 即可修。

**工具链**

8. `npm run build`（`tsc && vite build`）是**坏的**（40 个 TS 错误，根因 `utils/request.ts` 拦截器
   运行时返回 `data.data` 而类型仍是 `AxiosResponse`）⇒ **验证前端直接用 `npx vite build`**。
9. `sql/` 历史脚本多，`retail_module_utf8.sql` **有未闭合单引号**（会报错）。
   幂等脚本按序：`fix_menu_full.sql` → `grant_role_menus.sql` → `fix_retail_table_columns.sql`
   → `fix_code_rule.sql` → `add_log_menu.sql`。

## 八、开发与验证

- Maven `D:\soft\apache-maven-3.9.16\bin\mvn`，**打包不要加 `-o`**（离线缺 surefire 会失败）。
- ⚠️ 本机 bash 的 `env` **不是 coreutils 的 env**：`~/.local/bin/env` 是修 PATH 的 sh 脚本，
  会**静默吞掉 `-u` 直接退出** ⇒ `env -u XXX java -jar` 什么都没启动（日志 0 字节、端口不监听、退出码 0）。
  正确：`unset SERVER__PORT SERVER__HOST && timeout 1800 java -jar ...`，或 `/usr/bin/env -u ...`。
  环境注入了 `SERVER__PORT` / `SERVER__HOST`，Spring 宽松绑定会覆盖 yml，**必须先清**。
- ⚠️ **8080 常被用户在 IDEA 里 debug 的实例占用**，自建验证实例用 8081/8082，**不要 kill 用户进程**。
- 后台起长驻服务用 `run_in_background` + `timeout <秒> java -jar ...`；**结果落文件再 Read**。
- 改数据库前先 `mysqldump --skip-add-drop-table --no-create-info` 备份到 `.workbuddy/backup/`。
- **前端接口「调两次」有两个独立成因，别混为一谈**（2026-10-03 用 CDP 实测定位）：
  1. **`main.tsx` 的 `<React.StrictMode>`**：dev 下所有页面挂载时 effect 都跑两遍 ⇒
     每个菜单点击都发 **2 次**接口（`page` + `user-menus` 各 2）。**仅 development 生效**，
     `vite build` 产物是单次。对照实测：去掉 StrictMode → 全部变 1 次。属 React 预期行为，非 bug。
  2. 🔴 **真缺陷（生产同样存在）**：列表页普遍写成
     `useEffect(() => loadData(), [current, pageSize])` + 搜索/重置里 `setCurrent(1); loadData();`
     ⇒ 在 `current !== 1`（翻过页）时点「搜索」，**显式调用**与 **effect 触发**叠加，发 2 次
     （实测 `?current=2` 和 `?current=1` 各一次，**旧页码结果可能后到覆盖新结果**）。全仓 67 处。
     修法：① 参数收敛成 `query` state，effect 只依赖 `query`（**已采用**）；
     ② 极简版 `current !== 1 ? setCurrent(1) : loadData()`。
- 无头浏览器调试可用：Chrome `--headless=new --remote-debugging-port=9222` + Node 22 内置 `WebSocket`
  直连 CDP（**无需装 playwright**），`Network.requestWillBeSent` 精确计数接口调用。
  脚本样例见 `.workbuddy/backup/cdp_count_requests.js`。注意 vite 下前端模块路径也含 `/src/api/`，
  过滤后端接口必须用 `url.startsWith(origin + '/api/')`。
- ⚠️ **`vite build` 会先清空 `dist`，沙箱的 safe-delete 常超时**（`spawnSync ...genie-trash... ETIMEDOUT`）
  ⇒ 先把 `dist` `mv` 改名挪开再构建（2026-10-03 实测）。
- ⚠️ **`vite preview` 只监听 `[::1]`**（netstat 可见 `[::1]:3001 LISTENING`，`127.0.0.1:3001` 连不上）
  ⇒ CDP 脚本里的 BASE 必须写 `http://localhost:3001`。
- ⚠️ **`nohup java ... &` 起的进程会被工具回收**：表现为日志停在启动中途、端口不监听、退出码 0。
  长驻服务必须用 Bash 工具的 `run_in_background`，**不要** `nohup &` 后立即返回。
- ⚠️ **新增 Controller 漏 `import java.util.List;` 时 `mvn -q compile` 可能不暴露，`mvn package` 才炸**
  （2026-10-03 实际踩到）⇒ 改完 Controller 直接 `package`。


## 九、商品中心 SKU（2026-10-03 落地）

**模型**：`base_spu`（SPU·聚合层）1:N `base_material`（SKU）。
🔴 **`base_material.id` 就是 SKU ID，单据与库存表的 `product_id` 语义不变**
⇒ 13 张业务表、58 处 Java、23 个前端文件**零改动**。这是整套方案成立的前提，不要「顺手」改成 `sku_id`。

**表**：新增 6 张 —— `base_spu`、`base_sale_property`（属性组）、`base_sale_property_item`（属性值）、
`base_spu_sale_property`（SPU 选属性组）、`base_material_property_rel`（SKU 绑属性值）、`base_material_code`（一品多码）；
`base_material` 补 4 列 `spu_id` / `sale_property_text` / `main_image` / `is_multi_code`。

🔴 **关联表实体不继承 `BaseDataEntity`、走物理删除**（`BaseSpuSaleProperty` / `BaseMaterialPropertyRel` /
`BaseMaterialCode` / `BaseSalePropertyItem` 直接 `implements Serializable`）。
原因：这些表的唯一键（如 `uk_spu_property(spu_id, property_id)`）**不含 deleted 列**，
用逻辑删除后重新绑定会直接撞唯一键。只有 `base_spu` / `base_sale_property` 保留逻辑删除。

**SKU 矩阵**：勾选属性组后按**笛卡尔积**生成，上限 `MAX_MATRIX_SIZE=200`（超了报错，防误勾刷库）。
**幂等靠「属性组合签名」**（属性值 ID 排序拼接）——**只增不删**：属性组变更时已存在的组合跳过、绝不重建 SKU。
`spu_sale_property` 绑定走「先删后插」（物理删除，安全）。

**属性值采用差异更新**（`SalePropertyServiceImpl.saveItems`）：按**名称**匹配已有记录并复用其 ID，
未出现在入参中的旧值只有在**未被 SKU 引用**时才删除。这样编辑属性组不会换掉属性值 ID、不会切断 SKU 绑定。

**编号**：新增 `SPU` / `SALE_PROPERTY`(SP) / `SALE_PROPERTY_ITEM`(SI)，`reset_by_date=0`。
⚠️ **主数据编号流水必须取「含已逻辑删除记录」的最大值**（唯一键不含 `deleted`，流水落后就
`Duplicate entry 'MAT000005'`，实测新增 SKU 直接 500）。`add_sku_center.sql` 第 11 节统一校正 10 条规则，幂等可重跑。

**菜单**：`M000109` 商品SPU、`M000110` 销售属性（均挂在「物料管理」id=5 下）；
**物料信息（id=8，M000105）只改 `title` 为「商品SKU」，`code/path/permission` 冻结**。
一品多码**不新增权限串**，复用 `basedata:material:info`。

**端点**：`/basedata/spu`、`/basedata/sale-property`、`/basedata/material-code`；下拉 `list` **不加权限注解**。
物料分页新增 `spuId` 筛选与 `spuName` / `salePropertyText` 回填。验收：接口 29 + 冒烟 27 全过。
脚本：`sql/add_sku_center.sql`、`sql/add_product_center_menu.sql`（均已执行）。

## 十、库存体系补齐（2026-10-03 两段全部完成）

**第一段**：盘点参数 / 库存预警 / 库存成本调整。**第二段**：预先盘点单 / 盘点任务。方案与验收
全量记录见 `docs/库存体系补齐方案.md` §十。

**四条硬约定**

1. 🔴 **库存成本调整单只改价、不改量**：审核通过时直接 `UPDATE product_stock SET tax_price/tax_amount`
   （金额 = 新价 × 数量），**绝不能走 `addStock`/`subStock`**——数量没变，走库存入口会把 `stock_num` 再加一遍。
2. 🔴 **库存预警走「查询时实时算」**（`GET /stock/warning/hit-list` 直接 JOIN `product_stock` 比对上下限），
   而非「出入库挂钩子」：后者要动 12 个库存调用点，前者老链路零回归。
   无库存记录按 0 算 ⇒「下限 > 0 且无库存」会命中缺货预警，属预期。
3. 🔴 **配置类表不加唯一键**：`stock_warning` 的 `(sc_id, product_id)` 只做应用层判重——
   唯一键不含 `deleted`，逻辑删除后重建必撞（同 SKU 关联表教训）。
4. 成本调整单的**数量与调整前成本价一律服务端快照**，不信前端；审核前二次校验数量，不一致直接拒绝。

**表/编码**：8 张新增（`stock_warning`、`stock_cost_adjust_sheet(+detail)`、`take_stock_config`、
`take_stock_plan(+detail)`、`pre_take_stock_sheet(+detail)`）+ `stock_check` 补 `plan_id` / `pre_sheet_id`（可空）；
状态码 0/1/2（对齐 `StockAdjustSheetStatus`）；编号 `STOCK_TAKE_PLAN`(PD) / `STOCK_TAKE_PRE`(YPD) /
`STOCK_COST_ADJUST`(CB)；菜单 `M000207` / `M000208` / `M000211` + 按钮 `B000301~B000308`
（授权超管 + 仓库管理员）；脚本 `sql/add_stock_system.sql`（幂等，连跑 3 遍通过）。

**流水/端点**：`ProductStockBizType` 加 `COST_ADJUST(13)`（数量变动恒为 0，只标记成本调整）；
`/stock/warning`、`/stock/adjust/cost`（另有 `/stock-info` 供制单页展示当前库存与成本价）、`/stock/take/config`。
验收：接口 46 + 冒烟 27 全过。方案见 `docs/库存体系补齐方案.md`。

### 10.B 第二段（盘点任务 / 预先盘点单）四条硬约定

1. 🔴 **盘点任务自身不改库存**：差异汇成一张 `stock_check`（回填 `plan_id`），复用盘点单已有的审核流程；
   参数 `autoChangeStock=1` 时生成差异后顺带 `approveCheck`。
2. 🔴 **品牌盘点经 SPU 中转**：`base_material` **没有** `brand_id`，只有 `base_spu` 有
   ⇒ 品牌 → `base_spu.brand_id` → SKU（`base_material.spu_id`）。分类同理用 `base_material.category_id`。
3. **创建任务时把 `product_stock.stock_num` 写进 `take_stock_plan_detail.stock_num` 作快照**，
   之后「账面数」一律取快照（不是当前库存）；「全场」上限 2000 条明细。
4. 🔴 **`@RequestBody Map<String,Object>` 接明细数组必 500**：Jackson 反序列化成 `List<LinkedHashMap>`，
   按实体迭代时 `ClassCastException`（泛型擦除让强转编译期不报错）⇒ 必须强类型 DTO
   （已补 `TakeStockNumsParam`）。

**第二段表/编码**：`take_stock_plan(+detail)`、`pre_take_stock_sheet(+detail)`（第一段已建表）；
编号 `STOCK_TAKE_PLAN`(PD) / `STOCK_TAKE_PRE`(YPD)；菜单 `M000209`（盘点任务 `/stock/take/plan`）/
`M000210`（预先盘点单 `/stock/take/pre`）+ 按钮 `B000309~B000315`；
`stock_check` 补 `plan_id` / `pre_sheet_id`（**只在创建时写入，修改不覆盖**）。
`ErpApplication` 已加 `@EnableScheduling`，`TakeStockPlanExpireJob` 每 10 分钟扫 `cancel_hours` 超时任务。
验收：后端 58 项 + 前端 CDP 27 项全过。


## 十一、前端列表页查询的收敛写法（2026-10-03 已落地全仓 34 个文件）⚠️

> 解决两件事：
> ① dev 下 `<React.StrictMode>` 挂载 effect 双跑 —— **React 预期行为，不要改**（生产构建无影响）；
> ② **真缺陷**（生产同样存在）：搜索/重置里 `setCurrent(1); loadData();` 与
> `useEffect([current,pageSize])` 叠加，在 `current !== 1` 时发 **2 次**请求，
> 且 `?current=2` 可能**后到覆盖** `?current=1` 的结果（竞态）。

**实际采用的写法（最小改动 + 彻底消除竞态）**

```tsx
// 1) 分页收进一个对象，成为唯一取数依据
const [query, setQuery] = useState<any>({ current: 1, size: 10 });

// 2) loadData 里的筛选字段【原样保留】引用 searchXxx，只把 current/size 换成 ...query
const loadData = async () => {
    setLoading(true);
    try {
        const params = { ...query, name: searchName || undefined, status: searchStatus };
        const data = await getBrandPage(params);
        setDataSource(data.records); setTotal(data.total);
    } finally { setLoading(false); }
};

// 3) 唯一触发源。effect 在渲染后执行 ⇒ 读到的 searchName 必然是最新的（竞态自然消失）
useEffect(() => { loadData(); }, [query]);

// 4) 搜索/重置只改 query，不再显式调 loadData
const handleSearch = () => setQuery((q: any) => ({ ...q, current: 1 }));
const handleReset  = () => {
    setSearchName(''); setSearchStatus(undefined);
    setQuery((q: any) => ({ ...q, current: 1 }));
};

// 5) 分页器
pagination={{ current: query.current, pageSize: query.size, total,
    onChange: (page, size) => setQuery((q: any) => ({ ...q, current: page, size })) }}
```

**为什么不用「把筛选条件也塞进 query」**
`Search` 组件的 `onChange` 直接改 `searchName`，若 effect 依赖它就变成**逐字符实时搜索**；
要分离「输入中 / 已提交」得再引入一套 state，改动面大一倍。
当前写法靠 **effect 在渲染后运行 ⇒ 闭包最新**这一点，用最小改动拿到同样的正确性。

**顺带修掉的既有 bug**：原 `handleReset` 里 `setSearchXxx('')` 后立刻 `loadData()`，
闭包读到的是**旧条件**（重置后第一次查询条件错误，靠 effect 的第二次请求才纠正）。
现在只有一次请求、且条件一定正确。

**执行脚本（可复用）**
- `.workbuddy/backup/refactor_list_pages.py`：8 组 pattern（`P1-state` / `P2-params` / `P3-deps` /
  `P4-setcur` / `P5-pager` / `P6-onchange` + 单行 / 多行 / Tab / 定时器变体）。
  **默认 dry-run**，`--apply` 才写；逐文件校验「每组 pattern 命中数」+ **残留 `setCurrent/setPageSize` 检查**，
  不达标就跳过整个文件并报出来；写入前自动备份到 `.workbuddy/backup/list_page_orig/pages/...`。
- `.workbuddy/backup/fix_inline_params.py`：补内联调用残留。
- `.workbuddy/backup/verify_query_refactor.js`：CDP 实测脚本（见文末「验证方式」）。

**⚠️ 五个必须记住的坑**

1. 🔴 **`vite build` 用 esbuild、不做类型检查**：变量名改错（`pageSize` 未定义）照样构建成功，
   页面**静默不请求**（异常被 `catch` 吞掉）。
   **改完必须 `npx tsc --noEmit` 并 grep `Cannot find name`** —— 这是唯一可靠的检测手段。
   本次靠它抓出 **11 处**（9 个内联调用 + 2 个变体文件）。
2. **params 有三种形态**，只处理 `const params = {...}` 必漏：
   - `const params = { current, size: pageSize, ... }`（多行 / 单行）
   - **内联调用** `await getXxxPage({ current, size: pageSize, ... })` ⇒ 改 `({ ...query, ... })`
   - **带默认参数的签名** `loadData(page = current, size = pageSize)` ⇒ 改 `loadData()`，
     并把函数体里改成 `current: query.current, size: query.size`
3. **`setTimeout(loadData, 0)` 是「绕闭包过期」的历史 hack**（`Stock/Adjust/Cost`、`Stock/Warning`），收敛后直接删。
4. **antd `<Button>` 对恰好两个汉字会自动插入空格**（`搜 索`、`重 置`）⇒
   用 CDP/爬虫按 `textContent` 匹配按钮必须先去掉空白。
5. **Git Bash（MSYS）会把形如 `/system/loginlog` 的命令行参数转成 Windows 路径** ⇒
   做 CDP 导航时要用 `MSYS_NO_PATHCONV=1`，否则页面会静默重定向到 dashboard（看起来像「页面 0 请求」）。

**不需要改的文件（本来就是对的）**：`System/User`、`System/Role`、`Basedata/Warehouse`
用的是 `searchParams` state + `useEffect([current, pageSize, searchParams])`，
搜索里**只 `setSearchParams`/`setCurrent`、不显式调 `fetchData`** ⇒ 已是单触发源。

**范围**：34 个列表页（清单可复现于 `.workbuddy/backup/list_page_scan.tsv`）。
`Detail` / `Form` / `Chart` 页（依赖 `id` / `visible`）不在范围内。

**验证方式（无需装 playwright）**
- 前端用**生产构建**跑 `vite preview`（避开 dev 的 StrictMode 干扰）；临时 `vite.preview.config.ts`
  把 `/api` 代理到自建后端实例（**8080 被用户 IDEA 占用，用 8082**）。
- `node .workbuddy/backup/verify_query_refactor.js <token>`：CDP 直连 9222，逐页测
  「首屏挂载 / 翻第 2 页 / **在第 2 页点搜索** / 点重置」，断言各自只发 1 次请求。
- 数据不足 2 页时脚本会 SKIP，需先造数据（`sale_order` 只有 `id`/`code` 是 NOT NULL，只插 `code` 即可，
  验证完删除）。
- 实测结果：品牌 / 计量单位 / 登录日志 / 销售订单 四个页面全部 1 次（改造前「第 2 页点搜索」为 2 次）。

## 十二、SQL 脚本执行小抄

```bash
MYSQL=/d/soft/mysql-8.4.10-winx64/bin/mysql.exe
"$MYSQL" -h192.168.31.199 -uroot -p123456 --default-character-set=utf8mb4 erp-data < sql/xxx.sql
```

- 幂等脚本可以连跑多遍验证。
- 执行完与菜单/授权相关的脚本后，清 `erp:auth:*`：
  `python .workbuddy/backup/clear_auth_cache.py`
